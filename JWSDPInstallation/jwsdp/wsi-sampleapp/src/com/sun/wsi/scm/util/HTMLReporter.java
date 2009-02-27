/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class HTMLReporter implements WSIConstants {
	private static HTMLReporter _htmlReporter = null;
	private static PropertyResourceBundle _rb = null;
	private static Logger _logger = null;

	private HTMLReporter() {
		try {
			// Prepare the stream for retailer client resource bundle
			FileInputStream fis =
				new FileInputStream(System.getProperty("reporter.props"));
			_rb = new PropertyResourceBundle(fis);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String args[]) {
		HTMLReporter.getInstance().prepareHTMLReport();
	}

	public static HTMLReporter getInstance() {
		if (_htmlReporter == null)
			_htmlReporter = new HTMLReporter();
		_logger = Logger.getLogger(LOGGER);

		return _htmlReporter;
	}

	public void logEnvironment() {
		XMLWriter.writeStartTag("environment");
		SimpleDateFormat sdf =
			new SimpleDateFormat("EEE, dd MMM yyyy, HH:mm:ss Z");
		String date = sdf.format(Calendar.getInstance().getTime());

		XMLWriter.writeContent("timestamp", date);
		XMLWriter.writeContent(
			"runtime-name",
			"Java(TM) 2 Runtime Environment, Standard Edition");
		XMLWriter.writeContent(
			"runtime-version",
			System.getProperty("java.version"));
		XMLWriter.writeContent("os-name", System.getProperty("os.name"));
		XMLWriter.writeContent("os-version", System.getProperty("os.version"));
		XMLWriter.writeEndTag("environment");

		_logger.log(Level.FINE, _rb.getString("client.env"));
		_logger.log(
			Level.FINE,
			_rb.getString("client.env.timestamp"),
			date.toString());
		_logger.log(Level.FINE, _rb.getString("client.env.runtime.name"));
		_logger.log(
			Level.FINE,
			_rb.getString("client.env.runtime.version"),
			System.getProperty("java.version"));
		_logger.log(
			Level.FINE,
			_rb.getString("client.env.os.name"),
			System.getProperty("os.name"));
		_logger.log(
			Level.FINE,
			_rb.getString("client.env.os.version"),
			System.getProperty("os.version"));
	}

	public void prepareHTMLReport() {
		try {
			String xmlFileName = System.getProperty("log.file");
			String htmlFileName =
				System.getProperty("log.home")
					+ System.getProperty("file.separator");

			if (!xmlFileName.endsWith(".xml"))
				htmlFileName += xmlFileName + HTML_FILE_EXTENSION;
			else
				htmlFileName
					+= xmlFileName.substring(0, xmlFileName.lastIndexOf(".xml"))
					+ HTML_FILE_EXTENSION;

			_logger.log(
				Level.INFO,
				_rb.getString("client.htmlReport"),
				htmlFileName);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();

			Document document =
				builder.parse(
					new FileInputStream(
						System.getProperty("log.home")
							+ System.getProperty("file.separator")
							+ System.getProperty("log.file")));
			StreamSource xslSource =
				new StreamSource(new File(System.getProperty("html.style")));

			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(xslSource);
			StreamResult result =
				new StreamResult(new FileOutputStream(htmlFileName));
			DOMSource xmlSource = new DOMSource(document);
			transformer.transform(xmlSource, result);
			_logger.log(Level.INFO, _rb.getString("client.htmlReport.done"));
		} catch (Throwable t) {
			t.printStackTrace();
			_logger.log(Level.SEVERE, t.getMessage(), t);
		}
	}
}
