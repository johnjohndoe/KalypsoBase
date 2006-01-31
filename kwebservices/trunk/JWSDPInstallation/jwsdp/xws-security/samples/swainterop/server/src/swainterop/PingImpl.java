/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package swainterop;

import java.io.*;

import java.awt.Image;
import java.awt.Toolkit;


import javax.xml.rpc.*;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import javax.servlet.ServletContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.rpc.server.http.ServletEndpointContextImpl;
import com.sun.xml.rpc.server.TieBase;
import com.sun.xml.rpc.spi.runtime.Tie;


import com.sun.xml.wss.SubjectAccessor;

import javax.security.auth.Subject;
import java.util.Set;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.servlet.ServletContext;
import java.util.*; 
import javax.xml.soap.*;
import com.sun.xml.rpc.server.ServerPropertyConstants;

public  class PingImpl implements PingPort, ServiceLifecycle {
    
    private Object context = null;
    private ServletEndpointContext servletEndpointContext = null;
    private ServletContext servletContext = null;
        
    public String ping(String message) {
        System.out.println("The message is here : " + message);        
        return message;
    }

    /* (non-Javadoc)
     * @see javax.xml.rpc.server.ServiceLifecycle#destroy()
     */
    public void destroy() {
        servletEndpointContext = null;
        servletContext = null;            
    }
    
    public void init(Object context) throws ServiceException {
        this.context = context;
        servletEndpointContext = (ServletEndpointContext) context;
        servletContext = servletEndpointContext.getServletContext();    
    }    
}
