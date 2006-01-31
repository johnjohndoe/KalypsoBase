
Copyright 2004 Sun Microsystems, Inc. All rights reserved.
SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.

This file describes how to setup and run the XWS-Security samples.

The following samples can be run by following the steps indicated in this README.txt.

1. simple : Sample demonstrating various security configurations for client and server
2. swainterop : Sample that implements the interop scenarios defined for the WSS SWA profile 
   interop event.
3. samlinterop : Sample that implements the interop scenarios defined for the WSS SAML Token
   profile 1.0  interop event.
4. dynamic-response : Sample that demonstrates how the XWS-Security Callback Handler mechanism can
   be used to dynamically obtain the encryption key for encrypting the response back to the requester. 
5. dynamicpolicy : Sample that demonstrates how Security Policies can be set/changed dynamically through
   the callback handler mechanisms.
6. jaas-sample : Sample that demonstrates how JAAS Authentication can be plugged-in to validate a 
   WSS UsernameToken.
7. api-sample : Sample that demonstrates the use of Programmatic APIs for Security in Non JAXRPC and
   SAAJ like environments.
8. saajsecurity : Sample that demonstrates the use of Programmatic APIs to secure an end-to-end
   Servlet based application that uses SOAP messages for communication.
9. jaxws2.0 : Samples demonstrating how to secure jaxws scenarios.

These samples can be deployed onto any of the following containers:
1. Sun Java System Application Server PE 8.X and 9.0
http://wwws.sun.com/software/products/appsrvr/home_appsrvr.html
2. SJSWS 6.1 (Sun Java System WebServer 6.1)
http://wwws.sun.com/software/products/web_srvr/home_web_srvr.html
3. The Apache Tomcat WebServer
http://jakarta.apache.org/tomcat/

These samples use keystores and truststores which are included in
the /xws-security/etc/ directory.  For more information on using
keystore and truststore files, read the keytool documentation
at http://java.sun.com/j2se/1.4.2/docs/tooldocs/solaris/keytool.html.

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
        by the application server.  Set the value for the JCE provider 
        you've just added to 2.  Keep the Sun security provider at the 
        highest preference, with a value of 1.  
                security.provider.1=sun.security.provider.Sun

        Adjust the levels of the other security providers accordingly.

Configuring the Sample
-----------------------

1. copy the file build.properties.sample to build.properties and set the following properties in it.

2.  Make sure that you have installed the Java 2 Platform, Standard 
Edition version 1.4.2 or above (if you have not done this, you can download
it from http://java.sun.com/j2se/1.4.2/index.jsp).  Point to the 
location of this installation when setting up the environment variable
for JAVA_HOME and for the javahome property in the build.properties
file.

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

#VS.DIR : virtual server name needed for SJSWS only
VS.DIR=<Virtual-Server-Directory>

# The location where JWSDP is installed used by the client.
# The keystore and truststore URL's for the client are configured 
# relative to this property. 
jwsdp.home=<directory_location_of_your_JWSDP_installation>

# proxy server settings in case of remote endpoints
http.proxyHost=<proxy_server_address>
http.proxyPort=<proxy_server_port>

Running the sample:
-------------------

Follow these instructions to run the samples after completing the 
steps in the Configuring section.

1. Change directory to the sample which you wish to run.
2. Start the selected container and make sure the server is running.
To start the Application Server, 

  From a command prompt: asadmin start-domain domain1
  From a Windows system: Start->Programs->Sun Microsystems->
        J2EE 1.4 SDK->Start Default Server
        
3. Modify the sample's build.properties file to set up the security 
configuration that you want to run for the client and/or 
server. Look for a section titled "Plugging in Different Security 
Configurations" for more information.

4. Build and run the client application as follows:
   % asant run-sample (on SJSAS)
   OR
   % ant run-sample (on SJSWS or Tomcat)
 
Note: To run the sample against a remote server containing the deployed
endpoint, make use of the run-remote-sample ant target instead of run-sample.
Make sure that the properties endpoint.host, endpoint.port and service.url are
set correctly and also ensure that the properties http.proxyHost and
http.proxyPort are set correctly before running the sample.

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
