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
import org.onosproject.xmpp.XmppIqListener;
import org.onosproject.xmpp.driver.XmppDeviceDriver;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.xmpp.packet.IQ;

import static org.onosproject.xmpp.XmppDeviceId.uri;
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
    protected DriverService driverService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppController controller;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PubSubProviderRegistry providerRegistry;


    protected PubSubProviderService providerService;

    private XmppIqListener eventListener = new InternalXmppIqListener();

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
        controller.addXmppIqListener(eventListener);
        logger.info("Started");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        logger.info("Stopped");
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
        String jid = iq.getElement().attribute("from").getValue();
        DeviceId xmppDeviceId = DeviceId.deviceId(jid);

        PubSubInfoConstructor pubSubInfoConstructor = getPubSubConstructor(xmppDeviceId);

        PublishInfo publishInfo = pubSubInfoConstructor.parsePublishInfo(iq);

        return publishInfo;
    }

    private PubSubInfoConstructor getPubSubConstructor(DeviceId xmppDeviceId) {
        Driver driver;
        try {
            // TODO: temp solution, need to provide universal solution
            driver = driverService.getDriver(xmppDeviceId);
        } catch (ItemNotFoundException e) {
            throw e;
        }

        if (driver == null) {
            logger.error("No XMPP driver for {} : {}", xmppDeviceId);
            return null;
        }

        logger.info("Driver {} assigned to device {}", driver.name(), xmppDeviceId);

        if (!driver.hasBehaviour(PubSubInfoConstructor.class)) {
            logger.error("Driver {} does not support PubSubInfoConstructor behaviour", driver.name());
            return null;
        }

        DefaultDriverHandler handler =
                new DefaultDriverHandler(new DefaultDriverData(driver, xmppDeviceId));

        PubSubInfoConstructor pubSubInfoConstructor = driver.createBehaviour(handler, PubSubInfoConstructor.class);
        return pubSubInfoConstructor;
    }

    private class InternalXmppIqListener implements XmppIqListener {

        @Override
        public void handleEvent(IQ iqEvent) {
            if(XmppPubSubUtils.isPubSub(iqEvent)) {
                logger.info("IS PUBSUB!!!");
                logger.info("MethodIQ: {}", iqEvent.getChildElement().asXML());
                XmppPubSubUtils.Method method = XmppPubSubUtils.getMethod(iqEvent);
                logger.info("Method: {}", method);
                handlePubSubOperation(method, iqEvent);
            }
        }

    }





}
