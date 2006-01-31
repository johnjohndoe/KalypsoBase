/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package simple.client;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.Holder;
import javax.xml.namespace.QName;
import com.sun.xml.xwss.SecurityConfigurationFactory;
import com.sun.xml.xwss.XWSSecurityConfiguration;

import java.io.FileInputStream;

public class PingServiceClient {
    public static void main (String[] args) {
        try {
	    PingService service = new PingService();
            PingPort stub = (PingPort) service.getPort(new QName("http://xmlsoap.org/Ping","Ping"), PingPort.class); 

            TicketType ticket = new TicketType();
            ticket.setValue("SUNW"); 

            FileInputStream f = new FileInputStream("./etc/client_security_config.xml"); 
            XWSSecurityConfiguration config = SecurityConfigurationFactory.newXWSSecurityConfiguration(f);

             // put the security config info
            ((BindingProvider)stub).getRequestContext().
                put(XWSSecurityConfiguration.MESSAGE_SECURITY_CONFIGURATION, config);

            //Ping myping = new Ping();
            //myping.setText("Hello !");
            //myping.setTicket(ticket);
            //stub.ping(myping);
            Holder<String> hold = new Holder("Hello !");
            stub.ping(ticket, hold);
            
        } catch (Exception ex) {
            System.out.println ("Caught Exception: " + ex.getMessage() );
            ex.printStackTrace();
        }
    }
}
