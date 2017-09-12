package org.onosproject.xmpp.ctl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.onosproject.net.DeviceId;
import org.onosproject.xmpp.XmppDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;
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

    protected ExecutorService executorService =
            Executors.newFixedThreadPool(32, groupedThreads("onos/xmpp", "message-stats-%d", logger));

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof StreamOpen) {
            executorService.execute(new XmppStreamHandler(ctx, (StreamOpen) msg));
        }

        if(msg instanceof StreamClose) {
            executorService.execute(new XmppStreamHandler(ctx, (StreamClose) msg));
        }

        if(msg instanceof Packet) {
            executorService.execute(new XmppPacketHandler(ctx, (Packet) msg));
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        logger.warn(cause.getMessage());
        //TODO: add error handle mechanisms for each cases
    }


    private final class XmppStreamHandler implements Runnable {

        protected final ChannelHandlerContext ctx;
        // status of channel to realize if channel was activated/deactivated, 0 = inactive, 1 = active
        protected final Object streamPacket;

        public XmppStreamHandler(ChannelHandlerContext ctx, Object streamPacket) {
            this.ctx = ctx;
            this.streamPacket = streamPacket;
        }

        @Override
        public void run(){
            if(streamPacket instanceof StreamOpen) {
                openStream((StreamOpen) streamPacket);
            }
            if(streamPacket instanceof StreamClose) {
                closeStream();
            }
        }

        private void closeStream() {
            // get XMPP device by remoteAddress and close stream
            logger.info("Closing stream with");
            SocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
            logger.info("Closing stream with {}", addr);
            writeStreamClose();
        }

        private void writeStreamClose() {
            this.ctx.channel().writeAndFlush(new StreamClose());
        }

        private void openStream(StreamOpen streamPacket) {
            logger.info(streamPacket.asXML());

            // 1. respond with StreamOpen
            writeStreamOpen(streamPacket);

            // 2. set interal state of stream to OPEN

        }

        private void registerXmppDevice(StreamOpen streamPacket) {
            if(this.ctx.channel().isActive()) { // check if channel is still active
                XmppDevice device = factory.getXmppDeviceInstance((InetSocketAddress) ctx.channel().remoteAddress());
                device.setJID(streamPacket.getFromJID());
                device.setChannel(ctx.channel());
                device.connectDevice();
            }

        }

        private void writeStreamOpen(StreamOpen streamOpenFromDevice) {
            Element element = streamOpenFromDevice.getElement().createCopy();
            JID from = streamOpenFromDevice.getFromJID();
            JID to = streamOpenFromDevice.getToJID();
            element.addAttribute("from", to.toString());
            element.addAttribute("to", from.toString());
            element.addAttribute("id", this.ctx.channel().id().asShortText()); // use Netty Channel ID as XMPP stream ID

            StreamOpen streamOpenToDevice = new StreamOpen(element);
            this.ctx.channel().writeAndFlush(streamOpenToDevice);
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
            XmppDevice device = factory.getXmppDeviceInstance((InetSocketAddress) ctx.channel().remoteAddress());
            device.setJID(jid);
            device.handlePacket(packet);
        }
    }


}
