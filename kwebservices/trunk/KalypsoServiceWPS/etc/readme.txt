If you want to run the server in a tomcat, add the config directives to the config.ini there.

# Location of the Server, where the simulations will run: No default.
org.kalypso.service.wps.service=http://localhost/ogc

# Location for the results: No default.
org.kalypso.service.wps.results=C:/temp

# Replacement for the client URL: No default.
# org.kalypso.service.wps.client.replacement=

If you run in a local Jetty with eclipse, add the directives to the paramaters of the VM like this:

-Dorg.kalypso.service.wps.service=http://localhost/ogc -Dorg.kalypso.service.wps.results=C:/temp