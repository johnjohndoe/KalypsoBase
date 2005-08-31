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
* @author SAAJ Development Team
*/
public class ProcessingStates {
    public static final ProcessingStates CONTINUE = new ProcessingStates();
    public static final ProcessingStates STOP = new ProcessingStates();
    public static final ProcessingStates HEADER_DONE = new ProcessingStates();
    public static final ProcessingStates FAULT = new ProcessingStates();
    
    private ProcessingStates() {
    }
}
