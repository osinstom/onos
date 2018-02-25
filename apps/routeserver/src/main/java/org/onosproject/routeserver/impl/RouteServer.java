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

package org.onosproject.routeserver.impl;

import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.evpnrouteservice.EvpnInstanceName;
import org.onosproject.evpnrouteservice.EvpnRoute;
import org.onosproject.evpnrouteservice.EvpnRouteEvent;
import org.onosproject.evpnrouteservice.EvpnRouteListener;
import org.onosproject.evpnrouteservice.EvpnRouteService;
import org.onosproject.evpnrouteservice.EvpnRouteSet;
import org.onosproject.evpnrouteservice.EvpnRouteStore;
import org.onosproject.evpnrouteservice.EvpnRouteTableId;
import org.onosproject.evpnrouteservice.RouteDistinguisher;
import org.onosproject.evpnrouteservice.VpnRouteTarget;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.Objective;
import org.onosproject.net.host.HostService;
import org.onosproject.routeserver.api.DefaultVpnInstance;
import org.onosproject.routeserver.api.DefaultVrfInstance;
import org.onosproject.routeserver.api.EvpnService;
import org.onosproject.routeserver.api.VpnInstance;
import org.onosproject.routeserver.api.VpnInstanceId;
import org.onosproject.routeserver.api.VpnInstanceService;
import org.onosproject.routeserver.api.VrfInstance;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WallClockTimestamp;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of the EVPN service.
 */
@Component(immediate = true)
@Service
public class RouteServer implements EvpnService {

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

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    protected ApplicationId appId;

