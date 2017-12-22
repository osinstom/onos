package org.onosproject.evpncontrail.api;

import org.onosproject.evpnrouteservice.EvpnRoute;
import org.onosproject.net.Host;

/**
 *
 */
public interface EvpnService {

    /**
     * Transfer remote route to private route and set mpls flows out when
     * BgpRoute update.
     *
     * @param route evpn route
     */
    void onBgpEvpnRouteUpdate(EvpnRoute route);

    /**
     * Transfer remote route to private route and delete mpls flows out when
     * BgpRoute delete.
     *
     * @param route evpn route
     */
    void onBgpEvpnRouteDelete(EvpnRoute route);

    /**
     * Get VPN info from EVPN app store and create route, set flows when host
     * detected.
     *
     * @param host host information
     */
    void onHostDetected(Host host);

    /**
     * Get VPN info from EVPN app store and delete route, set flows when
     * host
     * vanished.
     *
     * @param host host information
     */
    void onHostVanished(Host host);

    /**
     * Get VPN info from EVPN app store and create route, set flows when
     * host
     * detected.
     *
     * @param vpnPort vpnPort information
     */
    void onVpnPortSet(VpnPort vpnPort);

    /**
     * Get VPN info from EVPN app store and delete route, set flows when host
     * vanished.
     *
     * @param vpnPort vpnPort information
     */
    void onVpnPortDelete(VpnPort vpnPort);

}
