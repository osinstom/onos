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

package org.onosproject.xmpp.evpn.host;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.HostLocation;
import org.onosproject.net.PortNumber;
import org.onosproject.net.SparseAnnotations;
import org.onosproject.net.host.DefaultHostDescription;
import org.onosproject.net.host.HostDescription;
import org.onosproject.net.host.HostProvider;
import org.onosproject.net.host.HostProviderRegistry;
import org.onosproject.net.host.HostProviderService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.provider.xmpp.bgpvpn.route.EvpnPublish;
import org.onosproject.provider.xmpp.bgpvpn.route.EvpnRetract;
import org.onosproject.xmpp.core.XmppDeviceId;
import org.onosproject.xmpp.pubsub.XmppPubSubController;
import org.onosproject.xmpp.pubsub.XmppPublishEventsListener;
import org.onosproject.xmpp.pubsub.model.XmppPublish;
import org.onosproject.xmpp.pubsub.model.XmppRetract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Component(immediate = true)
public class XmppEvpnHostProvider extends AbstractProvider implements HostProvider {

    private final Logger logger = LoggerFactory
            .getLogger(getClass());

    private static final String BGPVPN_NAMESPACE = "http://ietf.org/protocol/bgpvpn";
    private static final String PROVIDER = "org.onosproject.provider.xmpp.bgpvpn";
    private static final String APP_NAME = "org.onosproject.xmpp.bgpvpn";
    private static final String XMPP = "xmpp";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppPubSubController xmppPubSubController;

    private HostProviderService providerService;

    private InternalXmppPublishEventsListener xmppPublishEventsListener = new InternalXmppPublishEventsListener();

    public XmppEvpnHostProvider() {
        super(new ProviderId(XMPP, PROVIDER));
    }

    @Activate
    public void activate() {
        providerService = providerRegistry.register(this);
        xmppPubSubController.addXmppPublishEventsListener(xmppPublishEventsListener);
        logger.info("Started.");
    }

    @Deactivate
    public void deactivate() {
        xmppPubSubController.removeXmppPublishEventsListener(xmppPublishEventsListener);
        providerRegistry.unregister(this);
        providerService = null;
        logger.info("Stopped.");
    }

    @Override
    public void triggerProbe(Host host) {
        logger.info("Triggering probe on host {}", host);
    }

    private class InternalXmppPublishEventsListener implements XmppPublishEventsListener {

        @Override
        public void handlePublish(XmppPublish publishEvent) {
            if(publishEvent.getItemEntryNamespace().equals(BGPVPN_NAMESPACE)) {
                addHost(publishEvent);
            }
        }

        @Override
        public void handleRetract(XmppRetract retractEvent) {
            removeHost(retractEvent);
        }

    }

    private void removeHost(XmppRetract retract) {
        MacAddress macAddress = EvpnRetract.asEvpnInfo(retract.getItemID()).macAddress();
        providerService.hostVanished(HostId.hostId(macAddress, VlanId.vlanId()));
    }

    private void addHost(XmppPublish publish) {
        EvpnPublish info = EvpnPublish.asBgpInfo(publish);
        HostId hostId = HostId.hostId(MacAddress.valueOf(info.getMacAddress()), VlanId.vlanId());
        DeviceId deviceId = XmppDeviceId.asDeviceId(publish.getFrom());
        PortNumber portNumber = PortNumber.ANY;
        HostLocation location = new HostLocation(deviceId, portNumber,
                                                 0L);
        SparseAnnotations annotations = DefaultAnnotations.builder()
                .set("vpn-instance", publish.getNodeID()).build();

        HostDescription hostDescription = new DefaultHostDescription(
                MacAddress.valueOf(info.getMacAddress()),
                VlanId.vlanId(),
                location,
                IpAddress.valueOf(info.getNlriIpAddress()),
                annotations);
        providerService.hostDetected(hostId, hostDescription, false);
    }


}
