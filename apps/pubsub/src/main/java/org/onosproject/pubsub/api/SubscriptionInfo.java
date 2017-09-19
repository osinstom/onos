package org.onosproject.pubsub.api;

public class SubscriptionInfo {

    private String nodeId;

    public SubscriptionInfo(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

}
