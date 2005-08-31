/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

import java.util.Hashtable;

import com.sun.wsi.scm.configuration.ConfigurationEndpointRole;
import com.sun.wsi.scm.configuration.ConfigurationEndpointType;
import com.sun.wsi.scm.configuration.ConfigurationFaultType;
import com.sun.wsi.scm.configuration.ConfigurationType;

public class ConfigurationValidator {
	/**
	* Validate the configurationHeader
	*/
	public static Hashtable validateHeader(ConfigurationType configurationHeader)
		throws ConfigurationFaultType {
		if (configurationHeader.getUserId().equals(""))
			throw new ConfigurationFaultType(
				Boolean.FALSE,
				"Invalid UserId",
				new ConfigurationEndpointRole[0]);

		ConfigurationEndpointType[] serviceUrl =
			configurationHeader.getServiceUrl();
		if (serviceUrl == null)
			throw new ConfigurationFaultType(
				Boolean.FALSE,
				"Invalid serviceUrl",
				new ConfigurationEndpointRole[0]);

		if ((serviceUrl.length != 8)) {
			throw new ConfigurationFaultType(
				Boolean.FALSE,
				serviceUrl.length
					+ " entries found in configuration header, expected \"8\"",
				new ConfigurationEndpointRole[0]);
		}

		Hashtable roles = new Hashtable();
		for (int i = 0; i < serviceUrl.length; i++) {
			if (serviceUrl[i].getRole() == null)
				throw new ConfigurationFaultType(
					Boolean.FALSE,
					"Null role not permitted",
					new ConfigurationEndpointRole[0]);

			if (serviceUrl[i].get_value() == null) {
				ConfigurationEndpointRole[] role = { serviceUrl[i].getRole()};
				throw new ConfigurationFaultType(
					Boolean.FALSE,
					"Null URIs not permitted",
					role);
			}

			roles.put(
				serviceUrl[i].getRole(),
				serviceUrl[i].get_value().toString());

			// TODO: (maybe) check for the validity of configurationHeader
			// and throw ConfigurationFaultType for the first invalid
			// serviceUrl
		}

		return roles;
	}
}
