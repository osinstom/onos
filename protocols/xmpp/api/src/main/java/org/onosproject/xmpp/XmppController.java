package org.onosproject.xmpp;

/**
 * Controls XMPP protocol behaviour.
 */
public interface XmppController {

    void addXmppMessageListener(XmppMessageListener msgListener);

    void removeXmppMessageListener(XmppMessageListener msgListener);

    void addXmppIQListener(XmppIQListener iqListener);

    void removeXmppIQListener(XmppIQListener iqListener);

    void addXmppPresenceListener(XmppPresenceListener presenceListener);

    void removeXmppPresenceListener(XmppPresenceListener presenceListener);

    void processXmppPacket();

}
