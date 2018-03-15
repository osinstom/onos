/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.provider.xmpp.bgpvpn.flow;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleProvider;
import org.onosproject.net.flow.FlowRuleProviderRegistry;
import org.onosproject.net.flow.FlowRuleProviderService;
import org.onosproject.net.flow.oldbatch.FlowRuleBatchEntry;
import org.onosproject.net.flow.oldbatch.FlowRuleBatchOperation;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.routeserver.api.VpnInstanceService;
import org.onosproject.xmpp.pubsub.XmppPubSubController;
import org.onosproject.xmpp.pubsub.model.XmppEventNotification;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
@Component(immediate = true)
public class XmppEvpnFlowRuleProvider extends AbstractProvider
        implements FlowRuleProvider {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppPubSubController xmppPubSubController;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VpnInstanceService vpnInstanceService;

    private FlowRuleProviderService providerService;

    /**
     * Creates an XMPP EVPN flow provider.
     */
    public XmppEvpnFlowRuleProvider() {
        super(new ProviderId("xmpp", "org.onosproject.provider.xmpp.evpn"));
    }

    @Activate
    protected void activate(ComponentContext context) {
        providerService = providerRegistry.register(this);
        log.info(providerRegistry.getProviders().toString());
        log.info("Started.");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        providerRegistry.unregister(this);
        providerService = null;

        log.info("Stopped");
    }

    @Override
    public void applyFlowRule(FlowRule... flowRules) {
        log.info("Applying rules..");
        for (FlowRule flowRule : flowRules) {
            applyRule(flowRule);
        }
    }

    private void applyRule(FlowRule flowRule) {
        log.info("Applying rule");
    }

    @Override
    public void removeFlowRule(FlowRule... flowRules) {

    }

    @Override
    public void removeRulesById(ApplicationId id, FlowRule... flowRules) {

    }

    @Override
    public void executeBatch(FlowRuleBatchOperation batch) {
        checkNotNull(batch);

        for (FlowRuleBatchEntry fbe : batch.getOperations()) {
            XmppNotificationBuilder builder = XmppNotificationBuilder.builder(fbe.target(), vpnInstanceService);
            XmppEventNotification xmppEventNotification;
            switch (fbe.operator()) {
                case ADD:
                case MODIFY:
                    xmppEventNotification = builder.buildRouteUpdate();
                    break;
                case REMOVE:
                    xmppEventNotification = builder.buildRouteWithdraw();
                    break;
                default:
                    log.error("Unsupported batch operation {}; skipping flowmod {}",
                              fbe.operator(), fbe);
                    continue;
            }
            xmppPubSubController.notify(batch.deviceId(), xmppEventNotification);
        }

    }
}
