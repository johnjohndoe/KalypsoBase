/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.manufacturer;

import java.math.BigInteger;
import java.util.Vector;

import com.sun.wsi.scm.manufacturer.po.Item;
import com.sun.wsi.scm.manufacturer.po.Reason;
import com.sun.wsi.scm.manufacturer.po.SubmitPOFaultType_Exception;
import com.sun.wsi.scm.util.WSIConstants;

public class POValidator implements WSIConstants {
	public static void validateOrder(
		Item[] itemArray,
		int[] productId,
		int[][] manufacturerData)
		throws SubmitPOFaultType_Exception {

		Vector validProducts = new Vector();
		for (int i = 0; i < productId.length; i++)
			validProducts.add(new BigInteger(String.valueOf(productId[i])));

		for (int i = 0; i < itemArray.length; i++) {
			BigInteger productNumber = itemArray[i].getID();
			int qty = itemArray[i].getQty();

			// Reject the order for an invalid quantity (<= 0)
			if (qty <= 0) {
				throw new SubmitPOFaultType_Exception(
					Reason.fromString("InvalidQty"));
			}

			// Reject the order for a product that does not exist
			if (!validProducts.contains(productNumber)) {
				throw new SubmitPOFaultType_Exception(
					Reason.fromString("InvalidProduct"));
			}

			// When can we throw "MalformedOrder" fault
		}
	}
}
