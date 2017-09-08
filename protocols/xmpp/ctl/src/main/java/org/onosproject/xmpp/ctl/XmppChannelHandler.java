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

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
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
        executorService.execute(new XmppDeviceHandler(ctx, 1));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        executorService.execute(new XmppDeviceHandler(ctx, 0));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Document doc = (Document) msg;
        executorService.execute(new XmppPacketHandler(ctx, doc.getRootElement()));
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


    private final class XmppDeviceHandler implements Runnable {

        protected final ChannelHandlerContext ctx;
        // status of channel to realize if channel was activated/deactivated, 0 = inactive, 1 = active
        protected final int channelStatus;

        public XmppDeviceHandler(ChannelHandlerContext ctx, int channelStatus) {
            this.ctx = ctx;
            this.channelStatus = channelStatus;
        }

        @Override
        public void run() {
            XmppDevice device = factory.getXmppDeviceInstance((InetSocketAddress) ctx.channel().remoteAddress());
            if(channelStatus==1) {
                logger.info("NEW DEVICE CONNECTED");
                device.setChannel(ctx.channel());
                device.connectDevice();
            } else if(channelStatus==0) {
                logger.info("DEVICE DISCONNECTED");
                device.disconnectDevice();
            }
        }
    }

    /**
     * XMPP message handler.
     */
    private final class XmppPacketHandler implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        protected final ChannelHandlerContext ctx;
        protected final Element xml;

        public XmppPacketHandler(ChannelHandlerContext ctx, Element xml) {
            this.ctx = ctx;
            this.xml = xml;
        }

        @Override
        public void run() {

            logger.info(xml.asXML());
            if (xml.getName().equals("stream")){
                processStream();
                return;
            }
            Packet packet = getXmppPacket(xml);
            try {
                checkNotNull(packet);
            } catch(NullPointerException ex) {
                return;
            }
            JID jid = packet.getFrom();
            XmppDevice device = factory.getXmppDeviceInstance((InetSocketAddress) ctx.channel().remoteAddress());
            device.setJID(jid);
            device.handlePacket(packet);
        }

        private void processStream() {
            for( Attribute attr : (List<Attribute>) xml.attributes()) {
                logger.info("STREAM ATTR: {} {} " , attr.getName(), attr.getValue());
            }
            Packet streamResp = new Packet(xml) {
                @Override
                public Packet createCopy() {
                    return null;
                }
            };
            JID from = streamResp.getFrom();
            JID to = streamResp.getTo();
            streamResp.setFrom(to);
            streamResp.setTo(from);
            streamResp.getElement().addAttribute("id", generateID());
            logger.info("SENDING /n{}", streamResp.toString());
            this.ctx.channel().write(streamResp);
        }

        private String generateID() {
            return UUID.randomUUID().toString();
        }
    }


}
