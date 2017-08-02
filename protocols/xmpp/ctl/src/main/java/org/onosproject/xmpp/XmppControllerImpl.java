package org.onosproject.xmpp;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.xmpp.packet.*;

/**
 * The main class (bundle) of XMPP protocol.
 * Responsible for:
 * 1. Initialization and starting XMPP server.
 * 2. Handling XMPP packets from clients and writing to clients.
 * 3. Configuration parameters initialization.
 * 4.
 */
@Component(immediate = true)
@Service
public class XmppControllerImpl implements XmppController {

    // core services definition
    // configuration properties definition

    private final XmppServer xmppServer = new XmppServer();


    public void addXmppMessageListener(XmppPacketListener listener) {
    }

    public void removeXmppMessageListener(XmppPacketListener listener) {

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
