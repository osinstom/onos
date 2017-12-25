package org.onosproject.evpncontrail.cli;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.evpncontrail.api.VpnInstance;
import org.onosproject.evpncontrail.api.VpnInstanceService;

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