    private InternalRouteEventListener routeEventListener = new InternalRouteEventListener();
    private InternalDeviceListener deviceListener = new InternalDeviceListener();

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.evpncontrail");
        evpnRouteService.addListener(routeEventListener);
        deviceService.addListener(deviceListener);
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
        vpnInstanceService.createInstance(createVpnInstance("blue","blue", "20"));
        vpnInstanceService.createInstance(createVpnInstance("red","red", "10"));
    }

    private VpnInstance createVpnInstance(String instanceId, String name, String label) {
        VpnInstanceId vpnInstanceId = VpnInstanceId.vpnInstanceId(instanceId);
        EvpnInstanceName evpnInstanceName = EvpnInstanceName.evpnName(name);
        RouteDistinguisher routeDistinguisher = RouteDistinguisher.routeDistinguisher(name + "/" + label);
        Set<VpnRouteTarget> exportRouteTargets = new HashSet<>();
        exportRouteTargets.add(VpnRouteTarget.routeTarget(name));
        Set<VpnRouteTarget> importRouteTargets = new HashSet<>();
        importRouteTargets.add(VpnRouteTarget.routeTarget(name));
        Set<VpnRouteTarget> configRouteTargets = new HashSet<>();
        VpnInstance vpnInstance = new DefaultVpnInstance(vpnInstanceId, evpnInstanceName, "VPN with customer name '" + name +"'",
                                                         routeDistinguisher, exportRouteTargets, importRouteTargets, configRouteTargets);
        return vpnInstance;
    }

    @Deactivate
    public void deactivate() {
        evpnRouteService.removeListener(routeEventListener);
        deviceService.removeListener(deviceListener);
        vrfInstanceStore.destroy();
        logger.info("Stopped.");
    }

    @Override
    public void onBgpEvpnRouteUpdate(EvpnRoute route) {
        logger.info("onBgpEvpnRouteUpdate");
        deviceService.getAvailableDevices()
                .forEach(device -> {
                    logger.info("switch device is found");
                    Set<Host> hosts = getHostsByVpn(device, route);
                    logger.info(hosts.toString());
                    for (Host h : hosts) {
                        ForwardingObjective.Builder objective =
                                getEvpnFlowBuilder(device.id(),
                                                  route,
                                                  h);
                        logger.info("Installing flow rule");
                        flowObjectiveService.forward(device.id(),
                                                     objective.add());
                    }
                });
    }

    private ForwardingObjective.Builder getEvpnFlowBuilder(DeviceId id, EvpnRoute route, Host h) {
        TrafficTreatment.Builder builder = DefaultTrafficTreatment.builder();
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthSrc(h.mac())
                .matchEthDst(route.prefixMac()).build();
        TrafficTreatment build = builder.setTunnelId(route.label().getLabel())
                .setIpDst(route.ipNextHop()).build();

        return DefaultForwardingObjective
                .builder().withTreatment(build).withSelector(selector)
                .fromApp(appId).withFlag(ForwardingObjective.Flag.SPECIFIC)
                .withPriority(60000);
    }

    private Set<Host> getHostsByVpn(Device device, EvpnRoute route) {
            Set<Host> vpnHosts = Sets.newHashSet();
            Set<Host> hosts = hostService.getConnectedHosts(device.id());
            logger.info(hosts.toString());
            for (Host h : hosts) {
                String vpnName = h.annotations().value("vpn-instance");
                VpnInstanceId vpnInstanceId = VpnInstanceId.vpnInstanceId(vpnName);

                VpnInstance vpnInstance = vpnInstanceService
                        .getInstance(vpnInstanceId);

//                List<VpnRouteTarget> expRt = route.exportRouteTarget();
//                List<VpnRouteTarget> similar = new LinkedList<>(expRt);
//                similar.retainAll(vpnInstance.getImportRouteTargets());

                if (vpnInstance!=null) {
                    vpnHosts.add(h);
                    logger.info("Adding host");
                }
            }
            return vpnHosts;

    }

    @Override
    public void onBgpEvpnRouteDelete(EvpnRoute route) {
        logger.info("onBgpEvpnRouteDelete");
    }

    @Override
    public void onHostDetected(Host host) {
    }

    @Override
    public void onHostVanished(Host host) {

    }

    public Collection<EvpnRouteSet> getVpnRoutes(String vrfTableName) {
        for(EvpnRouteTableId evpnRouteTableId : evpnRouteStore.getRouteTables()) {
            if(evpnRouteTableId.name().equals(vrfTableName))
                return evpnRouteStore.getRoutes(evpnRouteTableId);
        }
        return null;
    }


    public void addVrfInstance(String vpnName, DeviceId device) {
        checkNotNull(vpnName);
        checkNotNull(device);

        VpnInstance vpnInstance = vpnInstanceService.getInstance(VpnInstanceId.vpnInstanceId(vpnName));
        String id = createVrfId(vpnInstance.id().vpnInstanceId(), device.toString());
        // TODO: temporary solution. Need to implement creation for new routing table id for each VPN/VRF
//        EvpnRouteTableId vrfId = new EvpnRouteTableId("evpn_ipv4");
        EvpnRouteTableId vrfId = new EvpnRouteTableId("VRF:" + id);

        VrfInstance vrfInstance = new DefaultVrfInstance(id, vpnInstance, device, vrfId);
        vrfInstanceStore.put(id, vrfInstance);
    }


    public void removeVrfInstance(String vpnName, DeviceId deviceId) {
        checkNotNull(vpnName);
        checkNotNull(deviceId);
        String vrfId = createVrfId(vpnName, deviceId.toString());
        vrfInstanceStore.remove(vrfId, vrfInstanceStore.get(vrfId));
    }

    private void removeVrfAssociatedWithDevice(DeviceId deviceId) {
        vrfInstanceStore.keySet().forEach(s -> {
            if(vrfInstanceStore.get(s).device().equals(deviceId)) {
                vrfInstanceStore.remove(s, vrfInstanceStore.get(s));
            }
        });
    }


    public VrfInstance getVrfInstance(String vpnName, DeviceId device) {
        checkNotNull(vpnName);
        checkNotNull(device);
        String vrfId = createVrfId(vpnName, device.toString());
        if(vrfInstanceStore.containsKey(vrfId))
            return vrfInstanceStore.get(vrfId);
        return null;
    }


    public boolean vrfExists(String vpnName, DeviceId device) {
        checkNotNull(vpnName);
        checkNotNull(device);
        return this.vrfInstanceStore.containsKey(createVrfId(vpnName, device.toString()));
    }


    public Collection<VrfInstance> getVrfInstances() {
        return vrfInstanceStore.values();
    }


    public Collection<VrfInstance> getVrfInstances(String vpnName) {
        Set<VrfInstance> vrfInstances = Sets.newHashSet();
        vrfInstanceStore.values().forEach(vrfInstance -> {
            if(vrfInstance.vpnInstance().id().vpnInstanceId().equals(vpnName))
                vrfInstances.add(vrfInstance);
        });
        return vrfInstances;
    }

    private String createVrfId(String vpnName, String deviceId) {
        return vpnName + "/" + deviceId;
    }


    private class InternalDeviceListener implements DeviceListener {

        @Override
        public void event(DeviceEvent event) {
            logger.info("DeviceEvent: " + event.toString());
            switch(event.type()) {
                case DEVICE_REMOVED:
                    removeVrfAssociatedWithDevice(event.subject().id());
                    break;
            }
        }
    }

    private class InternalRouteEventListener implements EvpnRouteListener {

        @Override
        public void event(EvpnRouteEvent event) {
            if (!(event.subject() instanceof EvpnRoute)) {
                return;
            }
            logger.info("Received: " + event.toString());
            EvpnRoute route = (EvpnRoute) event.subject();
            if (EvpnRouteEvent.Type.ROUTE_ADDED == event.type()) {
                onBgpEvpnRouteUpdate(route);
            } else if (EvpnRouteEvent.Type.ROUTE_REMOVED == event.type()) {
                onBgpEvpnRouteDelete(route);
            }
        }
    }
}
