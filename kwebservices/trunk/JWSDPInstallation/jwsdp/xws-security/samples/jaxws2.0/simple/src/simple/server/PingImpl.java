/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package simple.server;

@javax.jws.WebService (endpointInterface="simple.server.PingPort")
public class PingImpl implements PingPort {
    
   public String ping(TicketType ticket, String message) {
        System.out.println("The message is here : " + message);
        return message;
    }
                                                                                
    public String ping0(TicketType ticket, String message) {
        return ping(ticket, message);
    }
}
