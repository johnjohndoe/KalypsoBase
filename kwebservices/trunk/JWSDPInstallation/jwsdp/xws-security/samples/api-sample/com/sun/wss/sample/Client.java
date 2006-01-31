package com.sun.wss.sample;

import java.io.FileInputStream;
import java.io.File;
import javax.xml.soap.*;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.SubjectAccessor;

import com.sun.xml.wss.XWSSProcessorFactory;
import com.sun.xml.wss.XWSSProcessor;


/**
 * This sample demonstrates usage of XWSS 2.0 Security Api's to secure soap
 * messages.
 */

public class Client {
    
    public static void main(String[] args) throws Exception {
        
        
        FileInputStream clientConfig = null;
        FileInputStream serverConfig = null;
        try {
            //read client side security configuration
            clientConfig = new java.io.FileInputStream(
                    new java.io.File(System.getProperty("client.configfile")));
            //read server side security configuration
            serverConfig = new java.io.FileInputStream(
                    new java.io.File(System.getProperty("server.configfile")));
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
        
        //Create XWSSProcessor to veriy incomming soap messages.
        //Sample SecurityEnvironment is configured to
        //use server side keystores.
        
        XWSSProcessor sprocessor =
                factory.createProcessorForSecurityConfiguration(
                serverConfig, new SecurityEnvironmentHandler("server"));
        try{
            clientConfig.close();
            serverConfig.close();
        }catch(Exception ex){
            ex.printStackTrace();
           return;
        }
        
        for(int i=0;i<1;i++){
            
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
            
            //verify the secured message.
            context = new ProcessingContext();
            context.setSOAPMessage(secureMsg);
            
            
            
            SOAPMessage verifiedMsg= null;
            try{
                verifiedMsg= sprocessor.verifyInboundMessage(context);
                //System.out.println("\nRequester Subject " + SubjectAccessor.getRequesterSubject(context));
                
            }catch(Exception ex){
                ex.printStackTrace();
                //context.getSOAPMessage().writeTo(System.out);
            }
        }
    }
}
