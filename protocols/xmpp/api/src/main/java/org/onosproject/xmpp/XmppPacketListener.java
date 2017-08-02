package org.onosproject.xmpp;

import org.xmpp.packet.Packet;

/**
 *  Notifies providers about incoming XMPP packets. It should be implemented in XMPP provider.
 */
public interface XmppPacketListener {

    /**
     * Handles incoming XMPP packet.
     * @param xmppDevice
     * @param packet
     */
    void handleIncomingPacket(XmppDevice xmppDevice, Packet packet);

}
