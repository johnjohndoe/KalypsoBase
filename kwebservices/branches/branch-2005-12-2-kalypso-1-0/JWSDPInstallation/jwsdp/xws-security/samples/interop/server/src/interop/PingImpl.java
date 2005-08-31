/*
 * $Id$
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package interop;

import java.io.*;
import java.util.*;


import java.util.Properties;
import java.util.Vector;
import java.util.StringTokenizer;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.security.cert.X509Certificate;
import java.security.Security;
import java.security.Provider;
import java.security.KeyStore;
import java.security.PrivateKey;

import javax.xml.rpc.*;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import javax.servlet.ServletContext;

import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import com.sun.xml.rpc.server.http.ServletEndpointContextImpl;
import com.sun.xml.rpc.server.TieBase;
import com.sun.xml.rpc.spi.runtime.Tie;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.impl.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.filter.*;
import com.sun.xml.wss.reference.KeyIdentifier;

import com.sun.xml.rpc.security.SecurityConfigurator;
import com.sun.xml.rpc.security.SecurityHandlerConstants;
import com.sun.xml.rpc.security.LoggerConstants;

import com.sun.org.apache.xml.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.security.utils.Base64;


/*
 * Server side implementation of Pingservice interop application, 
 * as defined in the WSS interop spec.
 *
 * This is the base class common to all scenarios. This does common things like
 * initializing the username password database, and implement the "ping"
 * interface of the PingPort Service itself etc.
 *
 * How each scenario handles individual processing is a part of the Scenario#Impl
 * subclasses of this class.
 *
 * This class contains articles of mutual agreement needed for interoperating. 
 * Like USERNAME-PASSWORD list and CERT-VALUE.
 *
 */
public class PingImpl implements interop.PingPort, 
                                 ServiceLifecycle {

    /** the security configurator */
    protected SecurityConfigurator secCfg = null;
    /** the security domain */
    protected SecurityEnvironment secDomain = null;

    protected static String[] aliases = null;
    protected static String[] keyPasswords = null;
    protected static String   keystoreURL = null;
    protected static String   keystorePassword = null;
    protected static String   truststoreURL = null;
    protected static String   truststorePassword = null;
    protected static String  sessionKeyFile = null;
    protected static String[] trustedAliases = null;
    protected static InputStream   keystoreStream = null;
    protected static InputStream   truststoreStream = null;
    protected static InputStream   sessionkeyStream = null;

    private static Logger log =
        Logger.getLogger(
            LoggerConstants.RPC_SECURITY_DOMAIN,
            LoggerConstants.RPC_SECURITY_RESOURCE_BUNDLE);


    // --- implementation of main operation ---
    

    public java.lang.String ping(interop.TicketType ticket, java.lang.String message) {
        System.out.println("The message is here : " + message);        
        // System.out.println("The Ticket id is here : " + ticket.getId());
        return message;
    }

    /* (non-Javadoc)
     * @see javax.xml.rpc.server.ServiceLifecycle#destroy()
     */
    public void destroy() {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see javax.xml.rpc.server.ServiceLifecycle#init(java.lang.Object)
     */
    public void init(Object context) throws ServiceException {

        TieBase tie = getTie(context);

        initKeyAliasesAndPasswords(context);

        secCfg = new SecurityConfigurator(tie);

        try {
            secDomain = initializeSecurityEnvironment();
        } catch(Exception e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }

        secCfg.setSecurityEnvironment(secDomain);
    }
    
    protected SecurityEnvironment initializeSecurityEnvironment()
        throws Exception {
        DefaultSecurityEnvironmentImpl domain =
            new DefaultSecurityEnvironmentImpl(
                keystoreStream,
                keystorePassword,
                "JKS",
                truststoreStream,
                truststorePassword,
                "JKS",
                aliases,
                keyPasswords);
        // ------------------ Mutual Agreements ---------------------
        X509Certificate cert = (X509Certificate)
            domain.getKeyStore().getCertificate(aliases[0]);

        String certValue = Base64.encode(
               KeyIdentifier.getSubjectKeyIdentifier(cert));
        // set the agreement CERT-VALUE for scenario 2, 3, 4, 5
        //domain.setAgreementProperty("CERT-VALUE", certValue);

        Properties userPassMap = new Properties();
        
        // populate the set of valid usernames and passwords        
        // These are obtained from the OASIS website
        // TBD -- update this with the latest info.        
        
        userPassMap.put("Chris", "sirhC");
        userPassMap.put("Hal", "laH");
        userPassMap.put("Ron", "noR");
        userPassMap.put("Tony", "ynoT");
        userPassMap.put("Steve", "evetS");
        userPassMap.put("John", "nhoJ");
        userPassMap.put("Jerry", "yerrJ");
        
        domain.initializeUsernamePasswords(userPassMap);
        
        // read session key from file
        byte[] rawkey = new byte[(int)24]; //hardcoding session-key file size
        sessionkeyStream.read(rawkey, 0, 24);
        sessionkeyStream.close();

        DESedeKeySpec desks = new DESedeKeySpec(rawkey);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DESede", "BC");
        SecretKey sessionKey = skf.generateSecret(desks);

        domain.setAgreementProperty("SESSION-KEY-VALUE", "SessionKey");
        domain.setAgreementProperty("SessionKey", sessionKey);

        return domain;
    }

    private TieBase getTie(Object context) throws ServiceException {

        ServletEndpointContext sec;
        if (!(context instanceof ServletEndpointContext)) {
            log.log(Level.SEVERE,
                "context.not.instanceof.servletendpointcontext");
            throw new ServiceException(
                "Parameter 'context' is not instanceof ServletEndpointContext");
        }
        sec = (ServletEndpointContext) context;
        TieBase tie = (TieBase)
            ((ServletEndpointContextImpl) sec).getImplementor().getTie();
        return tie;
    }

    public void initKeyAliasesAndPasswords(Object context) {

        ServletEndpointContext sec = (ServletEndpointContext) context;
        ServletContext scontext = sec.getServletContext();

        keystoreURL = scontext.getInitParameter("keystore.url"); 
        System.out.println("Keystore URL:" + keystoreURL);
        keystoreStream = scontext.getResourceAsStream(keystoreURL);

        keystorePassword = scontext.getInitParameter("keystore.password");
        System.out.println("Keystore Password:" + keystorePassword);

        truststoreURL = scontext.getInitParameter("truststore.url"); 
        System.out.println("truststore URL:" + truststoreURL);
        truststoreStream = scontext.getResourceAsStream(truststoreURL);
        truststorePassword = scontext.getInitParameter("truststore.password");

        sessionKeyFile = scontext.getInitParameter("sessionkey.url");
        System.out.println("SessionKey URL:" + sessionKeyFile);
        sessionkeyStream = scontext.getResourceAsStream(sessionKeyFile);

        String strAliases = scontext.getInitParameter("key.aliases");
        String strKeyPasswords = scontext.getInitParameter("key.passwords");
        String strTrustedAliases = scontext.getInitParameter("trust.aliases");
        
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
