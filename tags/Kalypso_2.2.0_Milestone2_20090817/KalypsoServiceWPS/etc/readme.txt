If you want to run the server in a tomcat, add the config directives to the config.ini there.

# Location of the Server, where the simulations will run: No default.
org.kalypso.service.wps.service=[your service url]

# Location for the results: No default.
org.kalypso.service.wps.results=[your result directory]

# Replacement for the client URL: No default.
# org.kalypso.service.wps.client.replacement=[your replacement]

If you run in a local Jetty with eclipse, add the directives to the paramaters of the VM like this:

-Dorg.kalypso.service.wps.service=[your service url] -Dorg.kalypso.service.wps.results=[your result directory]

For use of the SendMailJob there are three other directives.

# The server, to which the emails will be send, he will do the redirection: No default.
org.kalypso.wps.sendmail.relais=[your relais] 

# The username for the relais server: No default.
# org.kalypso.wps.sendmail.relais.username=[your username]

# The password for the relais server: No default.
# org.kalypso.wps.sendmail.relais.password=[your password]

To a Jetty add

-Dorg.kalypso.wps.sendmail.relais=[your relais]