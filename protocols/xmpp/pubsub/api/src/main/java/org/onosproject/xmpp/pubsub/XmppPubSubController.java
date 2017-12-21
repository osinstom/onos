package org.onosproject.xmpp.pubsub;

import org.onosproject.net.DeviceId;
import org.onosproject.xmpp.pubsub.model.EventNotification;

/**
 * Responsible for controlling Publish/Subscribe functionality of XMPP.
 */
public interface XmppPubSubController {

    void notify(DeviceId deviceId, EventNotification eventNotification);


}
