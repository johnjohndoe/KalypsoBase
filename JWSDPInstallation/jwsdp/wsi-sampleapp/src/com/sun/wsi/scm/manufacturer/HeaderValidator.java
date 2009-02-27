/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.manufacturer;

import java.util.PropertyResourceBundle;

import com.sun.wsi.scm.configuration.ConfigurationEndpointRole;
import com.sun.wsi.scm.configuration.ConfigurationFaultType;
import com.sun.wsi.scm.manufacturer.cb.StartHeaderType;

public class HeaderValidator {

	public static void validateStartHeader(
		StartHeaderType startHeader,
		ConfigurationEndpointRole role,
		PropertyResourceBundle rb)
		throws ConfigurationFaultType {
		ConfigurationEndpointRole[] configRole = { role };
		if (startHeader.getConversationID().equals("")) {
			throw new ConfigurationFaultType(
				Boolean.FALSE,
				rb.getString("mfr.header.validator.conversationID"),
				configRole);
		}

		if (startHeader.getCallbackLocation().equals(""))
			throw new ConfigurationFaultType(
				Boolean.FALSE,
				rb.getString("mfr.header.validator.callback"),
				configRole);
	}
}
