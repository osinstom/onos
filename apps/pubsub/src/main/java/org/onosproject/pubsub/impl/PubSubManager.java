package org.onosproject.pubsub.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.net.provider.AbstractListenerProviderRegistry;
import org.onosproject.net.provider.AbstractProviderService;
import org.onosproject.pubsub.api.*;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
@Service
public class PubSubManager extends AbstractListenerProviderRegistry<PubSubEvent, PubSubListener, PubSubProvider, PubSubProviderService>
        implements PubSubService, PubSubAdminService, PubSubProviderRegistry{

    private final Logger logger = getLogger(getClass());

    @Activate
    public void activate() {
        eventDispatcher.addSink(PubSubEvent.class, listenerRegistry);
    }

    @Deactivate
    public void deactivate() {
        eventDispatcher.removeSink(PubSubEvent.class);
    }

    @Override
    protected PubSubProviderService createProviderService(PubSubProvider provider) {
        return new InternalPubSubProviderService(provider);
    }

    private class InternalPubSubProviderService
            extends AbstractProviderService<PubSubProvider>
            implements PubSubProviderService {

        public InternalPubSubProviderService(PubSubProvider provider) {
            super(provider);
        }

        @Override
        public void subscribe(SubscriptionInfo subscriptionInfo) {
            logger.info("SUBSCRIBE received");
            PubSubEvent event = new PubSubEvent(PubSubEvent.Type.NEW_SUBSCRIPTION, subscriptionInfo);
            post(event);
        }

        @Override
        public void unsubscribe(SubscriptionInfo subscriptionInfo) {
            logger.info("UNSUBSCRIBE received");
        }

        @Override
        public void publish(PublishInfo publishInfo) {
            logger.info("PUBLISH received");
        }

        @Override
        public void retract(PublishInfo publishInfo) {
            logger.info("RETRACT received");
        }
    }

}
