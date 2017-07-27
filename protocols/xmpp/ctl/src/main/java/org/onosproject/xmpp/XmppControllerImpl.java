package org.onosproject.xmpp;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.CoreService;
import org.xmpp.packet.IQ;

/**
 * The main class of XMPP protocol.
 */
@Component(immediate = true)
@Service
public class XmppControllerImpl implements XmppController {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    public void addXmppMessageListener(XmppPacketListener listener) {

    }

    public void removeXmppMessageListener(XmppPacketListener listener) {

    }

    public void processXmppPacket() {

    }

    public void writeXmppPacket() {

    }
}
