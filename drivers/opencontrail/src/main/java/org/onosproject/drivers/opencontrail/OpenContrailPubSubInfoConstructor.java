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

        BgpPublishInfo info = new BgpPublishInfo(vpnInstanceName);

        List<Element> items = pubsubPayload.elements();
        for(Element item : items) {
            List<Element> entries = item.elements();
            for(Element entry : entries) {
                BgpVpnPubSubEntry bgpVpnEntry = getBgpVpnPubSubEntry(entry);
                info.addEntry(bgpVpnEntry);
            }
        }

        return info;
    }

    private String getVpnInstanceName(Element element) {
        return element.attribute("node").getValue();
    }

    private BgpVpnPubSubEntry getBgpVpnPubSubEntry(Element entry) {
        Element nlri = entry.element("nlri");
        String nlriAf = nlri.attributeValue("af");
        String nlriAddress = nlri.attributeValue("address") != null ? nlri.attributeValue("address") : nlri.getText();
        Element nextHop = entry.element("next-hop");
        String nextHopAf = nextHop.attributeValue("af");
        String nextHopAddress = nextHop.attributeValue("address") != null ? nextHop.attributeValue("address") : nextHop.getText();
        Element version = entry.element("version");
        String versionId = version.attributeValue("id");
        Element label = entry.element("label");
        String labelId = label.getText();
        BgpVpnPubSubEntry bgpVpnEntry = new BgpVpnPubSubEntry(Integer.parseInt(labelId),Integer.parseInt(nlriAf), nlriAddress, Integer.parseInt(nextHopAf),
                nextHopAddress, Integer.parseInt(versionId));
        return bgpVpnEntry;
    }

}
