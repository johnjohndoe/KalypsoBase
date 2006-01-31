import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Collections;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dom.*;
import javax.xml.crypto.dsig.dom.*;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.soap.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/**
 * Construct a SOAP message, sign it and then validate the signature.
 * This implementation follows the 
 * <a ref="http://www.w3.org/TR/SOAP-dsig/">
 * W3C Note on digital signatures in SOAP messages
 * </a>. 
 * The validating key is included in the signature.
 * DOM Level 2 is used throughout.
 * <p>
 * The following SOAP message is signed:
 * <pre><code>
 *
 *     <?xml version="1.0" encoding="UTF-8"?>
 *     <soap-env:Envelope 
 *      xmlns:soap-env="http://schemas.xmlsoap.org/soap/envelope/">
 *       <soap-env:Header>
 *         <SOAP-SEC:Signature 
 *          mustUnderstand="1" 
 *          xmlns:SOAP-SEC="http://schemas.xmlsoap.org/soap/security/2000-12"/>
 *       </soap-env:Header>
 *       <soap-env:Body id="Body">
 *         <m:GetLastTradePrice xmlns:m="http://wombats.ztrade.com">
 *           <symbol>SUNW</symbol>
 *         </m:GetLastTradePrice>
 *       </soap-env:Body>
 *     </soap-env:Envelope>
 *
 * </code></pre>
 */
public class SignedSoap {

    private static boolean debug = false;

    public static void main(String[] args) throws Exception {

	int argc = args.length;

	if (argc == 1) {
	    if (args[0].equalsIgnoreCase("-help")) {
		System.out.println("Usage:  SignedSoap [-debug]");
		System.out.println("    -debug\tactivates debug messages");
		return;
	    }
	    debug = args[0].equalsIgnoreCase("-debug");
	}

	// Create the SOAP message

	System.out.println("Creating the SOAP message...");
	SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
	SOAPPart soapPart = soapMessage.getSOAPPart();
	SOAPEnvelope soapEnvelope = soapPart.getEnvelope();

	SOAPHeader soapHeader = soapEnvelope.getHeader();
	SOAPHeaderElement headerElement = soapHeader.addHeaderElement(
            soapEnvelope.createName("Signature", "SOAP-SEC",
            "http://schemas.xmlsoap.org/soap/security/2000-12"));

	SOAPBody soapBody = soapEnvelope.getBody();
	soapBody.addAttribute(soapEnvelope.createName("id", "SOAP-SEC",
	    "http://schemas.xmlsoap.org/soap/security/2000-12"), "Body");
	Name bodyName =soapEnvelope.createName("GetLastTradePrice", "m",
	    "http://wombats.ztrade.com");
	SOAPBodyElement gltp = soapBody.addBodyElement(bodyName);
	Name name = soapEnvelope.createName("symbol");
	SOAPElement symbol = gltp.addChildElement(name);
	symbol.addTextNode("SUNW");

	// Generate a DOM representation of the SOAP message

	System.out.println("Generating the DOM tree...");
	// Get input source
	Source source = soapPart.getContent();
	org.w3c.dom.Node root = null;

	if (source instanceof DOMSource) {
	    root = ((DOMSource)source).getNode();

	} else if (source instanceof SAXSource) {
	    InputSource inSource = ((SAXSource)source).getInputSource();
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
	    DocumentBuilder db = null;

	    synchronized (dbf) {
		db = dbf.newDocumentBuilder();
	    }
	    Document doc = db.parse(inSource);
	    root = (org.w3c.dom.Node) doc.getDocumentElement();

	} else {
	    System.err.println("error: cannot convert SOAP message (" +
		source.getClass().getName() + ") into a W3C DOM tree");
	    System.exit(-1);
	}

	if (debug) {
	    dumpDOMDocument(root);
	}

	// Generate a DSA key pair

	System.out.println("Generating the DSA keypair...");
	KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
	kpg.initialize(1024, new SecureRandom("not so random".getBytes()));
	KeyPair keypair = kpg.generateKeyPair();

        // Assemble the signature parts

	System.out.println("Preparing the signature...");
	String providerName = System.getProperty
            ("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
	XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM",
            (Provider) Class.forName(providerName).newInstance());
        Reference ref = sigFactory.newReference("#Body", 
	    sigFactory.newDigestMethod(DigestMethod.SHA1, null));
        SignedInfo signedInfo = sigFactory.newSignedInfo(
	    sigFactory.newCanonicalizationMethod(
		CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, 
		(C14NMethodParameterSpec) null),
            sigFactory.newSignatureMethod(SignatureMethod.DSA_SHA1, null),
	    Collections.singletonList(ref));
	KeyInfoFactory kif = sigFactory.getKeyInfoFactory();
	KeyValue kv = kif.newKeyValue(keypair.getPublic());
        KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(kv));

