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

import org.xmpp.packet.IQ;

/**
 * Abstracts Retract message of XMPP protocol.
 */
public class XmppRetract extends IQ {

    private String JID;
    private String nodeID;
    private String itemID;

    public XmppRetract(IQ iq)  {
        super(iq.getElement());
        this.JID = this.fromJID.toString();
        this.nodeID = this.getChildElement().element("retract").attribute("node").getValue();
        this.itemID = this.getChildElement().element("retract").element("item").attribute("id").getValue();
    }

    public String getJIDAddress() {
        return this.JID;
    }

    public String getNodeID() {
        return this.nodeID;
    }

    public String getItemID() {
        return this.itemID;
    }

}
