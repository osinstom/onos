package org.onosproject.xmpp.pubsub.ctl;

/**
 * Created by autonet on 22.12.17.
 */
public class XmppPubSubConstants {

    public final static String PUBSUB_NAMESPACE = "http://jabber.org/protocol/pubsub";
    public static final String PUBSUB_EVENT_NS = "http://jabber.org/protocol/pubsub#event";
    public static final String PUBSUB_ELEMENT = "pubsub";

    enum Method {
        SUBSCRIBE,
        UNSUBSCRIBE,
        PUBLISH,
        RETRACT
    }

}
