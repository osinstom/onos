package org.onosproject.drivers.opencontrail;


import org.apache.felix.scr.annotations.Component;
import org.onosproject.net.driver.AbstractDriverLoader;

/**
 * Loader for OpenContrail XMPP device drivers.
 */
@Component(immediate = true)
public class OpenContrailDriversLoader extends AbstractDriverLoader {

    /**
     * Creates a new loader for resource with the specified path.
     */
    public OpenContrailDriversLoader() {
        super("/opencontrail-drivers.xml");
    }

}
