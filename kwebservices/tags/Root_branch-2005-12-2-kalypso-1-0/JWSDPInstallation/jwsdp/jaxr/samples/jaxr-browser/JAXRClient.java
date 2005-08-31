/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.net.*;
import java.util.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/**
 * Contains the JAXR client code used by the browser
 */
public class JAXRClient {

    /* These can be hardcoded, or they may be set by the main()
     * method of RegistryBrowser.java at runtime. Default is no proxy.
     */
    String httpProxyHost = "";
    String httpProxyPort = "";
    String httpsProxyHost = "";
    String httpsProxyPort = "";

    Connection connection;
    BusinessQueryManager bqManager;
    BusinessLifeCycleManager blcManager;

    /**
     * Makes a connection to a JAXR Registry.
     *
     * @param url The URL of the registry.
     */
    void makeNewConnection(String url) {
	try {
	    Properties props = new Properties();
	    props.setProperty("javax.xml.registry.queryManagerURL", url);
	    props.setProperty("javax.xml.registry.lifeCycleManagerURL", url);

	    // if these are empty strings or null, no proxy is used
	    props.setProperty("com.sun.xml.registry.http.proxyHost", httpProxyHost);
	    props.setProperty("com.sun.xml.registry.http.proxyPort", httpProxyPort);
	    props.setProperty("com.sun.xml.registry.https.proxyHost", httpsProxyHost);
	    props.setProperty("com.sun.xml.registry.https.proxyPort", httpsProxyPort);

	    ConnectionFactory connFactory = ConnectionFactory.newInstance();
            connFactory.setProperties(props);
            connection = connFactory.createConnection();
	    
	    RegistryService service = connection.getRegistryService();
	    bqManager = service.getBusinessQueryManager();
	    blcManager = service.getBusinessLifeCycleManager();
	} catch (JAXRException e) {
	    RegistryBrowser.displayError(ResourceBundle.getBundle("BrowserStrings").getString("Error_creating_new_Connection."), e);
	}
    }

    /**
     * returns the business life cycle query manager. 
     * This should go away once the client code has all been moved here.
     */
    BusinessLifeCycleManager getBusinessLifeCycleManager() {
	return blcManager;
    }

    /**
     * Find classification schemes
     */
    Collection getClassificationSchemes() {
	String errMsg = ResourceBundle.getBundle("BrowserStrings").getString("Error_getting_ClassificationSchemes");

	// include these classification schemes
	Collection schemeNames = new ArrayList();
	schemeNames.add("naics");
	schemeNames.add("iso-ch");
    schemeNames.add("unspsc-org:unspsc:3-1");
	Iterator nameIter = schemeNames.iterator();

	// look up schemes and add to collection
	String schemeName = null;
	ClassificationScheme scheme = null;
	Collection schemes = new ArrayList();
	while(nameIter.hasNext()) {
	    try {
		schemeName = (String) nameIter.next();
		scheme = bqManager.findClassificationSchemeByName(null, schemeName);
		
		if (scheme != null) {
		    schemes.add(scheme);
		}
	    } catch (JAXRException e) {
		RegistryBrowser.displayError(errMsg, e);
		System.err.println(e);
		schemes = new ArrayList();
	    }
	}
	return schemes;
    }

    /**
     * Find organizations given a business name.
     */
    Collection getOrganizations(String organizationName) {
	String errMsg = ResourceBundle.getBundle("BrowserStrings").getString("Error_occurred_finding_Organizations");
	Collection organizations = null;
	try {
	    
	    // create namePattern collection
	    Collection names = new ArrayList();
	    names.add(organizationName);

	    // make JAXR request
	    BulkResponse response = 
		bqManager.findOrganizations(null, names, null, null, null, null);

	    // check for errors
	    Collection exceptions = response.getExceptions();
	    if (exceptions != null) {
		Iterator iter = exceptions.iterator();
		Exception exception = null;
		while (iter.hasNext()) {
		    exception = (Exception) iter.next();
		    RegistryBrowser.displayError(errMsg, exception);
		}
	    }

	    // collection may be empty if there were errors
	    organizations = response.getCollection();
	} catch (JAXRException e) {
	    RegistryBrowser.displayError(errMsg, e);
	    organizations = new ArrayList();
	}
	return organizations;
    }

