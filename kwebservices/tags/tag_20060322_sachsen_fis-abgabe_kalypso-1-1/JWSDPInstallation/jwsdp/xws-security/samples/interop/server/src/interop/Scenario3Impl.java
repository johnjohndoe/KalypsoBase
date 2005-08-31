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

import com.sun.org.apache.xml.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.security.utils.Base64;



/*
 * Server side implementation of Pingservice interop application,
 * as defined in the WSS interop spec Scenario # 3.
 *
 * This class extends from the base implementation of PingImpl,
 * and handles server side processing needed for the scenario.
 *
 * @author Kumar Jayanti
 *
 */
public class Scenario3Impl extends interop.PingImpl {


    // -------------- methods --------------

    /* (non-Javadoc)
     * @see javax.xml.rpc.server.ServiceLifecycle#init(java.lang.Object)
     */
    public void init(Object context) throws ServiceException {
        super.init(context);
        scenario3();
    }

    /*
     * Sign and Encrypt the message, WSS Scenario 3
     */
    private void scenario3() throws ServiceException {
        try {
            secCfg.addDumpRequest();

            secCfg.addFilterForIncomingMessages(
                new ProcessSecurityHeaderFilter());
            secCfg.addFilterForIncomingMessages(
                new AuthorizationFilter());

             // The certificate that came in the header 
             // (for signature verification) should now be used for encryption
             // For now get this  certificate for encryption from truststore
             X509Certificate encrCert = (X509Certificate)
                ((DefaultSecurityEnvironmentImpl)secDomain).
                    getTrustStore().getCertificate(trustedAliases[0]);

             // The key of the server is used for signing
             // set the certificate as CERT-VALUE 
             // since we are using KEY_IDENTIFIER
             X509Certificate certificate = (X509Certificate)
                ((DefaultSecurityEnvironmentImpl)secDomain).
                    getKeyStore().getCertificate(aliases[0]);

             String certValue = Base64.encode(
               KeyIdentifier.getSubjectKeyIdentifier(certificate));
             ((DefaultSecurityEnvironmentImpl)secDomain).
                 setAgreementProperty("CERT-VALUE", certValue);

            //response
            secCfg.addRequestTimestamp();
            secCfg.addSignResponse(
                "//SOAP-ENV:Body",
                certificate,
                SecurityConfigurator.KEY_IDENTIFIER_STRATEGY);


            secCfg.addFilterForOutgoingMessages(new ExportEncryptedKeyFilter(
                new DirectReferenceStrategy(encrCert)));
            secCfg.addFilterForOutgoingMessages(
                new EncryptElementFilter("//SOAP-ENV:Body", true));

            secCfg.addFilterForOutgoingMessages(
                new ExportCertificateTokenFilter());

            secCfg.addDumpResponse();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException(ex);
        }
    }
}
