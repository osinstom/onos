package org.onosproject.xmpp.pubsub;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * The abstract event of XMPP Publish/Subscribe.
 */
public final class XmppPubSubEvent<S> {

    public enum Type {
        SUBSCRIBE,
        UNSUBSCRIBE,
        PUBLISH,
        RETRACT
    }

    private final Type type;
    private final S subject;

    public XmppPubSubEvent(Type type, S subject) {
        this.type = type;
        this.subject = subject;
    }

    /**
     * Returns the type of event.
     *
     * @return event type
     */
    public Type type() {
        return type;
    }

    /**
     * Returns the subject of event.
     *
     * @return subject to which this event pertains
     */
    public S subject() {
        return subject;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("type", type())
                .add("subject", subject()).toString();
    }
}
