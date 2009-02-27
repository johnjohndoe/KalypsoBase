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
container's log file.  The output from the client may be viewed using stdout.

In this example, server-side code is found in the /simple/server/src/simple/
directory.  Client-side code is found in the /simple/client/src/simple/
directory.  The ASant or Ant targets build objects under the /build/server/ 
and /build/client/ directories.  You can view other useful ASant or Ant 
targets by entering "ant" or "asant" at the command line in the /simple/ 
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

3. In the directory xws-security/samples/simple, copy the
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
http.proxyHost=<proxy_server_address>
http.proxyPort=<proxy_server_port>

5. Open the file /xws-security/samples/simple/build.xml.  Find
the prepare target.  Verify that this target is configured to use the 
appropriate container for your system.  If the target is not configured
correctly for your system, replace the existing value with the appropriate
one.  The choices are: 
   &sjsas;
   &sjsws;
   &tomcat;

6. Point the container on which you will run the example to the keystore
and truststore files provided for this example.  This task is same as
setting up the SSL configuration for the container.  To do this, follow the
steps listed here for the appropriate container.

6a. If you are running the samples on SJSAS, edit the following JVM options 
in <sjsas.home>/domains/domain1/config/domain.xml.  These currently point to
default keystore and truststore files for SJSAS 8 and need to be changed
to point to the keystore and truststore files for this example.

<jvm-options>-Djavax.net.ssl.keyStore=${com.sun.aas.instanceRoot}/../../
   xws-security/etc/server-keystore.jks</jvm-options>
<jvm-options>-Djavax.net.ssl.trustStore=${com.sun.aas.instanceRoot}/../../
   xws-security/etc/server-truststore.jks</jvm-options>

6b.If you are running the samples on Tomcat, add a secure SSL connector to
point to the keystore and truststore files, in the
<tomcat.home>/conf/server.xml file.


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
        
2. Modify the build.properties file to set up the security 
configuration that you want to run for the client and/or 
server. See the section titled "Plugging in Different Security 
Configurations" for more information.

3. Build and run the client application as follows:
   % asant run-sample (on SJSAS)
   OR
   % ant run-sample (on SJSWS or Tomcat)
 
Note: To run the sample against a remote server containing the deployed
endpoint, make use of the run-remote-sample ant target instead of run-sample.
Make sure that the properties endpoint.host, endpoint.port and service.url are
set correctly and also ensure that the properties http.proxyHost and
http.proxyPort are set correctly before running the sample.


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

6. sign-ticket-also-client.xml, dump-server.xml: An example that demonstrates
signing of the ticket element which is present inside the message body.  The
message body is also signed. The server simpy verifies these signatures.


	The #7 and #8 configurations demonstrate username-password based
authentication. To run these it is required that the username database
of the server has been configured.  Following are the steps that need to be
performed for each of the three different containers before starting the
server.

	TOMCAT:
	-------
		Edit the <tomcat.home>/conf/tomcat-users.xml file using
	the following steps:

	1. Add a new role "xws-security-client".  This can be achieved by
           adding the following line in this file

		<role rolename="xws-security-client"/>

	2. To add a user to the database, assign it "xws-security-client"
           role, eg,

	<user username="Ron" password="noR" roles="xws-security-client"/>

	SJSAS (Sun Java System Application Server):
	-------------------------------------------
		Perform the following steps:

	1. Create a username-password file, say, xws-security-users.xml,
	   which should like the following

		<?xml version='1.0' encoding='utf-8'?>
		<xws-security-users>
			<user username="Ron" password="noR"/>
			<user username="Vishal" password="changeit"/>
		</xws-security-users>

	2. In <sjsas.home>/domains/domain1/config/domain.xml, add the
           following jvm option

		<jvm-options>-Dcom.sun.xml.wss.usersFile={location of xws-security-users.xml file}</jvm-options>

	SJSWS (Sun Java System Web Server):
	-----------------------------------
		The steps are the same as for the SJSAS, except that in this
	case the <sjsws.home>/<Virtual-Server-Instance-Dir>/config/server.xml
	needs to be updated and the following jvm option needs to be added

		<JVMOPTIONS>-Dcom.sun.xml.wss.usersFile={location of xws-security-users.xml file}</JVMOPTIONS>


7. user-pass-authenticate-client.xml, dump-server.xml: Add a username password
token.  The username and password would be authenticated by the server against
its user/password database.

8. encrypted-user-pass-also-client.xml, dump-server.xml: Add a Username Password Token, and then encrypt the username token before sending out the request.


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
