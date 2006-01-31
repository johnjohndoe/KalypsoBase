
/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package saaj.security;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.xml.soap.*;

import javax.activation.*;
import javax.naming.*;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.SubjectAccessor;
import com.sun.xml.wss.*;

import java.net.*;
import java.io.*;
import java.util.*;

import javax.servlet.http.*;
import javax.servlet.*;

public class SoapProcessorSecurity {

    public static void main(String args[]) throws Exception {

        FileInputStream clientConfig = null;

        try {
            //read client side security configuration
            clientConfig = new java.io.FileInputStream(
                    new java.io.File(System.getProperty("client.configfile")));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        //Create a XWSSProcessFactory.
        XWSSProcessorFactory factory = XWSSProcessorFactory.newInstance();

        //Create XWSSProcessor to secure out going soap messages.
        //Sample SecurityEnvironment is configured to
        //use client side keystores.

        XWSSProcessor cprocessor =
                factory.createProcessorForSecurityConfiguration(
                clientConfig, new SecurityEnvironmentHandler("client"));

        try{
            clientConfig.close();
        }catch(Exception ex){
            ex.printStackTrace();
           return;
        }

         // create SOAPMessage
         SOAPMessage msg = MessageFactory.newInstance().createMessage();
         SOAPBody body = msg.getSOAPBody();
         SOAPBodyElement sbe = body.addBodyElement(
                    SOAPFactory.newInstance().createName(
                    "StockSymbol",
                    "tru",
                    "http://fabrikam123.com/payloads"));
         sbe.addTextNode("QQQ");

         //Create processing context and set the soap
         //message to be processed.
         ProcessingContext context = new ProcessingContext();
         context.setSOAPMessage(msg);

         //secure the message.
         SOAPMessage secureMsg = cprocessor.secureOutboundMessage(context);
         secureMsg.saveChanges();

         // Create a SOAP connection
         SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
         SOAPConnection con = scf.createConnection();

         String to = System.getProperty("to");
         System.out.println("Sending request to : " + to);

	 SOAPMessage reply = con.call(secureMsg, new URL(to));

         System.out.println("Done Sending request to : " + to);

         SOAPBody receiveMsg = reply.getSOAPBody();
         if (receiveMsg.hasFault() ) {
  	     SOAPFault newFault = body.getFault();
	     System.out.println(newFault.getFaultString());
	 } else {
	     System.out.println("Successfully executed");
             SOAPMessage verifiedMsg= null;
             context = new ProcessingContext();
             context.setSOAPMessage(reply);
             try{
                   System.out.println("Before calling Verify");
                   verifiedMsg= cprocessor.verifyInboundMessage(context);
                   System.out.println("Finished calling Verify");

                }catch(Exception ex){
                    ex.printStackTrace();
                    SOAPBody bodyRecvdMsg = verifiedMsg.getSOAPBody(); 
                    SOAPFault fault = bodyRecvdMsg.addFault();
                    fault.setFaultString("Message can not be parsed.");
                    throw new ServletException("Message can not be parsed");
                }
	 }
    }
}
