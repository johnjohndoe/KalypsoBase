/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.manufacturer;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.Stub;

import java.rmi.RemoteException;

import com.sun.wsi.scm.configuration.ConfigurationFaultType;
import com.sun.wsi.scm.configuration.ConfigurationType;
import com.sun.wsi.scm.manufacturer.cb.CallbackFaultType;
import com.sun.wsi.scm.manufacturer.cb.CallbackHeaderType;
import com.sun.wsi.scm.manufacturer.cb.StartHeaderType;
import com.sun.wsi.scm.manufacturer.po.PurchOrdType;
import com.sun.wsi.scm.manufacturer.sn.ShipmentNoticeType;
import com.sun.wsi.scm.util.WSIConstants;

public class CallbackInvoker implements Runnable, WSIConstants {

	ShipmentNoticeType _shipmentNotice = null;
	CallbackHeaderType _callbackHeader = null;

	ConfigurationType _configHeader = null;

	WarehouseCallbackPortType_Stub _warehouseCallbackStub = null;

	Logger _logger = null;

	String _className = getClass().getName();

	public CallbackInvoker(
		PurchOrdType purchaseOrder,
		ConfigurationType configurationHeader,
		StartHeaderType startHeader,
		Logger logger) {

		_logger = logger;
		_logger.entering(_className, CALLBACK_INVOKER);

		_configHeader = configurationHeader;

		com.sun.wsi.scm.manufacturer.po.ItemList poItemList =
			purchaseOrder.getItems();
		com.sun.wsi.scm.manufacturer.po.Item[] poItems = poItemList.getItem();

		com.sun.wsi.scm.manufacturer.sn.Item[] snItems =
			new com.sun.wsi.scm.manufacturer.sn.Item[poItems.length];

		for (int i = 0; i < poItems.length; i++) {
			snItems[i] = new com.sun.wsi.scm.manufacturer.sn.Item();
			snItems[i].setID(poItems[i].getID());
			snItems[i].setQty(poItems[i].getQty());
			snItems[i].setPrice(poItems[i].getPrice());
		}
		com.sun.wsi.scm.manufacturer.sn.ItemList snItemList =
			new com.sun.wsi.scm.manufacturer.sn.ItemList();
		snItemList.setItem(snItems);

		_shipmentNotice = new ShipmentNoticeType();
		_shipmentNotice.setShipNum("123");
		_shipmentNotice.setOrderNum(purchaseOrder.getOrderNum());
		_shipmentNotice.setCustomerRef(purchaseOrder.getCustomerRef());
		_shipmentNotice.setItems(snItemList);
		_shipmentNotice.setTotal(purchaseOrder.getTotal());

		_callbackHeader = new CallbackHeaderType();
		_callbackHeader.setConversationID(startHeader.getConversationID());

		ManufacturerService_Impl service = new ManufacturerService_Impl();
		_warehouseCallbackStub =
			(WarehouseCallbackPortType_Stub) service.getWarehouseCallbackPort();
		((Stub) _warehouseCallbackStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			startHeader.getCallbackLocation());

		_logger.exiting(_className, CALLBACK_INVOKER);
	}

	public void run() {
		_logger.entering(_className, RUN);

		try {
			Thread.sleep(CALLBACK_DELAY);
			boolean status =
				_warehouseCallbackStub.submitSN(
					_shipmentNotice,
					_configHeader,
					_callbackHeader);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			_logger.log(Level.WARNING, ex.getMessage(), ex);
		} catch (ConfigurationFaultType ex) {
			ex.printStackTrace();
			_logger.log(Level.WARNING, ex.getMessage(), ex);
		} catch (CallbackFaultType ex) {
			ex.printStackTrace();
			_logger.log(Level.WARNING, ex.getMessage(), ex);
		} catch (RemoteException ex) {
			ex.printStackTrace();
			_logger.log(Level.WARNING, ex.getMessage(), ex);
		} catch (Throwable t) {
			t.printStackTrace();
			_logger.log(Level.WARNING, t.getMessage(), t);
		} finally {
			_logger.exiting(_className, RUN);
		}
	}
}
