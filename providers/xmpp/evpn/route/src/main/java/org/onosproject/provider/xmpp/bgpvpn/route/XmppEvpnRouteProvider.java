package org.onosproject.provider.xmpp.bgpvpn.route;

import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.*;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onosproject.evpnrouteservice.*;
import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.routeserver.api.VpnInstance;
import org.onosproject.routeserver.api.VpnInstanceId;
import org.onosproject.routeserver.api.VpnInstanceService;
import org.onosproject.xmpp.pubsub.XmppPubSubController;
import org.onosproject.xmpp.pubsub.XmppPublishEventsListener;
import org.onosproject.xmpp.pubsub.XmppSubscribeEventsListener;
import org.onosproject.xmpp.pubsub.model.XmppEventNotification;
import org.onosproject.xmpp.pubsub.model.XmppPublish;
import org.onosproject.xmpp.pubsub.model.XmppRetract;
import org.onosproject.xmpp.pubsub.model.XmppSubscribe;
import org.onosproject.xmpp.pubsub.model.XmppUnsubscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provider which will provide EVPN abstractions based on XMPP Publish/Subscribe payload.
 */
@Component(immediate = true)
public class XmppEvpnRouteProvider extends AbstractProvider  {

    private final Logger logger = LoggerFactory
            .getLogger(XmppEvpnRouteProvider.class);

    private static final String BGPVPN_NAMESPACE = "http://ietf.org/protocol/bgpvpn";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EvpnRouteAdminService evpnRouteAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VpnInstanceService vpnInstanceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppPubSubController xmppPubSubController;

    private InternalXmppPubSubEventListener xmppPubSubEventsListener =
            new InternalXmppPubSubEventListener();
    private InternalEvpnRouteListener routeListener = new InternalEvpnRouteListener();

    public XmppEvpnRouteProvider() {
        super(new ProviderId("route",
                "org.onosproject.provider.xmpp.bgpvpn.route"));
    }

    @Activate
    public void activate() {
        xmppPubSubController.addXmppSubscribeEventsListener(xmppPubSubEventsListener);
        xmppPubSubController.addXmppPublishEventsListener(xmppPubSubEventsListener);
        logger.info("Started.");
    }

    @Deactivate
    public void deactivate() {
        xmppPubSubController.removeXmppSubscribeEventsListener(xmppPubSubEventsListener);
        xmppPubSubController.removeXmppPublishEventsListener(xmppPubSubEventsListener);
        logger.info("Stopped.");
    }

    private void updateRoute(XmppPublish publish) {
        logger.info("Handling PUBLISH");

        EvpnPublish info = EvpnPublish.asBgpInfo(publish);

        Ip4Address ipAddress = Ip4Address
                .valueOf(info.getNlriIpAddress());

        Ip4Address ipNextHop = Ip4Address
                .valueOf(info.getNextHopAddress());

        EvpnRoute.Source source = EvpnRoute.Source.LOCAL;
        List<VpnRouteTarget> exportRt = new LinkedList<>();
        exportRt.add(VpnRouteTarget.routeTarget(publish.getNodeID()));
        EvpnRoute evpnRoute = new EvpnRoute(source,
                MacAddress.valueOf(info.getMacAddress()),
                IpPrefix.valueOf(ipAddress, 32),
                ipNextHop,
                info.getRouteDistinguisher(publish.getJabberId(), publish.getNodeID()),
                null, //empty rt
                exportRt,
                info.getLabel());

        evpnRouteAdminService.update(Collections
                .singleton(evpnRoute));
    }

    private void withdrawRoute(XmppRetract retract) {
        logger.info("Handling RETRACT");
        EvpnRetract evpnInfo = EvpnRetract.asEvpnInfo(retract.getItemID());
        VpnInstance vpnInstance = vpnInstanceService.getInstance(VpnInstanceId.vpnInstanceId(retract.getNodeID()));
        if(vpnInstance!=null) {
            String label = vpnInstance.routeDistinguisher().getRouteDistinguisher().split("/")[1];
            RouteDistinguisher rd = RouteDistinguisher.routeDistinguisher(retract.getItemID() + "/" + label);
            EvpnRoute evpnRoute = new EvpnRoute(EvpnRoute.Source.LOCAL,
                    evpnInfo.macAddress(),
                    IpPrefix.valueOf(evpnInfo.nlriIpAddress(), 32),
                    null,
                    rd,
                    null,
                    null,
                    Label.label(Integer.parseInt(label)));

            evpnRouteAdminService.withdraw(Collections.singleton(evpnRoute));
        } else {
            // TODO: return error
        }
    }

