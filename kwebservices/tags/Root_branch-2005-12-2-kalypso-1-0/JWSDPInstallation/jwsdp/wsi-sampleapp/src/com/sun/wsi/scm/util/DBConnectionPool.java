/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.xml.rpc.server.ServletEndpointContext;

import com.sun.wsi.scm.logging.LogEntry;

public class DBConnectionPool implements WSIConstants {

	String CREATE_LOG =
		"CREATE TABLE log (DemoUserID VARCHAR(255), ServiceID VARCHAR(255), EventID VARCHAR(255), EventDescription VARCHAR(255), Timestamp TIMESTAMP)";
	String INSERT_LOG =
		"INSERT INTO log (DemoUserID, ServiceID, EventID, EventDescription, Timestamp) VALUES(?, ?, ?, ?, ?)";
	String QUERY_LOG =
		"SELECT DemoUserID, ServiceID, EventID, EventDescription, Timestamp from log where DemoUserID=?";

	String CREATE_CALLBACK =
		"CREATE TABLE callback (conversationID VARCHAR(255), warehouse_role VARCHAR(255), manufacturer_role VARCHAR(255))";
	String INSERT_CALLBACK =
		"INSERT INTO callback (conversationID, warehouse_role, manufacturer_role) VALUES(?, ?, ?)";
	String QUERY_CALLBACK =
		"SELECT warehouse_role, manufacturer_role from callback where conversationID=?";

	private static DBConnectionPool thePool = null;

	PreparedStatement loggingInsertStatement = null;
	PreparedStatement loggingQueryStatement = null;

	PreparedStatement callbackInsertStatement = null;
	PreparedStatement callbackQueryStatement = null;

	public static int LOGGING = 0;
	public static int CALLBACK = 1;

	private final String CALLBACK_TABLE = "Callback";
	private final String LOGGING_TABLE = "Logging";

	Connection connection = null;
	Logger _logger = null;

	static ServletContext _servletContext = null;

	PropertyResourceBundle _rb = null;

