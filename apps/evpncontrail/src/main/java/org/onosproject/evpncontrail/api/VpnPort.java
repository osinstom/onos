package org.onosproject.evpncontrail.api;

/**
 *
 */
public interface VpnPort {

    /**
     * Returns the VPN port identifier.
     *
     * @return VPN port identifier
     */
    VpnPortId id();

    /**
     * Returns the VPN instance identifier.
     *
     * @return VPN instance identifier
     */
    VpnInstanceId vpnInstanceId();

}
