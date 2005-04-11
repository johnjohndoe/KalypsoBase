/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/**
 */
public class ServicesPanel extends JPanel {

    private Organization organization;
    private LinkedList services;
    private ButtonGroup buttons;
    private GridBagLayout gb;
    private GridBagConstraints c;
    private boolean hasServices;
    private boolean displayOnly;
    
    /**
     * no-arg constructor creates panel
     * for input.
     */
    public ServicesPanel() {
	this(null);
    }

    /**
     * Class Constructor
     */
    public ServicesPanel(Organization org) {
	super();
        gb = new GridBagLayout();
        c = new GridBagConstraints();
        setLayout(gb);
        try {
            if (org == null) {
                displayOnly = false;
                
                // will hold ServiceInputPanels
                services = new LinkedList();
            } else {
                displayOnly = true;
                organization = org;
		Collection coll = organization.getServices();
		if (coll == null || coll.isEmpty()) {
		    JLabel emptyLabel =
                        new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("There_are_no_services_registered"));
		    add(emptyLabel);
		    hasServices = false;
		} else {
		    buttons = new ButtonGroup();
		    services = new LinkedList();


		    Service service = null;
		    JRadioButton serviceButton = null;
		    String serviceName = null;
                    String serviceDescription = null;
		    JLabel nameLabel = null;
		    JLabel descriptionLabel = null;
		    Iterator iter = coll.iterator();
		    int row = 0;
		    while (iter.hasNext()) {
			service = (Service) iter.next();
			services.add(service);
			serviceButton = new JRadioButton(getName(service));
			serviceButton.setActionCommand(String.valueOf(row));
                        serviceButton.setToolTipText(java.util.ResourceBundle.getBundle("BrowserStrings").getString("Select_service_for_details"));
                        RegistryBrowser.makeConstraints(c, 0, row, 1, 1, 0.0,
                            0.5, GridBagConstraints.NONE,
                            GridBagConstraints.WEST);
			c.insets = new Insets(0, 5, 0, 0);
			gb.setConstraints(serviceButton, c);
			buttons.add(serviceButton);
			if (row == 0) {
			    buttons.setSelected(serviceButton.getModel(), true);
			}
			add(serviceButton);
                        //serviceName = getName(service);
			//nameLabel = new JLabel(serviceName);
			//c.gridx = 1;			
			//c.weightx = 0.0;
			//gb.setConstraints(nameLabel, c);
			//add(nameLabel);
                        serviceDescription = getDescription(service);
			descriptionLabel = new JLabel(serviceDescription);
			//c.gridx = 2;
			c.gridx = 1;
			c.weightx = 0.9;
			gb.setConstraints(descriptionLabel, c);
			add(descriptionLabel);
                            
			row++;
		    }
		    hasServices = true;
		}
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
            e.printStackTrace();
        }
    }

    /*
     * Return currently selected Service
     */
    Service getSelectedService() {
	if (hasServices()) {
	    int index =
                Integer.parseInt(buttons.getSelection().getActionCommand());
	    return (Service)services.get(index);
	} else {
	    return null;
	}
    }

    /*
     * Utility for setting "Show Service Bindings"
     * button enabled in drill down dialog.
     */
    boolean hasServices() {
	return hasServices;
    }
    
    /**
     * Adds service input panel to screen
     */
    void addService() {
        ServiceInputPanel service = new ServiceInputPanel();
        service.setBorder(BorderFactory.createEtchedBorder());
        services.add(service);
        RegistryBrowser.makeConstraints(c, 0, services.size()-1,
            1, 1, 1, 1, GridBagConstraints.HORIZONTAL,
            GridBagConstraints.NORTHWEST);
        gb.setConstraints(service, c);
        add(service);
        revalidate();
    }

    void removeService(ServiceInputPanel panel) {
        services.remove(panel);
        remove(panel);
        revalidate();
    }

    /*
     * Fills out the fields for editing existing
     * services of an organization.
     */
    void editServices(Collection services) throws JAXRException {
        if (displayOnly == true) {
            return;
        } 
        
        LinkedList inputPanels = this.services;
        inputPanels.clear();
        removeAll();
        revalidate();
        Iterator iter = services.iterator();
        Service service = null;
        ServiceInputPanel inputPanel = null;
        while (iter.hasNext()) {
            service = (Service) iter.next();
            addService();
            inputPanel =
                (ServiceInputPanel) inputPanels.get(inputPanels.size() - 1);
            inputPanel.editService(service);
        }
    }

    /**
     * When used for submitting, the 'services' collection
     * holds a collection of ServiceInputPanels. These contain
     * the information entered into the gui by the user.
     *
     * @return The ServiceInputPanels used by this panel.
     */
    Collection getServiceInputPanels() {
        return services;
    }

    private String getName(RegistryObject ro) {
        String name = null;
        try {
            InternationalString iString = ro.getName();
            if (iString != null) {
                name = iString.getValue();
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        if (name == null) {
            name = "";
        }
        return name;
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

