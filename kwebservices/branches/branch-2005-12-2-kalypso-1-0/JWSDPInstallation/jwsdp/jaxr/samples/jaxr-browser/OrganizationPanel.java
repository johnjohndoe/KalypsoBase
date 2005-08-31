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
 * Panel used in a popup dialog that holds the information
 * about an organization such as its registry object and
 * registry entry information
 */
public class OrganizationPanel extends javax.swing.JPanel {

    Organization organization;

    RegistryObjectPanel obPanel;
    private boolean displayOnly;
    
    /**
     * Creates new OrganizationPanel 
     */
    public OrganizationPanel(Organization org) {
	organization = org;

        obPanel = new RegistryObjectPanel(organization);
        obPanel.setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("BrowserStrings").getString("Registry_Object")));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(obPanel);
    }

    /*
     * Utility method for dialog containing this panel
     */
    String getOrganizationName() {
        String name = null;
        try {
            InternationalString iString = organization.getName();
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

    /*
     * Utility method
     */
    Organization getOrganization() {
	return organization;
    }
}

