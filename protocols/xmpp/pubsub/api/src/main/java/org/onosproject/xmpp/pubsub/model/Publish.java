package org.onosproject.xmpp.pubsub.model;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

/**
 *
 */
public class Publish extends IQ {

    public Publish(IQ iq) {
        super(iq.getElement());
    }

    public String getJIDAddress() {
        return this.fromJID.toString();
    }

    public String getNodeID() {
        return this.getChildElement().element("publish").attribute("node").getValue();
    }

    public Element getItem() {
        return this.getChildElement().element("publish").element("item");
    }

    public String getItemID() {
        return this.getChildElement().element("publish").element("item").attribute("id").getValue();
    }

    public Element getItemEntry() {
        return this.getChildElement().element("publish").element("item").element("entry");
    }

    public String getItemEntryNamespace() {
        return this.getChildElement().element("publish").element("item").element("entry").getNamespaceURI();
    }

    @Override
    public String toString() {
        return "Publish{" +
                "JID=" + fromJID +
                "NodeID=" + this.getNodeID() +
                "Item=\n" + this.getItem().asXML() +
                '}';
    }
}
