/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package interop;

import java.io.File;
import java.util.Date;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.xml.rpc.ServiceException;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.reference.KeyIdentifier;
import com.sun.xml.wss.filter.*;
import com.sun.xml.wss.keyinfo.*;
import com.sun.xml.wss.impl.DefaultSecurityEnvironmentImpl;

import com.sun.xml.rpc.security.SecurityConfigurator;



/*
 * Server side implementation of Pingservice interop application,
 * as defined in the WSS interop spec Scenario # 5.
 *
 * This class extends from the base implementation of PingImpl,
 * and handles server side processing needed for the scenario.
 *
 * @author Kumar Jayanti
 *
 */
public class Scenario5Impl extends interop.PingImpl {


    // -------------- methods --------------

    /* (non-Javadoc)
     * @see javax.xml.rpc.server.ServiceLifecycle#init(java.lang.Object)
     */
    public void init(Object context) throws ServiceException {
        super.init(context);
        scenario5();
    }

    /*
     * Sign and Encrypt the message, WSS Scenario 5
     */
    private void scenario5() throws ServiceException {
        try {
            secCfg.addDumpRequest();

            secCfg.addFilterForIncomingMessages(
                new ProcessSecurityHeaderFilter());
            secCfg.addFilterForIncomingMessages(
                new AuthorizationFilter());

            secCfg.addDumpResponse();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException(ex);
        }
    }
}
