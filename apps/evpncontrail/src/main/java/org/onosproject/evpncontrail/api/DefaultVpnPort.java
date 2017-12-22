package org.onosproject.evpncontrail.api;

/**
 * Created by autonet on 22.12.17.
 */

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of VPN port.
 */
public class DefaultVpnPort implements VpnPort {

    private final VpnPortId id;
    private final VpnInstanceId vpnInstanceId;

    /**
     * creates vpn port object.
     *
     * @param id            vpn port id
     * @param vpnInstanceId vpn instance id
     */
    public DefaultVpnPort(VpnPortId id, VpnInstanceId vpnInstanceId) {
        this.id = checkNotNull(id, "ID cannot be null");
        this.vpnInstanceId = checkNotNull(vpnInstanceId,
                "VPN Instance ID cannot be null");
    }

    @Override
    public VpnPortId id() {
        return id;
    }

    @Override
    public VpnInstanceId vpnInstanceId() {
        return vpnInstanceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vpnInstanceId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DefaultVpnPort) {
            final DefaultVpnPort that = (DefaultVpnPort) obj;
            return Objects.equals(this.id, that.id)
                    && Objects.equals(this.vpnInstanceId, that.vpnInstanceId);
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("id", id)
                .add("vpnInstanceId", vpnInstanceId).toString();
    }
}