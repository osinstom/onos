package org.onosproject.evpncontrail.cli;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.evpncontrail.api.VrfInstanceService;

/**
 *
 */
@Command(scope = "onos", name = "vrfs", description = "Lists" +
        " all VRFs in network")
public class VrfListCommand  extends AbstractShellCommand {

    private static final String FORMAT_HEADER =
            "   VPN name            VRF-ID         RoutingTableID";
    public static final String FORMAT_VRF = "   %-18s %-15s %-10s";

    @Override
    protected void execute() {
        VrfInstanceService vrfInstanceService = AbstractShellCommand.get(VrfInstanceService.class);
        print(FORMAT_HEADER);
        vrfInstanceService.getVrfInstances().forEach(vrfInstance -> {
            print(FORMAT_VRF, vrfInstance.vpnInstance().vpnInstanceName().getEvpnName(),
                    vrfInstance.id(), vrfInstance.routingInstanceId().name());
        });
    }

}
