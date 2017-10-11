package org.onosproject.drivers.opencontrail;

import org.onosproject.net.DeviceId;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.pubsub.api.PubSubInfoConstructor;
import org.onosproject.pubsub.api.PublishInfo;
import org.dom4j.*;
import org.slf4j.Logger;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class OpenContrailPubSubInfoConstructor extends AbstractHandlerBehaviour implements PubSubInfoConstructor {

    private final Logger logger = getLogger(getClass());


    @Override
    public PublishInfo parsePublishInfo(DeviceId deviceId, Object payload) {
        Element pubsubPayload = (Element) payload;
        logger.info(pubsubPayload.asXML());

        String vpnInstanceName = getVpnInstanceName(pubsubPayload);

//        BgpPublishInfo info = new BgpPublishInfo(deviceId, vpnInstanceName);
//
//        List<Element> items = pubsubPayload.elements();
//        for(Element item : items) {
//            List<Element> entries = item.elements();
//            for(Element entry : entries) {
//                BgpVpnPubSubEntry bgpVpnEntry = getBgpVpnPubSubEntry(entry);
//                info.addEntry(bgpVpnEntry);
//            }
//        }

        PublishInfo info = new PublishInfo(deviceId, vpnInstanceName);
        Element item = (Element) pubsubPayload.elements().get(0);
        info.setPayload(item);

        return info;
    }

    @Override
    public List<Element> constructPayload(PublishInfo info) {
        BgpPublishInfo bgpInfo = (BgpPublishInfo) info;
        DocumentFactory df = DocumentFactory.getInstance();

        List<Element> entries = new ArrayList<Element>();
        for(BgpVpnPubSubEntry bgpEntry : bgpInfo.getEntries()) {
            // entry element
            Element entry = df.createElement("entry");
            Element nlri = df.createElement("nlri");
            Element nlriAf = df.createElement("af");
            nlriAf.addText(Integer.toString(bgpEntry.getNrliAf()));
            Element nlriIpAddress = df.createElement("address");
            nlriIpAddress.addText(bgpEntry.getNrliIpAddress());
            nlri.elements().add(nlriAf);
            nlri.elements().add(nlriIpAddress);
            entry.elements().add(nlri);
            entries.add(entry);
        }

        return entries;
    }

    @Override
    public Packet constructNotification(Object message) throws IllegalArgumentException {
        if(message instanceof Element) {
            Packet config = createConfigXmppPacket(message);
            return config;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Packet createConfigXmppPacket(Object message) {
        Element config = (Element) message;
        IQ iq = new IQ(IQ.Type.set);
        iq.setChildElement(config);
        return iq;
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
