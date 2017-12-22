package org.onosproject.provider.xmpp.bgpvpn.route;

import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.*;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onosproject.evpnrouteservice.*;
import org.onosproject.net.Device;
import org.onosproject.net.Host;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.xmpp.pubsub.XmppPubSubController;
import org.onosproject.xmpp.pubsub.XmppPubSubEvent;
import org.onosproject.xmpp.pubsub.XmppPubSubEventListener;
import org.onosproject.xmpp.pubsub.model.EventNotification;
import org.onosproject.xmpp.pubsub.model.Publish;
import org.onosproject.xmpp.pubsub.model.Retract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 */
@Component(immediate = true)
public class XmppBgpVpnRouteProvider extends AbstractProvider  {

    private static final Logger logger = LoggerFactory
            .getLogger(XmppBgpVpnRouteProvider.class);

    private static final String BGPVPN_NAMESPACE = "http://ietf.org/protocol/bgpvpn";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EvpnRouteAdminService evpnRouteAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppPubSubController xmppPubSubController;

    private InternalXmppPubSubEventListener xmppPubSubEventListener = new InternalXmppPubSubEventListener();
    private InternalEvpnRouteListener routeListener = new InternalEvpnRouteListener();

    public XmppBgpVpnRouteProvider() {
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
        BgpVpnPublish info = asBgpInfo(publish);

        logger.info("Received: " + info.toString());
        Ip4Address ipAddress = Ip4Address
                .valueOf(info.getNlriIpAddress());

        Ip4Address ipNextHop = Ip4Address
                .valueOf(info.getNextHopAddress());

        EvpnRoute.Source source = EvpnRoute.Source.LOCAL;
        List<VpnRouteTarget> exportRt = new LinkedList<>();
        exportRt.add(VpnRouteTarget.routeTarget(publish.getNodeID()));
        EvpnRoute evpnRoute = new EvpnRoute(source,
                MacAddress.ZERO,
                IpPrefix.valueOf(ipAddress, 32),
                ipNextHop,
                info.getRouteDistinguisher(),
                null, //empty rt
                exportRt,
                info.getLabel());

        evpnRouteAdminService.update(Collections
                .singleton(evpnRoute));
    }

    private void handleRetract(Retract retract) {

    }

    private BgpVpnPublish asBgpInfo(Publish publish) {
        Element entry = publish.getItemEntry();
        int nlriAf = Integer.parseInt(entry.element("nlri").attribute("af").getValue());
        String nlri = entry.element("nlri").getStringValue();
        int nextHopAf = Integer.parseInt(entry.element("next-hop").attribute("af").getValue());
        String nextHop = entry.element("next-hop").getStringValue();
        int label = Integer.parseInt(entry.element("label").getStringValue());
        int version = Integer.parseInt(entry.element("version").attribute("id").getValue());
        return new BgpVpnPublish(label, nlriAf, nlri, nextHopAf, nextHop, version);
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

    private void sendEventNotification(Device device, EvpnRoute evpnRoute) {
        Element payload = asXmppPayload(evpnRoute);
        xmppPubSubController.notify(device.id(), new EventNotification(evpnRoute.exportRouteTarget().get(0).getRouteTarget(), payload));
    }

    private Element asXmppPayload(EvpnRoute evpnRoute) {
        DocumentFactory df = DocumentFactory.getInstance();
        Element item = df.createElement("item");
        item.addAttribute("id", evpnRoute.prefixIp().address().toString() + ":1:" + evpnRoute.ipNextHop().toString());
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

    private class InternalEvpnRouteListener implements EvpnRouteListener {

        @Override
        public void event(EvpnRouteEvent event) {
            EvpnRoute evpnRoute = event.subject();
            logger.info("Event received for public route {}", evpnRoute);
            if (evpnRoute.source().equals(EvpnRoute.Source.REMOTE)) {
                return;
            }

            Set<Device> vpnDevices = getDevicesByVpn(evpnRoute);

            switch (event.type()) {
                case ROUTE_ADDED:
                case ROUTE_UPDATED:
                    logger.info("route added");
                    vpnDevices.forEach(device -> {
                        sendEventNotification(device, evpnRoute);
                    });
                    break;
                case ROUTE_REMOVED:
                    logger.info("route deleted");

                    break;
                default:
                    break;
            }
        }

    }

    private Set<Device> getDevicesByVpn(EvpnRoute evpnRoute) {
        Set<Device> vpnDevices = Sets.newHashSet();
        deviceService.getDevices(Device.Type.VIRTUAL).forEach( device -> {
            evpnRoute.exportRouteTarget().forEach( vpnRouteTarget -> {
                logger.info(vpnRouteTarget + " ? " + device.annotations().value("VPN"));
                if(vpnRouteTarget.getRouteTarget().equals(device.annotations().value("VPN"))) {
                    vpnDevices.add(device);
                }
            });
        });
        return vpnDevices;
    }

}
