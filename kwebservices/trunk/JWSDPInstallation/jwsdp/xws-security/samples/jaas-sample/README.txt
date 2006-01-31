Copyright 2004 Sun Microsystems, Inc. All rights reserved.
SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.

Example: jaas-sample

This sample demonstrates the following

1. Obtaining a UserName, Password at runtime, and sending it in a WSS UsernameToken to the server. 
2. How the server application can use Jaas Authentication to authenticate the Username-Password 
   it received.
3. How the EndPoint Implementation methods can access the senders Subject after authentication.

 
The application prints out both the client and server request and response
SOAP messages.  The output from the server may be viewed in the appropriate 
container's log file.  The output from the client is sent to stdout or to whichever 
stream is used by the configured log Handler. Messages are logged at the INFO level.

In this example, server-side code is found in the /jaas-sample/server/src/sample/
directory.  Client-side code is found in the /jaas-sample/client/src/sample/
directory.  The ASant or Ant targets build objects under the /build/server/ 
and /build/client/ directories.  You can view other useful ASant or Ant 
targets by entering "ant" or "asant" at the command line in the /jaas-sample/ 
directory.

The default username and password that has been supplied 
is present in the build.xml line 315 as follows
 <sysproperty key="username.password" value="Ron noR"/>

The username and password are separated using a " " (a blank). 

The JAAS Login module also makes use of a username-password list file that lists the
set of valid usernames along with their passwords. 

The file userpasslist.xml should be present in the directory <container.home>/xws-security/etc. 
Where <container.home> denotes one of SJSAS, SJSWS or Tomcat instances onto which JWSDP
was installed.

1. If the container being used in SJSAS or SJSWS, then add the following JAAS policy to
the corresponding JAAS policy file of the server. 

/** Login Configuration for the Sample Application **/
XWS_SECURITY_SERVER {
   com.sun.xml.wss.sample.UserPassLoginModule REQUIRED debug=true;
};

 if the container is SJSAS then add this Login Configuration to <SJSAS.home>/domains/domain1/config/login.conf towards the end of the file.


 if the container is SJSWS then add this Login Configuration to <SJSWS.home>/<Virtual-Server-Dir>/config/login.conf file.
 Where <Virtual-Server-Dir> denotes the name of the Virtual Server which was specified during installation.


2. If the container being used is SJSAS, add the following pemissions into the <SJSAS.home>/domains/domain1/config/server.policy file of Application server.

grant codeBase "file:${com.sun.aas.instanceRoot}/applications/j2ee-modules/jaassample/WEB-INF/-" {
      	permission javax.security.auth.AuthPermission "modifyPrincipals";
	permission javax.security.auth.AuthPermission "modifyPrivateCredentials";
	permission javax.security.auth.PrivateCredentialPermission "* * \"*\"","read";
	permission javax.security.auth.AuthPermission "getSubject";
	permission javax.security.auth.AuthPermission "createLoginContext.other";
        permission javax.security.auth.AuthPermission "createLoginContext.XWS_SECURITY_SERVER";
};

  If the container being used is SJSWS, then add the following permissions into the 
  <SJSWS.home>/<Virtual-Server-Dir>/config/server.policy file.

grant codeBase "file:${com.sun.web.instanceRoot}/webapps/<Virtual-Server-Dir>/jaassample/WEB-INF/-" {
      	permission javax.security.auth.AuthPermission "modifyPrincipals";
	permission javax.security.auth.AuthPermission "modifyPrivateCredentials";
	permission javax.security.auth.PrivateCredentialPermission "* * \"*\"","read";
	permission javax.security.auth.AuthPermission "getSubject";
	permission javax.security.auth.AuthPermission "createLoginContext.other";
        permission javax.security.auth.AuthPermission "createLoginContext.XWS_SECURITY_SERVER";
};

Where <Virtual-Server-Dir> denotes the name of the Virtual Server which was specified during installation.
