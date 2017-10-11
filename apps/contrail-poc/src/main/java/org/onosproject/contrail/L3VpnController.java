package org.onosproject.contrail;

import com.google.common.collect.Maps;
import org.apache.felix.scr.annotations.*;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.onosproject.l3vpn.netl3vpn.BgpInfo;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.DefaultDriverData;
import org.onosproject.net.driver.DefaultDriverHandler;
import org.onosproject.net.driver.Driver;
import org.onosproject.net.driver.DriverService;
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
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetL3VpnStore store;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DriverService driverService;

    private PubSubListener listener = new InternalPubSubListener();
    private DeviceListener deviceListener = new InternalDeviceListener();

    private ConcurrentMap<String, List<DeviceId>> mapStore = Maps.newConcurrentMap();

    @Activate
    public void activate() {
        logger.info("Started");
        pubSubService.addListener(listener);
        deviceService.addListener(deviceListener);
    }

    @Deactivate
    public void deactivate() {
        logger.info("Stopped");
        pubSubService.removeListener(listener);
        deviceService.removeListener(deviceListener);
    }

    private void handlePublish(PublishInfo publishInfo) {
        String vpnInstance = publishInfo.getNodeId();
        if(mapStore.containsKey(vpnInstance)) {
            DeviceId publisher = publishInfo.getFromDevice();
            List<DeviceId> devicesToNotify = getListOfDevicesToNotify(vpnInstance, publisher);
            pubSubService.notifyPublishEvent(devicesToNotify, publishInfo);
            logger.info("Status of the VPN Store after Publish: " + mapStore.toString());
        } else {
            // TODO: notify error <item-not-found>
        }
    }

    /**
     * This method should return all VPN members except publisher
     * @return
     */
    private List<DeviceId> getListOfDevicesToNotify(String vpnInstance, DeviceId publisher) {
        List<DeviceId> vpnDevices = mapStore.get(vpnInstance);
        List<DeviceId> devicesToNotify = new ArrayList<DeviceId>();

        for(DeviceId device : vpnDevices)
            if(!device.equals(publisher))
                devicesToNotify.add(device);
        return devicesToNotify;
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

//        Element config = DocumentFactory.getInstance().createElement("config");
        String config = "error";
        pubSubService.sendEventNotification(device, config);

    }

    private void handleDeleteSubscription(SubscriptionInfo info) {
        String vpnInstanceName = info.getNodeId();
        DeviceId device = info.getFromDevice();
        removeFromVpnIfExists(vpnInstanceName, device);
    }

    private void removeFromVpnIfExists(String vpnInstanceName, DeviceId deviceId) {
        List<DeviceId> vpnDevices = mapStore.get(vpnInstanceName);
        if(vpnDevices.contains(deviceId)) {
            vpnDevices.remove(deviceId);
            logger.info("Device '{}' has been removed from VPN '{}'", deviceId, vpnInstanceName);
        }
    }

    private void removeFromStoreIfExists(DeviceId deviceId) {
        for(String vpn : mapStore.keySet()) {
            List<DeviceId> vpnDevices = mapStore.get(vpn);
            if(vpnDevices.contains(deviceId)) {
                vpnDevices.remove(deviceId);
                logger.info("Device '{}' has been removed from VPN '{}'", deviceId, vpn);
            }
        }
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
                    handleDeleteSubscription(info1);
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


    private class InternalDeviceListener implements DeviceListener {

        @Override
        public void event(DeviceEvent event) {
            switch (event.type()) {
                case DEVICE_UPDATED:
                case DEVICE_SUSPENDED:
                case DEVICE_AVAILABILITY_CHANGED:
                case DEVICE_REMOVED:
                    DeviceId device = event.subject().id();
                    if(!deviceService.isAvailable(device)) {
                        removeFromStoreIfExists(device);
                    }
                    break;
            }
        }
    }
}
