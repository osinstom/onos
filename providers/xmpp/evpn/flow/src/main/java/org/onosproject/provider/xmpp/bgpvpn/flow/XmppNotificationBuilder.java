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
import org.onosproject.evpnrouteservice.Label;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.EthCriterion;
import org.onosproject.net.flow.criteria.IPCriterion;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.flow.instructions.L2ModificationInstruction;
import org.onosproject.net.flow.instructions.L3ModificationInstruction;
import org.onosproject.routeserver.api.VpnInstance;
import org.onosproject.routeserver.api.VpnInstanceService;
import org.onosproject.xmpp.pubsub.model.XmppEventNotification;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public class XmppNotificationBuilder {

    private static final String BGPVPN_NAMESPACE = "http://ietf.org/protocol/bgpvpn";
    private static final String EVPN_AF = "5"; // it points to Evpn Route Type 5
    private static final String EVPN_SAFI = "70"; // it points to L2VPN
    private final VpnInstanceService vpnInstanceService;

    private FlowRule flowRule;
    private DocumentFactory df;
    private XmppFlowDataBuilder.XmppFlowData xmppFlowData;

    private XmppNotificationBuilder(FlowRule flowRule, VpnInstanceService vpnInstanceService) {
        this.flowRule = flowRule;
        this.vpnInstanceService = vpnInstanceService;
        this.df = DocumentFactory.getInstance();
        this.xmppFlowData = mapAttributesToXmppFlowData();
    }

    public static XmppNotificationBuilder builder(FlowRule flowRule, VpnInstanceService vpnInstanceService) {
        return new XmppNotificationBuilder(flowRule, vpnInstanceService);
    }

    private XmppFlowDataBuilder.XmppFlowData mapAttributesToXmppFlowData() {
        XmppFlowDataBuilder flowDataBuilder = XmppFlowDataBuilder.builder();
        for (Criterion c : flowRule.selector().criteria()) {
            switch (c.type()) {
                case ETH_DST:
                    EthCriterion ethCriterion = (EthCriterion) c;
                    flowDataBuilder.addMacAddress(ethCriterion.mac().toString());
                    break;
                case IPV4_DST:
                    IPCriterion ipCriterion = (IPCriterion) c;
                    flowDataBuilder.addIpPrefix(ipCriterion.ip().toString());
                    break;
            }
        }

        for (Instruction i : flowRule.treatment().allInstructions()) {
            switch (i.type()) {
                case L2MODIFICATION:
                    // add label to XmppFlowData
                    L2ModificationInstruction.ModTunnelIdInstruction tunnelId =
                            (L2ModificationInstruction.ModTunnelIdInstruction) i;
                    Integer label = Math.toIntExact(tunnelId.tunnelId());
                    flowDataBuilder.addLabel(label.toString());
                    // add NodeId based on Label to XmppFlowData
                    VpnInstance vpnInstance = vpnInstanceService.getInstanceByLabel(Label.label(label));
                    try {
                        checkNotNull(vpnInstance);
                        String vpnInstanceName = vpnInstance.id().toString();
                        flowDataBuilder.addNodeId(vpnInstanceName);
                    } catch (NullPointerException ex) {
                        // TODO: handle exception here
                    }
                    break;
                case L3MODIFICATION:
                    L3ModificationInstruction.ModIPInstruction ip =
                            (L3ModificationInstruction.ModIPInstruction) i;
                    flowDataBuilder.addNextHop(ip.ip().getIp4Address().toString());
                    break;
            }
        }

        return flowDataBuilder.build();
    }

    public XmppEventNotification buildRouteUpdate() {
        String vpn = getVpnName();
        Element payload = buildNotifyPayload();
        return new XmppEventNotification(vpn, payload);
    }

    private Element buildNotifyPayload() {
        Element item = df.createElement("item");
        String itemId = generateItemIdFromXmppFlowData();
        item.addAttribute("id", itemId);
        Element entry = df.createElement("entry", BGPVPN_NAMESPACE);
        Element nlri = buildNlri();
        Element nextHop = buildNextHop();
        entry.add(nlri);
        entry.add(nextHop);
        item.add(entry);
        return item;
    }

    private String generateItemIdFromXmppFlowData() {
        String itemId = String.format("%s/%s/%s/%s/%s",
                                      xmppFlowData.getNextHop(),
                                      xmppFlowData.getNodeId(),
                                      xmppFlowData.getIpPrefix(),
                                      xmppFlowData.getMacAddress(),
                                      xmppFlowData.getLabel());
        return itemId;
    }

    private String getVpnName() {
        return xmppFlowData.getNodeId();
    }

    private Element buildNlri() {
        Element nlri = df.createElement("nlri");
        Element af = df.createElement("af");
        af.addText(EVPN_AF);
        Element safi = df.createElement("safi");
        safi.addText(EVPN_SAFI);
        Element mac = df.createElement("mac");
        mac.addText(xmppFlowData.getMacAddress());
        Element address = df.createElement("address");
        address.addText(xmppFlowData.getIpPrefix());
        nlri.add(af);
        nlri.add(safi);
        nlri.add(mac);
        nlri.add(address);
        return nlri;
    }

    private Element buildNextHop() {
        Element nextHops = df.createElement("next-hops");
        Element nextHop = df.createElement("next-hop");
        Element nextHopLabel = df.createElement("label");
        nextHopLabel.addText(xmppFlowData.getLabel());
        nextHop.add(nextHopLabel);
        Element nextHopAddr = df.createElement("address");
        nextHopAddr.addText(xmppFlowData.getNextHop());
        nextHop.add(nextHopAddr);
        nextHops.add(nextHop);
        return nextHops;
    }

    public XmppEventNotification buildRouteWithdraw() {
        String vpn = getVpnName();
        Element retract = buildRetractPayload();
        return new XmppEventNotification(vpn, retract);
    }

    private Element buildRetractPayload() {
        Element retract = df.createElement("retract");
        retract.addAttribute("id", generateItemIdFromXmppFlowData());
        return retract;
    }

}
