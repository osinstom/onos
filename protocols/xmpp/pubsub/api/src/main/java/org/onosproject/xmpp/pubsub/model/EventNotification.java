package org.onosproject.xmpp.pubsub.model;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.xmpp.packet.Message;

import static org.onosproject.xmpp.pubsub.XmppPubSubConstants.PUBSUB_EVENT_NS;

/**
 *
 */
public class EventNotification extends Message {

    public EventNotification(String node, Element payload) {
        super(docFactory.createDocument().addElement("message"));
        this.addChildElement("event", PUBSUB_EVENT_NS);
        Element items = docFactory.createElement("items");
        items.addAttribute("node", node);
        items.add(payload);
        this.getElement().element("event").add(items);
    }

}
