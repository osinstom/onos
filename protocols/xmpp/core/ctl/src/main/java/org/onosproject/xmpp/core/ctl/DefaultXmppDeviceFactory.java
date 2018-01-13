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

package org.onosproject.xmpp.core.ctl;

import org.onosproject.xmpp.core.XmppDevice;
import org.onosproject.xmpp.core.XmppDeviceFactory;
import org.onosproject.xmpp.core.XmppDeviceId;
import org.onosproject.xmpp.core.XmppDeviceManager;
import org.slf4j.Logger;
import org.xmpp.packet.JID;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Generates XMPP device objects.
 */
public final class DefaultXmppDeviceFactory implements XmppDeviceFactory {

    private final Logger logger = getLogger(getClass());

    protected XmppDeviceManager manager;

    public void init(XmppDeviceManager manager) {
        setManager(manager);
    }

    /**
     * Configures XMPP device manager only if it is not initialized.
     *
     * @param manager reference object of XMPP device manager
     */
    private void setManager(XmppDeviceManager manager) {
        synchronized (manager) {
            if (this.manager == null) {
                this.manager = manager;
            } else {
                logger.warn("XMPP device manager has already been set.");
            }
        }
    }

    public void cleanManager() {
        synchronized (manager) {
            if (this.manager != null) {
                this.manager = null;
            } else {
                logger.warn("Manager for XMPP device is not configured");
            }
        }
    }

    public XmppDevice getXmppDevice(JID jid) {
        XmppDeviceId xmppDeviceId = new XmppDeviceId(jid);

        return getXmppDeviceInstance(xmppDeviceId);
    }

    private  XmppDevice getXmppDeviceInstance(XmppDeviceId xmppDeviceId) {
        XmppDevice device = manager.getDevice(xmppDeviceId);
        if (device != null) {
            return device;
        } else {
            XmppDevice newDevice = createXmppDeviceInstance(xmppDeviceId);
            return newDevice;
        }
    }

    private XmppDevice createXmppDeviceInstance(XmppDeviceId xmppDeviceId) {
        XmppDevice xmppDevice = new DefaultXmppDevice(xmppDeviceId, this.manager);
        return xmppDevice;
    }



}
