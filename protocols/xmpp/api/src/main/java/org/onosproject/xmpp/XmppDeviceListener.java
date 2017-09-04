package org.onosproject.xmpp;

/**
 * Created by autonet on 04.09.17.
 */
public interface XmppDeviceListener {

    void deviceConnected(XmppDeviceId deviceId);

    void deviceDisconnected(XmppDeviceId deviceId);

}
