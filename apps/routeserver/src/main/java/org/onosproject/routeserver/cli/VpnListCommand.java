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

package org.onosproject.routeserver.cli;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.routeserver.api.VpnInstance;
import org.onosproject.routeserver.api.VpnInstanceService;

import java.util.Collection;

/**
 *
 */
@Command(scope = "onos", name = "vpn-list", description = "Lists" +
        " all active VPNs in network")
public class VpnListCommand extends AbstractShellCommand {

    public static final String FORMAT_VPN_INSTANCE = "Id=%s, description=%s,"
            + " name=%s, routeDistinguisher=%s, routeTarget=%s";

    @Override
    protected void execute() {
        VpnInstanceService service = get(VpnInstanceService.class);
        Collection<VpnInstance> vpnInstances = service
                .getInstances();
        vpnInstances.forEach(vpnInstance -> {
            print(FORMAT_VPN_INSTANCE, vpnInstance.id(),
                    vpnInstance.description(),
                    vpnInstance.vpnInstanceName(),
                    vpnInstance.routeDistinguisher(),
                    vpnInstance.getExportRouteTargets(),
                    vpnInstance.getImportRouteTargets());
        });
    }

}
