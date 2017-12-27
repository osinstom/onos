package org.onosproject.provider.xmpp.bgpvpn.route;

import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.*;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onosproject.evpncontrail.api.*;
import org.onosproject.evpnrouteservice.*;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.host.HostProviderService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.xmpp.core.XmppDeviceId;
import org.onosproject.xmpp.pubsub.XmppPubSubController;
import org.onosproject.xmpp.pubsub.XmppPubSubEvent;
import org.onosproject.xmpp.pubsub.XmppPubSubEventListener;
import org.onosproject.xmpp.pubsub.model.EventNotification;
import org.onosproject.xmpp.pubsub.model.Publish;
import org.onosproject.xmpp.pubsub.model.Retract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
@Component(immediate = true)
public class XmppEvpnRouteProvider extends AbstractProvider  {

    private final Logger logger = LoggerFactory
            .getLogger(XmppEvpnRouteProvider.class);

    private static final String BGPVPN_NAMESPACE = "http://ietf.org/protocol/bgpvpn";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EvpnRouteAdminService evpnRouteAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VrfInstanceService vrfInstanceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VpnInstanceService vpnInstanceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppPubSubController xmppPubSubController;

    private HostProviderService providerService;

    private InternalXmppPubSubEventListener xmppPubSubEventListener = new InternalXmppPubSubEventListener();
    private InternalEvpnRouteListener routeListener = new InternalEvpnRouteListener();

    public XmppEvpnRouteProvider() {
        super(new ProviderId("route",
                "org.onosproject.provider.xmpp.bgpvpn.route"));
    }

    @Activate
    public void activate() {
        xmppPubSubController.addXmppPubSubEventListener(xmppPubSubEventListener);
        evpnRouteAdminService.addListener(routeListener);
        logger.info("Started.");
    }

    @Deactivate
    public void deactivate() {
        xmppPubSubController.removeXmppPubSubEventListener(xmppPubSubEventListener);
        evpnRouteAdminService.removeListener(routeListener);
        logger.info("Stopped.");
    }

    private void handlePublish(Publish publish) {
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
                info.getRouteDistinguisher(publish.getNodeID()),
                null, //empty rt
                exportRt,
                info.getLabel());

        evpnRouteAdminService.update(Collections
                .singleton(evpnRoute));
    }

    private void handleRetract(Retract retract) {
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

    private class InternalXmppPubSubEventListener implements XmppPubSubEventListener {

        @Override
        public void handle(XmppPubSubEvent event) {
            switch (event.type()) {
                case PUBLISH:
                    Publish publish = (Publish) event.subject();
                    if(publish.getItemEntryNamespace().equals(BGPVPN_NAMESPACE))
                        handlePublish(publish);
                    break;
                case RETRACT:
                    Retract retract = (Retract) event.subject();
                    handleRetract(retract);
                    break;
            }
        }
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
        xmppPubSubController.notify(deviceId, new EventNotification(vpnName, payload));
    }

    private Element asXmppPublishPayload(EvpnRoute evpnRoute) {
        DocumentFactory df = DocumentFactory.getInstance();
        Element item = df.createElement("item");
        item.addAttribute("id", generateItemId(evpnRoute));
        Element entry = df.createElement("entry", BGPVPN_NAMESPACE);
        Element nlri = df.createElement("nlri");
        nlri.addText(evpnRoute.prefixIp().address().toString());
        Element nextHop = df.createElement("next-hop");
        nextHop.addText(evpnRoute.ipNextHop().toString());
        Element label = df.createElement("label");
        label.addText(Integer.toString(evpnRoute.label().getLabel()));
        entry.add(nlri);
        entry.add(nextHop);
        entry.add(label);
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

            Set<DeviceId> vpnDevices = getDevicesToDistributeInformation(evpnRoute);

            switch (event.type()) {
                case ROUTE_ADDED:
                case ROUTE_UPDATED:
                    logger.info("route added");
                    vpnDevices.forEach(deviceId -> {
                        populateBgpUpdate(deviceId, evpnRoute);
                    });
                    break;
                case ROUTE_REMOVED:
                    logger.info("route deleted");
                    vpnDevices.forEach(deviceId -> {
                        populateBgpDelete(deviceId, evpnRoute);
                    });
                    break;
                default:
                    break;
            }
        }

    }

    private Set<DeviceId> getDevicesToDistributeInformation(EvpnRoute evpnRoute) {
        Set<DeviceId> vpnDevices = Sets.newHashSet();
        VpnInstance routeEvpnInstance = getVpnInstanceForEvpnRoute(evpnRoute.routeDistinguisher());
        Set<VpnRouteTarget> exportRouteTargets = routeEvpnInstance.getExportRouteTargets();
        Set<VpnInstance> exportVpnInstances = getVpnInstancesToExportBgpInfo(exportRouteTargets);
        Set<VrfInstance> vrfInstancesToExportBgpInfo = getVrfInstancesToExportBgpInfo(exportVpnInstances);
        vpnDevices = getDevicesToPopulateEvpnInfo(vrfInstancesToExportBgpInfo, evpnRoute);
        return vpnDevices;
    }

    private Set<DeviceId> getDevicesToPopulateEvpnInfo(Set<VrfInstance> vrfInstancesToExportBgpInfo, EvpnRoute evpnRoute) {
        Set<DeviceId> devices = Sets.newHashSet();
        IpAddress fromDeviceIpAddress = evpnRoute.ipNextHop();
        vrfInstancesToExportBgpInfo.forEach(vrfInstance -> {
            DeviceId deviceId = vrfInstance.device();
            IpAddress deviceIpAddress = IpAddress.valueOf(deviceService.getDevice(deviceId).annotations().value("IpAddress"));
            if(!deviceIpAddress.equals(fromDeviceIpAddress))
                devices.add(deviceId);
        });
        return devices;
    }

    private Set<VrfInstance> getVrfInstancesToExportBgpInfo(Set<VpnInstance> exportVpnInstances) {
        Set<VrfInstance> vrfInstances = Sets.newHashSet();
        exportVpnInstances.forEach(vpnInstance -> {
            vrfInstances.addAll(vrfInstanceService.getVrfInstances(vpnInstance.id().vpnInstanceId()));
        });
        return vrfInstances;
    }

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

    private Set<Device> getDevicesByVpn(EvpnRoute evpnRoute) {
        Set<Device> vpnDevices = Sets.newHashSet();

        deviceService.getDevices().forEach( device -> {
            evpnRoute.exportRouteTarget().forEach(vpnRouteTarget -> {
                if(vrfInstanceService.getVrfInstance(vpnRouteTarget.getRouteTarget(), device.id()) != null) {
                    logger.info("IP? " + device.annotations().value("IpAddress") + " ? " + evpnRoute.ipNextHop().toString());
                    if(!device.annotations().value("IpAddress").equals(evpnRoute.ipNextHop().toString()))
                        vpnDevices.add(device);
                }

            });
        });
        return vpnDevices;
    }

}
