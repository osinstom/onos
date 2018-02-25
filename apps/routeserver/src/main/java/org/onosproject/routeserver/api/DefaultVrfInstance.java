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

package org.onosproject.routeserver.api;

import org.onosproject.evpnrouteservice.EvpnRouteTableId;
import org.onosproject.evpnrouteservice.RouteDistinguisher;
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
    public RouteDistinguisher routeDistinguisher() {
        return this.vpnInstance.routeDistinguisher();
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
                .add("routeDistinguisher", routeDistinguisher())
                .add("tableId", routeTableId)
                .toString();
    }


}
