package org.onosproject.xmpp.pubsub.ctl;

import org.apache.felix.scr.annotations.*;
import org.onosproject.net.DeviceId;
import org.onosproject.xmpp.core.XmppController;
import org.onosproject.xmpp.core.XmppIqListener;
import org.onosproject.xmpp.pubsub.XmppPubSubController;
import org.onosproject.xmpp.pubsub.model.EventNotification;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;

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


    private class InternalXmppIqListener implements XmppIqListener {
        @Override
        public void handleIqStanza(IQ iq) {
            logger.info("IQ");
        }
    }
}
