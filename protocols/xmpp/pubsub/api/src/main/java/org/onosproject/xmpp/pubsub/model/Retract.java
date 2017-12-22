package org.onosproject.xmpp.pubsub.model;

import org.xmpp.packet.IQ;

/**
 *
 */
public class Retract extends IQ {

    public Retract(IQ iq)  {
        super(iq.getElement());
    }

    public String getJIDAddress() {
        return this.fromJID.toString();
    }

    public String getNodeID() {
        return this.getChildElement().element("retract").attribute("node").getValue();
    }

    public String getItemID() {
        return this.getChildElement().element("retract").element("item").attribute("id").getValue();
    }

}
