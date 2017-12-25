package org.onosproject.xmpp.pubsub;

import org.onosproject.net.DeviceId;
import org.onosproject.xmpp.pubsub.model.EventNotification;
import org.onosproject.xmpp.pubsub.model.XmppPubSubError;

/**
 * Responsible for controlling Publish/Subscribe functionality of XMPP.
 */
public interface XmppPubSubController {

    void notify(DeviceId deviceId, EventNotification eventNotification);

    void notify(DeviceId deviceId, XmppPubSubError error);

    void addXmppPubSubEventListener(XmppPubSubEventListener xmppPubSubEventListener);

    void removeXmppPubSubEventListener(XmppPubSubEventListener xmppPubSubEventListener);


}
