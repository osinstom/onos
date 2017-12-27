package org.onosproject.evpncontrail.api;

import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public interface VrfInstanceService {

    void addVrfInstance(String vpnName, DeviceId device);

    void removeVrfInstance(String vpnName, DeviceId deviceId);

    VrfInstance getVrfInstance(String vpnName, DeviceId device);

    boolean vrfExists(String vpnName, DeviceId device);

    Collection<VrfInstance> getVrfInstances();

    Collection<VrfInstance> getVrfInstances(String vpnName);

}
