package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;

public class SubscriptionInfo {

    private DeviceId fromDevice;
    private String nodeId;

    public SubscriptionInfo(DeviceId fromDevice, String nodeId) {
        this.fromDevice = fromDevice;
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public DeviceId getFromDevice() {
        return fromDevice;
    }
}
