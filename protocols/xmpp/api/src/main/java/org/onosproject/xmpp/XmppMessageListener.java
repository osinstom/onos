package org.onosproject.xmpp;

import org.xmpp.packet.Message;

/**
 * Notifies providers about XMPP Message packets. It should be implemented in XMPP provider.
 */
public interface XmppMessageListener {

    /**
     * Handles incoming XMPP Message packets.
     * @param device
     * @param message
     */
    void handleIncomingXmppMessage(XmppDevice device, Message message);

    /**
     * Handles outgoing XMPP Message packets.
     * @param device
     * @param message
     */
    void handleOutgoingXmppMessage(XmppDevice device, Message message);

}
