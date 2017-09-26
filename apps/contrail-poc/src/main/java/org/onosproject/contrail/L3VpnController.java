package org.onosproject.contrail;

import com.google.common.collect.Maps;
import org.apache.felix.scr.annotations.*;
import org.onosproject.l3vpn.netl3vpn.BgpInfo;
import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.AbstractListenerProviderRegistry;
import org.onosproject.net.provider.AbstractProviderRegistry;
import org.onosproject.net.provider.AbstractProviderService;
import org.onosproject.pubsub.api.*;
import org.onosproject.l3vpn.netl3vpn.NetL3VpnStore;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetL3VpnStore store;

    private PubSubListener listener = new InternalPubSubListener();

    private ConcurrentMap<String, List<DeviceId>> mapStore = Maps.newConcurrentMap();

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

    private void handlePublish(PublishInfo publishInfo) {
        String vpnInstance = publishInfo.getNodeId();
        if(mapStore.containsKey(vpnInstance)) {
            List<DeviceId> vpnDevices = mapStore.get(vpnInstance);
            pubSubService.notifyPublishEvent(vpnDevices, publishInfo);
        } else {
            // TODO: notify error <item-not-found>
        }
    }

    private void handleNewSubscription(SubscriptionInfo info) {
        String vpnInstanceName = info.getNodeId();
        List<DeviceId> vpnDevices = mapStore.get(vpnInstanceName);
        DeviceId device = info.getFromDevice();
        if(vpnDevices!=null) {
            if(!vpnDevices.contains(device))
                vpnDevices.add(device);
        } else {
            List<DeviceId> devices = new ArrayList<DeviceId>();
            devices.add(device);
            mapStore.put(vpnInstanceName, devices);
        }
        logger.info("NEW_SUBSCRIPTION handled. Status of subscrptions: /n {}", mapStore.toString());
    }

    private class InternalPubSubListener implements PubSubListener {

        @Override
        public void event(PubSubEvent event) {
            PubSubEvent.Type type = event.type();
            switch(type) {
                case NEW_SUBSCRIPTION:
                    SubscriptionInfo info = (SubscriptionInfo) event.subject();
                    logger.info(info.toString());
                    handleNewSubscription(info);
                    break;
                case DELETE_SUBSCRIPTION:
                    SubscriptionInfo info1 = (SubscriptionInfo) event.subject();
                    logger.info(info1.toString());
                    // TODO: Remove subscription
                    break;
                case PUBLISH:
                    PublishInfo publishInfo = (PublishInfo) event.subject();
                    logger.info(publishInfo.toString());
                    handlePublish(publishInfo);
                    // TODO: Handle publish
                    break;
                case RETRACT:
                    break;
            }
        }
    }

}
