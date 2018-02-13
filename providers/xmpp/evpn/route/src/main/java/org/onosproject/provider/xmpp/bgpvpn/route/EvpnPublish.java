package org.onosproject.provider.xmpp.bgpvpn.route;

import org.dom4j.Element;
import org.onosproject.xmpp.pubsub.model.Publish;
import org.onosproject.xmpp.pubsub.model.XmppPublish;

/**
 *
 */
public class EvpnPublish {

    private int nlriSafi;
    private int label;
    private int nlriAf;
    private String macAddress;
    private String nlriIpAddress;
    private int nextHopAf;
    private String nextHopAddress;

    public EvpnPublish(int label, int nlriAf, int nlriSafi, String macAddress, String nlriIpAddress, int nextHopAf, String nextHopAddress) {
        this.label = label;
        this.macAddress = macAddress;
        this.nlriAf = nlriAf;
        this.nlriSafi = nlriSafi;
        this.nlriIpAddress = nlriIpAddress;
        this.nextHopAf = nextHopAf;
        this.nextHopAddress = nextHopAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getNlriSafi() {
        return nlriSafi;
    }

    public int getLabel() {
        return label;
    }

    public int getNlriAf() {
        return nlriAf;
    }

    public String getNlriIpAddress() {
        return nlriIpAddress;
    }

    public int getNextHopAf() {
        return nextHopAf;
    }

    public String getNextHopAddress() {
        return nextHopAddress;
    }

    public String getRouteDistinguisher(String deviceId, String vpnInstanceId) {
        return String.format("%s/%s/%s", deviceId,vpnInstanceId,label);
//        return String.format("%s/%s/%s/%s/%s", nextHopAddress, vpnInstanceId, nlriIpAddress, macAddress, label);
    }

    @Override
    public String toString() {
        return "EvpnPublish{" +
                "label=" + label +
                ", nlriAf=" + nlriAf +
                ", macAddress=" + macAddress + '\'' +
                ", nlriIpAddress='" + nlriIpAddress + '\'' +
                ", nextHopAf=" + nextHopAf +
                ", nextHopAddress='" + nextHopAddress + '\'' +
                '}';
    }

    public static EvpnPublish asBgpInfo(XmppPublish publish) {
        Element entry = publish.getItemEntry();
        Element nlri = entry.element("nlri");
        int nlriAf = Integer.parseInt(nlri.element("af").getStringValue());
        int nlriSafi = Integer.parseInt(nlri.element("safi").getStringValue());
        String macAddress = nlri.element("mac").getStringValue();
        String ipAddress = nlri.element("address").getStringValue();
        Element nextHop = (Element) entry.element("next-hops").elements().get(0);
        int nextHopAf = Integer.parseInt(nextHop.element("af").getStringValue());
        String nextHopIpAddress = nextHop.element("address").getStringValue();
        int label = Integer.parseInt(nextHop.element("label").getStringValue());
        return new EvpnPublish(label, nlriAf, nlriSafi, macAddress, ipAddress, nextHopAf, nextHopIpAddress);
    }

}
