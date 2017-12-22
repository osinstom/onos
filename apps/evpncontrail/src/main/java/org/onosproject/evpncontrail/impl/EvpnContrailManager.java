package org.onosproject.evpncontrail.impl;

import org.apache.felix.scr.annotations.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.evpncontrail.api.EvpnService;
import org.onosproject.evpncontrail.api.VpnPort;
import org.onosproject.evpnrouteservice.EvpnRoute;
import org.onosproject.evpnrouteservice.EvpnRouteEvent;
import org.onosproject.evpnrouteservice.EvpnRouteListener;
import org.onosproject.evpnrouteservice.EvpnRouteService;
import org.onosproject.net.Device;
import org.onosproject.net.Host;
import org.onosproject.net.device.DeviceService;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of the OpenContrail EVPN service.
 */
@Component(immediate = true)
@Service
public class EvpnContrailManager implements EvpnService {

    private final Logger logger = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EvpnRouteService evpnRouteService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    protected ApplicationId appId;

    private InternalRouteEventListener routeEventListener = new InternalRouteEventListener();

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.evpncontrail");
        evpnRouteService.addListener(routeEventListener);
        logger.info("Started.");
    }

    @Deactivate
    public void deactivate() {
        evpnRouteService.removeListener(routeEventListener);
        logger.info("Stopped.");
    }

    @Override
    public void onBgpEvpnRouteUpdate(EvpnRoute route) {
        logger.info("onBgpEvpnRouteUpdate");

        deviceService.getAvailableDevices(Device.Type.SWITCH).forEach( device -> {


        });
    }

    @Override
    public void onBgpEvpnRouteDelete(EvpnRoute route) {

    }

    @Override
    public void onHostDetected(Host host) {

    }

    @Override
    public void onHostVanished(Host host) {

    }

    @Override
    public void onVpnPortSet(VpnPort vpnPort) {

    }

    @Override
    public void onVpnPortDelete(VpnPort vpnPort) {

    }

    private class InternalRouteEventListener implements EvpnRouteListener {

        @Override
        public void event(EvpnRouteEvent event) {
            if (!(event.subject() instanceof EvpnRoute)) {
                return;
            }
            EvpnRoute route = (EvpnRoute) event.subject();
            if (EvpnRouteEvent.Type.ROUTE_ADDED == event.type()) {
                onBgpEvpnRouteUpdate(route);
            } else if (EvpnRouteEvent.Type.ROUTE_REMOVED == event.type()) {
                onBgpEvpnRouteDelete(route);
            }
        }
    }


}
