/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package simple;

import javax.xml.namespace.QName;
import javax.xml.rpc.*;

public class TestClient {    

    private static final QName portName = 
        new QName("http://xmlsoap.org/Ping", "Ping");

    public static void main(String[] args) throws Exception {    
        PingService pingService = new PingService_Impl();
        // use static stubs to override endpoint property of WSDL       
        String serviceHost = System.getProperty("endpoint.host");
        String servicePort = System.getProperty("endpoint.port");
        String serviceURLFragment = System.getProperty("service.url");
        String serviceURL = 
            "http://" + serviceHost + ":" + servicePort + serviceURLFragment;

        System.out.println("Service URL=" + serviceURL);

        PingPort_Stub stub = (PingPort_Stub)  
                    (pingService.getPing());           

        stub._setProperty(  
            javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, serviceURL);   

        stub.ping(new TicketType(null, "SUNW"), "Hello !"); 
        //stub.ping(new TicketType("Id", "SUNW"), "Hello !"); 
    }
}
