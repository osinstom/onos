package org.onosproject.xmpp.ctl;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.handler.codec.xml.XmlFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onlab.util.Tools.groupedThreads;

/**
 * Creates pipeline for server-side XMPP channel.
 */
public class XmppChannelInitializer extends ChannelInitializer<ServerSocketChannel> {

    protected static final Logger logger = LoggerFactory.getLogger(XmppChannelInitializer.class);

    protected XmppServer xmppServer;


    public XmppChannelInitializer(XmppServer server) {
        this.xmppServer = server;
    }

    /**
     * Initializes pipeline for XMPP channel.
     * @throws Exception
     */
    @Override
    protected void initChannel(ServerSocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        XmppChannelHandler handler = new XmppChannelHandler();
        // TODO: try add XmlDecoder to pipeline as it has better performance, uses FasterXML Aalto.
        pipeline.addLast("xmppdecoder", new XmppDecoder());
//        pipeline.addLast("xmppencoder",
        pipeline.addLast("handler", handler);

    }
}
