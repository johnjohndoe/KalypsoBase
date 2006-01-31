Copyright 2004-2005 Sun Microsystems, Inc. All rights reserved.
SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.

Example: api-sample

This sample demonstrates how the XWS-Security APIs can be used by standalone 
(non-JAX-RPC) SAAJ users to secure and validate SOAPMessages.

This sample makes use of the  XWSSProcessorFactory and XWSSProcessor interface. 
The Client (com.sun.wss.sample.Client) code makes use of the XWSSProcessor interface 
to secure SOAP Messages.

How to run the sample.
-----------------------
1.ant run-sample


Plugging in Different Security Configurations 
---------------------------------------------

The following pairs of security configuration files work for this
example.  The following list provides the client configuration first, 
then the server configuration:

   sign-rsign.xml, no_security.xml
   username.xml, no_security.xml
   encryptv1.xml, no_security.xml
   encryptv2.xml, no_security.xml
   signv1.xml, no_security.xml
   signv2.xml, no_security.xml
   signv3.xml, no_security.xml
   signv4.xml, no_security.xml
   str_transform.xml, no_security.xml

To set up the security configuration that you want to run for 
the client, set the client.configfile property to one of the
security configurations in build.proeprties.
Similarly for the server, set the server.configfile property in build.properties

Results:
--------
You should see a message similar to the following for a successful run:

run-sample:
     [echo] Running the com.sun.wss.sample.Client program....
     [java]  Secure Message Start .........
     [java] <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"><SOAP-ENV:Header>
     [java] <wsse:Security SOAP-ENV:mustUnderstand="1" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
     ........
     [java]  Secure Message End .........

     [java]  Verified Message Start .........
     [java] <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"><SOAP-ENV:Header>
     [java] </SOAP-ENV:Header><SOAP-ENV:Body xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" wsu:Id="XWSSGID-1115460936690589590130"><tru:StockSymbol xmlns:tru="http://fabrikam123.com/payloads">QQQ</tru:StockSymbol></SOAP-ENV:Body></SOAP-ENV:Envelope>
     [java]  Verified Message End .........

     [java] Requester Subject Subject:
     [java] 	Principal: EMAILADDRESS=NA, CN=xws-security-client, OU=NA, O=NA, L=NA, ST=NA, C=NA
     [java] 	Public Credential: [
     [java]........ ]

