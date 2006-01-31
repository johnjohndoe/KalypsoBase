Copyright 2004 Sun Microsystems, Inc. All rights reserved.
SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.

Example: samlinterop

This sample demonstrates support for OASIS WSS SAML Token Profile 1.0 in XWS-Security.

The application prints out both the client and server request and response
SOAP messages.  The output from the server may be viewed in the appropriate 
container's log file.  The output from the client is sent to stdout or to whichever 
stream is used by the configured log Handler. Messages are logged at the INFO level.

In this example, server-side code is found in the /samlinterop/server/src/sample/
directory.  Client-side code is found in the /samlinterop/client/src/sample/
directory.  The ASant or Ant targets build objects under the /build/server/ 
and /build/client/ directories.

To run the sample:
-------------------
1.ant run-sample

You can view other useful ASant or Ant targets by entering "ant" or "asant" at the command line in the /samlinterop directory.


Plugging in Different Security Configurations
---------------------------------------------
                                                                                                            
This example makes it simple to plug in different client and server-
side configurations describing security settings. 
                                                                                                            
1. To set up the security configuration that you want to run for
the client, set the client.security.config property to one of the
security configurations discussed in "Security Configuration Options".

# Client Security Config. file (uncomment one of)
# the config file pairs (sv-saml-client.xml, sv-saml-server.xml),
# (sv-saml-client3.xml, sv-saml-server3.xml), (hok-saml-client.xml, hok-saml-server.xml)
# model the OASIS SAML interop scenarios 1,3 and 4 respectively
client.security.config=config/sv-saml-client.xml
#client.security.config=config/sv-saml-client3.xml
#client.security.config=config/hok-saml-client.xml


2. To set up the security configuration that you want to run for
the server, set the server.security.config property to one of the

security configurations discussed in "Security Configuration Options".
# Server Security Config. file (uncomment corresponding server config)
server.security.config=config/sv-saml-server.xml
#server.security.config=config/sv-saml-server3.xml
#server.security.config=config/hok-saml-server.xml
