package org.onosproject.xmpp;

import org.xmpp.packet.Packet;

/**
 * Responsible for keeping track of the current set of XMPP clients/devices connected to the system.
 */
public interface XmppSession {

    /**
     * Adds newly connected XMPP device to global XMPP device store.
     * @param xmppDevice
     */
    void addConnectedXmppDevice(XmppDevice xmppDevice);

    /**
     * Removes disconnected XMPP device from global XMPP device store.
     * @param xmppDevice
     */
    void removeConnectedXmppDevice(XmppDevice xmppDevice);

    /**
     *
     * @param packet
     */
    void processXmppPacket(Packet packet);


}
