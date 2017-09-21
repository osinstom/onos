package org.onosproject.xmpp.ctl.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.onosproject.xmpp.XmppDevice;
import org.onosproject.xmpp.ctl.exception.UnsupportedStanzaTypeException;
import org.onosproject.xmpp.stream.StreamEvent;
import org.onosproject.xmpp.ctl.XmppDeviceFactory;
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
public class XmppChannelHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final XmppDeviceFactory factory = XmppDeviceFactory.getInstance();

    private XmppStreamHelper streamHelper = new XmppStreamHelper();

    protected ExecutorService executorService =
            Executors.newFixedThreadPool(32, groupedThreads("onos/xmpp", "message-stats-%d", logger));

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof StreamEvent) {
            executorService.execute(new XmppStreamHandler(ctx, (StreamEvent) msg));
        }

        if(msg instanceof Packet) {
            executorService.execute(new XmppPacketHandler(ctx, (Packet) msg));
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.info("Exception caught: {}", cause.getMessage());
        logger.info(cause.getCause().getMessage());

        //TODO: add error handle mechanisms for each cases
        if(cause.getCause() instanceof UnsupportedStanzaTypeException)
            streamHelper.sendStreamError(ctx.channel(), StreamError.Condition.unsupported_stanza_type);
        else {
            streamHelper.sendStreamError(ctx.channel(), StreamError.Condition.internal_server_error);
        }
    }

    /**
     * XMPP message handler.
     */
    private final class XmppPacketHandler implements Runnable {

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
            XmppDevice device = factory.getXmppDeviceInstanceByJid(jid);
            device.handlePacket(packet);
        }
    }


}
