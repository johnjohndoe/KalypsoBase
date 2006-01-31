/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.messaging.soap;

import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPException;

public abstract class SOAPAnnotator {

    public abstract SOAPHeader annotateHeader(SOAPHeader hdr)
        throws SOAPException;

}
