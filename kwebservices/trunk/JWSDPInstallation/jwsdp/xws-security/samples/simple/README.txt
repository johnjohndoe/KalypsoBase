Copyright 2004 Sun Microsystems, Inc. All rights reserved.
SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.

Example: simple

This example is a fully-developed sample application that demonstrates
various configurations that can be used to exercise XML and Web Services
Security (xws-security) framework code.  By modifying one property in the 
build.properties file for the example, you can change the type of security
that is being used for the client and/or the server.  The types of security
configurations possible in this example include Digital Signature (signing),
XML Encryption (encrypt), and username-token verification.  This example
allows and demonstrates combinations of these basic security mechanisms
through the specification of the appropriate security configuration files.

The application prints out both the client and server request and response
SOAP messages.  The output from the server may be viewed in the appropriate 
container's log file. The output from the client is sent to stdout or to whichever 
stream is used by the configured log Handler. Messages are logged at the INFO level.

In this example, server-side code is found in the /simple/server/src/simple/
directory.  Client-side code is found in the /simple/client/src/simple/
directory.  The ASant or Ant targets build objects under the /build/server/ 
and /build/client/ directories.  You can view other useful ASant or Ant 
targets by entering "ant" or "asant" at the command line in the /simple/ 
directory.

Plugging in Different Security Configurations
---------------------------------------------

This example makes it simple to plug in different client and server-
side configurations describing security settings.  This example
has support for digital signatures, XML encryption/decryption, and 
username/token verification.  This example allows and demonstrates
combinations of these basic security mechanisms through configuration 
files. To set up a different security configuration, open the 
build.properties file for the example ($JWSDP_HOME/xws-security/
samples/simple/build.properties).  

1. To set up the security configuration that you want to run for 
the client, set the client.security.config property to one of the
security configurations discussed in "Security Configuration Options".  

For example,
# Client Security Config. file
client.security.config=config/encrypt-client.xml

2. To set up the security configuration that you want to run for 
the server, set the server.security.config property to one of the
security configurations discussed in "Security Configuration Options".  

For example,
# Server Security Config. file
server.security.config=config/encrypt-server.xml

Security Configuration Options
------------------------------

The configuration files available for this example are located in the
/xws-security/samples/simple/config directory.  The configuration 
pairs available under this sample by default are described here.

 1. dump-client.xml, dump-server.xml: This pair has no security operations,
it just dumps the request before it leaves the client and dumps the response
upon receipt from the server. 

The container's server logs also contain the dumps of the server
request and response.

2. encrypt-client.xml, encrypt-server.xml: This pair encrypts the request
body and sends it. The server decrypts the request and then sends back an
encrypted response. The client then decrypts the same.

3. sign-client.xml, sign-server.xml: This pair signs the request body. The
server verifies the signature. The server's response body is similarly 
signed. The client verifies the signature over the body.

4. sign-encrypt-client.xml, sign-encrypt-server.xml: This pair first signs
and then encrypts the request body and sends it out. The server first
decrypts and then verifies the signature. The server's response is similarly
signed and then encrypted.

5. encrypt-sign-client.xml, encrypt-sign-server.xml: This pair first encrypts
the request body, then signs it and sends it out.  The server first verifies
the signature and then decrypts the request body.  The server's response is
encrypted and then signed.

6. sign-ticket-also-client.xml, sign-ticket-also-server.xml: An example 
that demonstrates signing of the ticket element which is present inside 
the message body.  The message body is also signed. The server simply 
verifies these signatures.

7. timestamp-sign-client.xml, timestamp-sign-server.xml: An example that
demonstrates adding a timestamp to the signature.

8. encrypt-using-symmkey-client.xml, dump-server.xml: Encrypt the request,
using a shared symmetric key.

The following configurations demonstrate username-password based
authentication:

9. user-pass-authenticate-client.xml, user-pass-authenticate-server.xml: 
Add a username password token.  The username and password would be 
authenticated by the server against its user/password database.

10. encrypted-user-pass-client.xml, encrypted-user-pass-server.xml: 
Add a Username Password Token, and then encrypt the username token before 
sending out the request.

11. encrypt-usernameToken-client.xml, encrypt-usernameToken-server.xml:
Encrypt the request, then add a username token before sending the request.

The following configurations demonstrate adding XWS-Security at the method
level:

12. method-level-client.xml, method-level-server.xml: Adds security to a 
particular method.

13. flexiblec.xml, flexibles.xml : Shows how flexibility in receiver side
requirement processing w.r.t <wsu:Timestamp> and <wsse:UsernameToken> elements.
In particular the Timestamp and UsernameToken can appear anywhere in the 
incoming message whereas a strict ordering is inferred on the receiver 
between the <ds:Signature> and <xenc:Encrypt> elements.
