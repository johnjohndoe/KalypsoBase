/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Currently only used for Organizations.
 * Holds organization informattion and a ClassificationsPanel.
 */
public class RegistryObjectPanel extends JPanel {
    
    Organization organization; // later make RegistryObject
    ClassificationsPanel classificationsPanel;
    
    JTextField idText;
    JTextField nameText;
    JTextField descriptionText;
    JTextField contactNameText;
    JTextField contactPhoneText;
    JTextField contactEmailText;
    
    /**
     * Used for submitting objects
     */
    public RegistryObjectPanel() {
        this(null);
    }
    
    /**
     * Used for displaying objects
     */
    public RegistryObjectPanel(Organization ro) {
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints  c = new GridBagConstraints();
        setLayout(gb);
        JLabel orgLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Organization_Information"));
        RegistryBrowser.makeConstraints(c, 0, 0, GridBagConstraints.REMAINDER,
        1, 0.0, 0.5, GridBagConstraints.NONE, GridBagConstraints.WEST);
        c.insets = new Insets(5, 5, 3, 0);
        gb.setConstraints(orgLabel, c);
        add(orgLabel);
        
        JLabel nameLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Name:"));
        RegistryBrowser.makeConstraints(c, 0, 1, 1, 1, 0.0, 0.5,
        GridBagConstraints.NONE, GridBagConstraints.WEST);
        c.insets = new Insets(0, 5, 0, 0);
        gb.setConstraints(nameLabel, c);
        add(nameLabel);
        
        JLabel idLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Id:"));
        RegistryBrowser.makeConstraints(c, 0, 2, 1, 1, 0.0, 0.5,
        GridBagConstraints.NONE, GridBagConstraints.WEST);
        c.insets = new Insets(0, 5, 0, 0);
        gb.setConstraints(idLabel, c);
        add(idLabel);
        
        JLabel descriptionLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Description:"));
        RegistryBrowser.makeConstraints(c, 0, 3, 1, 1, 0.0, 0.5,
        GridBagConstraints.NONE, GridBagConstraints.WEST);
        c.insets = new Insets(0, 5, 0, 0);
        gb.setConstraints(descriptionLabel, c);
        add(descriptionLabel);
        
        JLabel contactLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Primary_Contact_Information"));
        RegistryBrowser.makeConstraints(c, 0, 4, GridBagConstraints.REMAINDER,
        1, 0.0, 0.5, GridBagConstraints.NONE, GridBagConstraints.WEST);
        c.insets = new Insets(5, 5, 3, 0);
        gb.setConstraints(contactLabel, c);
        add(contactLabel);
        
        JLabel contactNameLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Name:"));
        RegistryBrowser.makeConstraints(c, 0, 5, 1, 1, 0.0, 0.5,
        GridBagConstraints.NONE, GridBagConstraints.WEST);
        c.insets = new Insets(0, 5, 0, 0);
        gb.setConstraints(contactNameLabel, c);
        add(contactNameLabel);
        
        JLabel contactPhoneLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Phone:"));
        RegistryBrowser.makeConstraints(c, 0, 6, 1, 1, 0.0, 0.5,
        GridBagConstraints.NONE, GridBagConstraints.WEST);
        c.insets = new Insets(0, 5, 0, 0);
        gb.setConstraints(contactPhoneLabel, c);
        add(contactPhoneLabel);
        
        JLabel contactEmailLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Email:"));
        RegistryBrowser.makeConstraints(c, 0, 7, 1, 1, 0.0, 0.5,
        GridBagConstraints.NONE, GridBagConstraints.WEST);
        c.insets = new Insets(0, 5, 0, 0);
        gb.setConstraints(contactEmailLabel, c);
        add(contactEmailLabel);
        
        if (ro == null) {
            
            //submission only
            int textFieldSize = 15;
            idText = new JTextField(textFieldSize);
            idText.setEnabled(false); // not set by user
            nameText = new JTextField(textFieldSize);
            descriptionText = new JTextField(textFieldSize);
            contactNameText = new JTextField(textFieldSize);
            contactPhoneText = new JTextField(textFieldSize);
            contactEmailText = new JTextField(textFieldSize);
            classificationsPanel = new ClassificationsPanel();
            
            nameLabel.setLabelFor(nameText);
            idLabel.setLabelFor(idText);
            descriptionLabel.setLabelFor(descriptionText);
            contactNameLabel.setLabelFor(contactNameText);
            contactPhoneLabel.setLabelFor(contactPhoneText);
            contactEmailLabel.setLabelFor(contactEmailText);
            
            nameLabel.setDisplayedMnemonic('n');
            idLabel.setDisplayedMnemonic('i');
            descriptionLabel.setDisplayedMnemonic('c');
            contactNameLabel.setDisplayedMnemonic('m');
            contactPhoneLabel.setDisplayedMnemonic('p');
            contactEmailLabel.setDisplayedMnemonic('e');
            
            RegistryBrowser.makeConstraints(c, 1, 1, 1, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 5, 0);
            gb.setConstraints(nameText, c);
            add(nameText);
            
            RegistryBrowser.makeConstraints(c, 1, 2, 1, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 5, 0);
            gb.setConstraints(idText, c);
            add(idText);
            
            RegistryBrowser.makeConstraints(c, 1, 3, 1, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 5, 0);
            gb.setConstraints(descriptionText, c);
            add(descriptionText);
            
            RegistryBrowser.makeConstraints(c, 1, 5, 1, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 5, 0);
            gb.setConstraints(contactNameText, c);
            add(contactNameText);
            
            RegistryBrowser.makeConstraints(c, 1, 6, 1, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 5, 0);
            gb.setConstraints(contactPhoneText, c);
            add(contactPhoneText);
            
            RegistryBrowser.makeConstraints(c, 1, 7, 1, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 5, 0);
            gb.setConstraints(contactEmailText, c);
            add(contactEmailText);
            
            RegistryBrowser.makeConstraints(c, 0, 8,
            GridBagConstraints.REMAINDER, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 5, 0);
            gb.setConstraints(classificationsPanel, c);
            add(classificationsPanel);
            
        } else {
            
            // display only
            organization = ro;
            boolean hasClassifications = false;
            JLabel nameValueLabel = null;
            JLabel idValueLabel = null;
            JLabel descriptionValueLabel = null;
            JLabel contactNameValueLabel = new JLabel();
            JLabel contactPhoneValueLabel = new JLabel();
            JLabel contactEmailValueLabel = new JLabel();
            classificationsPanel = new ClassificationsPanel(ro);
            
            try {
                nameValueLabel = new JLabel(getName(organization));
                idValueLabel = new JLabel(organization.getKey().getId());
                descriptionValueLabel =
                    new JLabel(getDescription(organization));
                
                contactNameValueLabel.setText(getContactName(organization));
                contactPhoneValueLabel.setText(getPhoneNumber(organization));
                contactEmailValueLabel.setText(getEmailAddress(organization));

                if (organization.getClassifications().size() > 0) {
                    hasClassifications = true;
                }
            } catch (JAXRException e) {
                System.err.println(ResourceBundle.getBundle("BrowserStrings").getString("Error_creating_RegistryObjectPanel"));
                e.printStackTrace();
            }
            
            RegistryBrowser.makeConstraints(c, 1, 1, 1, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 0, 0);
            gb.setConstraints(nameValueLabel, c);
            add(nameValueLabel);
            
            RegistryBrowser.makeConstraints(c, 1, 2, 1, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 0, 0);
            gb.setConstraints(idValueLabel, c);
            add(idValueLabel);
            
            RegistryBrowser.makeConstraints(c, 1, 3, 1, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 0, 0);
            gb.setConstraints(descriptionValueLabel, c);
            add(descriptionValueLabel);
            
            RegistryBrowser.makeConstraints(c, 1, 5, 1, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 0, 0);
            gb.setConstraints(contactNameValueLabel, c);
            add(contactNameValueLabel);
            
            RegistryBrowser.makeConstraints(c, 1, 6, 1, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 0, 0);
            gb.setConstraints(contactPhoneValueLabel, c);
            add(contactPhoneValueLabel);
            
            RegistryBrowser.makeConstraints(c, 1, 7, 1, 1, 0.9, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
            c.insets = new Insets(0, 5, 0, 0);
            gb.setConstraints(contactEmailValueLabel, c);
            add(contactEmailValueLabel);
            
            if (hasClassifications == true) {
                RegistryBrowser.makeConstraints(c, 0, 8,
                GridBagConstraints.REMAINDER, 1, 0.9, 0.5,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
                c.insets = new Insets(0, 5, 0, 0);
                gb.setConstraints(classificationsPanel, c);
                add(classificationsPanel);
            }
        }
    }
    
    void editOrganization(Organization org) throws JAXRException {
        Key key = org.getKey();
        if (key != null) {
            idText.setText(key.getId());
        }
        nameText.setText(getName(org));
        descriptionText.setText(getDescription(org));
        User user = org.getPrimaryContact();
        contactNameText.setText(getContactName(org));
        contactPhoneText.setText(getPhoneNumber(org));
        contactEmailText.setText(getEmailAddress(org));
        classificationsPanel.editClassifications(org.getClassifications());
    }
    
    /*
     * Utility method for getting contact name.
     */
    private String getContactName(Organization org) throws JAXRException {
        String name = "";
        User user = org.getPrimaryContact();
        if (user != null) {
            PersonName personName = user.getPersonName();
            if (personName != null) {
                name = personName.getFullName();
            }
        }
        return name;
    }
    
    /*
     * Utility method for getting contact's phone number.
     */
    private String getPhoneNumber(Organization org) throws JAXRException {
        String numberString = "";
        User user = org.getPrimaryContact();
        if (user != null) {
            
            // use null to get all phone numbers
            Collection numbers = user.getTelephoneNumbers(null);
            Iterator iter = numbers.iterator();
            if (iter.hasNext()) {
                TelephoneNumber number = (TelephoneNumber) iter.next();
				String areaCode = null;
				try {
                	areaCode = number.getAreaCode();
				} catch (UnsupportedCapabilityException usce){
					//do nothing
				}
                if (areaCode != null) {
                    areaCode = "(" + areaCode + ") ";
                } else {
                    areaCode = "";
                }
                numberString = areaCode + number.getNumber();
            }
        }
        return numberString;
    }

    /*
     * Utility method for getting contact's email address.
     */
    private String getEmailAddress(Organization org) throws JAXRException {
        String emailAddressString = "";
        User user = org.getPrimaryContact();
        if (user != null) {
            Collection addresses = user.getEmailAddresses();
            Iterator iter = addresses.iterator();
            if (iter.hasNext()) {
		EmailAddress emailAddress = (EmailAddress) iter.next();
		emailAddressString = emailAddress.getAddress();
            }
        }
        return emailAddressString;
    }

    /**
     * Returns the ClassificationsPanel used by this panel. This
     * is used to obtain the organization's classifications.
     *
     * @return Panel's ClassificationsPanel.
     */
    ClassificationsPanel getClassificationsPanel() {
	return classificationsPanel;
    }

    /**
     * Returns the String information entered by the
     * user. This allows the separation of the JAXRClient
     * from the gui details.
     *
     * @return The organization's name.
     */
    String getOrganizationName() {
        return nameText.getText();
    }
    
    /**
     * Returns the String information entered by the
     * user. This allows the separation of the JAXRClient
     * from the gui details.
     *
     * @return The organization's decription.
     */
    String getOrganizationDescription() {
        return descriptionText.getText();
    }
    
    /**
     * Returns the String information entered by the
     * user. This allows the separation of the JAXRClient
     * from the gui details.
     *
     * @return The primary contact's full name.
     */
    String getContactFullName() {
        return contactNameText.getText();
    }
    
    /**
     * Returns the String information entered by the
     * user. This allows the separation of the JAXRClient
     * from the gui details.
     *
     * @return The primary contact's phone number.
     */
    String getContactPhoneNumber() {
        return contactPhoneText.getText();
    }
    
    /**
     * Returns the String information entered by the
     * user. This allows the separation of the JAXRClient
     * from the gui details.
     *
     * @return The primary contact's email address.
     */
    String getContactEmail() {
        return contactEmailText.getText();
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
