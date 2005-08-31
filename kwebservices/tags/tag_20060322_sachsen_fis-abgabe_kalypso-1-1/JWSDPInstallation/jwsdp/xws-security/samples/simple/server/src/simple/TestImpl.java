/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package simple;

import javax.xml.rpc.ServiceException;

public class TestImpl extends PingImpl {

    /* (non-Javadoc)
     * @see javax.xml.rpc.server.ServiceLifecycle#init(java.lang.Object)
     */
    public void init(Object context) throws ServiceException {    
        super.init(context);
    }
}
