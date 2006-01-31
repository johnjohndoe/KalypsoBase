/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.messaging.soap;

import java.util.Iterator;
import java.util.Vector;

import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPException;

public abstract class SOAPRecipient {

    protected Vector roles = new Vector();
    protected Vector supportedHeaders = new Vector();

    public void addRole(String role) {
        roles.addElement(role);
    }

    public Iterator getRoles() {
        return roles.iterator();
    }

    public boolean supportsRole(String role) {
        for(Iterator it = roles.iterator(); it.hasNext(); ) {
            String thisRole = (String)it.next();
            if (role.equals(thisRole))
                return true;
        }
        return false;
    }

    public void addHeader(String header) {
        supportedHeaders.addElement(header);
    }

    public Iterator getHeaders() {
        return supportedHeaders.iterator();
    }

    public boolean supportsHeader(String header) {
        for(Iterator it = supportedHeaders.iterator(); it.hasNext(); ) {
            String thisHeader = (String)it.next();
            if (header.equals(thisHeader))
                return true;
        }
        return false;
    }
 
    public abstract SOAPHeaderElement processHeaderElement(
        SOAPHeaderElement elem)
        throws SOAPException;

}
