package org.onosproject.xmpp.ctl.handlers;

import com.fasterxml.aalto.WFCException;
import io.netty.channel.*;
import org.onosproject.xmpp.XmppDevice;
import org.onosproject.xmpp.ctl.exception.UnsupportedStanzaTypeException;
import org.onosproject.xmpp.ctl.exception.XmppValidationException;
import org.onosproject.xmpp.stream.*;
import org.onosproject.xmpp.ctl.XmppDeviceFactory;
import org.onosproject.xmpp.stream.StreamError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.*;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onlab.util.Tools.groupedThreads;
import static org.onlab.util.Tools.stream;

/**
 * Handles XMPP channel related events. It will extend Netty ChannelHandler.
 */
public class XmppChannelHandler extends CombinedChannelDuplexHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private XmppStreamHelper streamHelper = new XmppStreamHelper();

    protected ExecutorService executorService =
            Executors.newFixedThreadPool(32, groupedThreads("onos/xmpp", "message-stats-%d", logger));

    private volatile ChannelState state;

    public XmppChannelHandler() {
        ChannelInboundHandlerAdapter inboundHandlerAdapter = new ChannelInboundHandlerAdapter();
        ChannelOutboundHandlerAdapter outboundHandlerAdapter = new ChannelOutboundHandlerAdapter();
        this.init(inboundHandlerAdapter, outboundHandlerAdapter);
        this.state = ChannelState.IDLE;
    }

    enum XmppEvent {
        StreamClose, StreamOpen, StreamError, IQ, Message, Presence
    }

    enum ChannelState {

        IDLE() {
            @Override
            void processStreamClose(XmppChannelHandler handler, ChannelHandlerContext ctx, StreamClose msg) {
                // ignore
            }

            @Override
            void processStreamError(ChannelHandlerContext ctx, StreamError streamError) {
                // ignore
            }

            @Override
            void processUpstreamXmppPacket(XmppChannelHandler handler, ChannelHandlerContext ctx,  Object msg) {
                // ignore
                handler.logger.info("XMPP Packet in state IDLE received. Packet ignored..");
            }
        },

        WAIT_STREAM_CLOSE() {
            @Override
            void processDownstreamXmppEvent(XmppChannelHandler handler, ChannelHandlerContext ctx,  Object msg) {
                /**
                 * Block all downstream events during WAIT_STREAM_CLOSE.
                 *
                 * RFC 6120
                 * 4.4 Closing a Stream
                 * "2. Refrain from sending any further data over its outbound stream to the other entity,
                 * but continue to process data received from the other entity (and, if necessary, process such data)."
                 */
            }

            @Override
            void processStreamClose(XmppChannelHandler handler, ChannelHandlerContext ctx, StreamClose msg) {
                XmppDevice device = XmppDeviceFactory.getInstance().getXmppDeviceInstanceBySocketAddress((InetSocketAddress) ctx.channel().remoteAddress());
                device.disconnectDevice();
                handler.setState(IDLE);
            }

            @Override
            void processStreamOpen(XmppChannelHandler handler, ChannelHandlerContext ctx, StreamOpen streamOpen) {
                // ignore
            }

        },

        STREAM_OPEN() {
            @Override
            void processStreamOpen(XmppChannelHandler handler, ChannelHandlerContext ctx, StreamOpen streamOpen) {
                // ignore
            }

            @Override
            void processDownstreamXmppEvent(XmppChannelHandler handler, ChannelHandlerContext ctx, Object msg) {
                if(msg instanceof StreamClose) {
                    handler.setState(ChannelState.WAIT_STREAM_CLOSE);
                }
                ctx.write(msg);
            }
        };

        void processStreamError(ChannelHandlerContext ctx, StreamError streamError) {
            XmppDevice device = XmppDeviceFactory.getInstance().getXmppDeviceInstanceBySocketAddress((InetSocketAddress) ctx.channel().remoteAddress());
            device.handleStreamError(streamError);
        }

        void processStreamOpen(XmppChannelHandler handler, ChannelHandlerContext ctx, StreamOpen streamOpen) {
            XmppDevice device = XmppDeviceFactory.getInstance().getXmppDeviceInstanceByJid(streamOpen.getFromJID());
//            device.setJID(streamOpen.getFromJID());
            device.setChannel(ctx.channel());
            device.openStream(streamOpen);
            device.connectDevice();
            handler.setState(STREAM_OPEN);
        }

        void processStreamClose(XmppChannelHandler handler, ChannelHandlerContext ctx, StreamClose msg) {
            XmppDevice device = XmppDeviceFactory.getInstance().getXmppDeviceInstanceBySocketAddress((InetSocketAddress) ctx.channel().remoteAddress());
            device.closeStream();
            device.disconnectDevice();
        }

        void processUpstreamXmppPacket(XmppChannelHandler handler, ChannelHandlerContext ctx,  Object msg) {
            handler.executorService.execute(new XmppPacketHandler(ctx, (Packet) msg));
        }

        void processDownstreamXmppEvent(XmppChannelHandler handler, ChannelHandlerContext ctx,  Object msg) {
            ctx.write(msg);
        }

        void processUpstreamXmppEvent(XmppChannelHandler handler,  ChannelHandlerContext ctx,  Object msg) {
            XmppEvent event = XmppEvent.valueOf(msg.getClass().getSimpleName());
            handler.logger.info("Xmpp event {} received in STATE={} for device: {}", event, handler.state, ctx.channel().remoteAddress());
            switch(event) {
                case StreamOpen:
                    processStreamOpen(handler, ctx, (StreamOpen) msg);
                    break;
                case StreamClose:
                    processStreamClose(handler, ctx, (StreamClose) msg);
                    break;
                case StreamError:
                    processStreamError(ctx, (StreamError) msg);
                    break;
                case IQ:
                case Message:
                case Presence:
                    processUpstreamXmppPacket(handler, ctx, msg);
                    break;
            }
        }
    }

    private void setState(ChannelState state) {
        logger.info("Transition from state {} to {}", this.state, state);
        this.state = state;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.state = ChannelState.IDLE;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.state.processUpstreamXmppEvent(this, ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.info("Exception caught: {}", cause.getMessage());
        logger.info(cause.getCause().getMessage());
        logger.info(cause.getStackTrace().toString());

        //TODO: add error handle mechanisms for each cases
        if(cause.getCause() instanceof UnsupportedStanzaTypeException)
            streamHelper.sendStreamError(ctx.channel(), StreamError.Condition.unsupported_stanza_type);
        else if(cause.getCause() instanceof WFCException) {
            streamHelper.sendStreamError(ctx.channel(), StreamError.Condition.bad_format);
        }
        else if(cause.getCause() instanceof XmppValidationException) {
            streamHelper.sendStreamError(ctx.channel(), StreamError.Condition.bad_format);
        }
        else {
            streamHelper.sendStreamError(ctx.channel(), StreamError.Condition.internal_server_error);
        }
        XmppDevice device = XmppDeviceFactory.getInstance().getXmppDeviceInstanceBySocketAddress((InetSocketAddress)ctx.channel().remoteAddress());
        device.closeStream();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        logger.info("Writing packet... State " + this.state.toString());
        this.state.processDownstreamXmppEvent(this, ctx,  msg);
    }

    /**
     * XMPP message handler.
     */
    private static final class XmppPacketHandler implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        protected final ChannelHandlerContext ctx;
        protected final Packet packet;

        public XmppPacketHandler(ChannelHandlerContext ctx, Packet packet) {
            this.ctx = ctx;
            this.packet = packet;
        }

        @Override
        public void run() {
            logger.info("RECEIVED: {}", packet.toXML());
            JID jid = packet.getFrom();
            XmppDevice device = XmppDeviceFactory.getInstance().getXmppDeviceInstanceByJid(jid);
            device.handlePacket(packet);
        }
    }
}
