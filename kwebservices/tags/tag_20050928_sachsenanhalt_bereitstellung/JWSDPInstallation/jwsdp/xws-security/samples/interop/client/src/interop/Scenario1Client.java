/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package interop;

import java.security.cert.X509Certificate;
import javax.xml.namespace.QName;
import javax.xml.rpc.*;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.filter.*;
import com.sun.xml.wss.impl.DefaultSecurityEnvironmentImpl;
import com.sun.xml.rpc.security.SecurityConfigurator;


/*
 * Implementation of interop scenario # 1 client.
 */ 
public class Scenario1Client {    

    
    // turn this flag on to view messages on the wire
    private static boolean debug = true;
    private static final QName portName = 
        new QName("http://xmlsoap.org/Ping", "Ping1");


    private static void scenario1(PingService service) throws Exception {
        try {
            
            SecurityConfigurator secCfg =
                new SecurityConfigurator(service, portName);
            secCfg.addFilterForOutgoingMessages(
                new ExportUsernameTokenFilter("Chris", "sirhC", true, false));

            if (debug) {           
                // see what the request and response look like
                secCfg.addDumpRequest().addDumpResponse();            
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {    

        PingService pingService = new PingService_Impl();
        // use static stubs to override endpoint property of WSDL       
        String serviceHost = System.getProperty("endpoint.host");
        String servicePort = System.getProperty("endpoint.port");
        String serviceURLFragment = System.getProperty("scenario1.service.url");
        String serviceURL = 
            "http://" + serviceHost + ":" + servicePort + serviceURLFragment;

        System.out.println("Service URL=" + serviceURL);

        scenario1(pingService);
        PingPort_Ping1_Stub stub = (PingPort_Ping1_Stub)  
                    (pingService.getPing1());           

        stub._setProperty(  
            javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, serviceURL);   

        stub.ping(new TicketType("Id", "SUNW"), "Sun Microsystems says hello scenario 1!"); 
        
    }
}
