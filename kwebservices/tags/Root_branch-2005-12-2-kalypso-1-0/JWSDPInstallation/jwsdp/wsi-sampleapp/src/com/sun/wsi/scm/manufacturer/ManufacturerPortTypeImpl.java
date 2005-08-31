/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.manufacturer;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.xml.rpc.Stub;
import javax.xml.rpc.server.ServletEndpointContext;

import com.sun.wsi.scm.configuration.ConfigurationEndpointRole;
import com.sun.wsi.scm.configuration.ConfigurationFaultType;
import com.sun.wsi.scm.configuration.ConfigurationType;
import com.sun.wsi.scm.logging.LogEventRequestType;
import com.sun.wsi.scm.logging.LoggingFacilityLogPortType;
import com.sun.wsi.scm.logging.LoggingFacilityService_Impl;
import com.sun.wsi.scm.manufacturer.cb.StartHeaderType;
import com.sun.wsi.scm.manufacturer.po.Item;
import com.sun.wsi.scm.manufacturer.po.ItemList;
import com.sun.wsi.scm.manufacturer.po.PurchOrdType;
import com.sun.wsi.scm.manufacturer.po.SubmitPOFaultType_Exception;
import com.sun.wsi.scm.util.ConfigurationValidator;
import com.sun.wsi.scm.util.Localizer;
import com.sun.wsi.scm.util.WSIConstants;
import com.sun.xml.rpc.client.StubBase;
import com.sun.xml.rpc.client.http.HttpClientTransportFactory;

public abstract class ManufacturerPortTypeImpl implements WSIConstants {

	private LoggingFacilityLogPortType _logStub = null;

	private Logger _logger = null;
	private Localizer _localizer = null;
	private PropertyResourceBundle _rb = null;

	// to be populated by sub class
	protected String _className = null;
	protected String _mfrName = null;
	protected int[] _mfrProducts = null;
	protected int[][] _mfrData = null;
	protected ConfigurationEndpointRole _mfrRole = null;

	protected void init(Object context) {
		ServletContext servletContext =
			((ServletEndpointContext) context).getServletContext();

		_logger = Logger.getLogger(LOGGER);
		_logger.entering(_className, INIT);
		_localizer = new Localizer();

		// Prepare the stream for manufacturer resource bundle
		InputStream is = servletContext.getResourceAsStream(MANUFACTURER_RESOURCES);

		try {
			_rb = new PropertyResourceBundle(is);
		} catch (IOException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		}

		LoggingFacilityService_Impl logService =
			new LoggingFacilityService_Impl();
		_logStub = logService.getLoggingFacilityPort();

		// Create SOAP message log files for FINE or higher logging level
		if (_logger.isLoggable(Level.FINE))
			((StubBase) _logStub)._setTransportFactory(
				new HttpClientTransportFactory(System.out));

		_logger.exiting(_className, INIT);
	}

	public boolean submitPO(
		PurchOrdType purchaseOrder,
		ConfigurationType configurationHeader,
		StartHeaderType startHeader)
		throws ConfigurationFaultType, SubmitPOFaultType_Exception, RemoteException {

		String methodName = _mfrName + "." + SUBMIT_PO;

		_logger.entering(_className, methodName);
		// ** - Validate the configurationHeader
		Hashtable roles = null;
		roles = ConfigurationValidator.validateHeader(configurationHeader);

		// Set the endpoint address of the logging facility
		// from the configurationHeader
		((Stub) _logStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			(String) roles.get(ConfigurationEndpointRole.LoggingFacility));

		LogEventRequestType logRequest = new LogEventRequestType();
		logRequest.setDemoUserID(configurationHeader.getUserId());
		logRequest.setServiceID(methodName);
		logRequest.setEventID("UC3-3");
		ItemList itemList = purchaseOrder.getItems();
		Item[] items = itemList.getItem();

		String desc = "";
		for (int i = 0; i < items.length; i++) {
			desc += items[i].getID();
			desc += (i == items.length - 1) ? "" : ", ";
		}

		desc =
			_localizer.localize(
				_rb.getString("mfr.stock.replenish"),
				new String[] { _mfrName, desc });
		logRequest.setEventDescription(desc);
		_logger.log(Level.CONFIG, logRequest.getEventDescription());
		_logStub.logEvent(logRequest);

		// Validate the PO
		POValidator.validateOrder(
			purchaseOrder.getItems().getItem(),
			_mfrProducts,
			_mfrData);

		HeaderValidator.validateStartHeader(startHeader, _mfrRole, _rb);

		// Spawn a new thread for callback invocation
		_logger.log(
			Level.FINEST,
			_rb.getString("mfr.callback.invoke"),
			_mfrName);
		CallbackInvoker callback =
			new CallbackInvoker(
				purchaseOrder,
				configurationHeader,
				startHeader,
				_logger);
		Thread thread = new Thread(callback);
		thread.start();

		Hashtable qtyHash = new Hashtable();
		for (int i = 0; i < _mfrData.length; i++)
			qtyHash.put(
				new Integer(_mfrProducts[i]),
				new Integer(_mfrData[i][STOCK]));

		for (int i = 0; i < items.length; i++) {
			Integer stockQty =
				(Integer) qtyHash.get(new Integer(items[i].getID().intValue()));

			if (items[i].getQty() > stockQty.intValue()) {
				logRequest.setEventID("UC5-5");
				desc =
					_localizer.localize(
						_rb.getString("mfr.stock.additional"),
						new String[] {
							_mfrName,
							String.valueOf(items[i].getID().intValue()),
							String.valueOf(items[i].getQty())});
			} else {
				logRequest.setEventID("UC4-1");
				desc =
					_localizer.localize(
						_rb.getString("mfr.stock.inventory"),
						new String[] {
							_mfrName,
							String.valueOf(items[i].getID().intValue())});
			}

			logRequest.setEventDescription(desc);
			_logger.log(Level.CONFIG, logRequest.getEventDescription());
			_logStub.logEvent(logRequest);
		}

		_logger.exiting(_className, methodName);

		// If everything looks good, return a correct response
		return true;
	}

	public void destroy() {
	}
}
