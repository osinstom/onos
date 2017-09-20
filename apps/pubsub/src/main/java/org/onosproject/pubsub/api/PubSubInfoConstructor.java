package org.onosproject.pubsub.api;

import org.onosproject.net.driver.HandlerBehaviour;

public interface PubSubInfoConstructor extends HandlerBehaviour {

    PublishInfo parsePublishInfo(Object payload);

}
