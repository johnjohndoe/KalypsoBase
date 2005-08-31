/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

public abstract class StringConverter {
	public static String getWarehouseProductNumbersAsString(
		com.sun.wsi.scm.warehouse.ItemList itemList) {
		String desc = "";

		com.sun.wsi.scm.warehouse.Item[] items = itemList.getItem();
		for (int i = 0; i < items.length; i++) {
			desc += items[i].getProductNumber();
			if (i != (items.length - 1))
				desc += ", ";
		}

		return desc;
	}

	public static String getManufacturerProductNumbersAsString(com.sun.wsi.scm.manufacturer.sn.ItemList itemList) {
		String desc = "";

		com.sun.wsi.scm.manufacturer.sn.Item[] items = itemList.getItem();
		for (int i = 0; i < items.length; i++) {
			desc += items[i].getID();
			if (i != (items.length - 1))
				desc += ", ";
		}

		return desc;
	}
}
