package org.onosproject.xmpp;

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
    void handleIncomingIQ(XmppDevice xmppDevice, IQ iq);

    /**
     * Handles outgoing XMPP IQ packet.
     * @param xmppDevice
     * @param iq
     */
    void handleOutgoingIQ(XmppDevice xmppDevice, IQ iq);

}
