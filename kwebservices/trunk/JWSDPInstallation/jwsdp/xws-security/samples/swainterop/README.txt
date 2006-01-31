Copyright 2004 Sun Microsystems, Inc. All rights reserved.
SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.

Example: swainterop

This sample demonstrates support for OASIS WSS SWA Profile 1.0 in XWS-Security.

Note: OASIS WSS SWA Token Profile 1.0 spec is still undergoing changes.

The application prints out both the client and server request and response
SOAP messages.  The output from the server may be viewed in the appropriate 
container's log file.  The output from the client may be viewed using stdout.

In this example, server-side code is found in the server/src/swainterop/
directory.  Client-side code is found in the client/src/swainterop/
directory.  The ASant or Ant targets build objects under the /build/server/ 
and /build/client/ directories.  You can view other useful ASant or Ant 
targets by entering "ant" or "asant" at the command line in the swainterop 
directory.

To run the sample, run
ant run-sample-[n] where n = scenario 1, 2, 3, 4