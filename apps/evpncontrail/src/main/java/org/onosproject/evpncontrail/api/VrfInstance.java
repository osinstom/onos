package org.onosproject.evpncontrail.api;

import org.onosproject.evpnrouteservice.EvpnRouteTableId;
import org.onosproject.net.DeviceId;

/**
 *
 */
public interface VrfInstance {

    String id();

    VpnInstance vpnInstance();

    DeviceId device();

    EvpnRouteTableId routingInstanceId();

}