    /**
     * Find organizations given a collection of concepts (??)
     *
     * @param concepts A collection of Concepts. See
     * RegistryObjectsTableModel.java for an example of
     * creating Concepts.
     */
    Collection getOrganizations(Collection concepts) {
	String errMsg = ResourceBundle.getBundle("BrowserStrings").getString("Error_occurred_finding_Organizations");
	Collection organizations = null;
	try {
	    
	    // create Classifications from Concepts
	    Collection classifications = new ArrayList();
	    Classification classification = null;
	    Concept concept = null;
	    Iterator iter = concepts.iterator();
	    while (iter.hasNext()) {
		concept = (Concept) iter.next();
		classification =
		    blcManager.createClassification(concept);
		classifications.add(classification);
	    }

	    // make JAXR request
	    BulkResponse response = 
		bqManager.findOrganizations(null, null,
					    classifications,
					    null, null, null);

	    // check for errors
	    Collection exceptions = response.getExceptions();
	    if (exceptions != null) {
		iter = exceptions.iterator();
		Exception exception = null;
		while (iter.hasNext()) {
		    exception = (Exception) iter.next();
		    RegistryBrowser.displayError(errMsg, exception);
		}
	    }

	    // collection may be empty if there were errors
	    organizations = response.getCollection();
	} catch (JAXRException e) {
	    RegistryBrowser.displayError(errMsg, e);
	    organizations = new ArrayList();
	}
	return organizations;
    }

    /**
     * Creates an Organization and fills in information
     * from the browser gui components, then checks authentication
     * information, submits the organization, and returns
     * a String containing the Organization Key Id if successful.
     *
     * The method does not check to see if the organization
     * currently exists in the registry; rather, it submits a
     * new organization each time.
     *
     * @param submissionPanel GUI component that contains information
     * to be set in the Organization
     *
     * @return The organization key id string if submission is
     * successful.
     */
    String doSubmission(SubmissionPanel submissionPanel) {

        // check authorization before continuing
        if (setCredentials() == false) {
            return null;
        }

        String errMsg = ResourceBundle.getBundle("BrowserStrings").getString("Error_submitting_organization");
        String id = null;
        Collection classifications = null;
        Iterator iter1 = null;

        // first get the gui components that the user filled in
        RegistryObjectPanel objectPanel =
            submissionPanel.getRegistryObjectPanel();
        ServicesPanel servicesPanel =
            submissionPanel.getServicesPanel();
        ClassificationsPanel classificationsPanel = null;

        try {

            // create needed classes
            Organization organization = (Organization) blcManager.createObject(
                LifeCycleManager.ORGANIZATION);
            User user = blcManager.createUser();
            PersonName personName = (PersonName) blcManager.createObject(
                LifeCycleManager.PERSON_NAME);
            TelephoneNumber teleNumber =
                (TelephoneNumber) blcManager.createObject(
                    LifeCycleManager.TELEPHONE_NUMBER);
	    EmailAddress emailAddress =
		(EmailAddress) blcManager.createObject(
		     LifeCycleManager.EMAIL_ADDRESS);
            InternationalString orgName =
                blcManager.createInternationalString();
            InternationalString orgDescription =
                blcManager.createInternationalString();

            // add info to organization
            orgName.setValue(objectPanel.getOrganizationName());
            orgDescription.setValue(objectPanel.getOrganizationDescription());
            organization.setName(orgName);
            organization.setDescription(orgDescription);

	    // if name was left blank, don't submit contact info
	    if (objectPanel.getContactFullName().length() > 0) {
		personName.setFullName(objectPanel.getContactFullName());
		teleNumber.setNumber(objectPanel.getContactPhoneNumber());
		emailAddress.setAddress(objectPanel.getContactEmail());
		
		Collection numbers = new ArrayList();
		numbers.add(teleNumber);
		Collection emailAddresses = new ArrayList();
		emailAddresses.add(emailAddress);
		
		user.setPersonName(personName);
		user.setTelephoneNumbers(numbers);
		user.setEmailAddresses(emailAddresses);
		
		organization.setPrimaryContact(user);
	    }

            /* add classifications to organization
	     * (could pull the blcManager createClassification call
	     * out of ClassificationsPanel and into utility method in client)
	     */
            classificationsPanel = objectPanel.getClassificationsPanel();
            classifications = classificationsPanel.getClassifications();
            if (classifications.size() > 0) {
                organization.setClassifications(classifications);
            }

            // create services
            Collection serviceInputPanels =
                servicesPanel.getServiceInputPanels();
            Collection services = new ArrayList();
            iter1 = serviceInputPanels.iterator();
            ServiceInputPanel servicePanel = null;
            Service service = null;
            InternationalString iString = null;
            while (iter1.hasNext()) {
                service =
                    (Service) blcManager.createObject(LifeCycleManager.SERVICE);
                servicePanel = (ServiceInputPanel) iter1.next();

                // add info to service
                iString = blcManager.createInternationalString(
                    servicePanel.getServiceName());
                service.setName(iString);
                iString = blcManager.createInternationalString(
                    servicePanel.getServiceDescription());
                service.setDescription(iString);

                classificationsPanel = servicePanel.getClassificationsPanel();
                classifications = classificationsPanel.getClassifications();
                service.addClassifications(classifications);

                // create service bindings
                ServiceBindingsPanel bindingsPanel =
                    servicePanel.getBindingsPanel();
                Collection bindingInputPanels =
                    bindingsPanel.getBindingsInputPanels();
                ArrayList serviceBindings = new ArrayList();
		Iterator iter2 = bindingInputPanels.iterator();
                ServiceBindingInputPanel bindingPanel = null;
                while (iter2.hasNext()) {
                    bindingPanel = (ServiceBindingInputPanel) iter2.next();
                    ServiceBinding binding = blcManager.createServiceBinding();
                  iString = blcManager.createInternationalString(
                        bindingPanel.getBindingDescription());
                    binding.setDescription(iString);
                    binding.setAccessURI(bindingPanel.getAccessURI());
                    serviceBindings.add(binding);
                }

                // add service bindings to service
                if (serviceBindings.size() > 0) {
                    service.addServiceBindings(serviceBindings);
                }
                services.add(service);
            }

            // add services to organization
            if (services.size() > 0) {
                organization.addServices(services);
            }

            // submit organization
	    Iterator iter = null;
            Collection orgs = new ArrayList();
            orgs.add(organization);
            RegistryBrowser.setWaitCursor();
            BulkResponse response = blcManager.saveOrganizations(orgs);
	    Collection exceptions = response.getExceptions();
	    if (exceptions != null) {
		iter = exceptions.iterator();
		Exception exception = null;
		while (iter.hasNext()) {
		    exception = (Exception) iter.next();
		    RegistryBrowser.displayError(errMsg, exception);
		}
	    }
	    
            // may be empty if there was an error
            Collection keys = response.getCollection();
            iter = keys.iterator();
            Key key = null;
            if (iter.hasNext()) { // only expecting one key
                key = (Key) iter.next();
                id = key.getId();
                organization.setKey(key);
            }
            
            // this will fill in the new key information in the gui
            submissionPanel.editOrganization(organization);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(errMsg, e);
            System.err.println(e);
        } finally {
	    RegistryBrowser.setDefaultCursor();
	}
        return id;
    }

