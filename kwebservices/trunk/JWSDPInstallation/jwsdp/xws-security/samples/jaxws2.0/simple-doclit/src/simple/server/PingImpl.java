/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package simple.server;

@javax.jws.WebService (endpointInterface="simple.server.PingPort")
public class PingImpl implements PingPort {
    
   public PingResponse ping(Ping ping) {
        System.out.println("The message is here : " + ping.getText());
        PingResponse response = new PingResponse();
        response.setText(ping.getText());
        return response;
    }
                                                                                
    public PingResponse ping0(Ping0 ping) {
        System.out.println("The message is here : " + ping.getText());
        PingResponse response = new PingResponse();
        response.setText(ping.getText());
        return response;
    }
}
