/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

import javax.servlet.http.HttpServletRequest;

public class URLFormatter implements WSIConstants {
	public static String getCallbackURL(HttpServletRequest request) {
		return contextURL(request) + WAREHOUSE_CALLBACK_LOCATION;
	}

	public static String getConfiguratorURL(HttpServletRequest request) {
		return contextURL(request) + CONFIGURATOR_LOCATION;
	}

	private static String contextURL(HttpServletRequest request) {
		return request.getScheme()
			+ "://"
			+ request.getServerName()
			+ ":"
			+ request.getServerPort()
			+ request.getContextPath();

	}
}
