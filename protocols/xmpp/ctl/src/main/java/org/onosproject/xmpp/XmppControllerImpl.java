package org.onosproject.xmpp;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.CoreService;

/**
 * The main class of XMPP protocol.
 */
@Component(immediate = true)
@Service
public class XmppControllerImpl implements XmppController {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;


    public Map<DeviceId, XmppDevice> getXmppDevicesMap() {
        return null;
    }
}
