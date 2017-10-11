package org.onosproject.pubsub.api;

import org.onosproject.event.ListenerService;
import org.onosproject.net.DeviceId;

import java.util.List;

public interface PubSubService extends ListenerService<PubSubEvent, PubSubListener> {

    void notifyPublishEvent(List<DeviceId> devices, PublishInfo info);

    void sendEventNotification(List<DeviceId> devices, Object notificationInfo);

    void sendEventNotification(DeviceId deviceId, Object notificationInfo);

}
