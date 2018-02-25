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
import org.onosproject.routeserver.api.EvpnService;
import org.onosproject.routeserver.impl.RouteServer;
import org.onosproject.evpnrouteservice.EvpnRouteSet;

import java.util.Collection;

/**
 * Support for displaying EVPN/L3VPN routes.
 */
@Command(scope = "onos", name = "vpn-routes", description = "Lists" +
        " all EVPN/L3VPN routes")
public class RouteListCommand  extends AbstractShellCommand {

    private static final String FORMAT_HEADER =
            "   VPN name            Prefix         Next Hop";
    public static final String FORMAT_ROUTES = "   %-18s %-15s %-10s";

    @Override
    protected void execute() {
        EvpnService service = AbstractShellCommand.get(EvpnService.class);
        RouteServer manager = (RouteServer) service;
        Collection<EvpnRouteSet> routeSet = manager.getVpnRoutes("evpn_ipv4");
        if (routeSet != null) {
            print(FORMAT_HEADER);
            routeSet.forEach(evpnRouteSet -> {
                evpnRouteSet.routes().forEach(evpnRoute -> {
                    print(FORMAT_ROUTES, evpnRouteSet.tableId().name(),
                            evpnRoute.prefixIp().address().toString(), evpnRoute.ipNextHop().toString());
                });

            });
        }
    }

}
