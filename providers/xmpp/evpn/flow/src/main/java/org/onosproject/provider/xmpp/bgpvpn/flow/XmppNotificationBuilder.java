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

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.onosproject.net.flow.Extension;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.EthCriterion;
import org.onosproject.net.flow.criteria.ExtensionCriterion;
import org.onosproject.net.flow.criteria.IPCriterion;
import org.onosproject.net.flow.instructions.ExtensionPropertyException;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.flow.instructions.L2ModificationInstruction;
import org.onosproject.net.flow.instructions.L3ModificationInstruction;
import org.onosproject.xmpp.pubsub.model.XmppEventNotification;

/**
 *
 */
public class XmppNotificationBuilder {

    private static final String BGPVPN_NAMESPACE = "http://ietf.org/protocol/bgpvpn";

    private FlowRule flowRule;
    private DocumentFactory df;

    private XmppNotificationBuilder(FlowRule flowRule) {
        this.flowRule = flowRule;
        this.df = DocumentFactory.getInstance();
    }

    public static XmppNotificationBuilder builder(FlowRule flowRule) {
        return new XmppNotificationBuilder(flowRule);
    }

    public XmppEventNotification buildRouteUpdate() {
        String vpn = getVpnName();
        Element payload = buildPayload();
        return new XmppEventNotification(vpn, payload);
    }

    private Element buildPayload() {
        Element item = df.createElement("item");
        item.addAttribute("id", "test"); // TODO: hardcoded
        Element entry = df.createElement("entry", BGPVPN_NAMESPACE);
        Element nlri = buildNlri();
        Element nextHop = buildNextHop();
        entry.add(nlri);
        entry.add(nextHop);
        item.add(entry);
        return item;
    }

    private String getVpnName() {
        ExtensionCriterion extensionCriterion =
                (ExtensionCriterion) flowRule.selector().getCriterion(Criterion.Type.EXTENSION);
        String vpn = "";
        try {
             vpn = extensionCriterion.extensionSelector().getPropertyValue("vpn");
        } catch (ExtensionPropertyException e) {
        }
        return vpn;
    }

    private Element buildNlri() {
        Element nlri = df.createElement("nlri");
        for (Criterion c : flowRule.selector().criteria()) {
            switch (c.type()) {
                case ETH_DST:
                    EthCriterion ethCriterion = (EthCriterion) c;
                    Element mac = df.createElement("mac");
                    mac.addText(ethCriterion.mac().toString());
                    nlri.add(mac);
                    break;
                case IPV4_DST:
                    IPCriterion ipCriterion = (IPCriterion) c;
                    Element address = df.createElement("address");
                    address.addText(ipCriterion.ip().toString());
                    nlri.add(address);
                    break;
            }
        }
        return nlri;
    }

    private Element buildNextHop() {
        Element nextHops = df.createElement("next-hops");
        Element nextHop = df.createElement("next-hop");
        for (Instruction i : flowRule.treatment().allInstructions()) {
            switch (i.type()) {
                case L2MODIFICATION:
                    L2ModificationInstruction.ModTunnelIdInstruction tunnelId =
                            (L2ModificationInstruction.ModTunnelIdInstruction) i;
                    Element nextHopLabel = df.createElement("label");
                    nextHopLabel.addText(Long.toString(tunnelId.tunnelId()));
                    nextHop.add(nextHopLabel);
                    break;
                case L3MODIFICATION:
                    L3ModificationInstruction.ModIPInstruction ip =
                            (L3ModificationInstruction.ModIPInstruction) i;
                    Element nextHopAddr = df.createElement("address");
                    nextHopAddr.addText(ip.ip().getIp4Address().toString());
                    nextHop.add(nextHopAddr);
                    break;
            }
        }
        nextHops.add(nextHop);
        return nextHops;
    }

    public XmppEventNotification buildRouteWithdraw() {
        return null;
    }

}
