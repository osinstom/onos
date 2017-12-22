package org.onosproject.provider.xmpp.bgpvpn.device;

/**
 *
 */

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.ChassisId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.incubator.net.virtual.VirtualNetworkAdminService;
import org.onosproject.incubator.net.virtual.provider.VirtualDeviceProvider;
import org.onosproject.incubator.net.virtual.provider.VirtualNetworkProviderRegistry;
import org.onosproject.net.*;
import org.onosproject.net.device.*;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.xmpp.core.XmppController;
import org.onosproject.xmpp.core.XmppDeviceId;
import org.onosproject.xmpp.pubsub.XmppPubSubController;
import org.onosproject.xmpp.pubsub.XmppPubSubEvent;
import org.onosproject.xmpp.pubsub.XmppPubSubEventListener;
import org.onosproject.xmpp.pubsub.model.Subscribe;
import org.onosproject.xmpp.pubsub.model.Unsubscribe;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * XMPP Device provider.
 */
@Component(immediate = true)
public class XmppBgpVpnDeviceProvider extends AbstractProvider implements DeviceProvider {

    private final Logger logger = getLogger(getClass());

    private static final String PROVIDER = "org.onosproject.provider.xmpp.bgpvpn.device";
    private static final String APP_NAME = "org.onosproject.xmpp.bgpvpn";
    private static final String XMPP = "XMPP (XEP0060)";

    private static final String HARDWARE_VERSION = "XMPP Device";
    private static final String SOFTWARE_VERSION = "2.0";
    private static final String SERIAL_NUMBER = "unknown";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppPubSubController xmppPubSubController;

    protected DeviceProviderService providerService;

    protected ApplicationId appId;

    private InternalXmppPubSubEventListener xmppSubscriptionListener = new InternalXmppPubSubEventListener();

    public XmppBgpVpnDeviceProvider() {
        super(new ProviderId(XMPP, PROVIDER));
    }

    @Activate
    public void activate(ComponentContext context) {
        providerService = providerRegistry.register(this);
        appId = coreService.registerApplication(APP_NAME);
        xmppPubSubController.addXmppPubSubEventListener(xmppSubscriptionListener);
        logger.info("Started");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        xmppPubSubController.removeXmppPubSubEventListener(xmppSubscriptionListener);
        logger.info("Stopped");
    }

    @Override
    public void triggerProbe(DeviceId deviceId) {

    }

    @Override
    public void roleChanged(DeviceId deviceId, MastershipRole newRole) {

    }

    @Override
    public boolean isReachable(DeviceId deviceId) {
        return false;
    }

    @Override
    public void changePortState(DeviceId deviceId, PortNumber portNumber, boolean enable) {

    }

    private class InternalXmppPubSubEventListener implements XmppPubSubEventListener {

        @Override
        public void handle(XmppPubSubEvent event) {
            switch (event.type()) {
                case SUBSCRIBE:
                    Subscribe subscribe = (Subscribe) event.subject();
                    handleSubscribe(subscribe);
                    break;
                case UNSUBSCRIBE:
                    Unsubscribe unsubscribe = (Unsubscribe) event.subject();
                    handleUnsubscribe(unsubscribe);
                    break;
            }
        }
    }

    private void handleSubscribe(Subscribe subscribe) {
        DeviceId deviceId = DeviceId.deviceId(XmppDeviceId.uri(subscribe.getJIDAddress() + ":" + subscribe.getNodeID()));

        // Assumption: manufacturer is uniquely identified by domain part of JID
        String manufacturer = subscribe.getFrom().getDomain();

        ChassisId cid = new ChassisId();

        SparseAnnotations annotations = DefaultAnnotations.builder()
                .set(AnnotationKeys.PROTOCOL, XMPP)
                .set("VPN", subscribe.getNodeID())
                .build();

        DeviceDescription deviceDescription = new DefaultDeviceDescription(
                deviceId.uri(),
                Device.Type.VIRTUAL,
                manufacturer, HARDWARE_VERSION,
                SOFTWARE_VERSION, SERIAL_NUMBER,
                cid, true,
                annotations);

        if (deviceService.getDevice(deviceId) == null) {
            providerService.deviceConnected(deviceId, deviceDescription);
        } else if(deviceService.getDevice(deviceId) != null && !deviceService.getDevice(deviceId).annotations().value("VPN").equals(subscribe.getNodeID())) {
            providerService.deviceConnected(deviceId, deviceDescription);
        }

    }

    private void handleUnsubscribe(Unsubscribe unsubscribe) {
        DeviceId deviceId = DeviceId.deviceId(XmppDeviceId.uri(unsubscribe.getJIDAddress() + ":" + unsubscribe.getNodeID()));

        if (deviceService.getDevice(deviceId) != null) {
            providerService.deviceDisconnected(deviceId);
            logger.info("XMPP device {} removed from XMPP controller", deviceId);
        } else {
            logger.warn("XMPP device {} does not exist in the store, " +
                    "or it may already have been removed", deviceId);
        }
    }


}
