package org.onosproject.xmpp.pubsub.model;

import org.xmpp.packet.IQ;

/**
 *
 */
public class Unsubscribe extends IQ {

    public Unsubscribe(IQ iq)  {
        super(iq.getElement());
    }

    public String getJIDAddress() {
        return this.fromJID.toString();
    }

    public String getNodeID() {
        return this.getChildElement().element("subscribe").attribute("node").getValue();
    }

}
