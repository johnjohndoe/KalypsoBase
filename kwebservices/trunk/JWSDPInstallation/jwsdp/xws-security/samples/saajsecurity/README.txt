Copyright 2004 Sun Microsystems, Inc. All rights reserved.
SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.

Example: saajsecurity

This sample is a fully developed sample application that demonstrates 
security features available to secure a standlone SAAJ application. By modifying
the client-config.xml and server-config.xml files in /conf directory you can
change the type of security that is being used for the client and/or server.

The application prints out both the client and server request and response
SOAP messages.  The output from the server may be viewed in the appropriate 
container's log file. The output from the client is sent to stdout or to 
whichever stream is used by the configured log Handler. Messages are logged at the INFO level.

To run the sample, execute the run-sample ant target.
