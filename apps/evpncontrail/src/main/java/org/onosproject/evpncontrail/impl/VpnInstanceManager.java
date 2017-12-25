package org.onosproject.evpncontrail.impl;

import org.apache.felix.scr.annotations.*;
import org.onlab.util.KryoNamespace;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.evpncontrail.api.VpnInstance;
import org.onosproject.evpncontrail.api.VpnInstanceId;
import org.onosproject.evpncontrail.api.VpnInstanceService;
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
}