        XMLSignature sig = sigFactory.newXMLSignature(signedInfo, keyInfo);

	// Insert XML signature into DOM tree and sign

	System.out.println("Signing the SOAP message...");
	// Find where to insert signature
	Element envelope = getFirstChildElement(root);
	Element header = getFirstChildElement(envelope);
	DOMSignContext sigContext =
	    new DOMSignContext(keypair.getPrivate(), header);
	// Need to distinguish the Signature element in DSIG (from that in SOAP)
	sigContext.putNamespacePrefix(XMLSignature.XMLNS, "ds");
	// register Body ID attribute
	sigContext.setIdAttributeNS
	    (getNextSiblingElement(header),
	     "http://schemas.xmlsoap.org/soap/security/2000-12","id");
	sig.sign(sigContext);

	if (debug) {
	    dumpDOMDocument(root);
	}

	// Validate the XML signature

        // Locate the signature element
	Element sigElement = getFirstChildElement(header);
        // Validate the signature using the public key generated above
        DOMValidateContext valContext =
	    new DOMValidateContext(keypair.getPublic(), sigElement);
	// register Body ID attribute
	valContext.setIdAttributeNS
	    (getNextSiblingElement(header),
	     "http://schemas.xmlsoap.org/soap/security/2000-12","id");
        boolean isValid = sig.validate(valContext);
	System.out.println("Validating the signature... " +
	    (isValid ? "valid" : "invalid"));

    }

    /*
     * Outputs DOM representation to the standard output stream.
     *
     * @param root The DOM representation to be outputted
     */
    private static void dumpDOMDocument(org.w3c.dom.Node root)
	throws TransformerException, TransformerConfigurationException {

	System.out.println("\n");
	// Create a new transformer object
	Transformer transformer =
	    TransformerFactory.newInstance().newTransformer();
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	// Dump the DOM representation to standard output
	transformer.transform(new DOMSource(root), new StreamResult(System.out));
	System.out.println("\n");
    }

    /**
     * Returns the first child element of the specified node, or null if there
     * is no such element.
     *
     * @param node the node
     * @return the first child element of the specified node, or null if there
     *	  is no such element
     * @throws NullPointerException if <code>node == null</code>
     */
    private static Element getFirstChildElement(org.w3c.dom.Node node) {
	org.w3c.dom.Node child = node.getFirstChild();
	while (child != null && 
	       child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
            child = child.getNextSibling();
	}
        return (Element) child;
    }

    /**
     * Returns the next sibling element of the specified node, or null if there
     * is no such element.
     *
     * @param node the node
     * @return the next sibling element of the specified node, or null if there
     *	  is no such element
     * @throws NullPointerException if <code>node == null</code>
     */
    public static Element getNextSiblingElement(org.w3c.dom.Node node) {
	org.w3c.dom.Node sibling = node.getNextSibling();
	while (sibling != null && 
	       sibling.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
            sibling = sibling.getNextSibling();
	}
        return (Element) sibling;
    }
}
