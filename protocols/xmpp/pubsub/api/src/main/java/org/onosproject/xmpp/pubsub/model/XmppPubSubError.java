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

package org.onosproject.xmpp.pubsub.model;

import org.xmpp.packet.PacketError;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstracts Publish/Subscribe error message of XMPP protocol.
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
        if (this.applicationCondition.equals(PubSubApplicationCondition.ITEM_NOT_FOUND)) {
            // set to null, because base condition has the same value as application condition
            this.applicationCondition = null;
            return PacketError.Condition.item_not_found;
        } else if (this.applicationCondition.equals(PubSubApplicationCondition.NOT_SUBSCRIBED)) {
            return PacketError.Condition.unexpected_request;
        }
        return null;
    }

    public PacketError asPacketError() {
        PacketError packetError = new PacketError(this.baseCondition);
        if (applicationCondition != null) {
            packetError.setApplicationCondition(applicationCondition.toString().toLowerCase(), PUBSUB_ERROR_NS);
        }
        return packetError;
    }

}
