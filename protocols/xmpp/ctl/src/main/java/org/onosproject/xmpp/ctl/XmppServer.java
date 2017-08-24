package org.onosproject.xmpp.ctl;

import com.google.common.base.Strings;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Dictionary;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.get;
import static org.onlab.util.Tools.groupedThreads;

/**
 *  The XMPP server class.  Starts XMPP server and listens to new XMPP device connections.
 */
public class XmppServer {

    protected static final Logger logger = LoggerFactory.getLogger(XmppServer.class);

    protected final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;

    protected Integer port = 5259;

    private NioServerSocketChannelFactory execFactory;
    private Channel channel;


    /**
     * Initializes XMPP server.
     */
    public void init() {

    }

    /**
     * Runs XMPP server thread.
     */
    public void run() {
        try {
            final ServerBootstrap bootstrap = createServerBootStrap();

            ChannelPipelineFactory channelPipelineFactory = new XmppPipelineFactory(this);
            bootstrap.setPipelineFactory(channelPipelineFactory);

            InetSocketAddress socketAddress = new InetSocketAddress(port);
            channel = bootstrap.bind(socketAddress);

            logger.info("Listening for device connections on {}", socketAddress);

        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private ServerBootstrap createServerBootStrap() {
        execFactory = new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(groupedThreads("onos/of", "boss-%d", logger)),
                Executors.newCachedThreadPool(groupedThreads("onos/of", "worker-%d", logger)));

        ServerBootstrap bootstrap = new ServerBootstrap(execFactory);
        bootstrap.setOption("reuseAddr", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.sendBufferSize", SEND_BUFFER_SIZE);

        return bootstrap;
    }

    /**
     * TLS/SSL setup. If needed.
     */
    private void initTls() {

    }

    /**
     * Sets configuration parameters defined via ComponentConfiguration subsystem.
     * @param properties
     */
    public void setConfiguration(Dictionary<?, ?> properties) {
        String port = get(properties, "xmppPort");
        if(!Strings.isNullOrEmpty(port)) {
            this.port = Integer.parseInt(port);
        }
        logger.debug("XMPP port set to {}", this.port);
    }

    /**
     * Starts XMPP server.
     */
    public void start() {
        logger.info("XMPP Server has started.");
        this.run();
    }


}
