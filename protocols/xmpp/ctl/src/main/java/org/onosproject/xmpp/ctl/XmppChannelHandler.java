package org.onosproject.xmpp.ctl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.onosproject.net.DeviceId;
import org.onosproject.xmpp.XmppDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onlab.util.Tools.groupedThreads;

/**
 * Handles XMPP channel related events. It will extend Netty ChannelHandler.
 */
public class XmppChannelHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final XmppDeviceFactory factory = XmppDeviceFactory.getInstance();

    protected ExecutorService executorService =
            Executors.newFixedThreadPool(32, groupedThreads("onos/xmpp", "message-stats-%d", logger));

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("NEW DEVICE CONNECTED");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) { logger.info("DEVICE DISCONNECTED"); }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        executorService.execute(new XmppPacketHandler(ctx, (Document) msg));
    }

    private Packet getXmppPacket(Element root) {
        Packet packet = null;
        if(root.getName().equals("iq")) {
            logger.info("IQ XMPP Packet");
            packet = new IQ(root);
        } else if (root.getName().equals("message")) {
            packet = new Message(root);
        } else if (root.getName().equals("presence")) {
            packet = new Presence(root);
        } else {
            // do not parse XML data if it is not XMPP
            logger.info("Something else: " + root.getName());
        }
        return packet;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.warn(cause.getMessage());
        //TODO: add error handle mechanisms for each cases
    }

    /**
     * XMPP message handler.
     */
    private final class XmppPacketHandler implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        protected final ChannelHandlerContext ctx;
        protected final Document doc;

        public XmppPacketHandler(ChannelHandlerContext ctx, Document doc) {
            this.ctx = ctx;
            this.doc = doc;
        }

        @Override
        public void run() {
            // TODO: implement XMPP packet handling
            logger.info("Executing 2");
            Element root = doc.getRootElement();
            logger.info(root.asXML());
            Packet packet = getXmppPacket(root);
            checkNotNull(packet);
            JID jid = packet.getFrom();

            XmppDevice device = factory.getXmppDeviceInstance(jid);
            device.setChannel(ctx.channel());
            device.handlePacket(packet);

        }
    }


}
