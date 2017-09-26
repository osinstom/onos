package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.Provider;
import org.onosproject.pubsub.impl.PubSubManager;

import java.util.List;

public interface PubSubProvider extends Provider {

    void sendNotifications(List<DeviceId> devices, PublishInfo publishInfo);
}
