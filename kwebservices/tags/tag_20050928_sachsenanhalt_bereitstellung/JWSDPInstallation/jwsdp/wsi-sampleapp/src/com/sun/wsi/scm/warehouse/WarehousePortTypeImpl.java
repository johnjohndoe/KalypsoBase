/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.warehouse;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PropertyResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.rpc.Stub;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.server.ServletEndpointContext;

import com.sun.wsi.scm.configuration.ConfigurationEndpointRole;
import com.sun.wsi.scm.configuration.ConfigurationFaultType;
import com.sun.wsi.scm.configuration.ConfigurationType;
import com.sun.wsi.scm.logging.LogEventRequestType;
import com.sun.wsi.scm.logging.LoggingFacilityLogPortType;
import com.sun.wsi.scm.logging.LoggingFacilityService_Impl;
import com.sun.wsi.scm.manufacturer.ManufacturerPortType;
import com.sun.wsi.scm.manufacturer.ManufacturerService_Impl;
import com.sun.wsi.scm.manufacturer.cb.StartHeaderType;
import com.sun.wsi.scm.manufacturer.po.PurchOrdType;
import com.sun.wsi.scm.manufacturer.po.SubmitPOFaultType_Exception;
import com.sun.wsi.scm.util.ConfigurationValidator;
import com.sun.wsi.scm.util.DBConnectionPool;
import com.sun.wsi.scm.util.Localizer;
import com.sun.wsi.scm.util.StringConverter;
import com.sun.wsi.scm.util.URLFormatter;
import com.sun.wsi.scm.util.WSIConstants;
import com.sun.xml.rpc.client.StubBase;
import com.sun.xml.rpc.client.http.HttpClientTransportFactory;

public class WarehousePortTypeImpl implements WSIConstants {
	private Hashtable _dataHash = null;
	private ConfigurationType _configHeader = null;

	private LoggingFacilityLogPortType _logStub = null;

	private Logger _logger = null;
	private Localizer _localizer = null;
	private PropertyResourceBundle _rb = null;

	private DBConnectionPool _dbPool = null;
	private ServletEndpointContext _servletEndpointContext = null;

	// to be populated by sub class
	protected String _className = null;
	protected String _warehouseName = null;
	protected String _warehouseRole = null;
	protected int[][] _warehouseData = null;

	public void init(Object context) {
		_servletEndpointContext = (ServletEndpointContext) context;
		ServletContext servletContext =
			_servletEndpointContext.getServletContext();

		_logger = Logger.getLogger(LOGGER);
		_logger.entering(_className, INIT);
		_localizer = new Localizer();

		// Prepare the stream for warehouse resource bundle
		InputStream is = servletContext.getResourceAsStream(WAREHOUSE_RESOURCES);

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

		_dataHash = new Hashtable();

		for (int i = 0; i < _warehouseData.length; i++) {
			Data data = new Data();
			data.setStock(_warehouseData[i][0]);
			data.setMinimum(_warehouseData[i][1]);
			data.setMaximum(_warehouseData[i][2]);

			_dataHash.put(new BigInteger(String.valueOf(PRODUCT_ID[i])), data);
		}

		_dbPool = DBConnectionPool.getInstance(context);
		_logger.exiting(_className, INIT);
	}

