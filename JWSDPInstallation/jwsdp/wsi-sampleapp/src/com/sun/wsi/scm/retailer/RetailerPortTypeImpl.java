/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.retailer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.PropertyResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

import com.sun.wsi.scm.configuration.ConfigurationEndpointRole;
import com.sun.wsi.scm.configuration.ConfigurationFaultType;
import com.sun.wsi.scm.configuration.ConfigurationType;
import com.sun.wsi.scm.logging.LogEventRequestType;
import com.sun.wsi.scm.logging.LoggingFacilityLogPortType;
import com.sun.wsi.scm.logging.LoggingFacilityService_Impl;
import com.sun.wsi.scm.util.ConfigurationValidator;
import com.sun.wsi.scm.util.Localizer;
import com.sun.wsi.scm.util.WSIConstants;
import com.sun.wsi.scm.warehouse.Item;
import com.sun.wsi.scm.warehouse.ItemList;
import com.sun.wsi.scm.warehouse.ItemShippingStatus;
import com.sun.wsi.scm.warehouse.ItemShippingStatusList;
import com.sun.wsi.scm.warehouse.WarehouseService_Impl;
import com.sun.wsi.scm.warehouse.WarehouseShipmentsPortType;
import com.sun.xml.rpc.client.StubBase;
import com.sun.xml.rpc.client.http.HttpClientTransportFactory;

