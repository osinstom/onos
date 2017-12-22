package org.onosproject.xmpp.pubsub.model;

import org.xmpp.packet.IQ;

/**
 *
 */
public class Subscribe extends IQ {

    private String JID;
    private String nodeID;

    public Subscribe(IQ iq)  {
        super(iq.getElement());
        this.JID = this.fromJID.toString();
        this.nodeID = this.getChildElement().element("subscribe").attribute("node").getValue();
    }

    public String getJIDAddress() {
        return this.JID;
    }

    public String getNodeID() {
        return this.nodeID;
    }
}
