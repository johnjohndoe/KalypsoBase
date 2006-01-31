/*
 * $Id$
 * $Revision$
 * $Date$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import javax.xml.soap.SOAPMessage;

public interface SOAPCallback {
	public void onMessage(SOAPMessage msg);
}
