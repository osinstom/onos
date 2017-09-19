package org.onosproject.contrail;

import org.apache.felix.scr.annotations.*;
import org.onosproject.net.provider.AbstractListenerProviderRegistry;
import org.onosproject.net.provider.AbstractProviderRegistry;
import org.onosproject.net.provider.AbstractProviderService;
import org.onosproject.pubsub.api.*;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of BGP-signaled End System L3VPN management for OpenContrail vRouters.
 */
@Component(immediate = true)
@Service(value = L3VpnController.class)
public class L3VpnController {

    private final Logger logger = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PubSubService pubSubService;

    

    private PubSubListener listener = new InternalPubSubListener();

    @Activate
    public void activate() {
        logger.info("Started");
        pubSubService.addListener(listener);
    }

    @Deactivate
    public void deactivate() {
        logger.info("Stopped");
        pubSubService.removeListener(listener);
    }


    private class InternalPubSubListener implements PubSubListener {

        @Override
        public void event(PubSubEvent event) {
            PubSubEvent.Type type = event.type();
            switch(type) {
                case NEW_SUBSCRIPTION:
                    logger.info("NEW_SUBS!!!");
                    break;
            }
        }
    }

}
