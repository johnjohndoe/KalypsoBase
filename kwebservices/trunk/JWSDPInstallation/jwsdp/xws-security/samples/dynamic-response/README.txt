Copyright 2004 Sun Microsystems, Inc. All rights reserved.
SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.

Example: dynamic-response

This sample demonstrates using the certificate that arrived in a signed
request to encrypt the response back to the requestor.

 
The application prints out both the client and server request and response
SOAP messages.  The output from the server may be viewed in the appropriate 
container's log file.  The output from the client is sent to stdout or to whichever 
stream is used by the configured log Handler. Messages are logged at the INFO level.

In this example, server-side code is found in the /dynamic-response/server/src/sample/
directory.  Client-side code is found in the /dynamic-response/client/src/sample/
directory.  The ASant or Ant targets build objects under the /build/server/ 
and /build/client/ directories.  You can view other useful ASant or Ant 
targets by entering "ant" or "asant" at the command line in the /dynamic-response/ 
directory.

This example can be deployed onto any of the following containers:
1. Sun Java System Application Server PE 8.X (SJSAS 8.X)
http://wwws.sun.com/software/products/appsrvr/home_appsrvr.html
2. SJSWS 6.1 (Sun Java System WebServer 6.1)
http://wwws.sun.com/software/products/web_srvr/home_web_srvr.html
3. The Apache Tomcat WebServer
http://jakarta.apache.org/tomcat/


Configuring a JCE Provider
--------------------------

If you are using JDK 1.5 (Tiger RC) then there is no need to configure a JCE provider
and this section can be skipped.

A Java Cryptography Extension (JCE) provider for J2SE 1.4 must be available
for these sample applications to run.  You must download and install this
JCE provider from one of the sources listed below because the JCE provider included
with JDK 1.4.x does not support RSA encryption.

To add a JCE provider statically as part of your JDK environment:

        1. Download a JCE provider jar. You will find a link to
        some JCE providers at this URL:

                http://java.sun.com/products/jce/jce14_providers.html

        2. Follow the instructions for installing the JAR file on its
        Web site.
        3. Copy the provider jar to the following directory:
                 $JAVA_HOME/jre/lib/ext
        (where $JAVA_HOME is the location of your Java 2 Standard 
        Edition installation)
        4. Edit the $JAVA_HOME/jre/lib/security/java.security 
        properties file in any text editor.  Add the JCE provider you've 
        just downloaded to this file.  

        The java.security file contains detailed instructions for adding 
        this provider. Basically, you need to add a line of the following 
        format in the location with similar properties: 
                security.provider.<n>=<provider class name>

        In the above example,  <n> is the order of preference to be used 
        by the application server.  Set the valude for the JCE provider 
        you've just added to 2.  Keep the Sun security provider at the 
        highest preference, with a value of 1.  
                security.provider.1=sun.security.provider.Sun

        Adjust the levels of the other security providers accordingly.

Configuring the Sample
-----------------------

Follow these steps to configure the example prior to running it.

1.  Make sure that you have installed the Java 2 Platform, Standard 
Edition version 1.5 or 1.4.2 (if you have not done this, you can download
it from http://java.sun.com/j2se).  Point to the 
location of this installation when setting up the environment variable
for JAVA_HOME and for the javahome property in the build.properties
file.

2. In the directory xws-security/samples/dynamic-response, copy the
file build.properties.sample to build.properties.

3. Many of the properties in this file need to be configured 
specifically for your system.  Find the following properties and
enter the correct value for your system.  

# java and jwsdp
javahome=<directory_location_of_your_J2SE_installation>

#container homes (uncomment the appropriate container home property)
#Note: only one of the 3 below should be uncommented at any point of time
sjsas.home=<directory_location_of_your_SJSAS_installation>
#tomcat.home=<directory_location_of_your_Tomcat_installation>
#sjsws.home=<directory_location_of_your_SJSWS_installation>

# Replace username and password values with the user name and
# password for a user assigned to the role of admin for
# the container instance being used for this sample
username=<user>
password=<password>

# the host and port for the server endpoint, for the default
# SJSAS installation, these values will be correct
endpoint.host=localhost
endpoint.port=8080

#place where standalone JWSDP is installed
jwsdp.home=<location of standalone jwsdp>

#VS.DIR : virtual server name needed for SJSWS only
VS.DIR=<Virtual-Server-Directory>

# proxy server settings in case of remote endpoints
http.proxyHost=<proxy_server_address>
http.proxyPort=<proxy_server_port>

4. If the container being used is SJSAS, add the following pemissions into the <SJSAS.home>/domains/domain1/config/server.policy file of Application server.

grant codeBase "file:${com.sun.aas.instanceRoot}/applications/j2ee-modules/dynamicresponse/WEB-INF/-" {
      	permission javax.security.auth.AuthPermission "modifyPrincipals";
	permission javax.security.auth.AuthPermission "modifyPrivateCredentials";
	permission javax.security.auth.PrivateCredentialPermission "* * \"*\"","read";
	permission javax.security.auth.AuthPermission "getSubject";
};

  If the container being used is SJSWS, then add the following permissions into the 
  <SJSWS.home>/<Virtual-Server-Dir>/config/server.policy file.

grant codeBase "file:${com.sun.web.instanceRoot}/webapps/<Virtual-Server-Dir>/dynamicresponse/WEB-INF/-" {
      	permission javax.security.auth.AuthPermission "modifyPrincipals";
	permission javax.security.auth.AuthPermission "modifyPrivateCredentials";
	permission javax.security.auth.PrivateCredentialPermission "* * \"*\"","read";
	permission javax.security.auth.AuthPermission "getSubject";
};

Where <Virtual-Server-Dir> denotes the name of the Virtual Server which was specified during installation.


Running the sample:
-------------------

Follow these instructions to run the example after completing the 
steps in the Configuring section.

1. Start the selected container and make sure the server is running.
To start the Application Server, 

  From a command prompt: asadmin start-domain domain1
  From a Windows system: Start->Programs->Sun Microsystems->
        J2EE 1.4 SDK->Start Default Server
        
2. Modify the build.properties as described above

3. Build and run the client application as follows:
   % asant run-sample (on SJSAS)
   OR
   % ant run-sample (on SJSWS or Tomcat)
 
Note: To run the sample against a remote server containing the deployed
endpoint, make use of the run-remote-sample ant target instead of run-sample.
Make sure that the properties endpoint.host, endpoint.port and service.url are
set correctly and also ensure that the properties http.proxyHost and
http.proxyPort are set correctly before running the sample remotely.


Results:
--------
You should see a message similar to the following for a successful run:
     [echo] Running the client program....
     [java] ==== Sending Message Start ====
     ...
     [java] ==== Sending Message End ====
     [java] ==== Received Message Start ====
     ...
     [java] ==== Received Message End ====
     [java] Hello to Duke! 


You can also see similar messages in the server logs at 
    ${sjsas.home}/domains/<domain-name>/logs/server.log (for SJSAS)
    ${tomcat.home}/logs/launcher.server.log (for Tomcat)
    ${sjsws.home}/<Virtual-Server-Dir>/logs/errors 
