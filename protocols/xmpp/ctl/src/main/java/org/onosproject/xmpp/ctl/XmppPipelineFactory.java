package org.onosproject.xmpp.ctl;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;

import static org.onlab.util.Tools.groupedThreads;

/**
 * Creates pipeline for server-side XMPP channel.
 */
public class XmppPipelineFactory implements ChannelPipelineFactory {

    protected static final Logger logger = LoggerFactory.getLogger(XmppPipelineFactory.class);

    protected XmppServer xmppServer;

    protected Timer timer;
    protected IdleStateHandler idleHandler;
    protected ReadTimeoutHandler readTimeoutHandler;

    public XmppPipelineFactory(XmppServer server) {
        this.xmppServer = server;
        this.timer = new HashedWheelTimer(groupedThreads("XmppPipelineFactory", "timer-%d", logger));
        this.idleHandler = new IdleStateHandler(timer, 20, 25, 0);
        this.readTimeoutHandler = new ReadTimeoutHandler(timer, 30);
    }


    /**
     * Returns pipeline for XMPP channel.
     * @return
     * @throws Exception
     */
    @Override
    public ChannelPipeline getPipeline() throws Exception {

        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("idle", idleHandler);
        pipeline.addLast("timeout", readTimeoutHandler);

        return pipeline;
    }
}
