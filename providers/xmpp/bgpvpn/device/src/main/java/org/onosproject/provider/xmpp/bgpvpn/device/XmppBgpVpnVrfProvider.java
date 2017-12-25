package org.onosproject.provider.xmpp.bgpvpn.device;

/**
 *
 */

import org.apache.felix.scr.annotations.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.evpncontrail.api.VpnInstanceId;
import org.onosproject.evpncontrail.api.VpnInstanceService;
import org.onosproject.evpncontrail.api.VrfInstance;
import org.onosproject.evpncontrail.api.VrfInstanceService;
import org.onosproject.net.*;
import org.onosproject.net.device.*;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.xmpp.core.XmppDeviceId;
import org.onosproject.xmpp.pubsub.XmppPubSubController;
import org.onosproject.xmpp.pubsub.XmppPubSubEvent;
import org.onosproject.xmpp.pubsub.XmppPubSubEventListener;
import org.onosproject.xmpp.pubsub.model.Subscribe;
import org.onosproject.xmpp.pubsub.model.Unsubscribe;
import org.onosproject.xmpp.pubsub.model.XmppPubSubError;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * XMPP Device provider.
 */
@Component(immediate = true)
public class XmppBgpVpnVrfProvider extends AbstractProvider {

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
    protected VrfInstanceService vrfInstanceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VpnInstanceService vpnInstanceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppPubSubController xmppPubSubController;


    protected DeviceProviderService providerService;

    protected ApplicationId appId;

    private InternalXmppPubSubEventListener xmppSubscriptionListener = new InternalXmppPubSubEventListener();

    public XmppBgpVpnVrfProvider() {
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
        DeviceId deviceId = XmppDeviceId.asDeviceId(subscribe.getFrom());
        String vpnName = subscribe.getNodeID();
        if(vpnInstanceService.exists(VpnInstanceId.vpnInstanceId(vpnName)))
            vrfInstanceService.addVrfInstance(vpnName, deviceId);
        else {
            logger.error("VPN with specified name does not exist.");
            xmppPubSubController.notify(deviceId, new XmppPubSubError(XmppPubSubError.PubSubApplicationCondition.ITEM_NOT_FOUND));
        }
    }

    private void handleUnsubscribe(Unsubscribe unsubscribe) {
        DeviceId deviceId = XmppDeviceId.asDeviceId(unsubscribe.getFrom());
        String vpnName = unsubscribe.getNodeID();
        if(vpnInstanceService.exists(VpnInstanceId.vpnInstanceId(vpnName)))
            if(vrfInstanceService.vrfExists(unsubscribe.getNodeID(), deviceId))
                vrfInstanceService.removeVrfInstance(unsubscribe.getNodeID(), deviceId);
            else {
                logger.error("Device is not subscribed to VPN.");
                xmppPubSubController.notify(deviceId, new XmppPubSubError(XmppPubSubError.PubSubApplicationCondition.NOT_SUBSCRIBED));
            }
        else {
            logger.error("VRF with specified name does not exist.");
            xmppPubSubController.notify(deviceId, new XmppPubSubError(XmppPubSubError.PubSubApplicationCondition.ITEM_NOT_FOUND));
        }
    }


}
