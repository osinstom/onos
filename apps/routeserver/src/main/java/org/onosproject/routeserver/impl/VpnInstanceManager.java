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

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.evpnrouteservice.Label;
import org.onosproject.evpnrouteservice.VpnRouteTarget;
import org.onosproject.routeserver.api.VpnInstance;
import org.onosproject.routeserver.api.VpnInstanceId;
import org.onosproject.routeserver.api.VpnInstanceService;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WallClockTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
@Component(immediate = true)
@Service
public class VpnInstanceManager implements VpnInstanceService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected EventuallyConsistentMap<VpnInstanceId, VpnInstance> vpnInstanceStore;
    protected ApplicationId appId;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.evpncontrail");
        KryoNamespace.Builder serializer = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API).register(VpnInstance.class)
                .register(VpnInstanceId.class);
        vpnInstanceStore = storageService
                .<VpnInstanceId, VpnInstance>eventuallyConsistentMapBuilder()
                .withName("vpn-instance-store").withSerializer(serializer)
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();
        logger.info("Started.");
    }

    @Deactivate
    public void deactivate() {
        vpnInstanceStore.destroy();
    }


    @Override
    public boolean exists(VpnInstanceId vpnInstanceId) {
        checkNotNull(vpnInstanceId);
        return vpnInstanceStore.containsKey(vpnInstanceId);
    }

    @Override
    public VpnInstance getInstance(VpnInstanceId vpnInstanceId) {
        checkNotNull(vpnInstanceId);
        return vpnInstanceStore.get(vpnInstanceId);
    }

    @Override
    public VpnInstance getInstanceByLabel(Label label) {
        checkNotNull(label);
        VpnInstance instanceWithLabel = null;
        for (VpnInstance vpnInstance : vpnInstanceStore.values()) {
            logger.info("Analyzing vpnInstance with label {}, vs. label {}", vpnInstance.label(), label);
            if (vpnInstance.label().equals(label)) {
                instanceWithLabel = vpnInstance;
            }
        }
        logger.info("Returning VPN Instance with name {}", instanceWithLabel.vpnInstanceName());
        return instanceWithLabel;
    }

    @Override
    public Collection<VpnInstance> getInstances() {
        return Collections.unmodifiableCollection(vpnInstanceStore.values());
    }

    @Override
    public boolean createInstance(VpnInstance vpnInstance) {
        checkNotNull(vpnInstance);
        vpnInstanceStore.put(vpnInstance.id(), vpnInstance);
        if (!vpnInstanceStore.containsKey(vpnInstance.id())) {
            logger.info("Vpn Instance creation failed",
                    vpnInstance.id().toString());
            return false;
        }
        return true;
    }

    @Override
    public boolean createInstances(Iterable<VpnInstance> vpnInstances) {
        checkNotNull(vpnInstances);
        for (VpnInstance vpnInstance : vpnInstances) {
            logger.info("EVPN instance ID is  {} ", vpnInstance.id().toString());
            vpnInstanceStore.put(vpnInstance.id(), vpnInstance);
            if (!vpnInstanceStore.containsKey(vpnInstance.id())) {
                logger.info("Vpn Instance creation failed",
                        vpnInstance.id().toString());
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean updateInstances(Iterable<VpnInstance> vpnInstances) {
        checkNotNull(vpnInstances);
        for (VpnInstance vpnInstance : vpnInstances) {
            if (!vpnInstanceStore.containsKey(vpnInstance.id())) {
                logger.info("Vpn Instance not exists",
                        vpnInstance.id().toString());
                return false;
            }
            vpnInstanceStore.put(vpnInstance.id(), vpnInstance);
            if (!vpnInstance.equals(vpnInstanceStore.get(vpnInstance.id()))) {
                logger.info("Vpn Instance update failed",
                        vpnInstance.id().toString());
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeInstances(Iterable<VpnInstanceId> vpnInstanceIds) {
        checkNotNull(vpnInstanceIds);
        for (VpnInstanceId vpnInstanceId : vpnInstanceIds) {
            vpnInstanceStore.remove(vpnInstanceId);
            if (vpnInstanceStore.containsKey(vpnInstanceId)) {
                logger.info("Vpn Instance delete failed", vpnInstanceId.toString());
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateImpExpRouteTargets(RouteTargetType routeTargetType, VpnRouteTarget vpnRouteTarget, VpnInstanceId vpnInstanceId) {

        VpnInstance vpnInstance = vpnInstanceStore.get(vpnInstanceId);
        checkNotNull(vpnInstance);
        switch(routeTargetType) {
            case EXPORT:
                vpnInstance.getExportRouteTargets().add(vpnRouteTarget);
                break;
            case IMPORT:
                vpnInstance.getImportRouteTargets().add(vpnRouteTarget);
                break;
            case BOTH:
                vpnInstance.getImportRouteTargets().add(vpnRouteTarget);
                vpnInstance.getExportRouteTargets().add(vpnRouteTarget);
                break;
        }
        logger.info("Import/Export RouteTarget policies has been updated.");
        logger.info("Current state: " +
                            "ExportRouteTargets:" + vpnInstance.getExportRouteTargets().toString() +
                            "ImportRouteTargets:" + vpnInstance.getImportRouteTargets().toString());
    }

    @Override
    public void withdrawImpExpRouteTargets(RouteTargetType routeTargetType, VpnRouteTarget vpnRouteTarget, VpnInstanceId vpnInstanceId) {
        VpnInstance vpnInstance = vpnInstanceStore.get(vpnInstanceId);
        checkNotNull(vpnInstance);
        switch(routeTargetType) {
            case EXPORT:
                vpnInstance.getExportRouteTargets().remove(vpnRouteTarget);
                break;
            case IMPORT:
                vpnInstance.getImportRouteTargets().remove(vpnRouteTarget);
                break;
            case BOTH:
                vpnInstance.getImportRouteTargets().remove(vpnRouteTarget);
                vpnInstance.getExportRouteTargets().remove(vpnRouteTarget);
                break;
        }
        logger.info("Import/Export RouteTarget policies has been withdrawn.");
        logger.info("Current state: " +
                            "ExportRouteTargets:" + vpnInstance.getExportRouteTargets().toString() +
                            "ImportRouteTargets:" + vpnInstance.getImportRouteTargets().toString());
    }


}
