package org.onosproject.xmpp;

import org.onosproject.xmpp.driver.AbstractXmppDevice;
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
    void handleIncomingXmppMessage(AbstractXmppDevice device, Message message);

    /**
     * Handles outgoing XMPP Message packets.
     * @param device
     * @param message
     */
    void handleOutgoingXmppMessage(AbstractXmppDevice device, Message message);

}
