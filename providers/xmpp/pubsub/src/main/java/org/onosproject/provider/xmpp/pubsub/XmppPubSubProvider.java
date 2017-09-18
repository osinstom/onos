package org.onosproject.provider.xmpp.pubsub;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.core.CoreService;
import org.onosproject.net.device.DeviceProviderRegistry;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.pubsub.api.PubSubProvider;
import org.onosproject.pubsub.api.PubSubProviderRegistry;
import org.onosproject.pubsub.api.PubSubProviderService;
import org.onosproject.xmpp.XmppController;
import org.onosproject.xmpp.XmppDeviceListener;
import org.onosproject.xmpp.XmppEvent;
import org.onosproject.xmpp.XmppEventListener;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class XmppPubSubProvider extends AbstractProvider implements PubSubProvider {

    private final Logger logger = getLogger(getClass());


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
    protected XmppPubSubProvider(ProviderId id) {
        super(id);
    }

    @Activate
    public void activate(ComponentContext context) {
        providerService = providerRegistry.register(this);
        controller.addXmppEventListener(eventListener);
        logger.info("Started");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {

    }

    private class InternalXmppEventListener implements XmppEventListener {

        @Override
        public void event(XmppEvent event) {

        }
    }
}
