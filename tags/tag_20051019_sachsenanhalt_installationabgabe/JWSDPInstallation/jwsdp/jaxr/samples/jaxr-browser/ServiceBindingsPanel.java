/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/**
 */
public class ServiceBindingsPanel extends javax.swing.JPanel {
    
    private Service service;
    private boolean displayOnly;
    private LinkedList serviceBindings;
    private GridBagLayout gb;
    private GridBagConstraints c;
    private Window parentWindow;
    
    /**
     * no-arg constructor creates panel
     * for input.
     */
    public ServiceBindingsPanel() {
        this(null);
    }
    
    /**
     * Class Constructor
     */
    public ServiceBindingsPanel(Service service) {
        super();
        gb = new GridBagLayout();
        c = new GridBagConstraints();
        setLayout(gb);
        setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("BrowserStrings").getString("Service_Bindings")));
        try {
            if (service == null) {
                displayOnly = false;
                
                // will hold ServiceBindingInputPanels
                serviceBindings = new LinkedList();
            } else {
                displayOnly = true;
                this.service = service;
                Collection serviceBindings = service.getServiceBindings();
                if (serviceBindings == null || serviceBindings.isEmpty()) {
                    JLabel emptyLabel =
			new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("No_service_bindings_registered_for_this_service_") );
                    add(emptyLabel);
                } else {
                    String bindingDescription = null;
                    ServiceBinding serviceBinding = null;
                    JLabel descriptionLabel = null;
                    JLabel accessURILabel = null;
                    JLabel externalURLLabel = null;
                    JLabel urlLabel = null;
		    Collection specLinks = null;
		    SpecificationLink specLink = null;
                    Collection externalLinks = null;
                    ExternalLink externalLink = null;
                    
                    Iterator iter = serviceBindings.iterator();
                    int row = 0;
                    while (iter.hasNext()) {
                        serviceBinding = (ServiceBinding) iter.next();
                        bindingDescription = getDescription(serviceBinding);
                        if (bindingDescription.length() == 0) {
                            bindingDescription = ResourceBundle.getBundle("BrowserStrings").getString("no_description");
                        }
                        descriptionLabel =
			    new JLabel(bindingDescription);
                        RegistryBrowser.makeConstraints(c, 0, row++, 2, 1, 0.0,
			    0.5, GridBagConstraints.NONE,
			    GridBagConstraints.WEST);
                        c.insets = new Insets(0, 5, 0, 0);
                        gb.setConstraints(descriptionLabel, c);
                        add(descriptionLabel);
                        
                        urlLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Access_URI:"));
                        RegistryBrowser.makeConstraints(c, 0, row++, 1, 1, 0.0,
			    0.5,  GridBagConstraints.NONE,
			    GridBagConstraints.WEST);
                        c.insets = new Insets(0, 5, 0, 0);
                        gb.setConstraints(urlLabel, c);
                        add(urlLabel);
                        
                        accessURILabel =
			    new JLabel(serviceBinding.getAccessURI());
                        c.gridx = 1;
                        c.weightx = 0.9;
                        gb.setConstraints(accessURILabel, c);
                        add(accessURILabel);
                        
			specLinks = serviceBinding.getSpecificationLinks();
			Iterator specLinkIter = specLinks.iterator();
			while (specLinkIter.hasNext()) {
			    specLink = (SpecificationLink) specLinkIter.next();
			    externalLinks = specLink.getExternalLinks();
			    int numLinks = externalLinks.size();
			    int spaceNumber = -1;
                        
			    if (numLinks != 0) {
				Iterator externalLinkIter =
				    externalLinks.iterator();
				JLabel spacerLabel[] = new JLabel[numLinks];
                            
				while (externalLinkIter.hasNext()) {
				    urlLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("External_URL:"));
				    RegistryBrowser.makeConstraints(c, 0, row++,
				        1, 1, 0.0, 0.5, GridBagConstraints.NONE,
                                        GridBagConstraints.WEST);
				    c.insets = new Insets(0, 5, 0, 0);
				    gb.setConstraints(urlLabel, c);
				    add(urlLabel);
                                
				    externalLink =
					(ExternalLink) externalLinkIter.next();
				    externalURLLabel =
					new JLabel(
					    externalLink.getExternalURI());
				    c.gridx = 1;
				    c.weightx = 0.9;
				    gb.setConstraints(externalURLLabel, c);
				    add(externalURLLabel);
				    
				    spacerLabel[++spaceNumber] =
					new JLabel("  ");
				    RegistryBrowser.makeConstraints(c, 0, row++,
                                        2, 1, 0.0, 0.5, GridBagConstraints.NONE,
                                        GridBagConstraints.WEST);
				    c.insets = new Insets(0, 5, 0, 0);
				    gb.setConstraints(spacerLabel[spaceNumber],
						      c);
				    add(spacerLabel[spaceNumber]);
                                
				}
			    }
			}
                    }
                }
            }
        } catch (JAXRException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Add ServiceBindingInputPanel to panel.
     */
    void addBinding() {
        ServiceBindingInputPanel panel = new ServiceBindingInputPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        serviceBindings.add(panel);
        RegistryBrowser.makeConstraints(c, 0, serviceBindings.size()-1,
					1, 1, 1, 1, GridBagConstraints.HORIZONTAL,
					GridBagConstraints.NORTHWEST);
        gb.setConstraints(panel, c);
        add(panel);
        revalidate();
    }
    
    void removeServiceBinding(ServiceBindingInputPanel panel) {
        serviceBindings.remove(panel);
        remove(panel);
        revalidate();
        parentWindow.pack();
    }
    
    void setParentWindow(Window window) {
        parentWindow = window;
    }

    /*
     * Return the bindings from panel. If panel is being used for
     * input, then it gets the bindings from each panel and returns
     * them in a collection.
     */
    Collection getBindings() {
        return serviceBindings;
    }

    /*
     * Set information for existing bindings
     */
    void editServiceBindings(Collection bindings) throws JAXRException {
        Iterator iter = bindings.iterator();
        ServiceBinding serviceBinding = null;
        ServiceBindingInputPanel panel = null;
        while (iter.hasNext()) {
            serviceBinding = (ServiceBinding) iter.next();
            addBinding();
            panel = (ServiceBindingInputPanel) serviceBindings.get(
								   serviceBindings.size()-1);
            panel.editServiceBinding(serviceBinding);
        }
    }

    /**
     * When used for submitting, the 'serviceBindings'
     * collection holds a collection of ServiceBindingInputPanels.
     * These contain the information entered into the gui by the user.
     *
     * @return The ServiceBindingInputPanels used by this panel.
     */
    Collection getBindingsInputPanels() {
        return serviceBindings;
    }
    
    private String getDescription(RegistryObject ro) {
        String description = null;
        try {
            InternationalString iString = ro.getDescription();
            if (iString != null) {
                description = iString.getValue();
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        if (description == null) {
            description = "";
        }
        return description;
    }
    
}
