package org.onosproject.provider.xmpp.pubsub;

import org.apache.felix.scr.annotations.*;
import org.dom4j.Element;
import org.onlab.util.ItemNotFoundException;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.driver.DefaultDriverData;
import org.onosproject.net.driver.DefaultDriverHandler;
import org.onosproject.net.driver.Driver;
import org.onosproject.net.driver.DriverService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.pubsub.api.*;
import org.onosproject.xmpp.XmppController;
import org.onosproject.xmpp.XmppDevice;
import org.onosproject.xmpp.XmppDeviceId;
import org.onosproject.xmpp.XmppIqListener;
import org.onosproject.xmpp.driver.XmppDeviceDriver;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.xmpp.XmppDeviceId.uri;
import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
public class XmppPubSubProvider extends AbstractProvider implements PubSubProvider {

    private final Logger logger = getLogger(getClass());

    private static final String PROVIDER = "org.onosproject.provider.xmpp.pubsub";
    private static final String APP_NAME = "org.onosproject.xmpp";
    private static final String XMPP = "xmpp";
    private static final String HARDWARE_VERSION = "XMPP Device";
    private static final String SOFTWARE_VERSION = "1.0";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DriverService driverService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppController controller;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PubSubProviderRegistry providerRegistry;


    protected PubSubProviderService providerService;

    private XmppIqListener eventListener = new InternalXmppIqListener();

    /**
     * Creates a XmppPubSubProvider with the supplied identifier.
     *
     */
    public XmppPubSubProvider() {
        super(new ProviderId(XMPP, PROVIDER));
    }

    @Activate
    public void activate(ComponentContext context) {
        logger.info("Started");
        providerService = providerRegistry.register(this);
        controller.addXmppIqListener(eventListener);
        logger.info("Started");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        logger.info("Stopped");
    }

    @Override
    public void sendNotifications(List<DeviceId> devices, PublishInfo publishInfo) {
        for(DeviceId deviceId : devices) {
            String strJid = deviceId.uri().getSchemeSpecificPart();
            JID jid = new JID(strJid);
            XmppDeviceId xmppDeviceId = new XmppDeviceId(jid);
            XmppDevice xmppDevice = controller.getDevice(xmppDeviceId);
            Packet packet = XmppPubSubUtils.constructXmppEventNotificationMessage(publishInfo);

            xmppDevice.sendPacket(packet);
        }
    }

    private void handlePubSubOperation(XmppPubSubUtils.Method method, IQ iq) {
        switch(method) {
            case SUBSCRIBE:
                handleSubscribe(iq);
                break;
            case UNSUBSCRIBE:
                handleUnsubscribe(iq);
                break;
            case PUBLISH:
                handlePublish(iq);
                break;
            case RETRACT:
                handleRetract(iq);
                break;
        }
    }

    private void handleSubscribe(IQ iq) {
        SubscriptionInfo subscriptionInfo = constructSubscriptionInfo(iq);
        notifyNewSubscriptionToCore(subscriptionInfo);
    }

    private void notifyNewSubscriptionToCore(SubscriptionInfo subscriptionInfo) {
        providerService.subscribe(subscriptionInfo);
    }

    private void handleUnsubscribe(IQ operationBody) {
//        providerService.unsubscribe();
    }

    private void handlePublish(IQ iq) {
        PublishInfo publishInfo = constructPublishInfo(iq);
        notifyPublishInfoToCore(publishInfo);
    }

    private void notifyPublishInfoToCore(PublishInfo publishInfo) {
        providerService.publish(publishInfo);
    }

    private void handleRetract(IQ operationBody) {

    }

    private SubscriptionInfo constructSubscriptionInfo(IQ iq) {
        String device = iq.getElement().attribute("from").getValue();
        logger.info("Device: " + device);
        String nodeId = XmppPubSubUtils.getChildElement(iq.getChildElement())
                .attribute("node").getValue();
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo(DeviceId.deviceId(device), nodeId);
        return subscriptionInfo;
    }

    private PublishInfo constructPublishInfo(IQ iq) {
        JID fromJid = iq.getFrom();
        PubSubInfoConstructor pubSubInfoConstructor = getPubSubConstructor(fromJid);


        Element publish = (Element) iq.getChildElement().elements().get(0);
        PublishInfo publishInfo = pubSubInfoConstructor.parsePublishInfo(publish);

        return publishInfo;
    }

    private PubSubInfoConstructor getPubSubConstructor(JID jid) {

        String domain = jid.getDomain();
        Driver driver = getDriverByJidDomain(domain);
        checkNotNull(driver);

        DeviceId deviceId = DeviceId.deviceId(jid.toString());
        logger.info("Driver {} assigned to device {}", driver.name(), deviceId);

        DefaultDriverHandler handler =
                new DefaultDriverHandler(new DefaultDriverData(driver, deviceId));

        PubSubInfoConstructor pubSubInfoConstructor = driver.createBehaviour(handler, PubSubInfoConstructor.class);
        return pubSubInfoConstructor;
    }

    private Driver getDriverByJidDomain(String domain) {
        Driver driver;
        try {
            driver = driverService.getDriver(domain, HARDWARE_VERSION, SOFTWARE_VERSION);
        } catch (ItemNotFoundException e) {
            throw e;
        }

        if (driver == null) {
            logger.error("No XMPP driver for domain: {}", domain);
            return null;
        }

        if (!driver.hasBehaviour(PubSubInfoConstructor.class)) {
            logger.error("Driver {} does not support PubSubInfoConstructor behaviour", driver.name());
            return null;
        }
        return driver;
    }


    private class InternalXmppIqListener implements XmppIqListener {

        @Override
        public void handleEvent(IQ iqEvent) {
            if(XmppPubSubUtils.isPubSub(iqEvent)) {
                XmppPubSubUtils.Method method = XmppPubSubUtils.getMethod(iqEvent);
                handlePubSubOperation(method, iqEvent);
            }
        }

    }





}
