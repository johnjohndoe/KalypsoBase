//$Id$
//Copyright 2004 Sun Microsystems, Inc. All rights reserved.

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XPathDemo {
	
    /**
     * <p>usage java XPathDemo &lt;XML file&gt; &lt;XPath Expression&gt;</p>
     * 
     * <p>Apply XPath Express against XML file and
     * output resulting NodeList.</p>
     */
	public static void main(String[] args) {
		
		// must have exactly 2 args
		if (args.length != 2) {
			System.err.println("Usage:"
					+ "java XPathDemo <XML file>"
					+ " <XPath Expression>");
			System.exit(1);
		}
		
		String xmlFile = args[0];
		String xpathExpression = args[1];
		
		// create XPath
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();
		
		NamespaceContextImpl namespaceContextImpl = new NamespaceContextImpl();
        
        namespaceContextImpl.bindPrefixToNamespaceURI(
                XMLConstants.DEFAULT_NS_PREFIX,
                "http://schemas.xmlsoap.org/wsdl/");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "tns",
                "http://hello.org/wsdl");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "ns2",
                "http://hello.org/types");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "xsd",
                "http://www.w3.org/2001/XMLSchema");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "soap",
                "http://schemas.xmlsoap.org/wsdl/soap/");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "soap11-enc",
                "http://schemas.xmlsoap.org/soap/encoding/");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "xsi",
                "http://www.w3.org/2001/XMLSchema-instance");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "wsdl",
                "http://schemas.xmlsoap.org/wsdl/");
                
		xpath.setNamespaceContext(namespaceContextImpl);
		
		NamespaceContext namespaceContext = xpath.getNamespaceContext();
		String namespaceContextClassName = null;
		if (namespaceContext != null) {
			namespaceContextClassName = namespaceContext.getClass().getName();
		} else {
			namespaceContextClassName = "null";
		}
		System.out.println(
                "XPath.getNamespaceContext() = "
                + namespaceContextClassName);
		
		// SAX as data model
		FileInputStream saxStream = null;
		try {
			saxStream = new FileInputStream(xmlFile);
		} catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
			System.exit(1);
		}
		
		NodeList saxNodeList = null;
		try {
			saxNodeList = (NodeList)xpath.evaluate(xpathExpression,
					new InputSource(saxStream),
					XPathConstants.NODESET);
		} catch (XPathExpressionException xpathExpressionException) {
			xpathExpressionException.printStackTrace();
			System.exit(1);
		}
		
		dumpNode("SAX via InputSource", xmlFile, xpathExpression, saxNodeList);
	}
	
	static void dumpNode(String objectModel,
			String inputFile,
			String xpathExpression,
			NodeList nodeList) {
		
		System.out.println("Object model: " + objectModel + "created from: " + inputFile + "\n"
				+ "XPath expression: " + xpathExpression + "\n"
				+ "NodeList.getLength(): " + nodeList.getLength());
		
		// dump each Node's info
		for (int onNode = 0; onNode < nodeList.getLength(); onNode++) {
			
			Node node = nodeList.item(onNode);
			String nodeName = node.getNodeName();
			String nodeValue = node.getNodeValue();
			if (nodeValue == null) {
				nodeValue = "null";
			}
			String namespaceURI = node.getNamespaceURI();
			if (namespaceURI == null) {
				namespaceURI = "null";
			}
			String namespacePrefix = node.getPrefix();
			if (namespacePrefix == null) {
				namespacePrefix = "null";
			}
			String localName = node.getLocalName();
			if (localName == null) {
				localName = "null";
			}
			
			System.out.println("result #: " + onNode + "\n"
					+ "\tNode name: " + nodeName + "\n"
					+ "\tNode value: " + nodeValue + "\n"
					+ "\tNamespace URI: " + namespaceURI + "\n"
					+ "\tNamespace prefix: " + namespacePrefix + "\n"
					+ "\tLocal name: " + localName);
		}
		// dump each Node's info
	}
}
