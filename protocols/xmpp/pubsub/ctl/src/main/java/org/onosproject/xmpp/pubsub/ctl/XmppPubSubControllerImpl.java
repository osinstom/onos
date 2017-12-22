package org.onosproject.xmpp.pubsub.ctl;

import org.apache.felix.scr.annotations.*;
import org.dom4j.Element;
import org.onosproject.net.DeviceId;
import org.onosproject.xmpp.core.XmppController;
import org.onosproject.xmpp.core.XmppIqListener;
import org.onosproject.xmpp.pubsub.XmppPubSubController;
import org.onosproject.xmpp.pubsub.XmppPubSubEvent;
import org.onosproject.xmpp.pubsub.XmppPubSubEventListener;
import org.onosproject.xmpp.pubsub.model.*;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.xmpp.pubsub.ctl.XmppPubSubConstants.PUBSUB_ELEMENT;
import static org.onosproject.xmpp.pubsub.ctl.XmppPubSubConstants.PUBSUB_NAMESPACE;

/**
 *
 */
@Component(immediate = true)
@Service
public class XmppPubSubControllerImpl implements XmppPubSubController {

    private static final Logger logger =
            LoggerFactory.getLogger(XmppPubSubControllerImpl.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppController xmppController;

    protected Set<XmppPubSubEventListener> xmppPubSubEventListeners = new CopyOnWriteArraySet<XmppPubSubEventListener>();

    private InternalXmppIqListener iqListener = new InternalXmppIqListener();

    @Activate
    public void activate(ComponentContext context) {
        xmppController.addXmppIqListener(iqListener);
        logger.info("XmppPubSubControllerImpl started.");
    }

    @Deactivate
    public void deactivate() {
        xmppController.removeXmppIqListener(iqListener);
        logger.info("Stopped");
    }

    @Override
    public void notify(DeviceId deviceId, EventNotification eventNotification) {

    }

    @Override
    public void addXmppPubSubEventListener(XmppPubSubEventListener xmppPubSubEventListener) {
        xmppPubSubEventListeners.add(xmppPubSubEventListener);
    }

    @Override
    public void removeXmppPubSubEventListener(XmppPubSubEventListener xmppPubSubEventListener) {
        xmppPubSubEventListeners.remove(xmppPubSubEventListener);
    }

    private class InternalXmppIqListener implements XmppIqListener {
        @Override
        public void handleIqStanza(IQ iq) {
            if(isPubSub(iq)) {
                logger.info("IQ");
                notifyListeners(iq);
            }
        }
    }

    private void notifyListeners(IQ iq) {
        XmppPubSubConstants.Method method = getMethod(iq);
        checkNotNull(method);
        XmppPubSubEvent event = null;
        switch(method) {
            case SUBSCRIBE:
                Subscribe subscribe = new Subscribe(iq);
                event = new XmppPubSubEvent<>(XmppPubSubEvent.Type.SUBSCRIBE, subscribe);
                break;
            case UNSUBSCRIBE:
                Unsubscribe unsubscribe = new Unsubscribe(iq);
                event = new XmppPubSubEvent<>(XmppPubSubEvent.Type.UNSUBSCRIBE, unsubscribe);
                break;
            case PUBLISH:
                Publish publish = new Publish(iq);
                event = new XmppPubSubEvent<>(XmppPubSubEvent.Type.PUBLISH, publish);
                break;
            case RETRACT:
                Retract retract = new Retract(iq);
                event = new XmppPubSubEvent<>(XmppPubSubEvent.Type.RETRACT, retract);
                break;
        }
        checkNotNull(event);
        notifyXmppPubSubEvent(event);
    }

    private void notifyXmppPubSubEvent(XmppPubSubEvent event) {
        for(XmppPubSubEventListener listener : xmppPubSubEventListeners) {
            listener.handle(event);
        }
    }

    private boolean isPubSub(IQ iq) {
        Element pubsub = iq.getElement().element(PUBSUB_ELEMENT);
        if(pubsub!=null && pubsub.getNamespaceURI().equals(PUBSUB_NAMESPACE)) {
            return true;
        }
        return false;
    }

    public static XmppPubSubConstants.Method getMethod(IQ iq) {
        Element pubsubElement = iq.getChildElement();
        Element methodElement = getChildElement(pubsubElement);
        String name = methodElement.getName();
        switch(name) {
            case "subscribe":
                return XmppPubSubConstants.Method.SUBSCRIBE;
            case "unsubscribe":
                return XmppPubSubConstants.Method.UNSUBSCRIBE;
            case "publish":
                return XmppPubSubConstants.Method.PUBLISH;
            case "retract":
                return XmppPubSubConstants.Method.RETRACT;
        }

        return null;
    }

    public static Element getChildElement(Element element) {
        Element child = (Element) element.elements().get(0); // the first element is related to pubsub operation
        return child;
    }

}
