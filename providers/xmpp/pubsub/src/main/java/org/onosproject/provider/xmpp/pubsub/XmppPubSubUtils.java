package org.onosproject.provider.xmpp.pubsub;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.util.UserDataElement;
import org.onosproject.pubsub.api.PubSubInfoConstructor;
import org.onosproject.pubsub.api.PublishInfo;
import org.slf4j.Logger;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class XmppPubSubUtils {

    private static final String PUBSUB_NS = "http://jabber.org/protocol/pubsub";
    private static final String PUBSUB_ELEMENT = "pubsub";
    private static final String PUBSUB_EVENT_NS = "http://jabber.org/protocol/pubsub#event";

    enum Method {
        SUBSCRIBE,
        UNSUBSCRIBE,
        PUBLISH,
        RETRACT
    }

    public static Packet constructXmppEventNotificationMessage(PublishInfo publishInfo) {

        Message message = new Message();

        DocumentFactory df = DocumentFactory.getInstance();

        Element event = df.createElement("event", PUBSUB_EVENT_NS );

        // items element
        Element items = df.createElement("items");
        items.addAttribute("node", publishInfo.getNodeId());

        // item element
        Element item = df.createElement("item");
        item.addAttribute("id", "dsadasda"); // temporary, should generate ID

        String domain = publishInfo.getFromDevice().uri().getSchemeSpecificPart().split("@")[1];

        PubSubInfoConstructor constructor = PubSubConstructorFactory.getInstance().getPubSubInfoConstructor(domain);

        List<Element> entries = constructor.constructPayload(publishInfo);

        for(Element element : entries) {
            item.elements().add(element);
        }

        items.elements().add(item);
        event.elements().add(items);

        // event element
        message.getElement().elements().add(event);

        message.setType(Message.Type.normal);

        return message;
    }

    public static boolean isPubSub(IQ iqEvent) {
        Element element = iqEvent.getElement().element(PUBSUB_ELEMENT);
        return element != null && element.getNamespace() != null && element.getNamespace().getURI().equals(PUBSUB_NS);
    }

    public static Method getMethod(IQ iq) {
        Element pubsubElement = iq.getChildElement();
        Element methodElement = getChildElement(pubsubElement);
        String name = methodElement.getName();
        switch(name) {
            case "subscribe":
                return Method.SUBSCRIBE;
            case "unsubscribe":
                return Method.UNSUBSCRIBE;
            case "publish":
                return Method.PUBLISH;
            case "retract":
                return Method.RETRACT;
        }

        return null;
    }

    public static Element getChildElement(Element element) {
        Element child = (Element) element.elements().get(0); // the first element is related to pubsub operation
        return child;
    }
}
