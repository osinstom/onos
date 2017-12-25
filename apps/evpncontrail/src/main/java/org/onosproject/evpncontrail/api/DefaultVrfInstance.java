package org.onosproject.evpncontrail.api;

import org.onosproject.evpnrouteservice.EvpnRouteTableId;
import org.onosproject.net.DeviceId;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 *
 */
public class DefaultVrfInstance implements VrfInstance {

    private String id;
    private VpnInstance vpnInstance;
    private DeviceId deviceId;
    private EvpnRouteTableId routeTableId;

    public DefaultVrfInstance(String id, VpnInstance vpnInstance, DeviceId deviceId, EvpnRouteTableId routeTableId) {
        this.id = id;
        this.vpnInstance = vpnInstance;
        this.deviceId = deviceId;
        this.routeTableId = routeTableId;
    }


    @Override
    public String id() {
        return id;
    }

    @Override
    public VpnInstance vpnInstance() {
        return vpnInstance;
    }

    @Override
    public DeviceId device() {
        return deviceId;
    }

    @Override
    public EvpnRouteTableId routingInstanceId() {
        return routeTableId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultVrfInstance that = (DefaultVrfInstance) o;

        if (!id.equals(that.id)) return false;
        if (!vpnInstance.equals(that.vpnInstance)) return false;
        if (!deviceId.equals(that.deviceId)) return false;
        return routeTableId.equals(that.routeTableId);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + vpnInstance.hashCode();
        result = 31 * result + deviceId.hashCode();
        result = 31 * result + routeTableId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("vpnInstance", vpnInstance)
                .add("device", deviceId)
                .add("tableId", routeTableId)
                .toString();
    }


}
