package org.onosproject.xmpp.ctl;

import org.apache.felix.scr.annotations.*;
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

    private static final Logger logger =
            LoggerFactory.getLogger(XmppControllerImpl.class);

    // core services declaration
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    // configuration properties definition

    // listener declaration
    protected Set<XmppIQListener> xmppIQListeners = new CopyOnWriteArraySet<XmppIQListener>();

    protected Set<XmppPresenceListener> xmppPresenceListeners = new CopyOnWriteArraySet<XmppPresenceListener>();

    protected Set<XmppMessageListener> xmppMessageListeners = new CopyOnWriteArraySet<XmppMessageListener>();

    @Activate
    public void activate(ComponentContext context) {
        logger.info("XmppControllerImpl started.");
        coreService.registerApplication(APP_ID);
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


    /**
     * XMPP message handler.
     */
    protected final class XmppPacketHandler implements Runnable {

        protected final DeviceId deviceId;
        protected final Packet xmppPacket;

        public XmppPacketHandler(DeviceId deviceId, Packet xmppPacket) {
            this.deviceId = deviceId;
            this.xmppPacket = xmppPacket;
        }

        public void run() {

        }
    }
}
