/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.manufacturer;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.PropertyResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.xml.rpc.Stub;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import com.sun.wsi.scm.configuration.ConfigurationEndpointRole;
import com.sun.wsi.scm.configuration.ConfigurationFaultType;
import com.sun.wsi.scm.configuration.ConfigurationType;
import com.sun.wsi.scm.logging.LogEventRequestType;
import com.sun.wsi.scm.logging.LoggingFacilityLogPortType;
import com.sun.wsi.scm.logging.LoggingFacilityService_Impl;
import com.sun.wsi.scm.manufacturer.cb.CallbackFaultType;
import com.sun.wsi.scm.manufacturer.cb.CallbackHeaderType;
import com.sun.wsi.scm.manufacturer.po.SubmitPOFaultType_Type;
import com.sun.wsi.scm.manufacturer.sn.ShipmentNoticeType;
import com.sun.wsi.scm.util.ConfigurationValidator;
import com.sun.wsi.scm.util.DBConnectionPool;
import com.sun.wsi.scm.util.Localizer;
import com.sun.wsi.scm.util.StringConverter;
import com.sun.wsi.scm.util.WSIConstants;
import com.sun.xml.rpc.client.StubBase;
import com.sun.xml.rpc.client.http.HttpClientTransportFactory;

public class WarehouseCallbackPortTypeImpl
	implements WarehouseCallbackPortType, ServiceLifecycle, WSIConstants {

	LoggingFacilityLogPortType _logStub;
	Logger _logger = null;

	PropertyResourceBundle _rb = null;

	String _className = getClass().getName();
	DBConnectionPool _dbPool = null;

	Localizer _localizer = new Localizer();

	public void init(Object context) {
		ServletContext servletContext =
			((ServletEndpointContext) context).getServletContext();
		_logger = Logger.getLogger(LOGGER);

		LoggingFacilityService_Impl logService =
			new LoggingFacilityService_Impl();
		// Prepare the stream for manufacturer resource bundle
		InputStream is =
			servletContext.getResourceAsStream(MANUFACTURER_RESOURCES);

		try {
			_rb = new PropertyResourceBundle(is);
		} catch (IOException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		}

		_logStub = logService.getLoggingFacilityPort();
		if (_logger.isLoggable(Level.FINE))
			((StubBase) _logStub)._setTransportFactory(
				new HttpClientTransportFactory(System.out));

		_dbPool = DBConnectionPool.getInstance(context);
	}

	public void destroy() {
	}

	public boolean submitSN(
		ShipmentNoticeType shipmentNotice,
		ConfigurationType configurationHeader,
		CallbackHeaderType callbackHeader)
		throws ConfigurationFaultType, CallbackFaultType, RemoteException {

		_logger.entering(_className, SUBMIT_SN);

		// ** - Validate the configurationHeader
		Hashtable roles = null;
		roles = ConfigurationValidator.validateHeader(configurationHeader);

		// Set the endpoint address of the logging facility
		// from the configurationHeader
		((Stub) _logStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			(String) roles.get(ConfigurationEndpointRole.LoggingFacility));

		Vector setters = new Vector();
		setters.add(callbackHeader.getConversationID());
		ArrayList list = _dbPool.query(DBConnectionPool.CALLBACK, setters);
		String warehouse = _rb.getString("callback.warehousex");
		String manufacturer = _rb.getString("callback.manufacturerx");
		if ((list != null) && (list.size() > 0)) {
			warehouse = (String) list.get(0);
			manufacturer = (String) list.get(1);
		}

		LogEventRequestType logRequest = new LogEventRequestType();
		logRequest.setDemoUserID(configurationHeader.getUserId());
		String desc =
			_localizer.localize(
				_rb.getString("callback.service.id"),
				new String[] { warehouse, SUBMIT_SN });
		logRequest.setServiceID(desc);
		logRequest.setEventID("UC3-7-1");
		desc =
			_localizer.localize(
				_rb.getString("callback.receive.notice"),
				new String[] {
					warehouse,
					StringConverter.getManufacturerProductNumbersAsString(
						shipmentNotice.getItems()),
					manufacturer });
		logRequest.setEventDescription(desc);
		_logStub.logEvent(logRequest);
		_logger.log(Level.CONFIG, logRequest.getEventDescription());

		logRequest.setEventID("UC3-7-2");
		desc =
			_localizer.localize(
				_rb.getString("callback.replenish.stock"),
				new String[] {
					warehouse,
					StringConverter.getManufacturerProductNumbersAsString(
						shipmentNotice.getItems())});
		logRequest.setEventDescription(desc);
		_logStub.logEvent(logRequest);
		_logger.log(Level.CONFIG, logRequest.getEventDescription());

		_logger.exiting(_className, SUBMIT_SN);
		return true;
	}

	// Not implemented as agreed upon in WS-I Sample Applications Working Group
	public boolean errorPO(
		SubmitPOFaultType_Type type,
		CallbackHeaderType callbackHeader)
		throws CallbackFaultType {
		/*
		// ** - Validate the configurationHeader
		Hashtable roles = null;
		roles = ConfigurationValidator.validateHeader(configurationHeader);
		
		// Set the endpoint address of the logging facility
		// from the configurationHeader
		((Stub)_logStub)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, (String)roles.get(ConfigurationEndpointRole.LoggingFacility));
		
		LogEventRequestType logRequest = new LogEventRequestType();
		logRequest.setDemoUserID(configurationHeader.getUserId());
		logRequest.setServiceID(WAREHOUSEA_ERROR_PO);
		logRequest.setEventID("UC3-7-4");
		logRequest.setEventDescription("WarehouseA is unable to correlate the notification of a shipping error with a pending replenishment request");
		_logStub.logEvent(logRequest);
		if (_logger.isDebugEnabled())
			_logger.debug(logRequest.getEventDescription());
		*/

		return true;
	}
}
