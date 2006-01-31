/*
 * $Id$
 * $Revision$
 * $Date$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

public interface SOAPListener {
	public void initMsgLoop(int port, SOAPCallback callback) throws IOException;
	public void startMsgLoop() throws IOException, ClosedChannelException,
		InterruptedException;
	public void startMsgLoopInThread();
	public void stopMsgLoopInThread();
}
