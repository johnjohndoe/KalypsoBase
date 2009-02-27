/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.soap;

/**
*
* @author SAAJ RI Development Team
*/
public class SOAPProcessorFactory {
    public static SOAPProcessor createSOAPProcessor() {
        return new SOAPProcessorImpl();
    }
}
