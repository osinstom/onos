package org.onosproject.xmpp.pubsub;

/**
 *
 */
public class XmppPubSubConstants {

    public final static String PUBSUB_NAMESPACE = "http://jabber.org/protocol/pubsub";
    public static final String PUBSUB_EVENT_NS = "http://jabber.org/protocol/pubsub#event";
    public static final String PUBSUB_ELEMENT = "pubsub";

    public enum Method {
        SUBSCRIBE,
        UNSUBSCRIBE,
        PUBLISH,
        RETRACT
    }

}
