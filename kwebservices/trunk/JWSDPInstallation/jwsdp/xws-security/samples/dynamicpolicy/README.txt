Copyright 2004 Sun Microsystems, Inc. All rights reserved.
SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.

Example: dynamicpolicy

This sample demonstrates how the request and response security policies can be 
set/changed at Runtime from the SecurityEnvironmentHandler callback. 

Configuration file has two sections
1. Sender settings
2. Receiver settings

Client configuration file
-----------------------------
<Incomplete sender setting>
  - for each configuration element there will be one DynamicPolicyCallback at
    runtime with appropriate Policy (For example, in case of incomplete Sign 
    configuratio element there will be one DynamicPolicyCallback with SignaturePolicy)
<Empty receiver setting>
  - At runtime a DynamicPolicyCallback with DynamicSecurityPolicy as the
    policy is made.

Server configuration file
-----------------------------
<Empty sender setting>
  - At runtime a DynamicPolicyCallback with DynamicSecurityPolicy as the
    policy is made.
<Incomplete receiver setting>
  - for each configuration element there will be one DynamicPolicyCallback at
    runtime with appropriate Policy (For example, in case of incomplete Sign 
    configuration element there will be one DynamicPolicyCallback with SignaturePolicy)


The application prints out both the client and server request and response
SOAP messages.  The output from the server may be viewed in the appropriate 
container's log file.  The output from the client is sent to stdout or to whichever 
stream is used by the configured log Handler. Messages are logged at the INFO level.

In this example, server-side code is found in the /dynamicpolicy/server/src/sample/
directory.  Client-side code is found in the /dynamicpolicy/client/src/sample/
directory.  The ASant or Ant targets build objects under the /build/server/ 
and /build/client/ directories.  You can view other useful ASant or Ant 
targets by entering "ant" or "asant" at the command line in the /dynamicpolicy/ 
directory.