public class RetailerPortTypeImpl
	implements RetailerPortType, ServiceLifecycle, WSIConstants {
	LoggingFacilityLogPortType _logStub = null;

	String _className = getClass().getName();

	Logger _logger = null;
	Localizer _localizer = null;
	PropertyResourceBundle _rb = null;

	public void init(Object context) {
		ServletContext servletContext =
			((ServletEndpointContext) context).getServletContext();

		_logger = Logger.getLogger(LOGGER);
		_logger.entering(_className, INIT);
		_localizer = new Localizer();

		// Prepare the stream for retailer resource bundle
		InputStream is = servletContext.getResourceAsStream(RETAILER_RESOURCES);

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

	public void destroy() {
	}

	public CatalogType getCatalog() {
		_logger.entering(_className, GET_CATALOG);

		CatalogType catalog = new CatalogType();

		CatalogItem[] catalogItems = new CatalogItem[10];
		for (int i = 0; i < 10; i++) {
			catalogItems[i] = new CatalogItem();
			catalogItems[i].setProductNumber(
				new BigInteger(
					_rb.getString(
						"retailer.catalogItem." + i + ".productNumber")));
			catalogItems[i].setName(
				_rb.getString("retailer.catalogItem." + i + ".name"));
			catalogItems[i].setBrand(
				_rb.getString("retailer.catalogItem." + i + ".brand"));
			catalogItems[i].setDescription(
				_rb.getString("retailer.catalogItem." + i + ".description"));
			catalogItems[i].setCategory(
				_rb.getString("retailer.catalogItem." + i + ".category"));
			catalogItems[i].setPrice(
				(new BigDecimal(_rb
					.getString("retailer.catalogItem." + i + ".price"))
					.setScale(2, BigDecimal.ROUND_HALF_UP)));

			_logger.log(
				Level.INFO,
				_rb.getString("retailer.catalogItem.thProduct"),
				String.valueOf(i + 1));
			_logger.log(
				Level.CONFIG,
				_rb.getString("retailer.catalogItem.productNumber"),
				String.valueOf(catalogItems[i].getProductNumber()));
			_logger.log(
				Level.CONFIG,
				_rb.getString("retailer.catalogItem.name"),
				String.valueOf(catalogItems[i].getName()));
			_logger.log(
				Level.CONFIG,
				_rb.getString("retailer.catalogItem.description"),
				String.valueOf(catalogItems[i].getDescription()));
			_logger.log(
				Level.CONFIG,
				_rb.getString("retailer.catalogItem.brand"),
				String.valueOf(catalogItems[i].getBrand()));
			_logger.log(
				Level.CONFIG,
				_rb.getString("retailer.catalogItem.category"),
				String.valueOf(catalogItems[i].getCategory()));
			_logger.log(
				Level.CONFIG,
				_rb.getString("retailer.catalogItem.price"),
				String.valueOf(catalogItems[i].getPrice()));
			_logger.log(Level.CONFIG, "");
		}

		catalog.setItem(catalogItems);

		_logger.exiting(_className, GET_CATALOG);

		return catalog;
	}

	private Vector getValidProductNumbers() {
		Vector v = new Vector();

		// 605010 is an invalid product and is thus not added
		for (int i = 0; i < 9; i++) {
			v.add(
				new BigInteger(
					_rb.getString(
						"retailer.catalogItem." + i + ".productNumber")));
		}

		return v;
	}

	public PartsOrderResponseType submitOrder(
		PartsOrderType partsOrder,
		CustomerDetailsType customerDetails,
		ConfigurationType configurationHeader)
		throws
			BadOrderFault,
			InvalidProductCodeType,
			ConfigurationFaultType,
			RemoteException {

		_logger.entering(_className, SUBMIT_ORDER);
		// ** - Validate the configurationHeader
		Hashtable roles = null;
		roles = ConfigurationValidator.validateHeader(configurationHeader);

		// Set the endpoint address of the logging facility
		// from the configurationHeader
		((Stub) _logStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			(String) roles.get(ConfigurationEndpointRole.LoggingFacility));

		_logger.log(
			Level.CONFIG,
			_rb.getString("retailer.endpoint.logging"),
			(String) roles.get(ConfigurationEndpointRole.LoggingFacility));

		_logger.log(
			Level.CONFIG,
			_rb.getString("retailer.endpoint.retailer"),
			(String) roles.get(ConfigurationEndpointRole.Retailer));

		_logger.log(
			Level.CONFIG,
			_rb.getString("retailer.endpoint.warehousea"),
			(String) roles.get(ConfigurationEndpointRole.WarehouseA));

		_logger.log(
			Level.CONFIG,
			_rb.getString("retailer.endpoint.warehouseb"),
			(String) roles.get(ConfigurationEndpointRole.WarehouseB));

		_logger.log(
			Level.CONFIG,
			_rb.getString("retailer.endpoint.warehousec"),
			(String) roles.get(ConfigurationEndpointRole.WarehouseC));

		_logger.log(
			Level.CONFIG,
			_rb.getString("retailer.endpoint.manufacturera"),
			(String) roles.get(ConfigurationEndpointRole.ManufacturerA));

		_logger.log(
			Level.CONFIG,
			_rb.getString("retailer.endpoint.manufacturerb"),
			(String) roles.get(ConfigurationEndpointRole.ManufacturerB));

		_logger.log(
			Level.CONFIG,
			_rb.getString("retailer.endpoint.manufacturerc"),
			(String) roles.get(ConfigurationEndpointRole.ManufacturerC));

		PartsOrderItem[] partsOrderList = partsOrder.getItem();

		String desc = _rb.getString("retailer.submitOrder.desc");
		for (int i = 0; i < partsOrderList.length; i++) {
			desc += partsOrderList[i].getProductNumber();
			desc += (i == (partsOrderList.length - 1)) ? "" : ", ";
		}
		desc = _localizer.localize(desc, customerDetails.getCustnbr());

		LogEventRequestType logRequest = new LogEventRequestType();
		logRequest.setDemoUserID(configurationHeader.getUserId());
		logRequest.setServiceID(RETAILER_SUBMIT_ORDER);
		logRequest.setEventID("UC1-5");
		logRequest.setEventDescription(desc);
		_logStub.logEvent(logRequest);
		_logger.log(Level.CONFIG, logRequest.getEventDescription());

		// ** - Validate the order

		// A BadOrder fault is returned if the order
		// contains no line items
		if (partsOrderList == null)
			throw new BadOrderFault(
				_rb.getString("retailer.submitOrder.badOrderFault"));

		// Throw InvalidProductCode fault, if it contains a line item
		// with an invalid (i.e. unknown) product code
		Vector productNumbersVector = getValidProductNumbers();
		for (int i = 0; i < partsOrderList.length; i++) {
			if (!productNumbersVector
				.contains(partsOrderList[i].getProductNumber())) {
				logRequest.setEventID("UC1-ALT1-1");
				logRequest.setEventDescription(
					_localizer.localize(
						_rb.getString("retailer.submitOrder.rejected"),
						new String[] {
							customerDetails.getCustnbr(),
							partsOrderList[i].getProductNumber().toString()}));
				_logStub.logEvent(logRequest);
				_logger.log(Level.CONFIG, logRequest.getEventDescription());

				// Throw SOAPFaultException as env:Client fault code
				// is required on the client side
				try {
					SOAPFactory soapFactory = SOAPFactory.newInstance();
					Detail detail = soapFactory.createDetail();
					QName faultcode =
						new QName(SOAP_ENVELOPE_NAMESPACE, "Client");
					Name name =
						soapFactory.createName(
							"InvalidProductCode",
							"ns1",
							RETAIL_ORDER_NAMESPACE);
					DetailEntry detailEntry = detail.addDetailEntry(name);
					SOAPElement soapElement =
						detailEntry.addChildElement(
							"Reason",
							"ns1",
							RETAIL_ORDER_NAMESPACE);
					soapElement.addTextNode("InvalidProductCode");
					soapElement =
						detailEntry.addChildElement(
							"ProductNumber",
							"ns1",
							RETAIL_ORDER_NAMESPACE);
					soapElement.addTextNode(
						partsOrderList[i].getProductNumber().toString());

					throw new SOAPFaultException(
						faultcode,
						"com.sun.wsi.scm.retailer.InvalidProductCodeType",
						RETAILER_SUBMIT_ORDER,
						detail);
				} catch (SOAPException ex) {
					_logger.log(Level.WARNING, ex.getMessage(), ex);
				}
			}
		}

		// Create the list of items requested
		_logger.log(Level.FINEST, _rb.getString("retailer.submitOrder.list"));
		Item[] requestItemArray = new Item[partsOrderList.length];
		for (int i = 0; i < partsOrderList.length; i++) {
			requestItemArray[i] = new Item();
			requestItemArray[i].setProductNumber(
				partsOrderList[i].getProductNumber());
			requestItemArray[i].setQuantity(
				partsOrderList[i].getQuantity().intValue());
			// partsOrderList[i].getPrice() is not getting used anywhere
			// and that's ok since the two prices may differ
		}
		ItemList requestItemList = new ItemList();
		requestItemList.setItem(requestItemArray);

		// ** - Initialize the response
		// Used to correlate with the list of items that
		// could not be ordered by a particular warehouse
		Hashtable requestHash = new Hashtable();
		Hashtable responseHash = new Hashtable();

		for (int i = 0; i < partsOrderList.length; i++) {
			// Initialize the requestHash with the original request
			requestHash.put(
				partsOrderList[i].getProductNumber(),
				partsOrderList[i]);

			// Initialize the response with productNumber
			PartsOrderResponseItem responseItem = new PartsOrderResponseItem();
			responseItem.setProductNumber(partsOrderList[i].getProductNumber());

			// Initialize the responseHash with the initialized response
			responseHash.put(
				partsOrderList[i].getProductNumber(),
				responseItem);
		}

		WarehouseService_Impl warehouseService = new WarehouseService_Impl();

		// configure WarehouseA stub
		WarehouseShipmentsPortType warehouseAStub =
			warehouseService.getWarehouseAPort();
		((Stub) warehouseAStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			roles.get(ConfigurationEndpointRole.WarehouseA));

		// Create SOAP message log files for FINE or higher logging level
		if (_logger.isLoggable(Level.FINE))
			((StubBase) warehouseAStub)._setTransportFactory(
				new HttpClientTransportFactory(System.out));

		// configure WarehouseB stub
		WarehouseShipmentsPortType warehouseBStub =
			warehouseService.getWarehouseBPort();
		((Stub) warehouseBStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			roles.get(ConfigurationEndpointRole.WarehouseB));

		// Create SOAP message log files for FINE or higher logging level
		if (_logger.isLoggable(Level.FINE))
			((StubBase) warehouseBStub)._setTransportFactory(
				new HttpClientTransportFactory(System.out));

		// configure WarehouseC stub
		WarehouseShipmentsPortType warehouseCStub =
			warehouseService.getWarehouseCPort();
		((Stub) warehouseCStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			roles.get(ConfigurationEndpointRole.WarehouseC));

		// Create SOAP message log files for FINE or higher logging level
		if (_logger.isLoggable(Level.FINE))
			((StubBase) warehouseCStub)._setTransportFactory(
				new HttpClientTransportFactory(System.out));

		// Process order with WarehouseA
		ItemList unshippedItemList =
			processOrder(
				warehouseAStub,
				requestItemList,
				customerDetails.getCustnbr(),
				configurationHeader,
				requestHash,
				responseHash,
				ConfigurationEndpointRole._WarehouseAString);

		// If WarehouseA is not able to ship all the goods,
		// then WarehouseB is invoked
		if (unshippedItemList.getItem().length > 0) {
			unshippedItemList =
				processOrder(
					warehouseBStub,
					unshippedItemList,
					customerDetails.getCustnbr(),
					configurationHeader,
					requestHash,
					responseHash,
					ConfigurationEndpointRole._WarehouseBString);

			// If WarehouseB is not able to ship the remaining goods,
			// then WarehouseC is invoked
			if (unshippedItemList.getItem().length > 0) {
				unshippedItemList =
					processOrder(
						warehouseCStub,
						unshippedItemList,
						customerDetails.getCustnbr(),
						configurationHeader,
						requestHash,
						responseHash,
						ConfigurationEndpointRole._WarehouseCString);

			}
		}

		// If there are any goods remaining to be shipped, they are 
		// marked with 0 quantity and "Insufficient stock" comment
		if (unshippedItemList.getItem().length > 0) {
			// If none of the items could be shipped by any of the warehouses
			// then logEvent is invoked
			if (partsOrder.getItem().length
				== unshippedItemList.getItem().length) {
				logRequest.setEventID("UC1-ALT2-1");
				desc =
					_localizer.localize(
						_rb.getString("retailer.submitOrder.noAvailability"),
						customerDetails.getCustnbr());
				logRequest.setEventDescription(desc);
				_logStub.logEvent(logRequest);
				_logger.log(Level.CONFIG, logRequest.getEventDescription());
			} else {
				logRequest.setEventID("UC1-9");
				desc =
					_localizer.localize(
						_rb.getString("retailer.submitOrder.finished"),
						customerDetails.getCustnbr());
				logRequest.setEventDescription(desc);
				_logStub.logEvent(logRequest);
				_logger.log(Level.CONFIG, logRequest.getEventDescription());
			}
			completeOrder(unshippedItemList, requestHash, responseHash);
		} else { // all the items are shipped correctly
			logRequest.setEventID("UC1-9");
			desc =
				_localizer.localize(
					_rb.getString("retailer.submitOrder.finished"),
					customerDetails.getCustnbr());
			logRequest.setEventDescription(desc);
			_logStub.logEvent(logRequest);
			_logger.log(Level.CONFIG, logRequest.getEventDescription());
		}

		// Convert the responseHash to PartsOrderResponseItem[] and return
		PartsOrderResponseItem[] partsOrderResponse =
			new PartsOrderResponseItem[responseHash.size()];

		int count = 0;
		Enumeration responseEnum = responseHash.elements();
		while (responseEnum.hasMoreElements()) {
			partsOrderResponse[count] =
				(PartsOrderResponseItem) responseEnum.nextElement();
			double price =
				partsOrderResponse[count].getQuantity().floatValue()
					* partsOrderResponse[count].getPrice().floatValue();

			partsOrderResponse[count++].setPrice(
				(new BigDecimal(price)).setScale(2, BigDecimal.ROUND_HALF_UP));
		}

		PartsOrderResponseType response = new PartsOrderResponseType();
		response.setItem(partsOrderResponse);

		_logger.exiting(_className, SUBMIT_ORDER);
		return response;
	}

	private String getProductNumbersAsString(ItemList itemList) {
		String desc = "";

		Item[] items = itemList.getItem();
		for (int i = 0; i < items.length; i++) {
			desc += items[i].getProductNumber();
			if (i != (items.length - 1))
				desc += ", ";
		}

		return desc;
	}

	/**
	* 1). Invoke warehouse stub to ship the goods
	* 2). Items shipped by a warehouse are marked as success
	*     in responseHash
	* 3). Items not not shipped are collected in unshippedItems
	* 4). Items not shipped by a warehouse are then populated
	*     with the original quantity and returned back
	*/
	private ItemList processOrder(
		WarehouseShipmentsPortType warehouseStub,
		ItemList requestItemList,
		String customerNumber,
		ConfigurationType configurationHeader,
		Hashtable requestHash,
		Hashtable responseHash,
		String role)
		throws ConfigurationFaultType, RemoteException {

		_logger.entering(_className, PROCESS_ORDER);
		_logger.log(
			Level.FINEST,
			_rb.getString("retailer.processOrder.start"),
			role);

		// Invoke the warehouse stub
		ItemShippingStatusList itemShippingStatusList = null;

		// Cast the warehouse to the appropriate stub based upon the role
		if (role.equals(ConfigurationEndpointRole._WarehouseAString)) {
			itemShippingStatusList =
				warehouseStub.shipGoods(
					requestItemList,
					customerNumber,
					configurationHeader);
		} else if (role.equals(ConfigurationEndpointRole._WarehouseBString)) {
			itemShippingStatusList =
				warehouseStub.shipGoods(
					requestItemList,
					customerNumber,
					configurationHeader);
		} else if (role.equals(ConfigurationEndpointRole._WarehouseCString)) {
			itemShippingStatusList =
				warehouseStub.shipGoods(
					requestItemList,
					customerNumber,
					configurationHeader);
		} else {
			_logger.log(
				Level.FINER,
				_rb.getString("retailer.processOrder,invalidRole"),
				role);
		}

		ItemShippingStatus[] itemShippingStatusArray =
			itemShippingStatusList.getItemStatus();

		ArrayList unshippedItems = new ArrayList();
		// Sets a valid value in the responseHash for all the items 
		// shipped by the warehouse defined by "role" parameter
		for (int i = 0; i < itemShippingStatusArray.length; i++) {
			BigInteger productNumber =
				itemShippingStatusArray[i].getProductNumber();
			if (itemShippingStatusArray[i].isStatus()) {
				PartsOrderItem requestItem =
					(PartsOrderItem) requestHash.get(productNumber);
				PartsOrderResponseItem responseItem =
					(PartsOrderResponseItem) responseHash.get(productNumber);
				responseItem.setQuantity(requestItem.getQuantity());
				responseItem.setPrice(requestItem.getPrice());
				String desc =
					_localizer.localize(
						_rb.getString("retailer.processOrder.inStock"),
						role);
				responseItem.setComment(desc);

				responseHash.put(productNumber, responseItem);
			} else {
				unshippedItems.add(
					itemShippingStatusArray[i].getProductNumber());
			}
		}

		// Collect products not shipped by a warehouse. Quanity is
		// obtained from the original request as ItemShippingStatus
		// (response from Warehouse) does not contain quantity
		Item[] requestItemArray = requestItemList.getItem();
		Hashtable quantityHash = new Hashtable();
		for (int i = 0; i < requestItemArray.length; i++)
			quantityHash.put(
				requestItemArray[i].getProductNumber(),
				new Integer(requestItemArray[i].getQuantity()));

		Item[] unshippedItemArray = new Item[unshippedItems.size()];
		for (int i = 0; i < unshippedItems.size(); i++) {
			BigInteger productNumber = (BigInteger) unshippedItems.get(i);
			unshippedItemArray[i] = new Item();
			unshippedItemArray[i].setProductNumber(productNumber);

			// quantity is ignored
			Integer quantityInteger =
				(Integer) quantityHash.get(
					new Integer(productNumber.intValue()));
			unshippedItemArray[i].setQuantity(
				((Integer) quantityHash.get(productNumber)).intValue());
		}

		ItemList unshippedItemList = new ItemList();
		unshippedItemList.setItem(unshippedItemArray);

		_logger.exiting(_className, PROCESS_ORDER);
		return unshippedItemList;
	}

	/**
	* For items not shipped by any of the warehouses, 0 quantity 
	* and "Insufficient stock" comment is specified in the response
	*/
	private void completeOrder(
		ItemList unshippedItemList,
		Hashtable requestHash,
		Hashtable responseHash) {
		_logger.entering(_className, COMPLETE_ORDER);
		_logger.log(Level.FINER, _rb.getString("retailer.completeOrder.start"));

		Item[] unshippedItemArray = unshippedItemList.getItem();

		for (int i = 0; i < unshippedItemArray.length; i++) {
			BigInteger productNumber = unshippedItemArray[i].getProductNumber();
			PartsOrderItem requestItem =
				(PartsOrderItem) requestHash.get(productNumber);
			PartsOrderResponseItem responseItem =
				(PartsOrderResponseItem) responseHash.get(productNumber);
			responseItem.setQuantity(BigInteger.ZERO);
			responseItem.setPrice(requestItem.getPrice());
			String desc = _rb.getString("retailer.completeOrder.insufficient");
			responseItem.setComment(desc);

			responseHash.put(productNumber, responseItem);
		}

		_logger.exiting(_className, COMPLETE_ORDER);
	}
}
