/*
 * $Id$
 */

/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.registry_server.util;

import java.io.*;

// JAXP packages
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


public class Validate {

    /** Constants used for JAXP 1.2 */
    static final String JAXP_SCHEMA_LANGUAGE =
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";
    static final String JAXP_SCHEMA_SOURCE =
        "http://java.sun.com/xml/jaxp/properties/schemaSource";

    public static void main(String args[]) throws Exception {

        // parse command line parms
        String schema = null;
        String instance = null;
        for (int i = 0; i < args.length; i++) {
            if ("-h".equals(args[i])) {
                usage();
                System.exit(0);
            } else if ("-s".equals(args[i])) {
                schema = args[++i];
            } else if ("-i".equals(args[i])) {
                instance = args[++i];
            }
        }
        if (schema == null || instance == null || instance.equals("${instance}")) {
            usage();
            System.exit(1);
        }
        System.out.println("Validate: using schema \"" + schema   + "\" " +
                           "to validate instance \"" + instance + "\"");

        // Create a JAXP SAXParserFactory and configure it
        // Explicitly using package renamed Xerce Factory class
        SAXParserFactory spf = new com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl();
                               // SAXParserFactory.newInstance();

        // Set namespaceAware to true to get a parser that corresponds to
        // the default SAX2 namespace feature setting.  This is necessary
        // because the default value from JAXP 1.0 was defined to be false.
        spf.setNamespaceAware(true);

        // Validation part 1: set whether validation is on
        spf.setValidating(true);

        // Create a JAXP SAXParser
        SAXParser saxParser = spf.newSAXParser();

        // Validation part 2a: set the schema language if necessary
	try {
	  saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
	} catch (SAXNotRecognizedException x) {
	  // This can happen if the parser does not support JAXP 1.2
	  System.err.println(
	    "Error: JAXP SAXParser property not recognized: "
            + JAXP_SCHEMA_LANGUAGE);
	  System.err.println(
            "Check to see if parser conforms to JAXP 1.2 spec.");
	  System.exit(1);
	}

        // Validation part 2b: Set the schema source, if any.  See the JAXP
        // 1.2 maintenance update specification for more complex usages of
        // this feature.
	saxParser.setProperty(JAXP_SCHEMA_SOURCE, schema);

        // Get the encapsulated SAX XMLReader
        XMLReader xmlReader = saxParser.getXMLReader();

        // Do not set a ContentHandler for the XMLReader since we just want to
        // validate

        // Set an ErrorHandler before parsing
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        // Tell the XMLReader to parse the XML document
        xmlReader.parse(new File(instance).toURL().toString());
    }

    // Error handler to report errors and warnings
    private static class MyErrorHandler implements ErrorHandler {
        /** Error handler output goes here */
        private PrintStream out;

        MyErrorHandler(PrintStream out) {
            this.out = out;
        }

        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                " Line=" + spe.getLineNumber() +
                " Column=" + spe.getColumnNumber() +
                ": " + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }
        
        public void error(SAXParseException spe) throws SAXException {
            out.println("Error: " + getParseExceptionInfo(spe));
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }

    //---------------------------------------------------------------------------
    private static void usage() {

        System.out.println("usage: java com.sun.registry_server.util.Validate " +
                           "-s schemaFile " +
                           "-i instanceFile");
    }
}
