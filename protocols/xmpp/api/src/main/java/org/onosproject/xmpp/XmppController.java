package org.onosproject.xmpp;

/**
 * Controls XMPP protocol behaviour.
 */
public interface XmppController {

    void addXmppDeviceListener(XmppDeviceListener deviceListener);

    void removeXmppDeviceListener(XmppDeviceListener deviceListener);

    void addXmppEventListener(XmppEventListener eventListener);

    void removeXmppEventListener(XmppEventListener eventListener);

    void processXmppPacket();

}
