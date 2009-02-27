/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

import java.text.MessageFormat;

public class Localizer {

	public Localizer() {
	}

	public String localize(String pattern, Object[] arguments) {
		return MessageFormat.format(pattern, arguments);
	}

	public String localize(String pattern, String argument) {
		Object[] args = { argument };

		return localize(pattern, args);
	}

	public String localize(String pattern, Object argument) {
		Object[] args = { argument };

		return localize(pattern, args);
	}

	public static String localizedBundleName(String name) {
		StringBuffer buffer = new StringBuffer(name);
		buffer.append("_en.properties");

		// If resource bundles are available in other locale, then
		// comment the the hard coded bundle names in previous line
		// and instead uncomment the line below.
		//buffer.append("_" + Locale.getDefault().toString() + ".properties");

		return buffer.toString();
	}
}
