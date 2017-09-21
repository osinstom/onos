package org.onosproject.drivers.opencontrail;

import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.pubsub.api.PubSubInfoConstructor;
import org.onosproject.pubsub.api.PublishInfo;
import org.dom4j.*;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class OpenContrailPubSubInfoConstructor extends AbstractHandlerBehaviour implements PubSubInfoConstructor {

    private final Logger logger = getLogger(getClass());


    @Override
    public PublishInfo parsePublishInfo(Object payload) {
        Element pubsubPayload = (Element) payload;
        logger.info(pubsubPayload.asXML());

        String vpnInstanceName = getVpnInstanceName(pubsubPayload);

        List<Element> items = pubsubPayload.elements();
        for(Element item : items) {
            List<Element> entries = item.elements();

        }
        PublishInfo info = new PublishInfo();


        return info;
    }

    private String getVpnInstanceName(Element element) {
        return element.attribute("node").getValue();
    }

}
