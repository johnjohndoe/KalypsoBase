Copyright 2004 Sun Microsystems, Inc. All rights reserved.
SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.

Example: interop

This example is a fully-developed sample application that demonstrates
various configurations that can be used to exercise XML and Web Services
Security (xws-security) framework code.  The types of security
configurations possible in this example include Digital Signature (signing),
XML Encryption (encrypt), and username-token verification.  This example
makes use of the xws-security framework to implement the 7 Web Services
Security (WSS) interop scenarios.

Under the WSS interop scenarios, developers can send and
receive messages compliant with the WSS Soap Message Security
specification. Developers can use the framework to implement
applications which have security requirements similar to those defined
in the WSS interop scenarios. More information on the scenarios can be
found at http://lists.oasis-open.org/archives/wss/200306/msg00002.

The application prints out both the client and server request and response
SOAP messages.  The output from the server may be viewed in the appropriate 
container's log file.  The output from the client may be viewed using stdout.

In this example, server-side code is found in the /interop/server/src/interop/
directory.  Client-side code is found in the /interop/client/src/interop/
directory.  The ASant or Ant targets build objects under the /build/server/ 
and /build/client/ directories.  You can view other useful ASant or Ant 
targets by entering "ant" or "asant" at the command line in the /interop/ 
directory.

This example can be deployed onto any of the following containers:
1. Sun Java System Application Server PE 8 (SJSAS 8)
http://wwws.sun.com/software/products/appsrvr/home_appsrvr.html
2. SJSWS 6.1 (Sun Java System WebServer 6.1)
http://wwws.sun.com/software/products/web_srvr/home_web_srvr.html
3. The Apache Tomcat WebServer
http://jakarta.apache.org/tomcat/

This example uses keystores and truststores which are included in
the /xws-security/etc/ directory.  For more information on using
keystore and truststore files, read the keytool documentation
at http://java.sun.com/j2se/1.4.2/docs/tooldocs/solaris/keytool.html.


Configuring a JCE Provider
--------------------------

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
Edition version 1.4.2 (if you have not done this, you can download
it from http://java.sun.com/j2se/1.4.2/index.jsp).  Point to the 
location of this installation when setting up the environment variable
for JAVA_HOME and for the javahome property in the build.properties
file.

2. Read and set up environment variables as described in the file
/xws-security/docs/samples.html.

3. In the directory xws-security/samples/interop, copy the
file build.properties.sample to build.properties.

4. Many of the properties in this file need to be configured 
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
http.proxyhost=<proxy_server_address>
http.proxyport=<proxy_server_port>

5. Open the file /xws-security/samples/interop/build.xml.  Find
the prepare target.  Verify that this target is configured to use the 
appropriate container for your system.  If the target is not configured
correctly for your system, replace the existing value with the appropriate
one.  The choices are: 
   &sjsas;
   &sjsws;
   &tomcat;

6. Point the container on which you will run the example to the keystore
and truststore files provided for this example.  To do this, follow the
steps listed here for the appropriate container.

Note:
    * Incase the container is already running, you will need to restart the
      container after making these changes.  As a side effect of setting 
      these properties the Container would now uses these keystores for its 
      SSL handshake.
    * The user may choose to use any other set of client and server keystores
      and truststores while running this sample (including the ones that
      come default with the container).


6a. If you are running the samples on SJSAS, edit the following JVM options 
in <sjsas.home>/domains/domain1/config/domain.xml.  These currently point to
default keystore and truststore files for SJSAS 8 and need to be changed
to point to the keystore and truststore files for this example.

<jvm-options>-Djavax.net.ssl.keyStore=${com.sun.aas.instanceRoot}/../../
   xws-security/etc/server-keystore.jks</jvm-options>
<jvm-options>-Djavax.net.ssl.trustStore=${com.sun.aas.instanceRoot}/../../
   xws-security/etc/server-truststore.jks</jvm-options>

6b.If you are running the samples on Tomcat, add a secure SSL connector to point to the keystore and truststore files.


   <!-- Define a SSL Coyote HTTP/1.1 Connector on port 8443 -->
   <Connector className="org.apache.coyote.tomcat5.CoyoteConnector"
              port="8443" minProcessors="5" maxProcessors="75"
              enableLookups="true" disableUploadTimeout="true"
              acceptCount="100" debug="0" scheme="https" secure="true"
              keystoreFile=
              "/opt/tomcat-jwsdp-1.4/xws-security/etc/server-keystore.jks"
              truststoreFile=
              "/opt/tomcat-jwsdp-1.4/xws-security/etc/server-truststore.jks"
              >

6c. If you are running the samples on SJSWS, add the following JVM options to 
<sjsws.home>/<Virtual-Server-Instance-Dir>/config/server.xml 

<JVMOPTIONS>-Djavax.net.ssl.keyStore=<sjsws.home>/xws-security/etc/server-keystore.jks</JVMOPTIONS>
<JVMOPTIONS>-Djavax.net.ssl.trustStore=<sjsws.home>/xws-security/etc/server-truststore.jks</JVMOPTIONS>


Running the sample:
-------------------

Follow these instructions to run the example after completing the 
steps in the Configuring section.

1. Start the selected container and make sure the server is running.
To start the Application Server, 

  From a command prompt: asadmin start-domain domain1
  From a Windows system: Start->Programs->Sun Microsystems->
        J2EE 1.4 SDK->Start Default Server
        
2. If you are using a proxy server, you must add the proxy information
to the run-client targets.    There are 7 run-client targets, and all 
targets must have these lines added to the run-client targets.  To
modify these targets, open the file /interop/build.xml. The run-client 
target definitions look like this:
   <target name="run-client1" ....> 

Add the following lines specifying the proxy information to the 
run-client targets:

   <sysproperty key="http.proxyHost" value="${http.proxyHost}"/>
   <sysproperty key="http.proxyPort" value="${http.proxyPort}"/>


3. Build and run the client application as follows:
   % asant run-all (on SJSAS)
   OR
   % ant run-all (on SJSWS or Tomcat)

4. Build and run an individual interop scenario using the following
command, where X is replaced by the number of the scenario to be run:
   % asant run-clientX (on SJSAS)
   for example, asant-run-client1, to run interop scenario 1

   OR

   % ant run-clientX (on SJSWS  or Tomcat)
   for example, ant run-client2, to run interop scenario 2
 

Note: To run the sample against a remote server containing the deployed
endpoint, make use of the interop-clientX ant target's instead of run-ClientX
targets.  Make sure that the properties endpoint.host, endpoint.port and service.url are set correctly and also ensure that the properties http.proxyHost and
http.proxyPort are set correctly before running the sample.

Note: When the server is a remote server and the application is already
available (deployed on the remote server),then the interop-clientX ant target's
can be used instead of the run-ClientX targets.

Results:
--------
You should see a message similar to the following for a successful run:
     [echo] Running the client program....
     [java] ==== Request Start ====
     ...
     [java] ==== Request End ====
     [java] ==== Response Start ====
     ...
     [java] ==== Response End ====
     [java] Hello to Duke! 

You can also see similar messages in the server logs at 
    ${sjsas.home}/domains/<domain-name>/logs/server.log (for SJSAS)
    ${tomcat.home}/logs/launcher.server.log (for Tomcat)
    ${sjsws.home}/<Virtual-Server-Dir>/logs/errors (for SJSWS)
