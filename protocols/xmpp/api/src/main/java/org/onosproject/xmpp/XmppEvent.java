package org.onosproject.xmpp;

import org.onosproject.net.DeviceId;

/**
 * Created by autonet on 05.09.17.
 */
public abstract class XmppEvent {

    protected DeviceId deviceId;

    protected XmppEvent(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    public abstract String toXML();

    public DeviceId getDeviceId() {
        return this.deviceId;
    }




}
