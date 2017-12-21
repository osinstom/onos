package org.onosproject.provider.xmpp.bgpvpn.device;

/**
 *
 */

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceProvider;
import org.onosproject.net.device.DeviceProviderRegistry;
import org.onosproject.net.device.DeviceProviderService;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.xmpp.core.XmppController;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * XMPP Device provider.
 */
@Component(immediate = true)
public class XmppBgpVpnDeviceProvider extends AbstractProvider implements DeviceProvider {

    private final Logger logger = getLogger(getClass());

    private static final String PROVIDER = "org.onosproject.provider.xmpp.bgpvpn.device";
    private static final String APP_NAME = "org.onosproject.xmpp.bgpvpn";
    private static final String XMPP = "xmpp";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceProviderService providerService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppController xmppController;

    protected ApplicationId appId;

    public XmppBgpVpnDeviceProvider() {
        super(new ProviderId(XMPP, PROVIDER));
    }

    @Activate
    public void activate(ComponentContext context) {
        providerService = providerRegistry.register(this);
        appId = coreService.registerApplication(APP_NAME);
        logger.info("Started");
    }

    @Override
    public void triggerProbe(DeviceId deviceId) {

    }

    @Override
    public void roleChanged(DeviceId deviceId, MastershipRole newRole) {

    }

    @Override
    public boolean isReachable(DeviceId deviceId) {
        return false;
    }

    @Override
    public void changePortState(DeviceId deviceId, PortNumber portNumber, boolean enable) {

    }
}
