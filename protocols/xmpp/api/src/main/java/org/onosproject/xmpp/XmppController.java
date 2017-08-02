package org.onosproject.xmpp;

import org.onosproject.net.DeviceId;
import org.xmpp.packet.Packet;

/**
 * Controls XMPP protocol behaviour.
 */
public interface XmppController {


    void addXmppMessageListener(XmppPacketListener listener);

    void removeXmppMessageListener(XmppPacketListener listener);

    void processXmppPacket();

}
