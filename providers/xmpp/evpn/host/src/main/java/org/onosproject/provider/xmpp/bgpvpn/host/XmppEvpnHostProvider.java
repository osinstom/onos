package org.onosproject.provider.xmpp.bgpvpn.host;

import org.apache.felix.scr.annotations.*;
import org.dom4j.Element;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.net.*;
import org.onosproject.net.host.*;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.provider.xmpp.bgpvpn.route.EvpnPublish;
import org.onosproject.provider.xmpp.bgpvpn.route.EvpnRetract;
import org.onosproject.xmpp.core.XmppDeviceId;
import org.onosproject.xmpp.pubsub.XmppPubSubController;
import org.onosproject.xmpp.pubsub.XmppPubSubEvent;
import org.onosproject.xmpp.pubsub.XmppPubSubEventListener;
import org.onosproject.xmpp.pubsub.model.Publish;
import org.onosproject.xmpp.pubsub.model.Retract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;

/**
 *
 */
@Component(immediate = true)
public class XmppEvpnHostProvider extends AbstractProvider implements HostProvider {

    private final Logger logger = LoggerFactory
            .getLogger(getClass());

    private static final String BGPVPN_NAMESPACE = "http://ietf.org/protocol/bgpvpn";
    private static final String PROVIDER = "org.onosproject.provider.xmpp.bgpvpn";
    private static final String APP_NAME = "org.onosproject.xmpp.bgpvpn";
    private static final String XMPP = "xmpp";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppPubSubController xmppPubSubController;

    private HostProviderService providerService;

    private InternalXmppPubSubEventListener xmppPubSubEventListener = new InternalXmppPubSubEventListener();

    public XmppEvpnHostProvider() {
        super(new ProviderId(XMPP, PROVIDER));
    }

    @Activate
    public void activate() {
        providerService = providerRegistry.register(this);
        xmppPubSubController.addXmppPubSubEventListener(xmppPubSubEventListener);
        logger.info("Started.");
    }

    @Deactivate
    public void deactivate() {
        xmppPubSubController.removeXmppPubSubEventListener(xmppPubSubEventListener);
        providerRegistry.unregister(this);
        providerService = null;
        logger.info("Stopped.");
    }

    @Override
    public void triggerProbe(Host host) {
        logger.info("Triggering probe on host {}", host);
    }

    private class InternalXmppPubSubEventListener implements XmppPubSubEventListener {

        @Override
        public void handle(XmppPubSubEvent event) {
            switch (event.type()) {
                case PUBLISH:
                    Publish publish = (Publish) event.subject();
                    if(publish.getItemEntryNamespace().equals(BGPVPN_NAMESPACE))
                        handlePublish(publish);
                    break;
                case RETRACT:
                    Retract retract = (Retract) event.subject();
                    handleRetract(retract);
                    break;
            }
        }

    }

    private void handleRetract(Retract retract) {
        MacAddress macAddress = EvpnRetract.asEvpnInfo(retract.getItemID()).macAddress();
        providerService.hostVanished(HostId.hostId(macAddress, VlanId.vlanId()));
    }

    private void handlePublish(Publish publish) {
        EvpnPublish info = EvpnPublish.asBgpInfo(publish);
        HostId hostId = HostId.hostId(MacAddress.valueOf(info.getMacAddress()), VlanId.vlanId());
        DeviceId deviceId = XmppDeviceId.asDeviceId(publish.getFrom());
        PortNumber portNumber = PortNumber.ANY;
        HostLocation location = new HostLocation(deviceId, portNumber,
                0L);
        SparseAnnotations annotations = DefaultAnnotations.builder().build();
        HostDescription hostDescription = new DefaultHostDescription(
                MacAddress.valueOf(info.getMacAddress()),
                VlanId.vlanId(),
                location,
                IpAddress.valueOf(info.getNlriIpAddress()),
                annotations);
        providerService.hostDetected(hostId, hostDescription, false);
    }

    
}
