package org.onosproject.evpncontrail.impl;

import org.apache.felix.scr.annotations.*;
import org.onlab.util.KryoNamespace;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.evpncontrail.api.*;
import org.onosproject.evpnrouteservice.*;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.device.DeviceService;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.*;
import org.slf4j.Logger;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of the OpenContrail EVPN service.
 */
@Component(immediate = true)
@Service
public class EvpnContrailManager implements EvpnService, VrfInstanceService {

    private final Logger logger = getLogger(getClass());

    protected EventuallyConsistentMap<String, VrfInstance> vrfInstanceStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EvpnRouteService evpnRouteService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EvpnRouteStore evpnRouteStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VpnInstanceService vpnInstanceService;

    protected ApplicationId appId;

    private InternalRouteEventListener routeEventListener = new InternalRouteEventListener();

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.evpncontrail");
        evpnRouteService.addListener(routeEventListener);

        KryoNamespace.Builder serializer = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API).register(VrfInstance.class)
                .register(VpnInstanceId.class);
        vrfInstanceStore = storageService
                .<String, VrfInstance>eventuallyConsistentMapBuilder()
                .withName("vrf-instance-store").withSerializer(serializer)
                .withTimestampProvider((k, v) -> new WallClockTimestamp()).build();

        initializeVpnInstances();
        logger.info("Started.");
    }

    private void initializeVpnInstances() {
        vpnInstanceService.createInstance(createVpnInstance("blue", "65000:20"));
        vpnInstanceService.createInstance(createVpnInstance("red", "65000:10"));

    }

    private VpnInstance createVpnInstance(String name, String rd) {
        VpnInstanceId vpnInstanceId = VpnInstanceId.vpnInstanceId(name);
        EvpnInstanceName evpnInstanceName = EvpnInstanceName.evpnName(name);
        RouteDistinguisher routeDistinguisher = RouteDistinguisher.routeDistinguisher(rd);
        Set<VpnRouteTarget> exportRouteTargets = new HashSet<>();
        Set<VpnRouteTarget> importRouteTargets = new HashSet<>();
        Set<VpnRouteTarget> configRouteTargets = new HashSet<>();
        VpnInstance vpnInstance = new DefaultVpnInstance(vpnInstanceId, evpnInstanceName, "", routeDistinguisher, exportRouteTargets, importRouteTargets, configRouteTargets);
        return vpnInstance;
    }

    @Deactivate
    public void deactivate() {
        evpnRouteService.removeListener(routeEventListener);
        vrfInstanceStore.destroy();
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

    public Collection<EvpnRouteSet> getVpnRoutes(String vrfTableName) {
        for(EvpnRouteTableId evpnRouteTableId : evpnRouteStore.getRouteTables()) {
            if(evpnRouteTableId.name().equals(vrfTableName))
                return evpnRouteStore.getRoutes(evpnRouteTableId);
        }
        return null;
    }

    @Override
    public void addVrfInstance(String vpnName, DeviceId device) {
        checkNotNull(vpnName);
        checkNotNull(device);

        VpnInstance vpnInstance = vpnInstanceService.getInstance(VpnInstanceId.vpnInstanceId(vpnName));
        String id = createVrfId(vpnInstance.id().vpnInstanceId(), device.toString());
        EvpnRouteTableId vrfId = new EvpnRouteTableId("VRF:" + id);
        VrfInstance vrfInstance = new DefaultVrfInstance(id, vpnInstance, device, vrfId);
        vrfInstanceStore.put(id, vrfInstance);
    }

    @Override
    public void removeVrfInstance(String vpnName, DeviceId deviceId) {
        checkNotNull(vpnName);
        checkNotNull(deviceId);
        String vrfId = createVrfId(vpnName, deviceId.toString());
        vrfInstanceStore.remove(vrfId, vrfInstanceStore.get(vrfId));
    }

    @Override
    public VrfInstance getVrfInstance(String vpnName, DeviceId device) {
        checkNotNull(vpnName);
        checkNotNull(device);
        String vrfId = createVrfId(vpnName, device.toString());
        if(vrfInstanceStore.containsKey(vrfId))
            return vrfInstanceStore.get(vrfId);
        return null;
    }

    @Override
    public boolean vrfExists(String vpnName, DeviceId device) {
        checkNotNull(vpnName);
        checkNotNull(device);
        return this.vrfInstanceStore.containsKey(createVrfId(vpnName, device.toString()));
    }

    @Override
    public Collection<VrfInstance> getVrfInstances() {
        return vrfInstanceStore.values();
    }

    private String createVrfId(String vpnName, String deviceId) {
        return vpnName + "/" + deviceId;
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
