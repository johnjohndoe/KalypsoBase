/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

	Localizer _localizer = new Localizer();
	final String _separator = ": ";

	public String format(LogRecord record) {
		StringBuffer sb = new StringBuffer();
		sb.append(record.getLevel());
		sb.append(_separator);
		sb.append(
			_localizer.localize(record.getMessage(), record.getParameters()));
		sb.append("\n");

		return sb.toString();
	}
}
