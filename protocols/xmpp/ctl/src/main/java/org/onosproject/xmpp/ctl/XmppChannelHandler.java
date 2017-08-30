package org.onosproject.xmpp.ctl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.onosproject.net.DeviceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.groupedThreads;

/**
 * Handles XMPP channel related events. It will extend Netty ChannelHandler.
 */
public class XmppChannelHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected ExecutorService executorService =
            Executors.newFixedThreadPool(32, groupedThreads("onos/xmpp", "message-stats-%d", logger));

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof Document) {
            logger.info("Document read");
            Document xmlDoc = (Document) msg;
            Element root = xmlDoc.getRootElement();
            logger.info(root.asXML());
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
                return;
            }
//            executorService.execute(new XmppPacketHandler(ctx, packet));
        }
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

        protected final ChannelHandlerContext ctx;
        protected final Packet xmppPacket;

        public XmppPacketHandler(ChannelHandlerContext ctx, Packet xmppPacket) {
            this.ctx = ctx;
            this.xmppPacket = xmppPacket;
        }

        public void run() {
            // TODO: implement XMPP packet handling

        }
    }


}