    /**
     * Delete an organization from the registry.
     *
     * @return Whether or not deleting the organization
     * was successful.
     */
    boolean deleteOrganization(Organization org) {
        String errMsg = ResourceBundle.getBundle("BrowserStrings").getString("Error_deleting_organization");
        boolean success = false;
        if (setCredentials() == false) {
            return false;
        }
        try {
            RegistryBrowser.setWaitCursor();
            ArrayList keys = new ArrayList();
            keys.add(org.getKey());
            
            // delete orgs
            BulkResponse response = blcManager.deleteOrganizations(keys);

            // check for errors
            Collection exceptions = response.getExceptions();
	    if (exceptions != null) {
		Iterator iter = exceptions.iterator();
		Exception exception = null;
		while (iter.hasNext()) {
		    exception = (Exception) iter.next();
		    RegistryBrowser.displayError(errMsg, exception);
		}
	    } else {
                success = true;
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(errMsg, e);
            System.err.println(e);
        } finally {
	    RegistryBrowser.setDefaultCursor();
	}
        return success;
    }
    
    /*
     * If the credentials have not already been set on
     * the connection, this method has the submission panel
     * prompt the user for username and password, and then
     * uses these to set the credentials. If credentials are
     * accepted with no errors, the method returns true.
     *
     * @return Whether the connection's credentials are
     * properly set.
     */
    private boolean setCredentials() {
        String errMsg = ResourceBundle.getBundle("BrowserStrings").getString("Error_authenticating_user");
        
        // panel used for getting user information
        SubmissionPanel submissionPanel =
            RegistryBrowser.getInstance().getSubmissionPanel();
        try {
		 /* 
             * Bug Id: 4922247, 4922260
             * Correction: creds!=null
             * Author: Anil Tappetla
            */

            Set creds = connection.getCredentials();
            if (creds!=null && creds.size() > 0) {
                return true;
            }
            
            PasswordAuthentication passwordAuth =
                submissionPanel.getUserAuthentication();
	    
	    // if user hit "Cancel"
	    if (passwordAuth == null) {
		return false;
	    }

            RegistryBrowser.setWaitCursor();
            Set credentials = new HashSet();
            credentials.add(passwordAuth);
            connection.setCredentials(credentials);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(errMsg, e);
            System.err.println(e);
            return false;
        } finally {
	    
	    // assuming an exception will be thrown if invalid
	    RegistryBrowser.setDefaultCursor();
	}
        return true;
    }
    
}
