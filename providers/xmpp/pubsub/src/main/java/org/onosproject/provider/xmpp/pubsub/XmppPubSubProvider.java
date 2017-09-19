package org.onosproject.provider.xmpp.pubsub;

import org.apache.felix.scr.annotations.*;
import org.onosproject.core.CoreService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.pubsub.api.PubSubProvider;
import org.onosproject.pubsub.api.PubSubProviderRegistry;
import org.onosproject.pubsub.api.PubSubProviderService;
import org.onosproject.pubsub.api.SubscriptionInfo;
import org.onosproject.xmpp.XmppController;
import org.onosproject.xmpp.XmppDeviceListener;
import org.onosproject.xmpp.XmppEvent;
import org.onosproject.xmpp.XmppEventListener;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
public class XmppPubSubProvider extends AbstractProvider implements PubSubProvider {

    private final Logger logger = getLogger(getClass());

    private static final String PROVIDER = "org.onosproject.provider.xmpp.pubsub";
    private static final String APP_NAME = "org.onosproject.xmpp";
    private static final String XMPP = "xmpp";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;


    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppController controller;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PubSubProviderRegistry providerRegistry;


    protected PubSubProviderService providerService;

    private XmppEventListener eventListener = new InternalXmppEventListener();

    /**
     * Creates a provider with the supplied identifier.
     *
     * @param id provider id
     */
    public XmppPubSubProvider() {
        super(new ProviderId(XMPP, PROVIDER));
    }

    @Activate
    public void activate(ComponentContext context) {
        logger.info("Started");
        providerService = providerRegistry.register(this);
        controller.addXmppEventListener(eventListener);
        logger.info("Started");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        logger.info("Stopped");
    }

    private class InternalXmppEventListener implements XmppEventListener {

        @Override
        public void event(XmppEvent event) {
            SubscriptionInfo subscriptionInfo = new SubscriptionInfo(event.getDeviceId(), "none");
            notifyNewSubscriptionToCore(subscriptionInfo);
        }

        private void notifyNewSubscriptionToCore(SubscriptionInfo subscriptionInfo) {
            providerService.subscribe(subscriptionInfo);
        }


    }
}