	public ItemShippingStatusList shipGoods(
		ItemList itemList,
		String customer,
		ConfigurationType configHeader)
		throws ConfigurationFaultType, RemoteException {
		String methodName = _warehouseName + "." + SHIP_GOODS;
		_logger.entering(_className, methodName);
		this._configHeader = configHeader;
		ItemShippingStatus[] itemShippingStatusArray =
			new ItemShippingStatus[itemList.getItem().length];

		// ** - Validate the configurationHeader
		Hashtable roles = null;
		roles = ConfigurationValidator.validateHeader(_configHeader);

		// Set the endpoint address of the logging facility
		// from the configurationHeader
		((Stub) _logStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			(String) roles.get(ConfigurationEndpointRole.LoggingFacility));

		LogEventRequestType logRequest = new LogEventRequestType();
		String demoUserId = _configHeader.getUserId();
		logRequest.setDemoUserID(demoUserId);
		logRequest.setServiceID(methodName);
		logRequest.setEventID("UC2-2-1");
		String desc =
			_localizer.localize(
				_rb.getString("warehouse.ability"),
				new String[] {
					_warehouseName,
					StringConverter.getWarehouseProductNumbersAsString(
						itemList)});
		logRequest.setEventDescription(desc);
		_logStub.logEvent(logRequest);
		_logger.log(Level.CONFIG, logRequest.getEventDescription());

		// ** - Set the status in the response. Replenish the stock if
		// the stock level falls below the minimum
		Vector stockRefillList = new Vector();

		// Segregate the shipped and unshipped items for logging
		ArrayList shippedItems = new ArrayList();
		ArrayList unshippedItems = new ArrayList();

		Item[] items = itemList.getItem();
		for (int i = 0; i < items.length; i++) {
			if (!_dataHash.containsKey(items[i].getProductNumber())) {
				_logger.log(
					Level.SEVERE,
					_rb.getString("warehouse.invalid.product"),
					new Object[] {
						_warehouseName,
						items[i].getProductNumber()});
				continue;
			}

			Data data = (Data) _dataHash.get(items[i].getProductNumber());

			itemShippingStatusArray[i] = new ItemShippingStatus();
			itemShippingStatusArray[i].setProductNumber(
				items[i].getProductNumber());

			// If requested quantity is more than the quantity in stock or
			// maximum quantity in stock, then the item cannot be shipped
			if ((items[i].getQuantity() > data.getMaximum())
				|| (items[i].getQuantity() > data.getStock())) {
				_logger.log(
					Level.FINEST,
					_rb.getString("warehouse.quantity.excess"));
				itemShippingStatusArray[i].setStatus(false);
				unshippedItems.add(items[i]);
				continue;
			} else {
				_logger.log(
					Level.FINEST,
					_rb.getString("warehouse.quantity.inStock"));
				itemShippingStatusArray[i].setStatus(true);
				shippedItems.add(items[i]);
			}

			// Replenish stock if the inventory level has fallen 
			// below its minimum level
			if ((data.getStock() - items[i].getQuantity())
				<= data.getMinimum()) {
				_logger.log(
					Level.FINEST,
					_rb.getString("warehouse.quantity.replenish"));
				stockRefillList.add(items[i]);
			}
		}

		ItemList shippedItemList = new ItemList();
		Item[] shippedItemsArray = (Item[]) shippedItems.toArray(new Item[0]);
		shippedItemList.setItem(shippedItemsArray);

		ItemList unshippedItemList = new ItemList();
		Item[] unshippedItemsArray =
			(Item[]) unshippedItems.toArray(new Item[0]);
		unshippedItemList.setItem(unshippedItemsArray);

		logRequest.setEventID("UC2-2-2");
		desc =
			_localizer.localize(
				_rb.getString("warehouse.ship.unship"),
				new String[] {
					_warehouseName,
					StringConverter.getWarehouseProductNumbersAsString(
						shippedItemList),
					StringConverter.getWarehouseProductNumbersAsString(
						unshippedItemList)});
		logRequest.setEventDescription(desc);
		_logStub.logEvent(logRequest);
		_logger.log(Level.CONFIG, logRequest.getEventDescription());

		try {
			replenishStock(stockRefillList, customer, roles, demoUserId);
		} catch (SubmitPOFaultType_Exception ex) {
			_logger.log(Level.WARNING, ex.getMessage(), ex);
		}

		ItemShippingStatusList itemShippingStatusList =
			new ItemShippingStatusList();
		itemShippingStatusList.setItemStatus(itemShippingStatusArray);

		return itemShippingStatusList;
	}

	/**
	* Batch process the stock replenishing from ManufacturerA,
	* ManufacturerB and ManufacturerC
	*/
	private void replenishStock(
		Vector stockRefillList,
		String customer,
		Hashtable roles,
		String demoUserId)
		throws SubmitPOFaultType_Exception, ConfigurationFaultType, RemoteException {
		_logger.entering(_className, REPLENISH_STOCK);

		ManufacturerService_Impl manufacturerService =
			new ManufacturerService_Impl();

		// Configure manufacturerA port
		ManufacturerPortType manufacturerAStub =
			manufacturerService.getManufacturerAPort();
		((Stub) manufacturerAStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			(String) roles.get(ConfigurationEndpointRole.ManufacturerA));
		if (_logger.isLoggable(Level.FINE))
			((StubBase) manufacturerAStub)._setTransportFactory(
				new HttpClientTransportFactory(System.out));

		// Configure manufacturerB port
		ManufacturerPortType manufacturerBStub =
			manufacturerService.getManufacturerBPort();
		((Stub) manufacturerBStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			(String) roles.get(ConfigurationEndpointRole.ManufacturerB));
		if (_logger.isLoggable(Level.FINE))
			((StubBase) manufacturerBStub)._setTransportFactory(
				new HttpClientTransportFactory(System.out));

		// Configure manufacturerC port
		ManufacturerPortType manufacturerCStub =
			manufacturerService.getManufacturerCPort();
		((Stub) manufacturerCStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			(String) roles.get(ConfigurationEndpointRole.ManufacturerC));

		if (_logger.isLoggable(Level.FINE))
			((StubBase) manufacturerCStub)._setTransportFactory(
				new HttpClientTransportFactory(System.out));

		Vector mfrAItems = new Vector();
		Vector mfrBItems = new Vector();
		Vector mfrCItems = new Vector();

		// Classify items ordered per manufacturer in different buckets
		Iterator iter = stockRefillList.iterator();
		while (iter.hasNext()) {
			Item item = (Item) iter.next();
			int productNumber = item.getProductNumber().intValue();

			if ((productNumber == 605001)
				|| (productNumber == 605004)
				|| (productNumber == 605007))
				mfrAItems.add(item);
			else if (
				(productNumber == 605002)
					|| (productNumber == 605005)
					|| (productNumber == 605008))
				mfrBItems.add(item);
			else if (
				(productNumber == 605003)
					|| (productNumber == 605006)
					|| (productNumber == 605009))
				mfrCItems.add(item);
			else
				_logger.log(
					Level.WARNING,
					_rb.getString("warehouse.invalid.product"),
					new String[] {
						_warehouseName,
						String.valueOf(productNumber)});
		}

		refillFromManufacturer(
			manufacturerAStub,
			mfrAItems,
			customer,
			ConfigurationEndpointRole._ManufacturerAString,
			demoUserId);
		refillFromManufacturer(
			manufacturerBStub,
			mfrBItems,
			customer,
			ConfigurationEndpointRole._ManufacturerBString,
			demoUserId);
		refillFromManufacturer(
			manufacturerCStub,
			mfrCItems,
			customer,
			ConfigurationEndpointRole._ManufacturerCString,
			demoUserId);

		_logger.exiting(_className, REPLENISH_STOCK);
	}

