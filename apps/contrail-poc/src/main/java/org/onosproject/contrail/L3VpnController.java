package org.onosproject.contrail;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.net.provider.AbstractProviderRegistry;
import org.onosproject.net.provider.AbstractProviderService;
import org.onosproject.pubsub.api.*;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of BGP-signaled End System L3VPN management for OpenContrail vRouters.
 */
@Component(immediate = true)
@Service
public class L3VpnController
        extends AbstractProviderRegistry<PubSubProvider, PubSubProviderService>
        implements PubSubAdminService, PubSubProviderRegistry {

    private final Logger logger = getLogger(getClass());

    @Activate
    public void activate() {
        logger.info("Started");
    }

    @Deactivate
    public void deactivate() {
        logger.info("Stopped");
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
