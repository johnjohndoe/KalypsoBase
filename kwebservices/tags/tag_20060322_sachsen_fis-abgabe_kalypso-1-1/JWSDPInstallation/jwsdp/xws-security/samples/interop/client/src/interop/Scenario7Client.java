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
import java.util.Vector;
import java.util.StringTokenizer;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.xml.namespace.QName;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.reference.KeyIdentifier;
import com.sun.xml.wss.impl.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.filter.*;
import com.sun.xml.wss.keyinfo.*;

import com.sun.xml.rpc.security.SecurityConfigurator;
import com.sun.xml.rpc.security.SecurityHandlerConstants;
import com.sun.xml.rpc.security.LoggerConstants;

import com.sun.org.apache.xml.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.security.utils.Base64;


/*
 * Implementation of interop scenario # 7 client.
 *
 * @author Kumar Jayanti (vbkumar.jayanti@sun.com)
 */ 
public class Scenario7Client {    

    /** the security configurator */
    SecurityConfigurator secCfg = null;

    private static String[] aliases = null;
    private static String[] keyPasswords = null;
    private static String keystorePassword =
        System.getProperty("keystore.password");
    private static String keystoreURL =
        System.getProperty("keystore.url");
    private static String truststorePassword =
        System.getProperty("truststore.password");
    private static String truststoreURL =
        System.getProperty("truststore.url");
    private static String[] trustedAliases = null;
    private static String keystoreType =
        System.getProperty("keystore.type");
    private static String truststoreType =
        System.getProperty("truststore.type");


    private static Logger log =
        Logger.getLogger(
            LoggerConstants.RPC_SECURITY_DOMAIN,
            LoggerConstants.RPC_SECURITY_RESOURCE_BUNDLE);

    
    // turn this flag on to view messages on the wire
    private static boolean debug = true;

    private static QName portName =
        new QName("http://xmlsoap.org/Ping", "Ping7");
    
    private static void scenario7(PingService service) throws Exception {
        try {
            
            SecurityConfigurator secCfg =
                new SecurityConfigurator(service, portName);

            SecurityEnvironment secDomain = initializeSecurityEnvironment();
            secCfg.setSecurityEnvironment(secDomain);

            X509Certificate certificate =
                (X509Certificate) ((DefaultSecurityEnvironmentImpl) secDomain).
                    getKeyStore().getCertificate(aliases[0]);

            secCfg.addRequestTimestamp();

            secCfg.addFilterForOutgoingMessages(
                new ExportSignatureFilter(
                    new DirectReferenceStrategy(certificate)));

            secCfg.addFilterForOutgoingMessages(
                new ExportCertificateTokenFilter());

            secCfg.addFilterForOutgoingMessages(
                new SignFilter(
                    "//SOAP-ENV:Body", "//wsse:SecurityTokenReference"));

            secCfg.addEncryptRequest(
                "//SOAP-ENV:Body",
                true,
                SecurityConfigurator.KEY_IDENTIFIER_STRATEGY);

            if (debug) {           
                // see what the request and response look like
                secCfg.addDumpRequest().addDumpResponse();
            }

            secCfg.addFilterForIncomingMessages(
                new ProcessSecurityHeaderFilter());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    public static void main(String[] args) throws Exception {    

        PingService pingService = new PingService_Impl();

        String serviceHost = System.getProperty("endpoint.host");
        String servicePort = System.getProperty("endpoint.port");

        String serviceURLFragment = System.getProperty("scenario7.service.url");        
        String serviceURL =
            "http://" + serviceHost + ":" + servicePort + serviceURLFragment;

        System.out.println("Service URL=" + serviceURL);


        initKeyAliasesAndPasswords();

        scenario7(pingService);     
        // use static stubs to override endpoint property of WSDL       
        PingPort_Ping7_Stub stub = (PingPort_Ping7_Stub)  
                    (pingService.getPing7());           

        stub._setProperty(  
            javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, serviceURL);  

        stub.ping(new TicketType("Id", "SUNW"), "Sun Microsystems says hello scenario 7!");
        
    }

    protected static SecurityEnvironment initializeSecurityEnvironment()
        throws Exception {
        DefaultSecurityEnvironmentImpl domain =
            new DefaultSecurityEnvironmentImpl(
                keystoreURL,
                keystorePassword,
                keystoreType,
                truststoreURL,
                truststorePassword,
                truststoreType,
                aliases,
                keyPasswords);

        // ------------------ Mutual Agreements ---------------------
        // Scenario 7 Certvalue is receiver cert
        X509Certificate cert = (X509Certificate)
            domain.getTrustStore().
                getCertificate(trustedAliases[0]);

        String certValue = Base64.encode(
               KeyIdentifier.getSubjectKeyIdentifier(cert));
        // set the agreement CERT-VALUE for scenario 7
        domain.setAgreementProperty("CERT-VALUE", certValue);
        return domain;
    }
    
    public static void initKeyAliasesAndPasswords() {
        String strAliases = System.getProperty("key.aliases");
        String strKeyPasswords = System.getProperty("key.passwords");
        String strTrustedAliases = System.getProperty("trust.aliases");

        // initialize key aliases
        StringTokenizer st = new StringTokenizer(strAliases, ",");
        Vector v = new Vector();
        while (st.hasMoreTokens()) {
            String tmp = st.nextToken();
            v.add(tmp);
        }
        aliases = new String[v.size()];
        for (int i = 0; i < v.size(); i++) {
            aliases[i] = (String)v.elementAt(i);
        }

        // initialize trusted aliases
        st = new StringTokenizer(strTrustedAliases, ",");
        v = new Vector();
        while (st.hasMoreTokens()) {
            String tmp = st.nextToken();
            v.add(tmp);
        }
        trustedAliases = new String[v.size()];
        for (int i = 0; i < v.size(); i++) {
            trustedAliases[i] = (String)v.elementAt(i);
        }

        // initialize key passwords
        st = new StringTokenizer(strKeyPasswords, ",");
        v = new Vector();
        while (st.hasMoreTokens()) {
            String tmp = st.nextToken();
            v.add(tmp);
         }
        keyPasswords = new String[v.size()];
        for (int i = 0; i < v.size(); i++) {
            keyPasswords[i] = (String)v.elementAt(i);
        }
    }
    
}
