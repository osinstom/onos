package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.ProviderService;

public interface PubSubProviderService extends ProviderService<PubSubProvider> {

    void subscribe(DeviceId deviceId, SubscriptionInfo subscriptionInfo);

    void unsubscribe(SubscriptionInfo subscriptionInfo);

    void publish(PublishInfo publishInfo);

    void retract(PublishInfo publishInfo);

}
