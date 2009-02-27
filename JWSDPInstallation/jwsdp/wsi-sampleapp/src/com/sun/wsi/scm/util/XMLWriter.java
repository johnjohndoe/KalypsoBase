/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

import java.io.PrintStream;

public class XMLWriter {
	protected static PrintStream _writer = null;

	public static void setOutputStream(PrintStream ps) {
		_writer = ps;
	}

	public static PrintStream getOutputStream() {
		return _writer;
	}

	/**
		@param elementName
		@param elementContent
	*/
	public static void writeContent(
		String elementName,
		String elementContent) {
		_writer.println(
			"<"
				+ elementName
				+ ">"
				+ elementContent
				+ "</"
				+ elementName
				+ ">");
	}

	/**
		@param elementName
		@param attributeNV name-value pair for attribute
		@param elementContent
	*/
	public static void writeContent(
		String elementName,
		AttributeNV attributes,
		String elementContent) {
		AttributeNV[] attNVArray = new AttributeNV[1];
		attNVArray[0] = attributes;
		writeContent(elementName, attNVArray, elementContent);
	}

	/**
		@param elementName
		@param attributeNV array of name-value pair for attributes
		@param elementContent
	*/
	public static void writeContent(
		String elementName,
		AttributeNV[] attributes,
		String elementContent) {
		String tag = elementName;
		if (attributes != null) {
			for (int i = 0; i < attributes.length; i++)
				tag += " "
					+ attributes[i].getName()
					+ "=\""
					+ attributes[i].getValue()
					+ "\"";
		}
		_writer.println(
			"<" + tag + ">" + elementContent + "</" + elementName + ">");
	}

	/**
		@param elementName
		@param attributes name-value pair for attribute
	*/
	public static void writeStartTag(String elementName) {
		AttributeNV[] attNVArray = new AttributeNV[0];

		writeStartTag(elementName, attNVArray);
	}

	public static void writeStartTag(
		String elementName,
		AttributeNV attributes) {
		AttributeNV[] attNVArray = new AttributeNV[1];
		attNVArray[0] = attributes;
		writeStartTag(elementName, attNVArray);
	}

	/**
		@param elementName
		@param attributes array of name-value pair for attributes
	*/
	public static void writeStartTag(
		String elementName,
		AttributeNV[] attributes) {
		String tag = elementName;
		if (attributes != null) {
			for (int i = 0; i < attributes.length; i++)
				tag += " "
					+ attributes[i].getName()
					+ "=\""
					+ attributes[i].getValue()
					+ "\"";
		}
		_writer.println("<" + tag + ">");
	}

	/**
		@param elementName
	*/
	public static void writeEndTag(String elementName) {
		_writer.println("</" + elementName + ">");
	}
}
