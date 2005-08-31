/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.Stub;

import com.sun.wsi.scm.util.HTMLReporter;
import com.sun.wsi.scm.util.JAXRConstants;
import com.sun.wsi.scm.util.WSIConstants;
import com.sun.wsi.scm.util.XMLWriter;
import com.sun.xml.rpc.client.StubBase;
import com.sun.xml.rpc.client.http.HttpClientTransportFactory;

public class WSIQuery implements JAXRConstants, WSIConstants {

	static ConfigOptionsType _configOptions = null;
	static Logger _logger = Logger.getLogger(LOGGER);
	static PropertyResourceBundle _rb = null;
	Properties _props = null;

	public WSIQuery(Properties props) {
		this(
			props,
			null,
			new Boolean(props.getProperty("log.results")).booleanValue());
	}

	public WSIQuery(
		Properties props,
		InputStream rbStream,
		boolean logResults) {
		_props = props;

		// Prepare the stream for configuration resource bundle
		try {
			// Required for the demo
			if (rbStream == null)
				rbStream =
					new FileInputStream(
						new File(_props.getProperty("query.props")));
			_rb = new PropertyResourceBundle(rbStream);

			query(logResults);
		} catch (IOException ex) {
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} catch (ConfiguratorFailedFault ex) {
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} catch (Exception ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	public static void main(String[] args) {
		try {
			Properties props = System.getProperties();
			InputStream rbStream =
				new FileInputStream(new File(props.getProperty("query.props")));
			_rb = new PropertyResourceBundle(rbStream);

			if ((props.getProperty("log.home") != null)
				&& (props.getProperty("log.file") != null)) {
				_logger.log(
					Level.CONFIG,
					_rb.getString("config.results.logging.on"));
				props.setProperty("log.results", "true");
			} else {
				_logger.log(
					Level.CONFIG,
					_rb.getString("config.results.logging.off"));
				props.setProperty("log.results", "false");
			}

			WSIQuery query = new WSIQuery(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ConfigOptionsType getConfigurationOptions() {
		return _configOptions;
	}

	void query(boolean logResults)
		throws ConfiguratorFailedFault, RemoteException, IOException {

		_logger.log(Level.INFO, _rb.getString("config.query.all"));
		ConfiguratorPortType port =
			(new ConfiguratorService_Impl()).getConfiguratorPort();

		// Identifying the configurator endpoint to be used
		String endpoint = _props.getProperty("endpoint");
		if (endpoint == null) {
			_logger.log(
				Level.WARNING,
				_rb.getString("config.endpoint.notSepcified"),
				DEFAULT_CONFIGURATOR_ENDPOINT);
			endpoint = DEFAULT_CONFIGURATOR_ENDPOINT;
		}

		_logger.log(Level.INFO, _rb.getString("config.endpoint"), endpoint);
		((Stub) port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, endpoint);

		// Create SOAP message log files for FINE or a higher logging level
		if (_logger.isLoggable(Level.FINE) & logResults) {
			// appends to the existing log file
			FileOutputStream soapLog =
				new FileOutputStream(
					System.getProperty("log.home")
						+ System.getProperty("file.separator")
						+ System.getProperty("configurator.soap.msgs.file"),
					true);

			((StubBase) port)._setTransportFactory(
				new HttpClientTransportFactory(soapLog));
		}

		// default is to get all endpoints from UDDI business registry
		boolean refresh = true;
		if (_props.getProperty("refresh") != null)
			refresh =
				_props.getProperty("refresh").equals("true") ? true : false;
		_logger.log(
			Level.INFO,
			_rb.getString(
				refresh
					? "config.query.public.registry"
					: "config.query.cache"));

		_configOptions = port.getConfigurationOptions(refresh);
		if (logResults)
			logQueryResults(_configOptions);
	}

	private void logQueryResults(ConfigOptionsType configOptions)
		throws IOException {
		_logger.log(Level.CONFIG, _rb.getString("config.uddi.service.results"));
		if ((configOptions == null)
			|| (configOptions.getConfigOption() == null)) {
			_logger.log(
				Level.WARNING,
				_rb.getString("config.uddi.service.notFound"));
			return;
		}

		PrintStream xmlLog =
			new PrintStream(
				new FileOutputStream(
					System.getProperty("log.home")
						+ System.getProperty("file.separator")
						+ System.getProperty("log.file")));
		XMLWriter.setOutputStream(xmlLog);
		XMLWriter.writeStartTag("query");
		HTMLReporter htmlReporter = HTMLReporter.getInstance();
		htmlReporter.logEnvironment();

		ConfigOptionType[] configOption = configOptions.getConfigOption();
		_logger.log(
			Level.INFO,
			_rb.getString("config.uddi.service.found"),
			String.valueOf(configOption.length));

		for (int i = 0; i < configOption.length; i++) {
			_logger.log(
				Level.CONFIG,
				_rb.getString("config.uddi.service.info"),
				new String[] {
					configOption[i].getName(),
					configOption[i]
						.getConfigurationEndpoint()
						.get_value()
						.toString()});
			_logger.log(
				Level.FINE,
				_rb.getString("config.uddi.service.params"),
				configOption[i].getSelectionParms());
			_logger.log(
				Level.FINE,
				_rb.getString("config.uddi.service.role"),
				configOption[i]
					.getConfigurationEndpoint()
					.getRole()
					.getValue());

			// log XML output
			XMLWriter.writeStartTag("service");
			XMLWriter.writeContent("name", configOption[i].getName());
			XMLWriter.writeContent(
				"endpoint",
				configOption[i]
					.getConfigurationEndpoint()
					.get_value()
					.toString());
			XMLWriter.writeContent(
				"role",
				configOption[i]
					.getConfigurationEndpoint()
					.getRole()
					.getValue());
			XMLWriter.writeContent(
				"params",
				configOption[i].getSelectionParms());
			XMLWriter.writeEndTag("service");
		}
		XMLWriter.writeEndTag("query");
		htmlReporter.prepareHTMLReport();

		_logger.log(
			Level.INFO,
			_rb.getString("config.uddi.service.success"),
			new String[] {
				String.valueOf(configOption.length),
				System.getProperty("log.home"),
				System.getProperty("file.separator"),
				System.getProperty("log.file")});
	}
}