	private DBConnectionPool(ServletContext servletContext) {
		_logger = Logger.getLogger(LOGGER);

		// Prepare the stream for database resource bundle
		InputStream is = servletContext.getResourceAsStream(DB_RESOURCES);
		try {
			_rb = new PropertyResourceBundle(is);
		} catch (IOException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		}

		Statement statement = null;

		try {
			is = servletContext.getResourceAsStream(DB_PROPS);
			Properties dbProps = new Properties();
			dbProps.load(is);
			String dbDriver = dbProps.getProperty("db.driver");
			String dbUrl = dbProps.getProperty("db.url");
			String dbUsername = dbProps.getProperty("db.username");
			String dbPassword = dbProps.getProperty("db.password");
			String dbTableExistProp = dbProps.getProperty("db.table.exist");

			if ((dbDriver == null)
				|| (dbUrl == null)
				|| (dbUsername == null)
				|| (dbPassword == null)
				|| (dbTableExistProp == null)) {

				_logger.log(Level.SEVERE, _rb.getString("db.file.corrupted"));
			}

			_logger.log(Level.CONFIG, _rb.getString("db.driver"), dbDriver);

			_logger.log(Level.CONFIG, _rb.getString("db.url"), dbUrl);

			_logger.log(Level.CONFIG, _rb.getString("db.username"), dbUsername);

			_logger.log(Level.CONFIG, _rb.getString("db.password"), dbPassword);

			_logger.log(
				Level.CONFIG,
				_rb.getString("db.tableExist"),
				dbTableExistProp);

			int dbTableExist = Integer.parseInt(dbTableExistProp);
			Class.forName(dbDriver);
			connection =
				DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
			DatabaseMetaData dbMetadata = connection.getMetaData();
			_logger.log(
				Level.INFO,
				_rb.getString("db.product.info"),
				new String[] {
					dbMetadata.getDatabaseProductName(),
					dbMetadata.getDatabaseProductVersion()});
			_logger.log(
				Level.INFO,
				_rb.getString("db.driver.info"),
				new String[] {
					dbMetadata.getDriverName(),
					dbMetadata.getDriverVersion()});
			statement = connection.createStatement();

			try {
				statement.executeUpdate(CREATE_LOG);
				_logger.log(
					Level.INFO,
					_rb.getString("db.tableCreated"),
					LOGGING_TABLE);
			} catch (SQLException ex) {
				if (ex.getErrorCode() == dbTableExist)
					_logger.log(
						Level.WARNING,
						_rb.getString("db.tableExisted"),
						LOGGING_TABLE);
				else {
					ex.printStackTrace();
					_logger.log(Level.SEVERE, ex.getMessage(), ex);
				}
			}

			try {
				statement.executeUpdate(CREATE_CALLBACK);
				_logger.log(
					Level.INFO,
					_rb.getString("db.tableCreated"),
					CALLBACK_TABLE);
			} catch (SQLException ex) {
				if (ex.getErrorCode() == dbTableExist)
					_logger.log(
						Level.WARNING,
						_rb.getString("db.tableExisted"),
						CALLBACK_TABLE);
				else {
					ex.printStackTrace();
					_logger.log(Level.SEVERE, ex.getMessage(), ex);
				}
			}

			loggingInsertStatement = connection.prepareStatement(INSERT_LOG);
			loggingQueryStatement = connection.prepareStatement(QUERY_LOG);

			callbackInsertStatement =
				connection.prepareStatement(INSERT_CALLBACK);
			callbackQueryStatement =
				connection.prepareStatement(QUERY_CALLBACK);
		} catch (IOException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} catch (SQLException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} finally {
			try {
				if (statement != null)
					statement.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
				_logger.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
	}

	public static DBConnectionPool getInstance(Object context) {
		if (thePool == null) {
			_servletContext =
				((ServletEndpointContext) context).getServletContext();
			thePool = new DBConnectionPool(_servletContext);
		}

		return thePool;
	}

	public synchronized void insert(int table, Vector setters) {
		try {
			if (connection.isClosed()) {
				_logger.log(Level.WARNING, _rb.getString("db.connectionAgain"));
				thePool = new DBConnectionPool(_servletContext);
			}
			int rowCount = 0;
			if (table == LOGGING) {
				for (int i = 0; i < setters.size() - 1; i++)
					loggingInsertStatement.setString(
						i + 1,
						(String) setters.get(i));
				loggingInsertStatement.setTimestamp(
					5,
					(Timestamp) setters.get(4));
				rowCount = loggingInsertStatement.executeUpdate();
				_logger.log(
					Level.FINEST,
					_rb.getString("db.rowUpdated"),
					new String[] { String.valueOf(rowCount), LOGGING_TABLE });
			} else if (table == CALLBACK) {
				for (int i = 0; i < setters.size(); i++)
					callbackInsertStatement.setString(
						i + 1,
						(String) setters.get(i));
				rowCount = callbackInsertStatement.executeUpdate();
				_logger.log(
					Level.FINEST,
					_rb.getString("db.rowUpdated"),
					new String[] { String.valueOf(rowCount), CALLBACK_TABLE });
			} else
				_logger.log(
					Level.SEVERE,
					_rb.getString("db.tableUnknown"),
					String.valueOf(table));
		} catch (SQLException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	public synchronized ArrayList query(int table, Vector setters) {
		ArrayList list = new ArrayList();

		ResultSet rs = null;

		try {
			if (connection.isClosed()) {
				_logger.log(Level.WARNING, _rb.getString("db.connectionAgain"));
				thePool = new DBConnectionPool(_servletContext);
			}
			if (table == LOGGING) {

				_logger.log(
					Level.FINEST,
					_rb.getString("db.queryTable"),
					LOGGING_TABLE);
				for (int i = 0; i < setters.size(); i++)
					loggingQueryStatement.setString(
						i + 1,
						(String) setters.get(i));
				rs = loggingQueryStatement.executeQuery();

				Calendar cal = Calendar.getInstance();

				while (rs.next()) {
					LogEntry logEntry = new LogEntry();
					logEntry.setServiceID(rs.getString(2));
					logEntry.setEventID(rs.getString(3));
					logEntry.setEventDescription(rs.getString(4));
					cal.setTimeInMillis(
						((Timestamp) rs.getTimestamp(5)).getTime());
					logEntry.setTimestamp(cal);
					list.add(logEntry);
				}
			} else if (table == CALLBACK) {
				_logger.log(
					Level.FINEST,
					_rb.getString("db.queryTable"),
					CALLBACK_TABLE);
				for (int i = 0; i < setters.size(); i++)
					callbackQueryStatement.setString(
						i + 1,
						(String) setters.get(i));
				rs = callbackQueryStatement.executeQuery();

				while (rs.next()) {
					list.add(rs.getString(1));
					list.add(rs.getString(2));
				}
			} else {
				_logger.log(
					Level.SEVERE,
					_rb.getString("db.tableUnknown"),
					String.valueOf(table));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
				_logger.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
		return list;
	}
}
