/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.xmpp.pubsub;

import org.onosproject.net.DeviceId;
import org.onosproject.xmpp.pubsub.model.XmppEventNotification;
import org.onosproject.xmpp.pubsub.model.XmppPubSubError;

/**
 * Responsible for controlling Publish/Subscribe functionality of XMPP.
 */
public interface XmppPubSubController {

    void notify(DeviceId deviceId, XmppEventNotification eventNotification);

    void notifyError(DeviceId deviceId, XmppPubSubError error);

    void addXmppPublishEventsListener(XmppPublishEventsListener xmppPublishEventsListener);

    void removeXmppPublishEventsListener(XmppPublishEventsListener xmppPublishEventsListener);

    void addXmppSubscribeEventsListener(XmppSubscribeEventsListener xmppSubscribeEventsListener);

    void removeXmppSubscribeEventsListener(XmppSubscribeEventsListener xmppSubscribeEventsListener);

}
