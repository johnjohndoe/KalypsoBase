/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sample;

import java.io.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.*;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import javax.servlet.ServletContext;

import com.sun.xml.rpc.server.http.ServletEndpointContextImpl;
import com.sun.xml.rpc.server.TieBase;
import com.sun.xml.rpc.spi.runtime.Tie;


import com.sun.xml.wss.SubjectAccessor;

import javax.security.auth.Subject;
import java.util.Set;
import java.util.Iterator;

public  class PingImpl implements PingPort, ServiceLifecycle {

    Object context = null;


    // --- implementation of main operation ---
    public String ping(
        TicketType ticket, String message) {
        System.out.println("The message is here : " + message);        

        Subject clientSubject = null;
        try {
             clientSubject = SubjectAccessor.getRequesterSubject(context);
        } catch(Exception e) {
            e.printStackTrace();
        }
        if (clientSubject != null) {
            Set principals = clientSubject.getPrincipals();
            for (Iterator it = principals.iterator(); it.hasNext();) {
                System.out.println("Client Principals:" +  it.next());
            }
        } else {
            System.out.println("Client Principal not set");
        }

        return message;
    }

    /* (non-Javadoc)
     * @see javax.xml.rpc.server.ServiceLifecycle#destroy()
     */
    public void destroy() {
        // Do nothing
    }
    
    public void init(Object context) throws ServiceException {
        this.context = context;
    }
}
