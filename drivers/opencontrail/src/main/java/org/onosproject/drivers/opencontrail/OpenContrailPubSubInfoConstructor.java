package org.onosproject.drivers.opencontrail;

import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.pubsub.api.PubSubInfoConstructor;
import org.onosproject.pubsub.api.PublishInfo;
import org.dom4j.*;

public class OpenContrailPubSubInfoConstructor extends AbstractHandlerBehaviour implements PubSubInfoConstructor {

    @Override
    public PublishInfo parsePublishInfo(Object payload) {
        Element pubsubPayload = (Element) payload;
        return null;
    }

}
