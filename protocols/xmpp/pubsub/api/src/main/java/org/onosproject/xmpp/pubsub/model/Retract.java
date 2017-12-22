package org.onosproject.xmpp.pubsub.model;

import org.xmpp.packet.IQ;

/**
 *
 */
public class Retract extends IQ {

    private String JID;
    private String nodeID;
    private String itemID;

    public Retract(IQ iq)  {
        super(iq.getElement());
        this.JID = this.fromJID.toString();
        this.nodeID = this.getChildElement().element("retract").attribute("node").getValue();
        this.itemID = this.getChildElement().element("retract").element("item").attribute("id").getValue();
    }

    public String getJIDAddress() {
        return this.JID;
    }

    public String getNodeID() {
        return this.nodeID;
    }

    public String getItemID() {
        return this.itemID;
    }

}
