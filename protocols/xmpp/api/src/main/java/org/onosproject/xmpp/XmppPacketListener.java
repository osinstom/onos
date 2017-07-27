package org.onosproject.xmpp;

/**
 *  Notifies provides about XMPP messages.
 */
public interface XmppPacketListener {

    void handleIncomingPacket(XmppDevice xmppDevice);

    void handleOutgoingPacket(XmppDevice xmppDevice);

}
