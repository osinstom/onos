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

package org.onosproject.routeserver.store;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onosproject.cluster.ClusterService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.routeserver.api.VpnInstance;
import org.onosproject.routeserver.api.VpnInstanceId;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.cluster.messaging.ClusterCommunicationService;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.EventuallyConsistentMapEvent;
import org.onosproject.store.service.EventuallyConsistentMapListener;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WallClockTimestamp;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashSet;

import static org.onosproject.store.service.EventuallyConsistentMapEvent.Type.PUT;
import static org.onosproject.store.service.EventuallyConsistentMapEvent.Type.REMOVE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
@Component(immediate = true)
@Service
public class DistributedVpnInstanceStore extends AbstractStore<VpnInstanceEvent,
        VpnInstanceStoreDelegate> implements VpnInstanceStore{


    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterCommunicationService clusterCommunicator;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;

    protected EventuallyConsistentMap<VpnInstanceId, VpnInstance> vpnInstanceStore;
    protected EventuallyConsistentMap<Device, VpnInstanceId> vpnDevicesStore;

    private EventuallyConsistentMapListener<VpnInstanceId, VpnInstance> vpnInstanceUpdateListener =
            new InternalVpnInstanceUpdateListener();
    private EventuallyConsistentMapListener<Device, VpnInstanceId> vpnDevicesUpdateListener =
            new InternalVpnDevicesUpdateListener();

    @Activate
    public void activate() {
        KryoNamespace.Builder vpnInstanceStoreSerializer = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API).register(VpnInstance.class)
                .register(VpnInstanceId.class);
        vpnInstanceStore = storageService
                .<VpnInstanceId, VpnInstance>eventuallyConsistentMapBuilder()
                .withName("vpn-instance-store").withSerializer(vpnInstanceStoreSerializer)
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();
        KryoNamespace.Builder vpnDevicesStoreSerializer = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API).register(Device.class)
                .register(VpnInstanceId.class);
        vpnDevicesStore = storageService
                .<Device, VpnInstanceId>eventuallyConsistentMapBuilder()
                .withName("vpn-devices-store").withSerializer(vpnDevicesStoreSerializer)
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();

        vpnInstanceStore.addListener(vpnInstanceUpdateListener);
        vpnDevicesStore.addListener(vpnDevicesUpdateListener);

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        vpnInstanceStore.removeListener(vpnInstanceUpdateListener);
        vpnDevicesStore.removeListener(vpnDevicesUpdateListener);
        vpnInstanceStore.destroy();
        vpnDevicesStore.destroy();
    }

    @Override
    public Collection<VpnInstance> getVpnInstances() {
        return vpnInstanceStore.values();
    }

    @Override
    public VpnInstance getVpnInstance(VpnInstanceId vpnInstanceId) {
        return vpnInstanceStore.get(vpnInstanceId);
    }

    @Override
    public VpnInstanceEvent createVpnInstance(VpnInstanceId instanceId, VpnInstance vpnInstance) {
        vpnInstanceStore.put(instanceId, vpnInstance);
        return null;
    }

    @Override
    public Collection<Device> getDevicesForVpn(VpnInstanceId vpnInstanceId) {
        Collection<Device> devices = new HashSet<>();
        for (Device device : vpnDevicesStore.keySet()) {
            if (vpnDevicesStore.get(device).equals(vpnInstanceId)) {
                devices.add(device);
            }
        }
        return devices;
    }

    @Override
    public VpnInstanceEvent attachDeviceToVpn(VpnInstanceId vpnInstanceId, Device device) {
        vpnDevicesStore.put(device, vpnInstanceId);
        VpnInstance vpnInstance = vpnInstanceStore.get(vpnInstanceId);
        return new VpnInstanceEvent(VpnInstanceEvent.Type.VPN_DEVICE_ATTACHED, vpnInstance, device);
    }

    @Override
    public VpnInstanceEvent detachDeviceFromVpn(VpnInstanceId vpnInstanceId, Device device) {
        vpnDevicesStore.remove(device);
        VpnInstance vpnInstance = vpnInstanceStore.get(vpnInstanceId);
        return new VpnInstanceEvent(VpnInstanceEvent.Type.VPN_DEVICE_DETACHED, vpnInstance, device);
    }

    private class InternalVpnInstanceUpdateListener implements EventuallyConsistentMapListener<VpnInstanceId, VpnInstance> {
        @Override
        public void event(EventuallyConsistentMapEvent<VpnInstanceId, VpnInstance> event) {

        }
    }

    private class InternalVpnDevicesUpdateListener implements EventuallyConsistentMapListener<Device, VpnInstanceId> {
        @Override
        public void event(EventuallyConsistentMapEvent<Device, VpnInstanceId> event) {
            Device device = event.key();
            VpnInstance vpnInstance = vpnInstanceStore.get(event.value());
            if (event.type() == PUT) {
                notifyDelegate(new VpnInstanceEvent(VpnInstanceEvent.Type.VPN_DEVICE_ATTACHED, vpnInstance, device));
            }
            else if (event.type() == REMOVE) {
                notifyDelegate(new VpnInstanceEvent(VpnInstanceEvent.Type.VPN_DEVICE_DETACHED, vpnInstance, device));
            }
        }
    }
}
