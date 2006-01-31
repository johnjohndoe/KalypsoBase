/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package saaj.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import java.io.FileInputStream;
import java.io.File;
import javax.xml.soap.*;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.SubjectAccessor;

import com.sun.xml.wss.*;


public class ReceivingServlet extends HttpServlet {

    MessageFactory msgFactory;
    InputStream serverConfigFile = null;
    MimeHeaders headers;
    XWSSProcessorFactory factory = null;
    XWSSProcessor sprocessor = null;
    
    public void init(ServletConfig servletConfig) throws ServletException {

        super.init(servletConfig);
        
        java.util.jar.JarFile warFile = null;
	try {
            msgFactory = MessageFactory.newInstance();
            ServletContext servletContext = servletConfig.getServletContext();
            String home = System.getProperty("jwsdp.home");
            
            serverConfigFile = servletContext.getResourceAsStream("/server-config.xml");
            System.out.println("serverConfig=" + serverConfigFile);
            
            //Create a XWSSProcessFactory.
            factory = XWSSProcessorFactory.newInstance();
            
            //Create XWSSProcessor to verify incoming soap messages.
            //Sample SecurityEnvironment is configured to
            //use server side keystores.                                                                                                          
             sprocessor =
                    factory.createProcessorForSecurityConfiguration(
                    serverConfigFile, new SecurityEnvironmentHandler("server"));

        } catch (Exception e) {
          e.printStackTrace();
          throw new ServletException(e);
        }
    }
    
    public void doPost( HttpServletRequest req, 
			HttpServletResponse resp) throws ServletException, IOException
       {
            System.out.println("Server got the message");
	    // Get all the headers from the HTTP request.
	    headers = getHeaders(req);

	    // Get the body of the HTTP request.
	    InputStream is = req.getInputStream();
	     

	    // Now internalize the contents of a HTTP request and
	    // create a SOAPMessage
	    SOAPMessage msg = null;
	    SOAPMessage reply = null; 
	    try {
	        msg = msgFactory.createMessage(headers, is);
	        reply = onMessage(msg);
                
                // Return the response : secured message
	        putHeaders(reply.getMimeHeaders(), resp);

                ServletOutputStream sOutputStream = resp.getOutputStream();
                reply.writeTo(sOutputStream);
	        sOutputStream.flush();
	    } catch (Exception e) {
	        throw new ServletException("Problem in processing received message");
	    }
    }

    static void putHeaders(MimeHeaders headers, HttpServletResponse res) {

        Iterator it = headers.getAllHeaders();
        while (it.hasNext()) {
            MimeHeader header = (MimeHeader)it.next();

            String[] values = headers.getHeader(header.getName());
            if (values.length == 1)
                res.setHeader(header.getName(), header.getValue());
            else {
                StringBuffer concat = new StringBuffer();
                int i = 0;
                while (i < values.length) {
                    if (i != 0)
                        concat.append(',');
                    concat.append(values[i++]);
                }

                res.setHeader(header.getName(),
                concat.toString());
            }
        }
    }

    static MimeHeaders getHeaders(HttpServletRequest req) {

	Enumeration enum1 = req.getHeaderNames();
	MimeHeaders headers = new MimeHeaders();

	while (enum1.hasMoreElements()) {
	    String headerName = (String)enum1.nextElement();
	    String headerValue = req.getHeader(headerName);
	    StringTokenizer values = new StringTokenizer(headerValue, ",");
	    while (values.hasMoreTokens())
		headers.addHeader(headerName, values.nextToken().trim());
	}

	return headers;
    }

    public SOAPMessage onMessage(SOAPMessage message) throws Exception, SOAPException, ServletException { 
                                                                                                          
        try{
            serverConfigFile.close();
        }catch(Exception ex){
           ex.printStackTrace();
	   SOAPBody body = message.getSOAPBody(); 
	   SOAPFault fault = body.addFault();
	   fault.setFaultString("Message can not be parsed.");
	   throw new ServletException("Message can not be parsed");
        }

        //Create processing context and set the soap
        //message to be processed.
        ProcessingContext context = new ProcessingContext();
        context.setSOAPMessage(message);

        SOAPMessage verifiedMsg= null;
        try{
            System.out.println("Before calling Verify");
            verifiedMsg= sprocessor.verifyInboundMessage(context);
            System.out.println("Finished calling Verify");
            //System.out.println("\nRequester Subject " + SubjectAccessor.getRequesterSubject(context));
                                                                                                          
        }catch(Exception ex){
            ex.printStackTrace();
            //context.getSOAPMessage().writeTo(System.out);
	    SOAPBody body = verifiedMsg.getSOAPBody(); 
	    SOAPFault fault = body.addFault();
	    fault.setFaultString("Message can not be parsed.");
	    throw new ServletException("Message can not be parsed");
        }

        verifiedMsg = context.getSOAPMessage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        verifiedMsg.writeTo(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        SOAPMessage sMsg = MessageFactory.newInstance().createMessage(verifiedMsg.getMimeHeaders(), bais);
        context = new ProcessingContext();
        context.setSOAPMessage(sMsg);
        SOAPMessage secureMsg = sprocessor.secureOutboundMessage(context);
        secureMsg.saveChanges();
        
        return secureMsg;
    }
}
