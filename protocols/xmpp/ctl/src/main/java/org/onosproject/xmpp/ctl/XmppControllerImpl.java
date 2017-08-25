package org.onosproject.xmpp.ctl;

import org.apache.felix.scr.annotations.*;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.xmpp.*;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.*;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The main class (bundle) of XMPP protocol.
 * Responsible for:
 * 1. Initialization and starting XMPP server.
 * 2. Handling XMPP packets from clients and writing to clients.
 * 3. Configuration parameters initialization.
 * 4. Notifing listeners about XMPP events/packets.
 */
@Component(immediate = true)
@Service
public class XmppControllerImpl implements XmppController {

    private static final String APP_ID = "org.onosproject.xmpp";
    private static final String XMPP_PORT = "5269";

    private static final Logger logger =
            LoggerFactory.getLogger(XmppControllerImpl.class);

    // core services declaration
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService cfgService;

    // configuration properties definition
    @Property(name = "xmppPort", value = XMPP_PORT,
            label = "Port number used by XMPP protocol; default is 5269")
    private String xmppPort = XMPP_PORT;


    // listener declaration
    protected Set<XmppIQListener> xmppIQListeners = new CopyOnWriteArraySet<XmppIQListener>();

    protected Set<XmppPresenceListener> xmppPresenceListeners = new CopyOnWriteArraySet<XmppPresenceListener>();

    protected Set<XmppMessageListener> xmppMessageListeners = new CopyOnWriteArraySet<XmppMessageListener>();

    private final XmppServer xmppServer = new XmppServer();

    @Activate
    public void activate(ComponentContext context) {
        logger.info("XmppControllerImpl started.");
        coreService.registerApplication(APP_ID);
        cfgService.registerProperties(getClass());
        xmppServer.setConfiguration(context.getProperties());
        xmppServer.start();
    }

    @Deactivate
    public void deactivate() {
        
    }

    public void addXmppMessageListener(XmppMessageListener msgListener) {

    }

    public void removeXmppMessageListener(XmppMessageListener msgListener) {

    }

    public void addXmppIQListener(XmppIQListener iqListener) {

    }

    public void removeXmppIQListener(XmppIQListener iqListener) {

    }

    public void addXmppPresenceListener(XmppPresenceListener presenceListener) {

    }

    public void removeXmppPresenceListener(XmppPresenceListener presenceListener) {

    }

    public void processXmppPacket() {

    }

    private class XmppSessionHandler implements XmppSession {

        public void addConnectedXmppDevice(XmppDevice xmppDevice) {

        }

        public void removeConnectedXmppDevice(XmppDevice xmppDevice) {

        }

        public void processXmppPacket(Packet packet) {

        }
    }



}
