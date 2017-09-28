package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;
import org.onosproject.net.driver.HandlerBehaviour;
import org.dom4j.Element;
import java.util.List;


public interface PubSubInfoConstructor extends HandlerBehaviour {

    PublishInfo parsePublishInfo(DeviceId device, Object payload);

    List<org.dom4j.Element> constructPayload(PublishInfo info);

}
