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
import org.onosproject.event.EventDeliveryService;
import org.onosproject.event.ListenerRegistry;
import org.onosproject.event.ListenerService;
import org.onosproject.evpnrouteservice.Label;
import org.onosproject.evpnrouteservice.VpnRouteTarget;
import org.onosproject.net.Device;
import org.onosproject.routeserver.api.VpnInstance;
import org.onosproject.routeserver.api.VpnInstanceId;
import org.onosproject.routeserver.api.VpnInstanceService;
import org.onosproject.routeserver.store.VpnInstanceEvent;
import org.onosproject.routeserver.store.VpnInstanceListener;
import org.onosproject.routeserver.store.VpnInstanceStore;
import org.onosproject.routeserver.store.VpnInstanceStoreDelegate;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WallClockTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
@Component(immediate = true)
@Service
public class VpnInstanceManager implements VpnInstanceService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VpnInstanceStore store;

    protected Set<VpnInstanceListener> listeners = new HashSet<>();

    private final VpnInstanceStoreDelegate vpnInstanceStoreDelegate = new
            InternalVpnInstanceStoreDelegate();

    @Activate
    public void activate() {
        store.setDelegate(vpnInstanceStoreDelegate);
        logger.info("Started.");
    }

    @Deactivate
    public void deactivate() {
        store.unsetDelegate(vpnInstanceStoreDelegate);
        logger.info("Stopped");
    }


    @Override
    public boolean exists(VpnInstanceId vpnInstanceId) {
        checkNotNull(vpnInstanceId);
        return store.getVpnInstance(vpnInstanceId)!=null;
    }

    @Override
    public VpnInstance getInstance(VpnInstanceId vpnInstanceId) {
        checkNotNull(vpnInstanceId);
        return store.getVpnInstance(vpnInstanceId);
    }

    @Override
    public VpnInstance getInstanceByLabel(Label label) {
        checkNotNull(label);
        VpnInstance instanceWithLabel = null;
        for (VpnInstance vpnInstance : store.getVpnInstances()) {
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
        return store.getVpnInstances();
    }

    @Override
    public boolean createInstance(VpnInstance vpnInstance) {
        checkNotNull(vpnInstance);
        store.createVpnInstance(vpnInstance.id(), vpnInstance);
        return true;
    }

    @Override
    public boolean createInstances(Iterable<VpnInstance> vpnInstances) {
        checkNotNull(vpnInstances);
        for (VpnInstance vpnInstance : vpnInstances) {
            logger.info("EVPN instance ID is  {} ", vpnInstance.id().toString());
            store.createVpnInstance(vpnInstance.id(), vpnInstance);
        }
        return true;
    }

    @Override
    public boolean updateInstances(Iterable<VpnInstance> vpnInstances) {
        checkNotNull(vpnInstances);
        createInstances(vpnInstances);
        return true;
    }

    @Override
    public boolean removeInstances(Iterable<VpnInstanceId> vpnInstanceIds) {
        checkNotNull(vpnInstanceIds);
        // TODO: not implemented
        return true;
    }

    @Override
    public void updateImpExpRouteTargets(RouteTargetType routeTargetType, VpnRouteTarget vpnRouteTarget, VpnInstanceId vpnInstanceId) {
        VpnInstance vpnInstance = store.getVpnInstance(vpnInstanceId);
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
        VpnInstance vpnInstance = store.getVpnInstance(vpnInstanceId);
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

    @Override
    public void attachDevice(VpnInstanceId vpnInstanceId, Device device) {
        store.attachDeviceToVpn(vpnInstanceId, device);
    }

    @Override
    public void detachDevice(VpnInstanceId vpnInstanceId, Device device) {
        store.detachDeviceFromVpn(vpnInstanceId, device);
    }

    @Override
    public void addListener(VpnInstanceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(VpnInstanceListener listener) {
        listeners.remove(listener);
    }

    private class InternalVpnInstanceStoreDelegate implements VpnInstanceStoreDelegate {
        @Override
        public void notify(VpnInstanceEvent event) {
            post(event);
        }
    }

    protected void post(VpnInstanceEvent event) {
        for(VpnInstanceListener l : listeners) {
            l.event(event);
        }
    }
}
