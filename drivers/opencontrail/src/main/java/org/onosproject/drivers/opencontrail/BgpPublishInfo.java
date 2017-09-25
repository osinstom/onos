package org.onosproject.drivers.opencontrail;

import org.onosproject.pubsub.api.PublishInfo;

import java.util.ArrayList;
import java.util.List;

public class BgpPublishInfo extends PublishInfo {

    private String vpnInstanceName;
    private List<BgpVpnPubSubEntry> entries = new ArrayList<BgpVpnPubSubEntry>();

    public BgpPublishInfo(String vpnInstanceName) {
        this.vpnInstanceName = vpnInstanceName;
    }

    public void addEntry(BgpVpnPubSubEntry entry) {
        entries.add(entry);
    }

    @Override
    public String toString() {
        return "BgpPublishInfo{" +
                "vpnInstanceName='" + vpnInstanceName + '\'' +
                ", entries=" + entries +
                '}';
    }
}
