package org.onosproject.provider.xmpp.bgpvpn.route;

import org.apache.felix.scr.annotations.*;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onosproject.evpnrouteservice.*;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.routeserver.api.VpnInstance;
import org.onosproject.routeserver.api.VpnInstanceId;
import org.onosproject.routeserver.api.VpnInstanceService;
import org.onosproject.xmpp.core.XmppDeviceId;
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

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    private InternalXmppPubSubEventListener xmppPubSubEventsListener =
            new InternalXmppPubSubEventListener();

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

        EvpnInfo info = EvpnInfo.parseXmppPublish(publish);

        Ip4Address ipAddress = Ip4Address
                .valueOf(info.getNlriIpAddress());

        Ip4Address ipNextHop = Ip4Address
                .valueOf(info.getNextHopAddress());

        EvpnRoute.Source source = EvpnRoute.Source.LOCAL;
        List<VpnRouteTarget> exportRt = new LinkedList<>();
        exportRt.add(info.getRouteTarget(publish.getFrom().toString()));
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

    private void withdrawRoute(XmppRetract retract) {
        logger.info("Handling RETRACT");

        EvpnInfo info = EvpnInfo.parseXmppRetract(retract);

        Ip4Address ipAddress = Ip4Address
                .valueOf(info.getNlriIpAddress());

        Ip4Address ipNextHop = Ip4Address
                .valueOf(info.getNextHopAddress());

        VpnInstance vpnInstance = vpnInstanceService.getInstance(VpnInstanceId.vpnInstanceId(retract.getNodeID()));
        if(vpnInstance!=null) {
            List<VpnRouteTarget> exportRt = new LinkedList<>();
            exportRt.add(info.getRouteTarget(retract.getFrom().toString()));
            EvpnRoute evpnRoute = new EvpnRoute(EvpnRoute.Source.LOCAL,
                    MacAddress.valueOf(info.getMacAddress()),
                    IpPrefix.valueOf(ipAddress, 32),
                    ipNextHop,
                    info.getRouteDistinguisher(retract.getNodeID()),
                    null, exportRt, info.getLabel());
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
            registerDevice(subscribeEvent);
        }

        @Override
        public void handleUnsubscribe(XmppUnsubscribe unsubscribeEvent) {
            withdrawRouteTarget(unsubscribeEvent);
            unregisterDevice(unsubscribeEvent);
        }
    }

    private void unregisterDevice(XmppUnsubscribe unsubscribeEvent) {
        VpnInstanceId vpnInstanceId = VpnInstanceId.vpnInstanceId(unsubscribeEvent.getNodeID());
        DeviceId deviceId = DeviceId.deviceId(XmppDeviceId.uri(unsubscribeEvent.getJabberId()));
        Device device = deviceService.getDevice(deviceId);
        vpnInstanceService.detachDevice(vpnInstanceId, device);
    }

    private void registerDevice(XmppSubscribe subscribeEvent) {
        VpnInstanceId vpnInstanceId = VpnInstanceId.vpnInstanceId(subscribeEvent.getNodeID());
        DeviceId deviceId = DeviceId.deviceId(XmppDeviceId.uri(subscribeEvent.getJabberId()));
        Device device = deviceService.getDevice(deviceId);
        vpnInstanceService.attachDevice(vpnInstanceId, device);
    }

    private void withdrawRouteTarget(XmppUnsubscribe unsubscribeEvent) {
        String vpnInstanceId = unsubscribeEvent.getNodeID();
        String label = String.valueOf(vpnInstanceService.getInstance(VpnInstanceId.vpnInstanceId(vpnInstanceId)).label().getLabel());
        String routeTarget = String.format("target/%s/%s", unsubscribeEvent.getJabberId(), label);
        vpnInstanceService.withdrawImpExpRouteTargets(VpnInstanceService.RouteTargetType.BOTH,
                                                    VpnRouteTarget.routeTarget(routeTarget),
                                                    VpnInstanceId.vpnInstanceId(vpnInstanceId));
    }

    private void updateRouteTarget(XmppSubscribe subscribeEvent) {
        String vpnInstanceId = subscribeEvent.getNodeID();
        String label = String.valueOf(vpnInstanceService.getInstance(VpnInstanceId.vpnInstanceId(vpnInstanceId)).label().getLabel());
        String routeTarget = String.format("target/%s/%s", subscribeEvent.getJabberId(), label);
        vpnInstanceService.updateImpExpRouteTargets(VpnInstanceService.RouteTargetType.BOTH,
                                                    VpnRouteTarget.routeTarget(routeTarget),
                                                    VpnInstanceId.vpnInstanceId(vpnInstanceId));
    }

    private void sendEventNotification(DeviceId deviceId, String vpnName, Element payload) {
        xmppPubSubController.notify(deviceId, new XmppEventNotification(vpnName, payload));
    }

}
