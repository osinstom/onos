package org.onosproject.xmpp;

/**
 *  Notifies provides about XMPP messages.
 */
public interface XmppMessageListener {

    void handleIncomingMessage(XmppDevice xmppDevice);

    void handleOutgoingMessage(XmppDevice xmppDevice);

}
