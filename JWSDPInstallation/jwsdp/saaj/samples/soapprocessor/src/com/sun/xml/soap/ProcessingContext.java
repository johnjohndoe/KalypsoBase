/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.soap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author SAAJ RI Development Team
 */
public class ProcessingContext {
    protected Map properties = new HashMap();
    
    public ProcessingContext() {
        setProperty(SOAPProcessorConstants.STATE_PROPERTY, ProcessingStates.CONTINUE);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public void removeProperty(String name) {
        properties.remove(name);
    }

    public boolean containsProperty(String name) {
        return properties.containsKey(name);
    }

    public java.util.Iterator getPropertyNames() {
        return properties.keySet().iterator();
    }

}
