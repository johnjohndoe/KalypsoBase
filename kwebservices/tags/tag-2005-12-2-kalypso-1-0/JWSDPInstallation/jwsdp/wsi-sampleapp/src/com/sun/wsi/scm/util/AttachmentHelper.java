/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttachmentHelper {
	protected Hashtable _attachments = null;
	protected Logger _logger = null;
	protected PropertyResourceBundle _crb = null;

	public AttachmentHelper() {
	}

	public AttachmentHelper(Collection attachments) {
		set_attachments(attachments);
	}

	public AttachmentHelper(
		Collection attachments,
		Logger logger,
		PropertyResourceBundle resourceBundle) {
		this(attachments);
		_logger = logger;
		_crb = resourceBundle;
	}

	public void set_attachments(Collection attachments) {
		_attachments = new Hashtable();
		for (Iterator iter = attachments.iterator(); iter.hasNext();) {
			AttachmentPart attachmentPart = (AttachmentPart) iter.next();
			_attachments.put(attachmentPart.getContentId(), attachmentPart);
		}
	}

	public void set_logger(
		Logger logger,
		PropertyResourceBundle resourceBundle) {
		_logger = logger;
		_crb = resourceBundle;
	}

	public DataHandler search(String contentId) throws SOAPException {

		DataHandler dataHandler = null;

		if ((_attachments != null) && _attachments.containsKey(contentId)) {
			AttachmentPart attachment =
				(AttachmentPart) _attachments.get(contentId);
			dataHandler =
				new DataHandler(
					attachment.getContent(),
					attachment.getContentType());
			if (_logger != null)
				_logger.log(
					Level.FINE,
					_crb.getString("catalog.client.registry.found"),
					contentId);
		} else {
			if (_logger != null)
				_logger.log(
					Level.WARNING,
					_crb.getString("catalog.client.registry.notfound"),
					contentId);
		}

		return dataHandler;
	}

	Enumeration keys() {
		return _attachments.keys();
	}
}
