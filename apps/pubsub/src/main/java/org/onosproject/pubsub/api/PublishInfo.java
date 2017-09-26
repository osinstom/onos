package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;

public class PublishInfo extends PubSubInfo {

    public PublishInfo(DeviceId fromDevice, String nodeId) {
        super(fromDevice, nodeId);
    }


    @Override
    public String toString() {
        return "PublishInfo{" +
                "fromDevice=" + fromDevice +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }

}