	// Stock refill from manufacturer
	void refillFromManufacturer(
		ManufacturerPortType manufacturerStub,
		Vector items,
		String customer,
		String role,
		String demoUserId)
		throws SubmitPOFaultType_Exception, ConfigurationFaultType, RemoteException {
		if (items.isEmpty())
			return;

		_logger.log(
			Level.FINEST,
			_rb.getString("warehouse.stock.replenish"),
			new String[] { _warehouseName, role });
		Iterator iter = items.iterator();

		ArrayList al = new ArrayList();

		// Convert Warehouse.Item to ManufacturerPO.Item
		while (iter.hasNext()) {
			Item item = (Item) iter.next(); // Warehouse.Item
			com.sun.wsi.scm.manufacturer.po.Item poItem =
				new com.sun.wsi.scm.manufacturer.po.Item();
			poItem.setID(item.getProductNumber());
			Data data = (Data) _dataHash.get(item.getProductNumber());
			poItem.setQty(
				data.getMaximum() - (data.getStock() - item.getQuantity()));
			poItem.setPrice(PO_ITEM_PRICE);
			al.add(poItem);
		}

		com.sun.wsi.scm.manufacturer.po.Item[] itemArray =
			(com.sun.wsi.scm.manufacturer.po.Item[]) al.toArray(
				new com.sun.wsi.scm.manufacturer.po.Item[0]);
		com.sun.wsi.scm.manufacturer.po.ItemList poItemList =
			new com.sun.wsi.scm.manufacturer.po.ItemList();
		poItemList.setItem(itemArray);

		PurchOrdType po = new PurchOrdType();

		// set a constant number since this is not 
		// used for correlation anywhere
		po.setOrderNum(ORDER_NUMBER);
		po.setCustomerRef(customer);
		po.setItems(poItemList);
		// Set a constant price
		po.setTotal(PO_TOTAL_PRICE);

		StartHeaderType startHeader = new StartHeaderType();
		String conversationID = demoUserId + "." + role;
		Vector setters = new Vector();
		setters.add(conversationID);
		setters.add(_warehouseRole);
		setters.add(role);
		_dbPool.insert(DBConnectionPool.CALLBACK, setters);

		startHeader.setConversationID(conversationID);
		MessageContext messageContext =
			_servletEndpointContext.getMessageContext();
		HttpServletRequest request =
			(HttpServletRequest) messageContext.getProperty(
				com
					.sun
					.xml
					.rpc
					.server
					.http
					.MessageContextProperties
					.HTTP_SERVLET_REQUEST);
		startHeader.setCallbackLocation(URLFormatter.getCallbackURL(request));

		// Cast the manufacturer to the appropriate stub based upon the role
		boolean response = false;
		if (role.equals(ConfigurationEndpointRole._ManufacturerAString)) {
			response =
				manufacturerStub.submitPO(po, _configHeader, startHeader);
		} else if (
			role.equals(ConfigurationEndpointRole._ManufacturerBString)) {
			response =
				manufacturerStub.submitPO(po, _configHeader, startHeader);
		} else if (
			role.equals(ConfigurationEndpointRole._ManufacturerCString)) {
			response =
				manufacturerStub.submitPO(po, _configHeader, startHeader);
		} else {
			_logger.log(
				Level.WARNING,
				_rb.getString("warehouse.invalid.role"),
				role);
		}
	}

	public void destroy() {
	}
}
