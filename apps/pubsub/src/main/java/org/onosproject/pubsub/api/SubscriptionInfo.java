package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;

public class SubscriptionInfo {

    private DeviceId fromDevice;
    private String nodeId;

    public SubscriptionInfo(DeviceId fromDevice, String nodeId) {
        this.fromDevice = fromDevice;
        this.nodeId = nodeId;
    }

    public SubscriptionInfo(String device, String nodeId) {
        this.fromDevice = DeviceId.deviceId(device);
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public DeviceId getFromDevice() {
        return fromDevice;
    }

    @Override
    public String toString() {
        return "SubscriptionInfo{" +
                "fromDevice=" + fromDevice +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }
}
