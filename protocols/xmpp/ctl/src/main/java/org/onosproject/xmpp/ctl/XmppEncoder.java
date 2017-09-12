package org.onosproject.xmpp.ctl;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

/**
 * Encodes XMPP message into a ChannelBuffer for netty pipeline
 */
public class XmppEncoder extends MessageToByteEncoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        byte[] bytes = null;

        if(msg instanceof StreamOpen) {
            StreamOpen streamOpen = (StreamOpen) msg;
            logger.info("SENDING: {}", streamOpen.asXML());
            bytes = streamOpen.asXML().getBytes(CharsetUtil.UTF_8);
        }

        if(msg instanceof StreamClose) {
            StreamClose streamClose = (StreamClose) msg;
            bytes = streamClose.asXML().getBytes(CharsetUtil.UTF_8);
        }

        if(msg instanceof Packet) {
            Packet pkt = (Packet) msg;
            logger.info("SENDING /n, {}", pkt.toString());
            bytes = pkt.toXML().getBytes(CharsetUtil.UTF_8);
        }

        if(bytes!=null) {
            out.writeBytes(bytes);
        }


    }
}
