/**
  * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
  * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */

package swainterop;

import javax.xml.rpc.*;
import javax.xml.namespace.QName;
import com.sun.xml.messaging.saaj.soap.*;

import javax.xml.rpc.handler.MessageContext;
import javax.servlet.ServletContext;
import java.util.*; 
import javax.xml.soap.*;
import com.sun.xml.rpc.server.ServerPropertyConstants;
import java.awt.Image;
import javax.mail.internet.MimeUtility;

import javax.xml.transform.stream.StreamSource;
import com.sun.xml.wss.swa.MimeConstants;

import java.io.*;
        
public class TestClient {    
    
    public static void main(String[] args) throws Exception {    
        String serviceHost = System.getProperty("endpoint.host");
        String servicePort = System.getProperty("endpoint.port");
        String serviceURLFragment = System.getProperty("service.url");
        String serviceURL = 
            "http://" + serviceHost + ":" + servicePort + serviceURLFragment;

        System.out.println("Service URL=" + serviceURL);
     
        String scenarioNumber = args[0];
        if (args[0] == null ) {
            System.err.println("Scenario # must be specified in command line: " +
                                "\n" + "(scenario1|scenario2|...)");             
        }
        
        if (scenarioNumber.equalsIgnoreCase("scenario1")) {
            runClient1(serviceURL);
        } else if (scenarioNumber.equalsIgnoreCase("scenario2")) {
            runClient2(serviceURL);
        } else if (scenarioNumber.equalsIgnoreCase("scenario3")) {
            runClient3(serviceURL);
        } else if (scenarioNumber.equalsIgnoreCase("scenario4")) {
            runClient4(serviceURL);
        }
    }

    public static void runClient1(String serviceURL) 
        throws Exception {

        PingService pingService =  new PingService_Impl(); 

        PingPort_Ping1_Stub stub = (PingPort_Ping1_Stub) (pingService.getPing1());           

        stub._setProperty(  
            javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, serviceURL);   
           
        stub._setProperty(
             com.sun.xml.rpc.client.StubPropertyConstants.SET_ATTACHMENT_PROPERTY,
             addEncodedFoto());   
        
        stub.ping("Sun Microsystems Scenario #1!");  
    }
        
    public static void runClient2(String serviceURL) 
        throws Exception {

        PingService pingService =  new PingService_Impl(); 

        PingPort_Ping2_Stub stub = (PingPort_Ping2_Stub) (pingService.getPing2());           

        stub._setProperty(  
            javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, serviceURL);   
           
        stub._setProperty(
             com.sun.xml.rpc.client.StubPropertyConstants.SET_ATTACHMENT_PROPERTY,
             addFoto());   
        
        stub.ping("Sun Microsystems Scenario #2!");  
    }

    public static void runClient3(String serviceURL) 
        throws Exception {

        PingService pingService =  new PingService_Impl(); 

        PingPort_Ping3_Stub stub = (PingPort_Ping3_Stub) (pingService.getPing3());           

        stub._setProperty(  
            javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, serviceURL);   
           
        stub._setProperty(
             com.sun.xml.rpc.client.StubPropertyConstants.SET_ATTACHMENT_PROPERTY,
             addXMLAttachment());   
        
        stub.ping("Sun Microsystems Scenario #3!");  
    }

    public static void runClient4(String serviceURL) 
        throws Exception { 

        PingService pingService =  new PingService_Impl(); 

        PingPort_Ping4_Stub stub = (PingPort_Ping4_Stub) (pingService.getPing4());           

        stub._setProperty(  
            javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, serviceURL);   
           
        stub._setProperty(
             com.sun.xml.rpc.client.StubPropertyConstants.SET_ATTACHMENT_PROPERTY,
             addXMLAttachment());   
        
        stub.ping("Sun Microsystems Scenario #4!");  
    }

    private static ArrayList addXMLAttachment() throws Exception {
        AttachmentPart att = MessageFactory.
                               newInstance().
                                  createMessage().
                                       createAttachmentPart();

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>    <START><A>Hello</A><B>World</B></START>";
        StringReader reader = new StringReader(xml);
        StreamSource content = new StreamSource(reader);

        att.setContent(content, "text/xml");        
        att.setMimeHeader("Content-Transfer-Encoding", "8bit");        
        att.setMimeHeader(MimeConstants.CONTENT_ID,"<foobar>");        
        //        att.setContentId("<foobar>");
        
        ArrayList list = new ArrayList();
        list.add(att);
        return list;
    }

    private static ArrayList addEncodedFoto() throws Exception {
        String imageName = "logo.jpg";
        AttachmentPart att = MessageFactory.
                               newInstance().
                                  createMessage().
                                       createAttachmentPart();

        att.setContent(getImage(imageName), "image/jpeg");        
        att.setMimeHeader("Content-Transfer-Encoding", "base64");        
        att.setContentId("<foobar>");
        
        ArrayList list = new ArrayList();
        list.add(att);
        return list;
    }

    private static ArrayList addFoto() throws Exception {
        String imageName = "logo.jpg";
        AttachmentPart att = MessageFactory.
                               newInstance().
                                  createMessage().
                                       createAttachmentPart();

        att.setContent(getImage(imageName), "image/jpeg");        
        att.setMimeHeader("Content-Transfer-Encoding", "8bit");        
        att.setContentId("<foobar>");
        
        ArrayList list = new ArrayList();
        list.add(att);
        return list;
    }

    private static Image getImage(String imageName) throws Exception {
        InputStream is = TestClient.class.getClassLoader().getResourceAsStream(imageName);
        return javax.imageio.ImageIO.read(is);
    }
}
