package org.onosproject.xmpp;

import org.onosproject.net.DeviceId;
import org.xmpp.packet.Packet;

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
