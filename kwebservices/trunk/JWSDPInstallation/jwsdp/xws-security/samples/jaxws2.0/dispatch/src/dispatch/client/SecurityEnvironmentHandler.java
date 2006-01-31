/*
 * $Id$
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package dispatch.client;

import java.io.IOException;
import java.io.FileInputStream;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Arrays;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import java.text.SimpleDateFormat;

import java.math.BigInteger;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertPathBuilder;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;

import javax.crypto.SecretKey;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;
import com.sun.xml.wss.impl.callback.DecryptionKeyCallback;
import com.sun.xml.wss.impl.callback.EncryptionKeyCallback;
import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureVerificationKeyCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;
import com.sun.xml.wss.impl.callback.TimestampValidationCallback;
import com.sun.org.apache.xml.internal.security.utils.RFC2253Parser;


/**
 * A sample implementation of a CallbackHandler.
 */
public  class SecurityEnvironmentHandler implements CallbackHandler {

    private String keyStoreURL;
    private String keyStorePassword;
    private String keyStoreType;

    private String trustStoreURL;
    private String trustStorePassword;
    private String trustStoreType;

    private String symmKeyStoreURL;
    private String symmKeyStorePassword;
    private String symmKeyStoreType;

    private KeyStore keyStore;
    private KeyStore trustStore;
    private KeyStore symmKeyStore;

    private static final String fileSeparator = System.getProperty("file.separator");

    private static final UnsupportedCallbackException unsupported =
        new UnsupportedCallbackException(null, "Unsupported Callback Type Encountered");

    // container Home
    private String home = "";

    public SecurityEnvironmentHandler() throws Exception {

         setContainerHome();
         Properties properties = new Properties();
         String clientPropsFile = home + fileSeparator + "xws-security" +
             fileSeparator + "etc" + fileSeparator + "client-security-env.properties";
         properties.load(new FileInputStream(clientPropsFile));

        this.keyStoreURL = home + properties.getProperty("keystore.url");
        this.keyStoreType = properties.getProperty("keystore.type");
        this.keyStorePassword = properties.getProperty("keystore.password");

        this.trustStoreURL = home + properties.getProperty("truststore.url");
        this.trustStoreType = properties.getProperty("truststore.type");
        this.trustStorePassword = properties.getProperty("truststore.password");

        this.symmKeyStoreURL = home + properties.getProperty("symmetrickeystore.url");
        this.symmKeyStoreType = properties.getProperty("symmetrickeystore.type");
        this.symmKeyStorePassword = properties.getProperty("symmetrickeystore.password");

        initTrustStore();
        initKeyStore();
        initSymmKeyStore();
    }

    public void setContainerHome() {
       /*
        Enumeration e = System.getProperties().propertyNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            System.out.println(key + ":" + System.getProperty(key));
        }
       */
        //System.out.println(System.getProperty("as.home"));
        //this.home = System.getProperty("as.home");
        //System.out.println(System.getProperty("tomcat.home"));
        //this.home = System.getProperty("tomcat.home");

        String containerHome = null;
        this.home = null;

        containerHome = System.getProperty("as.home");
        if("${as.home}".equals(containerHome)  || containerHome == null) {
	    containerHome = System.getProperty("ws.home") ;
            if("${ws.home}".equals(containerHome) || containerHome == null) {
                containerHome = System.getProperty("tomcat.home");
		if(!("${tomcat.home}".equals(containerHome) || containerHome == null)) {
		    //tomcat
		    this.home = containerHome;
		}
	    } else {
		//ws
		this.home = containerHome;
	    }
	} else {
	    //as
	    this.home = containerHome;
	}
    }

