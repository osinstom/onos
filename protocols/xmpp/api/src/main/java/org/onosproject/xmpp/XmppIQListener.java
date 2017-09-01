package org.onosproject.xmpp;

import org.onosproject.xmpp.driver.AbstractXmppDevice;
import org.xmpp.packet.IQ;

/**
 *  Notifies providers about XMPP IQ packets. It should be implemented in XMPP provider.
 */
public interface XmppIQListener {

    /**
     * Handles incoming XMPP IQ packet.
     * @param xmppDevice
     * @param iq
     */
    void handleIncomingIQ(AbstractXmppDevice xmppDevice, IQ iq);

    /**
     * Handles outgoing XMPP IQ packet.
     * @param xmppDevice
     * @param iq
     */
    void handleOutgoingIQ(AbstractXmppDevice xmppDevice, IQ iq);

}
