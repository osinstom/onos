package org.onosproject.provider.xmpp.bgpvpn.route;

import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;

/**
 *
 */
public class EvpnRetract {

    private MacAddress macAddress;
    private IpAddress nlriIpAddress;
    private IpAddress nextHopAddress;
    private String vpnInstanceName;

    private EvpnRetract() {}

    private EvpnRetract(String itemId) {
        this.nextHopAddress = IpAddress.valueOf(itemId.split("/")[0]);
        this.vpnInstanceName = itemId.split("/")[1];
        this.nlriIpAddress = IpAddress.valueOf(itemId.split("/")[2]);
        this.macAddress = MacAddress.valueOf(itemId.split("/")[3]);
    }

    public static EvpnRetract asEvpnInfo(String itemId) {
        return new EvpnRetract(itemId);
    }

    public MacAddress macAddress() {
        return macAddress;
    }

    public IpAddress nlriIpAddress() {
        return nlriIpAddress;
    }

    public IpAddress nextHopAddress() {
        return nextHopAddress;
    }

    public String vpnInstanceName() {
        return vpnInstanceName;
    }
}
