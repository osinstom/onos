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
import org.onosproject.evpnrouteservice.Label;
import org.onosproject.evpnrouteservice.RouteDistinguisher;
import org.onosproject.evpnrouteservice.VpnRouteTarget;
import org.onosproject.incubator.net.resource.label.LabelResource;
import org.onosproject.incubator.net.resource.label.LabelResourceAdminService;
import org.onosproject.incubator.net.resource.label.LabelResourceId;
import org.onosproject.incubator.net.resource.label.LabelResourceService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.host.HostService;
import org.onosproject.routeserver.api.DefaultVpnInstance;
import org.onosproject.routeserver.api.EvpnService;
import org.onosproject.routeserver.api.VpnInstance;
import org.onosproject.routeserver.api.VpnInstanceId;
import org.onosproject.routeserver.api.VpnInstanceService;
import org.onosproject.routeserver.api.VrfInstance;
import org.onosproject.routeserver.store.VpnInstanceEvent;
import org.onosproject.routeserver.store.VpnInstanceListener;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WallClockTimestamp;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LabelResourceAdminService labelAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LabelResourceService labelService;

    protected ApplicationId appId;

    private InternalRouteEventListener routeEventListener = new InternalRouteEventListener();
    private InternalDeviceListener deviceListener = new InternalDeviceListener();
    private VpnInstanceListener vpnInstanceListener = new InternalVpnInstanceListener();

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.routeserver");
        evpnRouteService.addListener(routeEventListener);
        deviceService.addListener(deviceListener);
        vpnInstanceService.addListener(vpnInstanceListener);
        KryoNamespace.Builder serializer = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API).register(VrfInstance.class)
                .register(VpnInstanceId.class);
        vrfInstanceStore = storageService
                .<String, VrfInstance>eventuallyConsistentMapBuilder()
                .withName("vrf-instance-store").withSerializer(serializer)
                .withTimestampProvider((k, v) -> new WallClockTimestamp()).build();
        // TODO: the global pool should be configured according to encapsulation method (MPLS/VXLAN)
        labelAdminService.createGlobalPool(LabelResourceId.labelResourceId(1),
                                           LabelResourceId.labelResourceId(1000));
        initializeVpnInstances();
        logger.info("Started.");
    }

    private void initializeVpnInstances() {
        vpnInstanceService.createInstance(createVpnInstance("blue"));
        vpnInstanceService.createInstance(createVpnInstance("red"));
    }

    private VpnInstance createVpnInstance(String name) {
        VpnInstanceId vpnInstanceId = VpnInstanceId.vpnInstanceId(name);
        EvpnInstanceName evpnInstanceName = EvpnInstanceName.evpnName(name);
        Label vpnLabel = generateVpnLabel();
        RouteDistinguisher routeDistinguisher = RouteDistinguisher.routeDistinguisher(name + ":" + vpnLabel.getLabel());
        Set<VpnRouteTarget> exportRouteTargets = new HashSet<>();
        Set<VpnRouteTarget> importRouteTargets = new HashSet<>();
        Set<VpnRouteTarget> configRouteTargets = new HashSet<>();
        VpnInstance vpnInstance = new DefaultVpnInstance(vpnInstanceId, evpnInstanceName, "VPN with customer name '" + name +"'",
                                                         routeDistinguisher, exportRouteTargets, importRouteTargets, configRouteTargets, vpnLabel);
        return vpnInstance;
    }

    @Deactivate
    public void deactivate() {
        vpnInstanceService.removeListener(vpnInstanceListener);
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

                    List<VpnRouteTarget> routeTargets = new LinkedList<>(route.exportRouteTarget());
                    logger.info("Route RT: " + routeTargets);
                    VpnInstance vpn = getVpnByRouteDistinguisher(route.routeDistinguisher());
                    Set<VpnRouteTarget> vpnRouteTargets = new LinkedHashSet<>(vpn.getImportRouteTargets());
                    logger.info("VPN Import targets: " + vpnRouteTargets);
                    vpnRouteTargets.removeAll(routeTargets);
                    logger.info("RT to notify, " + vpnRouteTargets);
                    if (!vpnRouteTargets.isEmpty()) {
                        if (isSimilarRouteTarget(vpnRouteTargets, device.id())) {
                            sendUpdate(device.id(), route);
                        }
                    }
                });
    }

    private VpnInstance getVpnByRouteDistinguisher(RouteDistinguisher routeDistinguisher) {
        for (VpnInstance vpnInstance : vpnInstanceService.getInstances()) {
            if (isSimilarRouteDistinguisher(vpnInstance, routeDistinguisher)) {
                return vpnInstance;
            }
        }
        return null;
    }

    private void sendUpdate(DeviceId deviceId, EvpnRoute evpnRoute) {
        ForwardingObjective.Builder objective =
                getEvpnFlowBuilder(evpnRoute);
        logger.info("Installing route");
        flowObjectiveService.forward(deviceId,
                                     objective.add());
    }

    private void sendWithdraw(DeviceId deviceId, EvpnRoute evpnRoute) {
        ForwardingObjective.Builder objective =
                getEvpnFlowBuilder(evpnRoute);
        logger.info("Removing route");
        flowObjectiveService.forward(deviceId,
                                     objective.remove());
    }

    private ForwardingObjective.Builder getEvpnFlowBuilder(EvpnRoute route) {
        TrafficTreatment.Builder builder = DefaultTrafficTreatment.builder();

        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchIPDst(route.prefixIp())
                .matchEthDst(route.prefixMac()).build();
        TrafficTreatment treatment = builder.setTunnelId(route.label().getLabel())
                .setIpDst(route.ipNextHop())
                .build();

        return DefaultForwardingObjective
                .builder().withTreatment(treatment).withSelector(selector)
                .fromApp(appId).withFlag(ForwardingObjective.Flag.SPECIFIC)
                .withPriority(60000);
    }

    private FlowRule getEvpnFlowRule(DeviceId id, EvpnRoute route, Host h) {
        TrafficTreatment.Builder builder = DefaultTrafficTreatment.builder();
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthSrc(h.mac())
                .matchEthDst(route.prefixMac()).build();
        TrafficTreatment treatment = builder.setTunnelId(route.label().getLabel())
                .setIpDst(route.ipNextHop()).build();

        return DefaultFlowRule.builder().forDevice(id)
                .withSelector(selector)
                .withTreatment(treatment)
                .fromApp(appId)
                .makePermanent().withPriority(60000).build();
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

                List<VpnRouteTarget> expRt = route.exportRouteTarget();
                List<VpnRouteTarget> similar = new LinkedList<>(expRt);
                similar.retainAll(vpnInstance.getImportRouteTargets());

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
        deviceService.getAvailableDevices()
                .forEach(device -> {
                    logger.info("switch device is found");

                    List<VpnRouteTarget> routeTargets = new LinkedList<>(route.exportRouteTarget());
                    logger.info("Route RT: " + routeTargets);
                    VpnInstance vpn = getVpnByRouteDistinguisher(route.routeDistinguisher());
                    Set<VpnRouteTarget> vpnRouteTargets = new LinkedHashSet<>(vpn.getImportRouteTargets());
                    logger.info("VPN Import targets: " + vpnRouteTargets);
                    vpnRouteTargets.removeAll(routeTargets);
                    logger.info("RT to notify, " + vpnRouteTargets);
                    if (!vpnRouteTargets.isEmpty()) {
                        if (isSimilarRouteTarget(vpnRouteTargets, device.id())) {
                            sendWithdraw(device.id(), route);
                        }
                    }
                });
        evpnRouteStore.removeRoute(route);
        logger.info("Route {} removed from store", route.evpnPrefix().toString());
    }

    @Override
    public void onHostDetected(Host host) {

    }

    @Override
    public void onHostVanished(Host host) {

    }

    private Label generateVpnLabel() {
        Collection<LabelResource> privateLabels = labelService
                .applyFromGlobalPool(1);
        Label privateLabel = Label.label(0);
        if (!privateLabels.isEmpty()) {
            privateLabel = Label.label(Integer.parseInt(
                    privateLabels.iterator().next()
                            .labelResourceId().toString()));
        }
        logger.info("Applying VPN label {}", privateLabel);
        return privateLabel;
    }


    public Collection<EvpnRouteSet> getVpnRoutes(String vrfTableName) {
        for(EvpnRouteTableId evpnRouteTableId : evpnRouteStore.getRouteTables()) {
            if(evpnRouteTableId.name().equals(vrfTableName))
                return evpnRouteStore.getRoutes(evpnRouteTableId);
        }
        return null;
    }

    private void removeAssociatedRoutes(Device device) {
        Set<EvpnRoute> routesToRemove = new HashSet<>();
        Collection<EvpnRouteSet> collection = evpnRouteStore.getRoutes(new EvpnRouteTableId("evpn_ipv4"));
        for (EvpnRouteSet evpnRouteSet : collection) {
            for(EvpnRoute evpnRoute : evpnRouteSet.routes()) {
                if (checkRouteAssociatedWithDevice(evpnRoute, device)) {
                    routesToRemove.add(evpnRoute);
                }
            }
        }
        routesToRemove.forEach(evpnRoute -> {
            evpnRouteStore.removeRoute(evpnRoute);
            logger.info("Route {} has been removed from store", evpnRoute.toString());
        });
    }

    private void removeDeviceFromVpns(Device device) {
        vpnInstanceService.getInstances().forEach(vpnInstance -> {
            vpnInstanceService.detachDevice(vpnInstance.id(), device);
            logger.info("Device {} has been detached from VPN {}", device.id(), vpnInstance.id());
        });
    }

    private boolean checkRouteAssociatedWithDevice(EvpnRoute evpnRoute, Device device) {
//        logger.info("Checking, {}. {}", rd.getRouteDistinguisher(), deviceId.uri().getSchemeSpecificPart());
//        return rd.getRouteDistinguisher().contains(deviceId.uri().getSchemeSpecificPart());
        // TODO: temporary solution, need to check if Route Distinguisher is similar
        return evpnRoute.ipNextHop().toString().equals(device.annotations().value("IpAddress"));
    }


    private class InternalDeviceListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            logger.info("DeviceEvent: " + event.toString());
            switch(event.type()) {
                case DEVICE_AVAILABILITY_CHANGED:
                case DEVICE_REMOVED:
                    if (!deviceService.isAvailable(event.subject().id())) {
                        removeAssociatedRoutes(event.subject());
                        removeDeviceFromVpns(event.subject());
                    }
                    break;
            }
        }
    }

    private class InternalRouteEventListener implements EvpnRouteListener {

        @Override
        public void event(EvpnRouteEvent event) {
            logger.info("Received: " + event.toString());
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

    private class InternalVpnInstanceListener implements VpnInstanceListener {
        @Override
        public void event(VpnInstanceEvent event) {
            if (event.type() == VpnInstanceEvent.Type.VPN_DEVICE_ATTACHED) {
                notifyEvpnRoutes(event.subject(), event.device());
            }
        }
    }

    private void notifyEvpnRoutes(VpnInstance vpn, Device device) {
        Collection<EvpnRouteSet> collection = evpnRouteStore.getRoutes(new EvpnRouteTableId("evpn_ipv4"));
        collection.forEach(evpnRouteSet -> {
            evpnRouteSet.routes().forEach(evpnRoute -> {
                if (!isSimilarRouteDistinguisher(vpn, evpnRoute.routeDistinguisher())) {
                    return;
                }
                List<VpnRouteTarget> routeTargets = new LinkedList<>(evpnRoute.exportRouteTarget());
                logger.info("Route RT: " + routeTargets);
                Set<VpnRouteTarget> vpnRouteTargets = new LinkedHashSet<>(vpn.getImportRouteTargets());
                logger.info("VPN Import targets: " + vpnRouteTargets);
                routeTargets.retainAll(vpnRouteTargets);
                logger.info("RT to notify, " + routeTargets);
                // if routeTargets do not point to device, notify route
                if (isSimilarRouteTarget(routeTargets, device.id())) {
                    sendUpdate(device.id(), evpnRoute);
                }
            });
        });
    }

    private boolean isSimilarRouteDistinguisher(VpnInstance vpn, RouteDistinguisher routeDistinguisher) {
        return vpn.routeDistinguisher().equals(routeDistinguisher);
    }

    private boolean isSimilarRouteTarget(Collection<VpnRouteTarget> routeTargets, DeviceId deviceId) {
        for(VpnRouteTarget vpnRouteTarget : routeTargets) {
            logger.info("Analyzing " + vpnRouteTarget.getRouteTarget() + " " + deviceId.uri().getSchemeSpecificPart());
            if (vpnRouteTarget.getRouteTarget().contains(deviceId.uri().getSchemeSpecificPart())) {
                return true;
            }
        }
        return false;
    }

}
