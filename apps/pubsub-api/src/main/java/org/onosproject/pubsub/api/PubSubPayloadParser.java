package org.onosproject.pubsub.api;

import org.onosproject.net.driver.HandlerBehaviour;

public interface PubSubPayloadParser extends HandlerBehaviour {

    PublishInfo parsePublishInfo(Object payload);

    SubscriptionInfo parseSubscriptionInfo(Object payload);

}
