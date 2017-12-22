package org.onosproject.xmpp.pubsub.model;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

/**
 *
 */
public class Publish extends IQ {

    private String JID;
    private String nodeID;
    private Element item;
    private String itemID;
    private Element itemEntry;
    private String itemEntryNamespace;

    public Publish(IQ iq) {
        super(iq.getElement());
        this.JID = this.fromJID.toString();
        this.nodeID = this.getChildElement().element("publish").attribute("node").getValue();
        this.item = this.getChildElement().element("publish").element("item");
        this.itemID = this.item.attribute("id").getValue();
        this.itemEntry = this.item.element("entry");
        this.itemEntryNamespace = this.itemEntry.getNamespaceURI();
    }

    public String getJIDAddress() {
        return this.JID;
    }

    public String getNodeID() {
        return this.nodeID;
    }

    public Element getItem() {
        return this.item;
    }

    public String getItemID() {
        return this.itemID;
    }

    public Element getItemEntry() {
        return this.itemEntry;
    }

    public String getItemEntryNamespace() {
        return this.itemEntryNamespace;
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
