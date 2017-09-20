package org.onosproject.provider.xmpp.pubsub;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

public class XmppPubSubUtils {

    private static final String PUBSUB_NS = "http://jabber.org/protocol/pubsub";
    private static final String PUBSUB_ELEMENT = "pubsub";

    public static boolean isPubSub(IQ iqEvent) {
        Element element = iqEvent.getElement().element(PUBSUB_ELEMENT);
        return element != null && element.getNamespace() != null && element.getNamespace().getURI().equals(PUBSUB_NS);
    }
}
