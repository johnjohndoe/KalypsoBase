/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package interop;

import javax.xml.rpc.ServiceException;

import com.sun.xml.rpc.security.SecurityConfigurator;
import com.sun.xml.wss.filter.*;



/*
 * Server side implementation of Pingservice interop application, 
 * as defined in the WSS interop spec Scenario # 1.
 *
 * This class extends from the base implementation of PingImpl, 
 * and handles server side processing needed for the first scenario.
 */
public class Scenario1Impl extends interop.PingImpl {

    /* (non-Javadoc)
     * @see javax.xml.rpc.server.ServiceLifecycle#init(java.lang.Object)
     */
    public void init(Object context) throws ServiceException {    
        super.init(context);
        scenario1();
    }

    /*
     *
     * Validate the username password that is in plaintext
     */
    private void scenario1() {
        secCfg.addDumpRequest().addDumpResponse();
        secCfg.addFilterForIncomingMessages(
            new ProcessSecurityHeaderFilter());
        secCfg.addFilterForIncomingMessages(
            new AuthorizationFilter());
    }

}
