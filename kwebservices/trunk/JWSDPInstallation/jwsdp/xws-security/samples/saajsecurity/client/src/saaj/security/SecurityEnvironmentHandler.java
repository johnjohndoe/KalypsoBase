package saaj.security;

import java.io.IOException;
import java.io.FileInputStream;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Arrays;

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

import com.sun.xml.wss.core.Timestamp;
//import com.sun.xml.wss.MessageConstants;

import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;
import com.sun.xml.wss.impl.callback.DecryptionKeyCallback;
import com.sun.xml.wss.impl.callback.EncryptionKeyCallback;
import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureVerificationKeyCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;

import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;

import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.PrivateKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;

//import com.sun.xml.wss.impl.configuration.WSSPolicyGenerator;
//import com.sun.xml.wss.impl.configuration.DynamicSecurityPolicy;
import com.sun.xml.wss.impl.configuration.StaticApplicationContext;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;

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
    
    String clientPropsFile  = null;
    
    public SecurityEnvironmentHandler(String role) throws Exception {
        
        Properties properties = new Properties();
        
        String home = System.getProperty("jwsdp.home");
        // we are on the client side
        if(role.equals("client")){
            clientPropsFile= home + fileSeparator + "xws-security" + fileSeparator + "etc" + fileSeparator + "client-security-env.properties";
        }else{
            clientPropsFile= home + fileSeparator + "xws-security" + fileSeparator + "etc" + fileSeparator + "server-security-env.properties";
        }
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
                
            } else if (callbacks[i] instanceof SignatureVerificationKeyCallback) {
                SignatureVerificationKeyCallback cb = (SignatureVerificationKeyCallback)callbacks[i];
                
                if (cb.getRequest() instanceof SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest) {
                    // subject keyid request
                    SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest request =
                    (SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest) cb.getRequest();
                    if (trustStore == null)
                        initTrustStore();
                    X509Certificate cert =
                    getCertificateFromTrustStore(
                    request.getSubjectKeyIdentifier());
                    request.setX509Certificate(cert);
                    
                } else if (cb.getRequest() instanceof SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest) {
                    // issuer serial request
                    SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest request =
                    (SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest) cb.getRequest();
                    if (trustStore == null)
                        initTrustStore();
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
                    if (keyStore == null)
                        initKeyStore();
                    getDefaultPrivKeyCert(request);
                    
                } else if (cb.getRequest() instanceof SignatureKeyCallback.AliasPrivKeyCertRequest) {
                    SignatureKeyCallback.AliasPrivKeyCertRequest request =
                    (SignatureKeyCallback.AliasPrivKeyCertRequest) cb.getRequest();
                    String alias = request.getAlias();
                    if (keyStore == null)
                        initKeyStore();
                    try {
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
                    if (keyStore == null)
                        initKeyStore();
                    PrivateKey privKey = getPrivateKey(ski);
                    request.setPrivateKey(privKey);
                    
                } else if (cb.getRequest() instanceof DecryptionKeyCallback.X509IssuerSerialBasedRequest) {
                    DecryptionKeyCallback.X509IssuerSerialBasedRequest request =
                    (DecryptionKeyCallback.X509IssuerSerialBasedRequest) cb.getRequest();
                    String issuerName = request.getIssuerName();
                    BigInteger serialNumber = request.getSerialNumber();
                    if (keyStore == null)
                        initKeyStore();
                    PrivateKey privKey = getPrivateKey(issuerName, serialNumber);
                    request.setPrivateKey(privKey);
                    
                } else if (cb.getRequest() instanceof DecryptionKeyCallback.X509CertificateBasedRequest) {
                    DecryptionKeyCallback.X509CertificateBasedRequest request =
                    (DecryptionKeyCallback.X509CertificateBasedRequest) cb.getRequest();
                    X509Certificate cert = request.getX509Certificate();
                    if (keyStore == null)
                        initKeyStore();
                    PrivateKey privKey = getPrivateKey(cert);
                    request.setPrivateKey(privKey);
                    
                } else if (cb.getRequest() instanceof DecryptionKeyCallback.AliasSymmetricKeyRequest) {
                    DecryptionKeyCallback.AliasSymmetricKeyRequest request =
                    (DecryptionKeyCallback.AliasSymmetricKeyRequest) cb.getRequest();
                    if (symmKeyStore == null)
                        initSymmKeyStore();
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
                    if (trustStore == null)
                        initTrustStore();
                    String alias = request.getAlias();
                    try {
                        X509Certificate cert =
                        (X509Certificate) trustStore.getCertificate(alias);
                        request.setX509Certificate(cert);
                    } catch (Exception e) {
                        throw new IOException(e.getMessage());
                    }
                    
                } else if (cb.getRequest() instanceof EncryptionKeyCallback.AliasSymmetricKeyRequest) {
                    EncryptionKeyCallback.AliasSymmetricKeyRequest request =
                    (EncryptionKeyCallback.AliasSymmetricKeyRequest) cb.getRequest();
                    if (symmKeyStore == null)
                        initSymmKeyStore();
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
            e.printStackTrace();
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
        byte[] dest = new byte[subjectKeyIdentifier.length - 4];
        System.arraycopy(
        subjectKeyIdentifier, 4, dest, 0, subjectKeyIdentifier.length - 4);
        return dest;
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
                if (trustStore == null)
                    initTrustStore();
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
