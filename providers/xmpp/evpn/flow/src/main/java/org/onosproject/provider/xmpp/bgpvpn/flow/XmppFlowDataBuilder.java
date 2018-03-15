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

package org.onosproject.provider.xmpp.bgpvpn.flow;

/**
 *
 */
public class XmppFlowDataBuilder {

    private String nodeId;
    private String itemId;
    private String nextHop;
    private String ipPrefix;
    private String macAddress;
    private String label;

    public class XmppFlowData {

        private String nodeId;
        private String itemId;
        private String nextHop;
        private String ipPrefix;
        private String macAddress;
        private String label;

        public XmppFlowData(String nodeId, String itemId, String nextHop, String ipPrefix, String macAddress, String label) {
            this.nodeId = nodeId;
            this.itemId = itemId;
            this.nextHop = nextHop;
            this.ipPrefix = ipPrefix;
            this.macAddress = macAddress;
            this.label = label;
        }

        public String getNodeId() {
            return nodeId;
        }

        public String getItemId() {
            return itemId;
        }

        public String getNextHop() {
            return nextHop;
        }

        public String getIpPrefix() {
            return ipPrefix;
        }

        public String getMacAddress() {
            return macAddress;
        }

        public String getLabel() {
            return label;
        }

    }

    private XmppFlowDataBuilder() {}

    public static XmppFlowDataBuilder builder() {
        return new XmppFlowDataBuilder();
    }

    public XmppFlowData build() {
        return new XmppFlowData(this.nodeId, this.itemId, this.nextHop,
                                this.ipPrefix, this.macAddress, this.label);
    }

    public XmppFlowDataBuilder addNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public XmppFlowDataBuilder addItemId(String itemId) {
        this.itemId = itemId;
        return this;
    }

    public XmppFlowDataBuilder addNextHop(String nextHop) {
        this.nextHop = nextHop;
        return this;
    }

    public XmppFlowDataBuilder addIpPrefix(String ipPrefix) {
        this.ipPrefix = ipPrefix;
        return this;
    }

    public XmppFlowDataBuilder addMacAddress(String macAddress) {
        this.macAddress = macAddress;
        return this;
    }

    public XmppFlowDataBuilder addLabel(String label) {
        this.label = label;
        return this;
    }


}