    private class InternalXmppPubSubEventListener implements XmppPublishEventsListener, XmppSubscribeEventsListener {

        @Override
        public void handlePublish(XmppPublish publishEvent) {
            if (publishEvent.getItemEntryNamespace().equals(BGPVPN_NAMESPACE)) {
                updateRoute(publishEvent);
            }
        }

        @Override
        public void handleRetract(XmppRetract retractEvent) {
            withdrawRoute(retractEvent);
        }

        @Override
        public void handleSubscribe(XmppSubscribe subscribeEvent) {
            updateRouteTarget(subscribeEvent);
        }

        @Override
        public void handleUnsubscribe(XmppUnsubscribe unsubscribeEvent) {
            withdrawRouteTarget(unsubscribeEvent);
        }
    }

    private void withdrawRouteTarget(XmppUnsubscribe unsubscribeEvent) {
        String routeTarget = "target:" + unsubscribeEvent.getJabberId();
        String vpnInstanceId = unsubscribeEvent.getNodeID();
        vpnInstanceService.withdrawImpExpRouteTargets(VpnInstanceService.RouteTargetType.BOTH,
                                                    VpnRouteTarget.routeTarget(routeTarget),
                                                    VpnInstanceId.vpnInstanceId(vpnInstanceId));
    }

    private void updateRouteTarget(XmppSubscribe subscribeEvent) {
        String routeTarget = "target:" + subscribeEvent.getJabberId();
        String vpnInstanceId = subscribeEvent.getNodeID();
        vpnInstanceService.updateImpExpRouteTargets(VpnInstanceService.RouteTargetType.BOTH,
                                                    VpnRouteTarget.routeTarget(routeTarget),
                                                    VpnInstanceId.vpnInstanceId(vpnInstanceId));
    }

    private void populateBgpUpdate(DeviceId deviceId, EvpnRoute evpnRoute) {
        Element payload = asXmppPublishPayload(evpnRoute);
        String vpnName = evpnRoute.exportRouteTarget().get(0).getRouteTarget();
        sendEventNotification(deviceId, vpnName, payload);
    }

    private void populateBgpDelete(DeviceId deviceId, EvpnRoute evpnRoute) {
        Element payload = asXmppRetractPayload(evpnRoute);
        String vpnName = evpnRoute.exportRouteTarget().get(0).getRouteTarget();
        sendEventNotification(deviceId, vpnName, payload);
    }

    private Element asXmppRetractPayload(EvpnRoute evpnRoute) {
        DocumentFactory df = DocumentFactory.getInstance();
        Element retract = df.createElement("retract");
        retract.addAttribute("id", generateItemId(evpnRoute));
        return retract;
    }

    private void sendEventNotification(DeviceId deviceId, String vpnName, Element payload) {
        xmppPubSubController.notify(deviceId, new XmppEventNotification(vpnName, payload));
    }

    private Element asXmppPublishPayload(EvpnRoute evpnRoute) {
        DocumentFactory df = DocumentFactory.getInstance();
        Element item = df.createElement("item");
        item.addAttribute("id", generateItemId(evpnRoute));
        Element entry = df.createElement("entry", BGPVPN_NAMESPACE);
        Element nlri = df.createElement("nlri");
        Element address = df.createElement("address");
        address.addText(evpnRoute.prefixIp().address().toString());
        Element mac = df.createElement("mac");
        mac.addText(evpnRoute.prefixMac().toString());
        nlri.add(address);
        nlri.add(mac);
        Element nextHops = df.createElement("next-hops");
        Element nextHop = df.createElement("next-hop");
        Element nextHopAddr = df.createElement("address");
        nextHopAddr.addText(evpnRoute.ipNextHop().toString());
        Element nextHopLabel = df.createElement("label");
        nextHopLabel.addText(Integer.toString(evpnRoute.label().getLabel()));
        nextHop.add(nextHopAddr);
        nextHop.add(nextHopLabel);
        nextHops.add(nextHop);
        entry.add(nlri);
        entry.add(nextHops);
        item.add(entry);
        return item;
    }

