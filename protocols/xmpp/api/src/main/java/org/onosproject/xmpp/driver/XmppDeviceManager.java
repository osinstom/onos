package org.onosproject.xmpp.driver;

import org.onosproject.xmpp.XmppDevice;
import org.onosproject.xmpp.XmppDeviceId;
import org.xmpp.packet.Packet;

/**
 * Created by autonet on 01.09.17.
 */
public interface XmppDeviceManager {

    boolean addConnectedDevice(XmppDeviceId deviceId, XmppDevice device);

    void removeConnectedDevice(XmppDeviceId deviceId);

    XmppDevice getDevice(XmppDeviceId deviceId);

    void processUpstreamEvent(XmppDeviceId deviceId, Packet packet);




}
