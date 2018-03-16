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

import org.onlab.util.Tools;
import org.onosproject.event.AbstractEvent;
import org.onosproject.net.Device;
import org.onosproject.routeserver.api.VpnInstance;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 *
 */
public class VpnInstanceEvent extends AbstractEvent<VpnInstanceEvent.Type, VpnInstance> {

    private final Device device;

    public enum Type {
        VPN_DEVICE_ATTACHED,
        VPN_DEVICE_DETACHED
    }

    protected VpnInstanceEvent(Type type, VpnInstance subject) {
        this(type, subject, null);
    }

    protected VpnInstanceEvent(Type type, VpnInstance subject, Device device) {
        super(type, subject);
        this.device = device;
    }

    protected VpnInstanceEvent(Type type, VpnInstance subject, Device device, long time) {
        super(type, subject, time);
        this.device = device;
    }

    public Device device() {
        return this.device;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("time", Tools.defaultOffsetDataTime(time()))
                .add("type", type())
                .add("subject", subject())
                .add("device", device)
                .toString();
    }
}