    private String getContainerHome() {
        String _home = "";
        String fileSeparator = System.getProperty("file.separator");
        String contHome = System.getProperty("catalina.home");
        if (contHome != null) {
            String isAS = System.getProperty("com.sun.aas.instanceRoot");
            if (isAS != null) {
                _home = contHome + fileSeparator +
                        ".." + fileSeparator + "..";
            } else {
                _home = contHome;
            }
        } else {
            _home = System.getProperty("jwsdp.home");
            if (_home == null) {
                _home = System.getProperty("as.home");
            }
        }
        return _home;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        for (int i=0; i < callbacks.length; i++) {

            if (callbacks[i] instanceof PasswordValidationCallback) {
                PasswordValidationCallback cb = (PasswordValidationCallback) callbacks[i];
                if (cb.getRequest() instanceof PasswordValidationCallback.PlainTextPasswordRequest) {
                    cb.setValidator(new PlainTextPasswordValidator());

                } else if (cb.getRequest() instanceof PasswordValidationCallback.DigestPasswordRequest) {
                    PasswordValidationCallback.DigestPasswordRequest request =
                        (PasswordValidationCallback.DigestPasswordRequest) cb.getRequest();
                    String username = request.getUsername();
                    if ("Ron".equals(username)) {
                        request.setPassword("noR");
                        cb.setValidator(new PasswordValidationCallback.DigestPasswordValidator());
                    }
                } else {
                    throw unsupported;
                }

            } else if (callbacks[i] instanceof TimestampValidationCallback) {
                TimestampValidationCallback cb = (TimestampValidationCallback) callbacks[i];
                cb.setValidator(new DefaultTimestampValidator());

            } else if (callbacks[i] instanceof SignatureVerificationKeyCallback) {
                SignatureVerificationKeyCallback cb = (SignatureVerificationKeyCallback)callbacks[i];

                if (cb.getRequest() instanceof SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest) {
                    // subject keyid request
                    SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest request =
                        (SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest) cb.getRequest();
                    X509Certificate cert =
                        getCertificateFromTrustStore(
                            request.getSubjectKeyIdentifier());
                    request.setX509Certificate(cert);

                } else if (cb.getRequest() instanceof SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest) {
                    // issuer serial request
                    SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest request =
                        (SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest) cb.getRequest();
                    X509Certificate cert =
                        getCertificateFromTrustStore(
                            request.getIssuerName(),
                            request.getSerialNumber());
                    request.setX509Certificate(cert);

                } else  {
                     throw unsupported;
                }

            } else if (callbacks[i] instanceof SignatureKeyCallback) {
                SignatureKeyCallback cb = (SignatureKeyCallback)callbacks[i];

                 if (cb.getRequest() instanceof SignatureKeyCallback.DefaultPrivKeyCertRequest) {
                    // default priv key cert req
                    SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                        (SignatureKeyCallback.DefaultPrivKeyCertRequest) cb.getRequest();
                    getDefaultPrivKeyCert(request);

                } else if (cb.getRequest() instanceof SignatureKeyCallback.AliasPrivKeyCertRequest) {
                    SignatureKeyCallback.AliasPrivKeyCertRequest request =
                        (SignatureKeyCallback.AliasPrivKeyCertRequest) cb.getRequest();
                    String alias = request.getAlias();
                    try {
                        System.out.println("*****In callback *****");
                        X509Certificate cert =
                            (X509Certificate) keyStore.getCertificate(alias);
                        request.setX509Certificate(cert);
                        // Assuming key passwords same as the keystore password
                        PrivateKey privKey =
                            (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
                        request.setPrivateKey(privKey);
                    } catch (Exception e) {
                        throw new IOException(e.getMessage());
                    }

                } else {
                     throw unsupported;
                }

            } else if (callbacks[i] instanceof DecryptionKeyCallback) {
                DecryptionKeyCallback cb = (DecryptionKeyCallback)callbacks[i];

                if (cb.getRequest() instanceof  DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest) {
                    DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest request =
                        (DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest) cb.getRequest();
                    byte[] ski = request.getSubjectKeyIdentifier();
                    PrivateKey privKey = getPrivateKey(ski);
                    request.setPrivateKey(privKey);

                } else if (cb.getRequest() instanceof DecryptionKeyCallback.X509IssuerSerialBasedRequest) {
                    DecryptionKeyCallback.X509IssuerSerialBasedRequest request =
                        (DecryptionKeyCallback.X509IssuerSerialBasedRequest) cb.getRequest();
                    String issuerName = request.getIssuerName();
                    BigInteger serialNumber = request.getSerialNumber();
                    PrivateKey privKey = getPrivateKey(issuerName, serialNumber);
                    request.setPrivateKey(privKey);

                } else if (cb.getRequest() instanceof DecryptionKeyCallback.X509CertificateBasedRequest) {
                    DecryptionKeyCallback.X509CertificateBasedRequest request =
                        (DecryptionKeyCallback.X509CertificateBasedRequest) cb.getRequest();
                    X509Certificate cert = request.getX509Certificate();
                    PrivateKey privKey = getPrivateKey(cert);
                    request.setPrivateKey(privKey);

                } else if (cb.getRequest() instanceof DecryptionKeyCallback.AliasSymmetricKeyRequest) {
                    DecryptionKeyCallback.AliasSymmetricKeyRequest request =
                        (DecryptionKeyCallback.AliasSymmetricKeyRequest) cb.getRequest();
                    String alias = request.getAlias();
                    try {
                        // Assuming key password same as key store password
                        SecretKey symmKey =
                            (SecretKey) symmKeyStore.getKey(alias, symmKeyStorePassword.toCharArray());
                        request.setSymmetricKey(symmKey);
                    } catch (Exception e) {
                        throw new IOException(e.getMessage());
                    }

                } else  {
                    throw unsupported;
                }

            } else if (callbacks[i] instanceof EncryptionKeyCallback) {
                EncryptionKeyCallback cb = (EncryptionKeyCallback)callbacks[i];

                if (cb.getRequest() instanceof EncryptionKeyCallback.AliasX509CertificateRequest) {
                    EncryptionKeyCallback.AliasX509CertificateRequest request =
                        (EncryptionKeyCallback.AliasX509CertificateRequest) cb.getRequest();
                    String alias = request.getAlias();
                    if (alias == null) {
                        //plugin code here to read the cert from the ThreadLocal
                    } else {
                        try {
                            X509Certificate cert =
                                (X509Certificate) trustStore.getCertificate(alias);
                            request.setX509Certificate(cert);
                        } catch (Exception e) {
                            throw new IOException(e.getMessage());
                        }
                   }

                } else if (cb.getRequest() instanceof EncryptionKeyCallback.AliasSymmetricKeyRequest) {
                    EncryptionKeyCallback.AliasSymmetricKeyRequest request =
                        (EncryptionKeyCallback.AliasSymmetricKeyRequest) cb.getRequest();
                    String alias = request.getAlias();
                    try {
                        // Assuming key password same as key store password
                        SecretKey symmKey =
                            (SecretKey) symmKeyStore.getKey(alias, symmKeyStorePassword.toCharArray());
                        request.setSymmetricKey(symmKey);
                    } catch (Exception e) {
                        throw new IOException(e.getMessage());
                    }

                } else {
                    throw unsupported;
                }

            } else if (callbacks[i] instanceof CertificateValidationCallback) {
                CertificateValidationCallback cb = (CertificateValidationCallback)callbacks[i];
                cb.setValidator(new X509CertificateValidatorImpl());

            } else {
                throw unsupported;
            }
        }
    }

    private void initTrustStore() throws IOException {
        try {
            trustStore = KeyStore.getInstance(trustStoreType);
            trustStore.load(new FileInputStream(trustStoreURL), trustStorePassword.toCharArray());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private void initKeyStore() throws IOException {
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(new FileInputStream(keyStoreURL), keyStorePassword.toCharArray());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private void initSymmKeyStore() throws IOException {
        try {
            symmKeyStore = KeyStore.getInstance(symmKeyStoreType);
            symmKeyStore.load(new FileInputStream(symmKeyStoreURL), symmKeyStorePassword.toCharArray());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private X509Certificate getCertificateFromTrustStore(byte[] ski)
        throws IOException {

        try {
            Enumeration aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = trustStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = getSubjectKeyIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(ski, keyId)) {
                    return x509Cert;
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    private X509Certificate getCertificateFromTrustStore(
        String issuerName,
        BigInteger serialNumber)
        throws IOException {

        try {
            Enumeration aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = trustStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                String thisIssuerName =
                    RFC2253Parser.normalize(x509Cert.getIssuerDN().getName());
                BigInteger thisSerialNumber = x509Cert.getSerialNumber();
                if (thisIssuerName.equals(issuerName) &&
                    thisSerialNumber.equals(serialNumber)) {
                    return x509Cert;
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    public PrivateKey getPrivateKey(byte[] ski) throws IOException {

        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias))
                    continue;
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = getSubjectKeyIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(ski, keyId)) {
                    // Asuumed key password same as the keystore password
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    public PrivateKey getPrivateKey(
        String issuerName,
        BigInteger serialNumber)
        throws IOException {

        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias))
                    continue;
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                String thisIssuerName =
                    RFC2253Parser.normalize(x509Cert.getIssuerDN().getName());
                BigInteger thisSerialNumber = x509Cert.getSerialNumber();
                if (thisIssuerName.equals(issuerName) &&
                    thisSerialNumber.equals(serialNumber)) {
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    public PrivateKey getPrivateKey(X509Certificate certificate)
        throws IOException {

        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias))
                    continue;
                Certificate cert = keyStore.getCertificate(alias);
                if (cert != null && cert.equals(certificate))
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    private void getDefaultPrivKeyCert(
        SignatureKeyCallback.DefaultPrivKeyCertRequest request)
        throws IOException {

        String uniqueAlias = null;
        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String currentAlias = (String) aliases.nextElement();
                if (keyStore.isKeyEntry(currentAlias)) {
                    Certificate thisCertificate = keyStore.getCertificate(currentAlias);
                    if (thisCertificate != null) {
                        if (thisCertificate instanceof X509Certificate) {
                            if (uniqueAlias == null) {
                                uniqueAlias = currentAlias;
                            } else {
                                // Not unique!
                                uniqueAlias = null;
                                break;
                            }
                        }
                    }
                }
            }
            if (uniqueAlias != null) {
                request.setX509Certificate(
                    (X509Certificate) keyStore.getCertificate(uniqueAlias));
                request.setPrivateKey(
                    (PrivateKey) keyStore.getKey(uniqueAlias, keyStorePassword.toCharArray()));
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private static byte[] getSubjectKeyIdentifier(X509Certificate cert) {
        String SUBJECT_KEY_IDENTIFIER_OID = "2.5.29.14";
        byte[] subjectKeyIdentifier =
            cert.getExtensionValue(SUBJECT_KEY_IDENTIFIER_OID);
        if (subjectKeyIdentifier == null)
            return null;

        try {
            sun.security.x509.KeyIdentifier keyId = null;

            sun.security.util.DerValue derVal = new sun.security.util.DerValue(
                 new sun.security.util.DerInputStream(subjectKeyIdentifier).getOctetString());

            keyId = new sun.security.x509.KeyIdentifier(derVal.getOctetString());
            return keyId.getIdentifier();
        } catch (NoClassDefFoundError ncde) {
            if (subjectKeyIdentifier == null)
                return null;
            byte[] dest = new byte[subjectKeyIdentifier.length - 4];
            System.arraycopy(
                subjectKeyIdentifier, 4, dest, 0, subjectKeyIdentifier.length - 4);
            return dest;
        } catch ( java.io.IOException ex) {
            //ignore
            return null;
        }

    }


    private class PlainTextPasswordValidator implements PasswordValidationCallback.PasswordValidator {

        public boolean validate(PasswordValidationCallback.Request request)
            throws PasswordValidationCallback.PasswordValidationException {

            PasswordValidationCallback.PlainTextPasswordRequest plainTextRequest =
                (PasswordValidationCallback.PlainTextPasswordRequest) request;
            if ("Ron".equals(plainTextRequest.getUsername()) &&
                "noR".equals(plainTextRequest.getPassword())) {
                return true;
            }
            return false;
        }
    }


    private class DefaultTimestampValidator implements TimestampValidationCallback.TimestampValidator {

        public void validate(TimestampValidationCallback.Request request)
            throws TimestampValidationCallback.TimestampValidationException {


            // validate timestamp creation and expiration time.
            TimestampValidationCallback.UTCTimestampRequest utcTimestampRequest =
                (TimestampValidationCallback.UTCTimestampRequest) request;


            SimpleDateFormat calendarFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat calendarFormatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'");
            Date created = null;
            Date expired = null;

            try {
                try {
                    created = calendarFormatter1.parse(utcTimestampRequest.getCreated());
                    if ( utcTimestampRequest.getExpired() != null )
                        expired = calendarFormatter1.parse(utcTimestampRequest.getExpired());
                } catch (java.text.ParseException pe) {
                    created = calendarFormatter2.parse(utcTimestampRequest.getCreated());
                    if ( utcTimestampRequest.getExpired() != null )
                        expired = calendarFormatter2.parse(utcTimestampRequest.getExpired());
                }
            } catch ( java.text.ParseException pe ) {
                throw new TimestampValidationCallback.TimestampValidationException(pe.getMessage());
            }

            long maxClockSkew = utcTimestampRequest.getMaxClockSkew();
            long timestampFreshnessLimit = utcTimestampRequest.getTimestampFreshnessLimit();

            // validate creation time
            validateCreationTime(created, maxClockSkew, timestampFreshnessLimit);

            // validate expiration time
            if ( expired != null )
                validateExpirationTime(expired, maxClockSkew, timestampFreshnessLimit);
        }
    }

    public void validateExpirationTime(
        Date expires, long maxClockSkew, long timestampFreshnessLimit)
        throws TimestampValidationCallback.TimestampValidationException {

        //System.out.println("Validate Expiration time called");
        Date currentTime =
            getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, false);
        if (expires.before(currentTime)) {
            throw new TimestampValidationCallback.TimestampValidationException(
                "The current time is ahead of the expiration time in Timestamp");
        }
    }

    public void validateCreationTime(
        Date created,
        long maxClockSkew,
        long timestampFreshnessLimit)
        throws TimestampValidationCallback.TimestampValidationException {

        //System.out.println("Validate Creation time called");
        Date current = getFreshnessAndSkewAdjustedDate(maxClockSkew, timestampFreshnessLimit);

        if (created.before(current)) {
            throw new TimestampValidationCallback.TimestampValidationException(
                "The creation time is older than " +
                " currenttime - timestamp-freshness-limit - max-clock-skew");
        }

        Date currentTime =
            getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, true);
        if (currentTime.before(created)) {
            throw new TimestampValidationCallback.TimestampValidationException(
                "The creation time is ahead of the current time.");
        }
    }

    private static Date getFreshnessAndSkewAdjustedDate(
    long maxClockSkew, long timestampFreshnessLimit) {
        Calendar c = new GregorianCalendar();
        long offset = c.get(Calendar.ZONE_OFFSET);
        if (c.getTimeZone().inDaylightTime(c.getTime())) {
            offset += c.getTimeZone().getDSTSavings();
        }
        long beforeTime = c.getTimeInMillis();
        long currentTime = beforeTime - offset;

        long adjustedTime = currentTime - maxClockSkew - timestampFreshnessLimit;
        c.setTimeInMillis(adjustedTime);

        return c.getTime();
    }


    private static Date getGMTDateWithSkewAdjusted(
    Calendar c, long maxClockSkew, boolean addSkew) {
        long offset = c.get(Calendar.ZONE_OFFSET);
        if (c.getTimeZone().inDaylightTime(c.getTime())) {
            offset += c.getTimeZone().getDSTSavings();
        }
        long beforeTime = c.getTimeInMillis();
        long currentTime = beforeTime - offset;

        if (addSkew)
            currentTime = currentTime + maxClockSkew;
        else
            currentTime = currentTime - maxClockSkew;

        c.setTimeInMillis(currentTime);
        return c.getTime();
    }

    private class X509CertificateValidatorImpl implements CertificateValidationCallback.CertificateValidator {

        public boolean validate(X509Certificate certificate)
            throws CertificateValidationCallback.CertificateValidationException {

            if (isSelfCert(certificate)) {
                return true;
            }

            try {
                certificate.checkValidity();
            } catch (CertificateExpiredException e) {
                e.printStackTrace();
                throw new CertificateValidationCallback.CertificateValidationException("X509Certificate Expired", e);
            } catch (CertificateNotYetValidException e) {
                e.printStackTrace();
                throw new CertificateValidationCallback.CertificateValidationException("X509Certificate not yet valid", e);
            }

            X509CertSelector certSelector = new X509CertSelector();
            certSelector.setCertificate(certificate);

            PKIXBuilderParameters parameters;
            CertPathBuilder builder;
            try {
                parameters = new PKIXBuilderParameters(trustStore, certSelector);
                parameters.setRevocationEnabled(false);
                builder = CertPathBuilder.getInstance("PKIX");
            } catch (Exception e) {
                e.printStackTrace();
                throw new CertificateValidationCallback.CertificateValidationException(e.getMessage(), e);
            }

            try {
                PKIXCertPathBuilderResult result =
                    (PKIXCertPathBuilderResult) builder.build(parameters);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        private boolean isSelfCert(X509Certificate cert)
            throws CertificateValidationCallback.CertificateValidationException {
            try {
                if (keyStore == null)
                    initKeyStore();
                Enumeration aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    if (keyStore.isKeyEntry(alias)) {
                        X509Certificate x509Cert =
                            (X509Certificate) keyStore.getCertificate(alias);
                        if (x509Cert != null) {
                            if (x509Cert.equals(cert))
                                return true;
                        }
                    }
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                throw new CertificateValidationCallback.CertificateValidationException(e.getMessage(), e);
            }
        }
    }
}
