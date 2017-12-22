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
public class XmppBgpVpnDeviceProvider extends AbstractProvider {

    private final Logger logger = getLogger(getClass());

    private static final String PROVIDER = "org.onosproject.provider.xmpp.bgpvpn.device";
    private static final String APP_NAME = "org.onosproject.xmpp.bgpvpn";
    private static final String XMPP = "xmpp";

    private static final String HARDWARE_VERSION = "XMPP Device";
    private static final String SOFTWARE_VERSION = "2.0";
    private static final String SERIAL_NUMBER = "unknown";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VirtualNetworkAdminService virtualNetworkAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppPubSubController xmppPubSubController;

    protected ApplicationId appId;

    private InternalXmppPubSubEventListener xmppSubscriptionListener = new InternalXmppPubSubEventListener();

    public XmppBgpVpnDeviceProvider() {
        super(new ProviderId(XMPP, PROVIDER));
    }

    @Activate
    public void activate(ComponentContext context) {
        appId = coreService.registerApplication(APP_NAME);
        xmppPubSubController.addXmppPubSubEventListener(xmppSubscriptionListener);
        logger.info("Started");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        xmppPubSubController.removeXmppPubSubEventListener(xmppSubscriptionListener);
        logger.info("Stopped");
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
        DeviceId deviceId = DeviceId.deviceId(XmppDeviceId.uri(subscribe.getJIDAddress()));
    }

    private void handleUnsubscribe(Unsubscribe unsubscribe) {
        DeviceId deviceId = DeviceId.deviceId(XmppDeviceId.uri(unsubscribe.getJIDAddress()));


    }


}
