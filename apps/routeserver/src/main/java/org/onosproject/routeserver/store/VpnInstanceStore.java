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

import org.onosproject.net.Device;
import org.onosproject.routeserver.api.VpnInstance;
import org.onosproject.routeserver.api.VpnInstanceId;
import org.onosproject.store.Store;

import java.util.Collection;

/**
 *
 */
public interface VpnInstanceStore extends Store<VpnInstanceEvent, VpnInstanceStoreDelegate> {

    Collection<VpnInstance> getVpnInstances();

    VpnInstance getVpnInstance(VpnInstanceId vpnInstanceId);

    VpnInstanceEvent createVpnInstance(VpnInstanceId instanceId, VpnInstance vpnInstance);

    Collection<Device> getDevicesForVpn(VpnInstanceId vpnInstanceId);

    VpnInstanceEvent attachDeviceToVpn(VpnInstanceId vpnInstanceId, Device device);

    VpnInstanceEvent detachDeviceFromVpn(VpnInstanceId vpnInstanceId, Device device);

}
