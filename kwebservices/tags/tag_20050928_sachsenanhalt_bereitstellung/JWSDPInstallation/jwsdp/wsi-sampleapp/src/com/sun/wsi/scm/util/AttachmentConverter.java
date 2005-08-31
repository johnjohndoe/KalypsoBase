/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.xml.transform.stream.StreamSource;

public class AttachmentConverter {
	protected ServletContext _servletContext = null;

	public AttachmentConverter(ServletContext servletContext) {
		_servletContext = servletContext;
	}

	/**
	 * @param streamName name of the stream in the WAR file
	 * @return java.awt.Image image created from the stream
	 * @throws IOException
	 */
	public Image streamToImage(String streamName) throws IOException {
		InputStream is = _servletContext.getResourceAsStream(streamName);
		byte[] imageBuf = new byte[is.available()];
		is.read(imageBuf);
		Image image = Toolkit.getDefaultToolkit().createImage(imageBuf);

		return image;
	}

	/**
	 * @param streamName name of the stream in the WAR file
	 * @return javax.xml.transform.stream.Source created from the stream
	 * @throws IOException
	 */
	public StreamSource streamToXML(String streamName) throws IOException {
		InputStream is = _servletContext.getResourceAsStream(streamName);

		return new StreamSource(is);
	}
}
