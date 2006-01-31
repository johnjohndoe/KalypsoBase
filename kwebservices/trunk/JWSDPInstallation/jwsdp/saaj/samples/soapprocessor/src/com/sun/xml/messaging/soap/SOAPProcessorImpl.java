/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.messaging.soap;

import java.util.Iterator;
import javax.xml.soap.*;

public class SOAPProcessorImpl extends SOAPProcessor {

    private String ultimateReceiverURL = 
        "http://schemas.xmlsoap.org/soap/actor/ultimateReceiver";

    /**
     * Implementation for SOAP 1.1
     */
    public SOAPMessage acceptMessage(SOAPMessage message) 
        throws SOAPException {

        SOAPHeader header = message.getSOAPHeader();
        Iterator headerElements = header.getChildElements();
        for (; headerElements.hasNext(); ) {
            SOAPHeaderElement elem = (SOAPHeaderElement)headerElements.next();
            String role = getRoleAttributeValue(elem);        
            boolean elementProcessed = false;
            boolean isTargeted = false;
            for (Iterator itr = recipients.iterator(); itr.hasNext(); ) {
                SOAPRecipient recp = (SOAPRecipient)itr.next();
                if (recp.supportsRole(role)) {
                    isTargeted = true; 
                    if (recp.supportsHeader(elem.getTagName())) {
                        recp.processHeaderElement(elem);
                        elementProcessed = true;
                    }
                }
            }
            if (!elementProcessed && isTargeted && elem.getMustUnderstand()) {
                // Generate FAULT
                generateFault(message, role);
            }
            if (isTargeted && !role.equals(ultimateReceiverURL))
                elem.detachNode();
        }
        message.saveChanges();
        return message;
    }

    public SOAPMessage prepareMessage(SOAPMessage message)
        throws SOAPException {

        SOAPHeader header = message.getSOAPHeader();
        for (Iterator it = annotators.iterator(); it.hasNext();) {
            SOAPAnnotator annotator = (SOAPAnnotator)it.next();    
            annotator.annotateHeader(header);
        }
        message.saveChanges();
        return message;    
    }

    /**
     * Implementation for SOAP 1.1
     * recognize both actor and role attributes ?
     */
    protected String getRoleAttributeValue(SOAPHeaderElement element) {
        String ret = element.getActor();
        // set it to ultimateReceiver ?
        if (ret.equals("") || ret == null)
            ret = ultimateReceiverURL;
        return ret;
    }

    private void generateFault(SOAPMessage message, String role)
        throws SOAPException {
        SOAPBody body = message.getSOAPBody();
        Iterator eachChild = body.getChildElements();
        while (eachChild.hasNext()) {
            SOAPBodyElement bodyElement = (SOAPBodyElement)eachChild.next();
            bodyElement.detachNode();
        }
        
        SOAPFault fault = body.addFault();
        String soapPrefix = body.getPrefix();
        fault.setFaultCode(soapPrefix + ":" + "mustUnderstand");
        fault.setFaultString("One or more mandatory SOAPHeader blocks " +
                             "not understood");
        fault.setFaultActor(role);
    }
}
