package org.onosproject.xmpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

/**
 *  The XMPP server class.  Starts XMPP server and listens to new XMPP device connections.
 */
public class XmppServer {

    protected static final Logger logger = LoggerFactory.getLogger(XmppServer.class);

    /**
     * Initializes XMPP server.
     */
    public void init() {

    }

    /**
     * Runs XMPP server thread.
     */
    public void run() {

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

    }

    /**
     * Starts XMPP server.
     */
    public void start() {
        logger.info("XMPP Server has started.");
    }

}
