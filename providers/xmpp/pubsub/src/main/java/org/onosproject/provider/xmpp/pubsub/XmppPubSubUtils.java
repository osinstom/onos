package org.onosproject.provider.xmpp.pubsub;

import org.dom4j.Element;
import org.dom4j.util.UserDataElement;
import org.onosproject.pubsub.api.PublishInfo;
import org.slf4j.Logger;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import static org.slf4j.LoggerFactory.getLogger;

public class XmppPubSubUtils {

    private static final String PUBSUB_NS = "http://jabber.org/protocol/pubsub";
    private static final String PUBSUB_ELEMENT = "pubsub";

    enum Method {
        SUBSCRIBE,
        UNSUBSCRIBE,
        PUBLISH,
        RETRACT
    }

    public static Packet constructXmppEventNotificationMessage(PublishInfo publishInfo) {

        Message message = new Message();
//        Element el = new UserDataElement();

        


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
