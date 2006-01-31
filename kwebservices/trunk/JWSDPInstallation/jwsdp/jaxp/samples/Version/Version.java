// $Id$

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 */

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.xml.sax.SAXException;

/**
 * <p>Print all available version information.</p>
 * 
 * @author <a href="mailto:Jeff.Suttor@Sun.com">Jeff Suttor</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public class Version {
	
	/**
	 * <p>All javax.xml package names.</p>
	 */
	public static final String[] JAVAX_XML_PACKAGE_NAMES = {
		"javax.xml",
		"javax.xml.datatype",
		"javax.xml.namespace",
		"javax.xml.parsers",
		"javax.xml.transform",
		"javax.xml.transform.dom",
		"javax.xml.transform.sax",
		"javax.xml.transform.stream",
		"javax.xml.validation",
		"javax.xml.xpath"
	};
	
	/**
	 * <p>All org.w3c package names.</p>
	 */
	public static final String[] ORG_W3C_PACKAGE_NAMES = {
        "org.w3c",
		"org.w3c.dom",
		"org.w3c.dom.bootstrap",
        "org.w3c.dom.events",
		"org.w3c.dom.ls",
		"org.w3c.dom.ranges",
		"org.w3c.dom.traversal"
	};

	/**
	 * <p>All org.xml package names.</p>
	 */
	public static final String[] ORG_XML_PACKAGE_NAMES = {
        "org.xml",
		"org.xml.sax",
		"org.xml.sax.ext",
		"org.xml.sax.helpers"
	};

	/*
	 * <p>Constant wording when a value isn't known.</p>
	 */
	private static final String NOT_KNOWN = "not known";

	/**
	 * <p><code>main</code> method to invoke.</p>
	 * 
	 * @param args All <code>args</code> are ignored.
	 */	
	public static void main(String[] args) {
		
		// print javax.xml packages
		for (int onPackage = 0; onPackage < JAVAX_XML_PACKAGE_NAMES.length; onPackage++) {
			printPackageVersion(JAVAX_XML_PACKAGE_NAMES[onPackage], "system: " + JAVAX_XML_PACKAGE_NAMES[onPackage]);
		}
		// org.w3c packages
		for (int onPackage = 0; onPackage < ORG_W3C_PACKAGE_NAMES.length; onPackage++) {
			printPackageVersion(ORG_W3C_PACKAGE_NAMES[onPackage], "system: " + ORG_W3C_PACKAGE_NAMES[onPackage]);
		}
		// org.xml packages
		for (int onPackage = 0; onPackage < ORG_XML_PACKAGE_NAMES.length; onPackage++) {
			printPackageVersion(ORG_XML_PACKAGE_NAMES[onPackage], "system: " + ORG_XML_PACKAGE_NAMES[onPackage]);
		}
		
		// parser implementation version information
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException parserConfigurationException) {
			System.err.println("Failure to create DocumentBuilder: " + parserConfigurationException.toString());
			System.exit(1);
		}

		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = null;
		try {
			saxParser = saxParserFactory.newSAXParser();
		} catch (ParserConfigurationException parserConfigurationException) {
			System.err.println("Failure to create SAXParser: " + parserConfigurationException.toString());
			System.exit(1);
		} catch (SAXException saxException) {
			System.err.println("Failure to create SAXParser: " + saxException.toString());
			System.exit(1);
		}
		
		// Transformer implementation version information
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer(); 
		} catch (TransformerConfigurationException transformerConfigurationException) {
			System.err.println("Failure to create Transformer: " + transformerConfigurationException.toString());
			System.exit(1);
		}

		// Schema implementation version information
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = null;
		try {
			schema = schemaFactory.newSchema(); 
		} catch (SAXException saxException) {
			System.err.println("Failure to create Schema: " + saxException.toString());
			System.exit(1);
		}

		// XPath implementation version information
		XPathFactory xPathFactory = null;
		try {
			xPathFactory = XPathFactory.newInstance();
		} catch (Exception xPathFactoryConfigurationException) {
			System.err.println("Failure to create XPath: " + xPathFactoryConfigurationException.toString());
			System.exit(1);
		} 
		XPath xPath = xPathFactory.newXPath();

		printPackageVersion(documentBuilderFactory.getClass().getPackage().getName(), "impl: DocumentBuilderFactory");
		printPackageVersion(documentBuilderFactory.getClass().getPackage().getName(), "impl: DocumentBuilder");
		printPackageVersion(saxParserFactory.getClass().getPackage().getName(), "impl: SAXParserFactory");
		printPackageVersion(saxParser.getClass().getPackage().getName(), "impl: SAXParser");

		printPackageVersion(transformerFactory.getClass().getPackage().getName(), "impl: TransformerFactory");
		printPackageVersion(transformer.getClass().getPackage().getName(), "impl: Transformer");

		printPackageVersion(schemaFactory.getClass().getPackage().getName(), "impl: SchemaFactory");
		printPackageVersion(schema.getClass().getPackage().getName(), "impl: Schema");
		
		printPackageVersion(xPathFactory.getClass().getPackage().getName(), "impl: XPathFactory");
		printPackageVersion(xPath.getClass().getPackage().getName(), "impl: XPath");
	}
	
	private static void printPackageVersion(String packageName, String prefix){
		
		// get package to query
		Package packageToQuery = Package.getPackage(packageName);
		if (packageToQuery == null) {
			System.out.println("[" + prefix + "]" + "No package information is available for " + packageName);
			return;
		}

		// specification version information		
		String specificationTitle = packageToQuery.getSpecificationTitle();
		if (specificationTitle == null) {
			specificationTitle = NOT_KNOWN;
		}
		String specificationVendor = packageToQuery.getSpecificationVendor();
		if (specificationVendor == null) {
			specificationVendor = NOT_KNOWN;
		}
		String specificationVersion = packageToQuery.getSpecificationVersion();
		if (specificationVersion == null) {
			specificationVersion = NOT_KNOWN;
		}

		// implementation version information
		String implementationTitle = packageToQuery.getImplementationTitle();
		if (implementationTitle == null) {
			implementationTitle = NOT_KNOWN;
		}
		String implementationVendor = packageToQuery.getImplementationVendor();
		if (implementationVendor == null) {
			implementationVendor = NOT_KNOWN;
		}
		String implementationVersion = packageToQuery.getImplementationVersion();
		if (implementationVersion == null) {
			implementationVersion = NOT_KNOWN;
		}
		
		System.out.println("[" + prefix + "]" + "Version information for Package " + packageName);
		System.out.println("[" + prefix + "]" + "\tSpecification:");
		System.out.println("[" + prefix + "]" + "\t\tTitle: " + specificationTitle);
		System.out.println("[" + prefix + "]" + "\t\tVendor: " + specificationVendor);
		System.out.println("[" + prefix + "]" + "\t\tVersion: " + specificationVersion);
		System.out.println("[" + prefix + "]" + "\tImplementation:");
		System.out.println("[" + prefix + "]" + "\t\tTitle: " + implementationTitle);
		System.out.println("[" + prefix + "]" + "\t\tVendor: " + implementationVendor);
		System.out.println("[" + prefix + "]" + "\t\tVersion: " + implementationVersion);
	}
} 