    private String generateItemId(EvpnRoute evpnRoute) {
        String vpnInstanceId = evpnRoute.exportRouteTarget().get(0).getRouteTarget();
        return String.format("%s/%s/%s/%s/%s", evpnRoute.ipNextHop(),vpnInstanceId, evpnRoute.prefixIp().address().toString(), evpnRoute.prefixMac().toString(), evpnRoute.label().getLabel());
    }

    private class InternalEvpnRouteListener implements EvpnRouteListener {

        @Override
        public void event(EvpnRouteEvent event) {
            EvpnRoute evpnRoute = event.subject();
            logger.info("Event received for route {}", evpnRoute);
            if (evpnRoute.source().equals(EvpnRoute.Source.REMOTE)) {
                return;
            }

//            Set<DeviceId> vpnDevices = getDevicesToDistributeInformation(evpnRoute);

            switch (event.type()) {
                case ROUTE_ADDED:
                case ROUTE_UPDATED:
                    logger.info("route added");
//                    vpnDevices.forEach(deviceId -> {
//                        populateBgpUpdate(deviceId, evpnRoute);
//                    });
                    break;
                case ROUTE_REMOVED:
                    logger.info("route deleted");
//                    vpnDevices.forEach(deviceId -> {
//                        populateBgpDelete(deviceId, evpnRoute);
//                    });
                    break;
                default:
                    break;
            }
        }

    }

//    private Set<DeviceId> getDevicesToDistributeInformation(EvpnRoute evpnRoute) {
//        Set<DeviceId> vpnDevices = Sets.newHashSet();
//        VpnInstance routeEvpnInstance = getVpnInstanceForEvpnRoute(evpnRoute.routeDistinguisher());
//        Set<VpnRouteTarget> exportRouteTargets = routeEvpnInstance.getExportRouteTargets();
//        Set<VpnInstance> exportVpnInstances = getVpnInstancesToExportBgpInfo(exportRouteTargets);
//        Set<VrfInstance> vrfInstancesToExportBgpInfo = getVrfInstancesToExportBgpInfo(exportVpnInstances);
//        vpnDevices = getDevicesToPopulateEvpnInfo(vrfInstancesToExportBgpInfo, evpnRoute);
//        return vpnDevices;
//    }
//
//    private Set<DeviceId> getDevicesToPopulateEvpnInfo(Set<VrfInstance> vrfInstancesToExportBgpInfo, EvpnRoute evpnRoute) {
//        Set<DeviceId> devices = Sets.newHashSet();
//        vrfInstancesToExportBgpInfo.forEach(vrfInstance -> {
//            DeviceId deviceId = vrfInstance.device();
//            String fromDevice = evpnRoute.routeDistinguisher().getRouteDistinguisher().split("/")[0];
//            if(!deviceId.uri().getSchemeSpecificPart().equals(fromDevice))
//                devices.add(deviceId);
//        });
//        return devices;
//    }
//
//    private Set<VrfInstance> getVrfInstancesToExportBgpInfo(Set<VpnInstance> exportVpnInstances) {
//        Set<VrfInstance> vrfInstances = Sets.newHashSet();
//        exportVpnInstances.forEach(vpnInstance -> {
//            vrfInstances.addAll(vrfInstanceService.getVrfInstances(vpnInstance.id().vpnInstanceId()));
//        });
//        return vrfInstances;
//    }

    private Set<VpnInstance> getVpnInstancesToExportBgpInfo(Set<VpnRouteTarget> exportRouteTargets) {
        Set<VpnInstance> vpnInstances = Sets.newHashSet();
        exportRouteTargets.forEach(vpnRouteTarget -> {
            VpnInstance vpnInstance = vpnInstanceService.getInstance(VpnInstanceId.vpnInstanceId(vpnRouteTarget.getRouteTarget()));
            vpnInstances.add(checkNotNull(vpnInstance));
        });
        return vpnInstances;
    }

    private VpnInstance getVpnInstanceForEvpnRoute(RouteDistinguisher routeDistinguisher) {
        String vpnInstanceId = routeDistinguisher.getRouteDistinguisher().split("/")[1];
        return checkNotNull(vpnInstanceService.getInstance(VpnInstanceId.vpnInstanceId(vpnInstanceId)));
    }

}
