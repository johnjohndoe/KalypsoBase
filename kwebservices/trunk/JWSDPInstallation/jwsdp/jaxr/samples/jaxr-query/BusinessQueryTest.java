/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import java.io.*;
import java.util.*;

public class BusinessQueryTest {
    // edit these if behind firewall, otherwise leave blank
    String httpProxyHost = "";
    String httpProxyPort = "";
    String regUrli  = "";
    String company ="";
    Properties connProps = new Properties();

    private static final String QUERY_URL = "query.url";
    private static final String PROXY_HOST = "http.proxy.host";
    private static final String PROXY_PORT = "http.proxy.port";
    private static final String COMPANY ="company";
    public static void main(String[] args) {
        String company = "%USA%";
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("./query.properties"));
            BusinessQueryTest bqt = new BusinessQueryTest();
            bqt.executeQueryTest(properties);
        } catch (JAXRException e){
	    System.err.println("Error during the test: " + e);
        } catch (IOException ioe) {
            System.err.println("Can not open properties file");
        }
        
    }
    
    public void executeQueryTest(Properties properties)
        throws JAXRException {
            try {
                assignUserProperties(properties);
                setConnectionProperties();
		
                ConnectionFactory factory = ConnectionFactory.newInstance();
                factory.setProperties(connProps);
                Connection conn = factory.createConnection();
                RegistryService rs = conn.getRegistryService();
                BusinessQueryManager bqm = rs.getBusinessQueryManager();
                
                
                ArrayList names = new ArrayList();
                names.add(company);
                
                Collection fQualifiers = new ArrayList();
                fQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
                
                BulkResponse br = bqm.findOrganizations(fQualifiers,
                    names, null, null, null, null);
                
                if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                    System.out.println("Successfully queried the " +
                       "registry for organization matching the " +
                       "name pattern: \"" + company + "\""); 
                    Collection orgs = br.getCollection();
                    System.out.println("Results found: " + orgs.size() + "\n");
                    Iterator iter = orgs.iterator();
                    while (iter.hasNext()) {
                        Organization org = (Organization) iter.next();
                        System.out.println("Organization Name: " +
                            getName(org));
                        System.out.println("Organization Key: " +
                            org.getKey().getId());
                        System.out.println("Organization Description: " +
                            getDescription(org));
                        
                        Collection services = org.getServices();
                        Iterator siter = services.iterator();
                        while (siter.hasNext()) {
                            Service service = (Service) siter.next();
                            System.out.println("\tService Name: " +
                                getName(service));
                            System.out.println("\tService Key: " +
                                service.getKey().getId());
                            System.out.println("\tService Description: " +
                                getDescription(service));
                        }
                    }
                } else {
		    System.err.println("One or more JAXRExceptions " +
		        "occurred during the query operation:");
		    Collection exceptions = br.getExceptions();
		    Iterator iter = exceptions.iterator();
		    while (iter.hasNext()) {
			Exception e = (Exception) iter.next();
			System.err.println(e.toString());
		    }
		}
            } catch (JAXRException e) {
                e.printStackTrace();
            }
    }
    
    private void assignUserProperties(Properties props) {
        String temp;
        
        temp = ((String)props.get(QUERY_URL)).trim();
        if (temp != null)
            regUrli = temp;
       
        temp = ((String)props.get(PROXY_HOST)).trim();
        if (temp != null)
            httpProxyHost = temp;
        
        temp = ((String)props.get(PROXY_PORT)).trim();
        if (temp != null)
            httpProxyPort = temp;
	temp = ((String)props.get(COMPANY)).trim();
	if(temp != null)
	    company = temp;
	else company = "%USA%";
    }
    
    private void setConnectionProperties() {
	connProps.setProperty("javax.xml.registry.queryManagerURL", regUrli);
        connProps.setProperty("javax.xml.registry.factoryClass",
                    "com.sun.xml.registry.uddi.ConnectionFactoryImpl");
        connProps.setProperty("com.sun.xml.registry.http.proxyHost", httpProxyHost);
	connProps.setProperty("com.sun.xml.registry.http.proxyPort", httpProxyPort);
    }
    
    private String getName(RegistryObject ro) throws JAXRException {
        try {
            return ro.getName().getValue();
        } catch (NullPointerException npe) {
            return "";
        }
    }
    
    private String getDescription(RegistryObject ro) throws JAXRException {
        try {
            return ro.getDescription().getValue();
        } catch (NullPointerException npe) {
            return "";
        }
    }
}
