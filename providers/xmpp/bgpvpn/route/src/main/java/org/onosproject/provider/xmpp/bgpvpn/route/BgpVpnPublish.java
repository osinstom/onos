package org.onosproject.provider.xmpp.bgpvpn.route;

/**
 *
 */
public class BgpVpnPublish {

    private int label;
    private int nlriAf;
    private String nlriIpAddress;
    private int nextHopAf;
    private String nextHopAddress;
    private int version;

    public BgpVpnPublish(int label, int nlriAf, String nlriIpAddress, int nextHopAf, String nextHopAddress, int version) {
        this.label = label;
        this.nlriAf = nlriAf;
        this.nlriIpAddress = nlriIpAddress;
        this.nextHopAf = nextHopAf;
        this.nextHopAddress = nextHopAddress;
        this.version = version;
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

    public int getVersion() {
        return version;
    }

    public String getRouteDistinguisher() {
        return nlriIpAddress + ":" + label;
    }

    @Override
    public String toString() {
        return "BgpVpnPublish{" +
                "label=" + label +
                ", nlriAf=" + nlriAf +
                ", nlriIpAddress='" + nlriIpAddress + '\'' +
                ", nextHopAf=" + nextHopAf +
                ", nextHopAddress='" + nextHopAddress + '\'' +
                ", version=" + version +
                '}';
    }
}
