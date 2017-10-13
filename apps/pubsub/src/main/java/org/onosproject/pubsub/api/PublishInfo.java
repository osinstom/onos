package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;

public class PublishInfo extends PubSubInfo {

    private Object payload;

    public PublishInfo(DeviceId fromDevice, String nodeId) {
        super(fromDevice, nodeId);
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

}
