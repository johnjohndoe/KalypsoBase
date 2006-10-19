To make ODS work within Tomcat and Win32, you'll need:
	- an install Tomcat 5 instance
	- the following bundles:
			* org.eclipse.equinox.servlet.api
			* org.eclipse.equinox.servlet.bridge.http
		from http://download.eclipse.org/eclipse/equinox/ 
		and place them in your eclipse plugins directory
	- bridge.war: Get it at 
			http://www.eclipse.org/equinox/server/downloads/bridge.war 
		(or from the lib-directory) and place it in your webapps directory. Start Tomcat 
		(if its not currently running) and the war-file will be extracted to a directory 
		named bridge.
	- Export the feature to TOMCAT_DIR/webapps/bridge/WEB-INF/eclipse - this might take
		up to 10 minutes
	- Deploy the newly exported feature by visiting
			http://localhost:8080/bridge/sp_redeploy
		in your browser
	- Start your ODS experience by visiting
			http://localhost:8080/ods/ods?SERVICE=ods&REQUEST=GetCapabilities

			
		
	