/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.soap;

/**
 * @author XWS-Security Development Team
 */
public interface ProcessingFaultHandler {
    public void handleIncomingFault(ProcessingContext context);
    public void handleOutgoingFault(ProcessingContext context);
}
