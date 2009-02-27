/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package hello;  
                 
public class HelloClient {      

public static void main(String[] args) {    
    try 
    {   
        HelloIF_Stub stub = (HelloIF_Stub)  
        (new HelloWorldService_Impl().getHelloIFPort());   
        stub._setProperty(  
        javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY,   
        System.getProperty("endpoint"));       
        System.out.println(stub.sayHelloBack("JAXRPC Sample")); 
     } catch (Exception ex) {        
        ex.printStackTrace();       
     }       
   }   
}
