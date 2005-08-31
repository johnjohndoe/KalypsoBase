/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package simple;

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

import com.sun.xml.rpc.security.LoggerConstants;

public  class PingImpl implements PingPort, ServiceLifecycle {
    private static Logger log =
        Logger.getLogger(
            LoggerConstants.RPC_SECURITY_DOMAIN,
            LoggerConstants.RPC_SECURITY_RESOURCE_BUNDLE);


    // --- implementation of main operation ---
    public String ping(
        TicketType ticket, String message) {
        System.out.println("The message is here : " + message);        
        // System.out.println("The Ticket id is here : " + ticket.getId());
        return message;
    }

    /* (non-Javadoc)
     * @see javax.xml.rpc.server.ServiceLifecycle#destroy()
     */
    public void destroy() {
        // Do nothing
    }
    
    public void init(Object context) throws ServiceException {
    }

}
