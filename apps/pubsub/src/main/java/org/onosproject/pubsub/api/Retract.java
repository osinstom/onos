package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;

public class Retract extends PubSubInfo {
    public Retract(DeviceId fromDevice, String nodeId) {
        super(fromDevice, nodeId);
    }
}
