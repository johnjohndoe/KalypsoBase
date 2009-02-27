/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.logging;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.PropertyResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import com.sun.wsi.scm.util.DBConnectionPool;
import com.sun.wsi.scm.util.WSIConstants;

public class LoggingFacilityLogPortTypeImpl
	implements LoggingFacilityLogPortType, WSIConstants, ServiceLifecycle {

	PropertyResourceBundle _rb = null;

	Logger _logger = null;
	DBConnectionPool _dbPool = null;
	String _className = getClass().getName();

	public void init(Object context) {
		ServletContext servletContext =
			((ServletEndpointContext) context).getServletContext();

		_logger = Logger.getLogger(LOGGER);
		_logger.entering(_className, INIT);

		// Prepare the stream for logging resource bundle
		InputStream is = servletContext.getResourceAsStream(LOGGING_RESOURCES);
		try {
			_rb = new PropertyResourceBundle(is);
		} catch (IOException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		}

		_dbPool = DBConnectionPool.getInstance(context);
		_logger.exiting(_className, INIT);
	}

	public void logEvent(LogEventRequestType request) {
		_logger.entering(_className, LOG_EVENT);

		Timestamp timestamp =
			new Timestamp(Calendar.getInstance().getTimeInMillis());
		_logger.log(
			Level.FINEST,
			_rb.getString("logging.log.event"),
			new String[] {
				request.getDemoUserID(),
				request.getServiceID(),
				request.getEventID(),
				request.getEventDescription()});

		Vector setters = new Vector();
		setters.add(request.getDemoUserID());
		setters.add(request.getServiceID());
		setters.add(request.getEventID());
		setters.add(request.getEventDescription());
		setters.add(timestamp);

		_dbPool.insert(DBConnectionPool.LOGGING, setters);
		_logger.exiting(_className, LOG_EVENT);

		return;
	}

	public GetEventsResponseType getEvents(GetEventsRequestType request)
		throws GetEventsFaultType {
		_logger.entering(_className, GET_EVENTS);
		GetEventsResponseType response = new GetEventsResponseType();

		Vector setters = new Vector();
		setters.add(request.getDemoUserID());
		ArrayList list = _dbPool.query(DBConnectionPool.LOGGING, setters);
		LogEntry[] logEntryArray = (LogEntry[]) list.toArray(new LogEntry[0]);
		response.setLogEntry(logEntryArray);

		_logger.exiting(_className, GET_EVENTS);
		return response;
	}

	public void destroy() {
	}

}
