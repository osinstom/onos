package org.onosproject.xmpp.pubsub.model;

import org.xmpp.packet.PacketError;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public class XmppPubSubError {

    private static final String PUBSUB_ERROR_NS = "http://jabber.org/protocol/pubsub#errors";

    public enum PubSubApplicationCondition {
        NOT_SUBSCRIBED, ITEM_NOT_FOUND
    }

    private PacketError.Condition baseCondition;
    private PubSubApplicationCondition applicationCondition;

    public XmppPubSubError(PubSubApplicationCondition applicationCondition) {
        this.applicationCondition = applicationCondition;
        this.baseCondition = checkNotNull(matchToApplicationCondition());
    }

    private PacketError.Condition matchToApplicationCondition() {
        if(this.applicationCondition.equals(PubSubApplicationCondition.ITEM_NOT_FOUND)) {
            // set to null, because base condition has the same value as application condition
            this.applicationCondition = null;
            return PacketError.Condition.item_not_found;
        } else if(this.applicationCondition.equals(PubSubApplicationCondition.NOT_SUBSCRIBED)) {
            return PacketError.Condition.unexpected_request;
        }
        return null;
    }

    public PacketError asPacketError() {
        PacketError packetError = new PacketError(this.baseCondition);
        if(applicationCondition!=null)
            packetError.setApplicationCondition(applicationCondition.toString().toLowerCase(), PUBSUB_ERROR_NS);
        return packetError;
    }

}
