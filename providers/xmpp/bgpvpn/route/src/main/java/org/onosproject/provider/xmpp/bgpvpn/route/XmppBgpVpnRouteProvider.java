package org.onosproject.provider.xmpp.bgpvpn.route;

import org.apache.felix.scr.annotations.*;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
//import org.onosproject.xmpp.pubsub.XmppPubSubController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Component(immediate = true)
public class XmppBgpVpnRouteProvider extends AbstractProvider  {

    private static final Logger logger = LoggerFactory
            .getLogger(XmppBgpVpnRouteProvider.class);

//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    protected XmppPubSubController xmppPubSubController;

    protected XmppBgpVpnRouteProvider() {
        super(new ProviderId("route",
                "org.onosproject.provider.xmpp.bgpvpn.route"));
    }

    @Activate
    public void activate() {
        logger.info("Started.");
    }

    @Deactivate
    public void deactivate() {
        logger.info("Stopped.");
    }


}
