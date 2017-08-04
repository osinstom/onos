package org.onosproject.provider.xmpp.pubsub.impl;

import org.apache.felix.scr.annotations.*;
import org.onosproject.core.CoreService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.xmpp.XmppController;

/**
 * XMPP PubSub extension provider.
 */
@Component(immediate = true)
public class XmppPubSubProvider extends AbstractProvider {

    protected static final String XMPP = "xmpp";
    private static final String PROVIDER = "org.onosproject.provider.xmpp.pubsub";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppController controller;

    public XmppPubSubProvider() {
        super(new ProviderId(XMPP, PROVIDER));
    }

    @Activate
    public void activate() {

    }

    @Deactivate
    public void deactivate() {

    }

}
