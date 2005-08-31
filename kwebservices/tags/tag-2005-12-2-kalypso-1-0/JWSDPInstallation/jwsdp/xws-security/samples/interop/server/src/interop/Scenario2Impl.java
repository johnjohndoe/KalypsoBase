/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package interop;

import java.io.File;
import java.security.KeyStore;

import javax.xml.rpc.ServiceException;

import com.sun.xml.wss.MessageConstants;
import com.sun.xml.wss.filter.*;

import com.sun.xml.rpc.security.SecurityConfigurator;



/*
 * Server side implementation of Pingservice interop application,
 * as defined in the WSS interop spec Scenario # 2.
 *
 * This class extends from the base implementation of PingImpl,
 * and handles server side processing needed for the scenario.
 *
 * @author Manveen Kaur
 *
 */
public class Scenario2Impl extends interop.PingImpl {

    // ---------- private variables and constants -----------

    /*
     * The following two parameters are required to correctly
     * create or process messages, but are not a matter of
     * mutual agreement
     */

    // value of the maximum skew between the local times of two
    // systems (in milliseconds).
    private static int MAX_CLOCK_SKEW = 10000;

    // value of the length of time a previously received nonce value will be
    // stored (in milliseconds). We have set it to one day here. TBD.
    private static long MAX_NONCE_AGE = 200000;

    // -------------- methods --------------

    /* (non-Javadoc)
     * @see javax.xml.rpc.server.ServiceLifecycle#init(java.lang.Object)
     */
    public void init(Object context) throws ServiceException {
        super.init(context);
        scenario2();
    }

    /*
     *
     * Validate the username password that is in plaintext
     * in the WSS: UserName token profile
     */
    private void scenario2() throws ServiceException {
        try {

        secCfg.addDumpRequest().addDumpResponse();
        secCfg.addFilterForIncomingMessages(new ProcessSecurityHeaderFilter());
        secCfg.addFilterForIncomingMessages(new AuthorizationFilter());

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException(ex);
        }

        // ------------- Decrypt Username token ---------------
        // get username token

        // Extract CipherKey element.
        // Decrypt it using the private key of the x509certificate.
        // to obtain the random key

        // CipherData element contains the actual data in encrypted form.
        // use the random key obtained above to decrypt CipherData
        // get the actual username token.


        // ------  Username Token Processing -------
        // (1) extract nonce. (should not be in base64 encoding now).
        //  update nonce list, against MAX_NONCE_AGE , reject stale nonce's
        // verify nonce is NOT there in the list of nonces.
        // if it is,throw a "Nonce already present" fault.
        // else add nonce to the list above.

        // (2) Extract created time.
        // Compare it with the current time.
        // Make sure it is within MAX_CLOCK_SKEW time range.
        // If not, then throw a fault

        // (3) Extract username and password
        // Verify it with mutual agreement database of valid values

    }
}
