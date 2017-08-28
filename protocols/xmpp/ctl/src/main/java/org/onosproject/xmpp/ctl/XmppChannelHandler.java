package org.onosproject.xmpp.ctl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.onosproject.net.DeviceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

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
        final ByteBuf buffer = (ByteBuf)msg;
//        logger.info(msg.toString());
        //prints out String representation of xml doc
        logger.info("read : {}" + buffer.toString((CharsetUtil.UTF_8)));

//        executorService.execute(new XmppPacketHandler(ctx, (Packet) msg));
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
