package org.onosproject.xmpp.driver;

import org.onosproject.xmpp.XmppDevice;
import org.onosproject.xmpp.XmppDeviceId;

/**
 * Created by autonet on 01.09.17.
 */
public interface XmppDeviceManager {

    boolean addConnectedDevice(XmppDeviceId deviceId, XmppDevice device);

    void removeConnectedDevice(XmppDeviceId deviceId);

}
