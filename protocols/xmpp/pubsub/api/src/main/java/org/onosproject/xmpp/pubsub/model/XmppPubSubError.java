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

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.xmpp.pubsub.XmppPubSubConstants.PUBSUB_ERROR_NS;

/**
 * Abstracts Publish/Subscribe error message of XMPP protocol.
 */
public class XmppPubSubError {

    public enum PubSubApplicationCondition {
        NOT_SUBSCRIBED,
        ITEM_NOT_FOUND
    }

    private static final Map<PubSubApplicationCondition, PacketError.Condition> APP_BASE_CONDITION_MAP = new HashMap<>();

    static {
        APP_BASE_CONDITION_MAP.put(PubSubApplicationCondition.ITEM_NOT_FOUND, PacketError.Condition.item_not_found);
        APP_BASE_CONDITION_MAP.put(PubSubApplicationCondition.NOT_SUBSCRIBED, PacketError.Condition.unexpected_request);
    }

    private PacketError.Condition baseCondition;
    private PubSubApplicationCondition applicationCondition;

    public XmppPubSubError(PubSubApplicationCondition applicationCondition) {
        this.applicationCondition = applicationCondition;
        this.baseCondition = setBasedOnAppCondition();
    }

    private PacketError.Condition setBasedOnAppCondition() {
        return APP_BASE_CONDITION_MAP.getOrDefault(this.applicationCondition, PacketError.Condition.undefined_condition);
    }

    public PacketError asPacketError() {
        PacketError packetError = new PacketError(this.baseCondition);
        if (applicationCondition != null) {
            packetError.setApplicationCondition(applicationCondition.toString().toLowerCase(), PUBSUB_ERROR_NS);
        }
        return packetError;
    }

}
