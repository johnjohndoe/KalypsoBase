/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.soap;

import javax.xml.soap.SOAPHeader;

public abstract class SOAPAnnotator implements ProcessingFaultHandler{

    public abstract void annotateHeader(SOAPHeader header, ProcessingContext context)
        throws Exception;

}
