/*
 * $Id$
 */

/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package com.sun.registry_server.util;


import com.sun.registry_server.common.UDDIConstants;

import java.io.FileNotFoundException;
import java.io.IOException ;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;


/** Simple command-line driven UDDI client. This class takes a UDDI request as
 * input (specified as an XML file), sends the request to a URL and prints the
 * response to stdout (as unformatted XML).
 * @author Jeff.Suttor@Sun.com
 * @version 1.0
 */
public class CLIClient {

  public static void main(String[] args) {

    // parse command line parms
    String registry = null;
    String filename = null;
    for (int i = 0; i < args.length; i++) {
      if ("-h".equals(args[i]))
	usage();
      else if ("-i".equals(args[i]))
	filename = args[++i];
      else if ("-r".equals(args[i]))
	registry = args[++i];
    }
    if (filename == null || registry == null) {
      usage();
    }


    // build the DOM representation of a SOAP <Envelope> with the UDDI
    // request message as the <Body>
    DOMSource requestDomSource = null;
    try {
      // create a document builder
      // Explicitly using package renamed Xerce Factory class
      DocumentBuilderFactory documentBuilderFactory =
        new com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl();
	// DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      documentBuilderFactory.setValidating(false);
      DocumentBuilder documentBuilder =
	documentBuilderFactory.newDocumentBuilder();
      
      // construct SOAP document with <Body> set to the UDDI message

      // build a document in a standard way, e.g. use a DOM implementation
      DOMImplementation domImplementation =
	documentBuilder.getDOMImplementation();
      DocumentType documentType =
	domImplementation.createDocumentType("qualifiedName",
					     "publicID",
					     "systemID");
      Document soapDocument =
	domImplementation.createDocument("namespaceURI",
					 "qualifiedName",
					 documentType);
      // drop the document element node so we have an empty document
      Element tmpElement = soapDocument.getDocumentElement();
      Node tmpNode = soapDocument.removeChild(tmpElement);
      // soapDocument now empty and ready to populate with SOAP message

      Element envelope = soapDocument.createElementNS(
	"http://schemas.xmlsoap.org/soap/envelope/",
	"soap-env:Envelope");

      // need to create a Namespace declaration for the SOAP Namespace Prefix
      envelope.setAttributeNS(UDDIConstants.NAMESPACE_URI_XMLNS,
			      "xmlns:" + UDDIConstants.DEFAULT_SOAP_PREFIX,
			      UDDIConstants.NAMESPACE_URI_SOAP);


      Element header = soapDocument.createElementNS(
	"http://schemas.xmlsoap.org/soap/envelope/",
	"soap-env:Header");

      Element body = soapDocument.createElementNS(
	"http://schemas.xmlsoap.org/soap/envelope/",
	"soap-env:Body");

      // no headers to add, attach <Header> to <Envelope>
      envelope.appendChild(header);

      // attach UDDI message to <Body> and then attach <Body> to <Envelope>

      // uddi message in a file -> Document
      Document requestDocument = documentBuilder.parse(filename);
      Element requestRoot = requestDocument.getDocumentElement();
      // import from UDDI document to SOAP document
      Node requestNode = soapDocument.importNode(requestRoot, true);
      body.appendChild(requestNode);
      // done with <Body>, attach to <Envelope>
      envelope.appendChild(body);

      // done with <Envelope>, attach to the document
      soapDocument.appendChild(envelope);

      // methods want to work with a DOMSource v. DOM
      requestDomSource = new DOMSource(soapDocument);
      
    } catch (ParserConfigurationException exception) {
      throw new RuntimeException("ParserConfigurationException: " +
				 exception.getMessage());
    } catch (FileNotFoundException exception) {
      throw new RuntimeException("FileNotFoundException: " +
				 exception.getMessage());
    } catch (IOException exception) {
      throw new RuntimeException("IOException: " +
				 exception.getMessage());
    } catch (SAXException exception) {
      throw new RuntimeException("SAXException: " +
				 exception.getMessage());
    }

    // create the SOAP message that will be the request
    SOAPMessage request = null;
    try {
      // create a message that will serve as the request
      MessageFactory messageFactory = MessageFactory.newInstance();  
      request = messageFactory.createMessage();

      // use the request represented as a DOM to build a SOAP message
      SOAPPart soapPart = request.getSOAPPart();
      soapPart.setContent(requestDomSource);
      request.saveChanges();
    } catch (SOAPException soapException) {
      throw new RuntimeException("SOAPException: " +
				 soapException.getMessage());
    }

    // output request for user to see
    System.out.println("\nCLIClient: request = ");
    try {
      request.writeTo(System.out);
    } catch (Exception exception) {
      throw new RuntimeException("Exception in writing request: " +
				 exception.getMessage());
    }
    System.out.println("\n--------");

    // send the SOAP request and get a SOAP response
    SOAPMessage response = null;
    SOAPConnection soapConnection = null;
    try {
      // need a SOAPConnection
      SOAPConnectionFactory soapConnectionFactory =
	SOAPConnectionFactory.newInstance();
      soapConnection = soapConnectionFactory.createConnection();

      // send the request, get the response
      response = soapConnection.call(request, registry);
    } catch (SOAPException soapException) {
      throw new RuntimeException("SOAPException: " +
				 soapException.getMessage());
    } finally {
      try {
	if (soapConnection != null) {
	  soapConnection.close();
	}
      } catch (Exception exception) {
	// ignore
      }
    }

    // output the response for the user to see
    System.out.println("\nCLIClient: response = ");
    try {
      response.writeTo(System.out);
    } catch (Exception exception) {
      throw new RuntimeException("Exception in writing response: " +
				 exception.getMessage());
    }
    System.out.println("\n--------");
 
    System.exit(0);
  }

  //--------------------------------------------------------------------------
  public static void usage() {

    System.out.println("CLIClient use:");
    System.out.println("java com.sun.registry_server.util.CLIClient " +
		       "[-h] " +
		       "-i request.xml " +
		       "-r URL");
    System.out.println("request.xml should be a path to a valid XML file " +
		       "containing a UDDI request.");
    System.out.println("URL is the HTTP-POST accessible URL of the UDDI " +
		       "Registry Server.");

    System.exit(0);
  }
}
