package org.onosproject.xmpp.core.driver;

import org.onosproject.xmpp.core.XmppDevice;
import org.onosproject.xmpp.core.XmppDeviceId;
import org.xmpp.packet.Packet;

import java.net.InetSocketAddress;

/**
 * Responsible for keeping track of the current set of devices
 * connected to the system. As well as notifying Xmpp Stanza listeners.
 *
 */
public interface XmppDeviceManager {

    boolean addConnectedDevice(XmppDeviceId deviceId, XmppDevice device);

    void removeConnectedDevice(XmppDeviceId deviceId);

    XmppDevice getDevice(XmppDeviceId deviceId);

    void processUpstreamEvent(XmppDeviceId deviceId, Packet packet);

    XmppDeviceId getXmppDeviceIdBySocketAddress(InetSocketAddress address);

}

