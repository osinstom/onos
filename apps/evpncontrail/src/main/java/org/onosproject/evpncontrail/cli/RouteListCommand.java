package org.onosproject.evpncontrail.cli;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.evpncontrail.api.EvpnService;
import org.onosproject.evpncontrail.impl.EvpnContrailManager;
import org.onosproject.evpnrouteservice.EvpnRouteSet;

import java.util.Collection;
import java.util.Set;

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
        EvpnContrailManager manager = (EvpnContrailManager) service;
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
