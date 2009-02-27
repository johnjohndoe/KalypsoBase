/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.xml.registry.*; // JAXRException
import javax.xml.registry.infomodel.*;

/**
 * Contains a split pane with organization details
 * on left and service/service binding details
 * on the right.
 */
public class SubmissionPanel extends JPanel {

    JSplitPane splitPane;
    RegistryObjectPanel registryObjectPanel;
    ServicesPanel servicesPanel;
    PasswordAuthentication passwordAuth;
    
    /**
     * Class Constructor.
     */
    public SubmissionPanel() {
        GridBagConstraints c = new GridBagConstraints();
        
        // setup service panel
        servicesPanel = new ServicesPanel();
        JPanel orgServicesPanel = new JPanel();
        orgServicesPanel.setBorder(
            BorderFactory.createTitledBorder(ResourceBundle.getBundle("BrowserStrings").getString("Services")));
        GridBagLayout servicesPanelLayout = new GridBagLayout();
        orgServicesPanel.setLayout(servicesPanelLayout);
        RegistryBrowser.makeConstraints(c, 0, 0, 1, 1, 1, 1,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH);
        servicesPanelLayout.setConstraints(servicesPanel, c);
        orgServicesPanel.add(servicesPanel);
        
        // setup organization panel
        registryObjectPanel = new RegistryObjectPanel();
        JPanel organizationPanel = new JPanel();
        organizationPanel.setBorder(
            BorderFactory.createTitledBorder(ResourceBundle.getBundle("BrowserStrings").getString("Organization")));
        GridBagLayout orgPanelLayout = new GridBagLayout();
        organizationPanel.setLayout(orgPanelLayout);
        RegistryBrowser.makeConstraints(c, 0, 0, 1, 1, 1, 1,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH);
        orgPanelLayout.setConstraints(registryObjectPanel, c);
        organizationPanel.add(registryObjectPanel);
        
        // add sub-panels to the submission panel in scroll panes
        JScrollPane organizationScrollPane = new JScrollPane(organizationPanel);
        JScrollPane servicesScrollPane = new JScrollPane(orgServicesPanel);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
            organizationScrollPane, servicesScrollPane);
        
        // put everything in submission panel
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
    }

    RegistryObjectPanel getRegistryObjectPanel() {
        return registryObjectPanel;
    }
    
    ServicesPanel getServicesPanel() {
        return servicesPanel;
    }

    /*
     * Edit organization for submission. This sets the panel
     * fields to contain the organization's information.
     */
    void editOrganization(Organization org) throws JAXRException {
        registryObjectPanel.editOrganization(org);
	servicesPanel.editServices(org.getServices());
        splitPane.setDividerLocation(0.4);
    }
    
    /**
     * Gets the username and password entered by the user.
     */
    PasswordAuthentication getUserAuthentication() {
        int fieldSize = 15;
        JLabel nameLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Username:"));
        JLabel pwdLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Password:"));
        final JTextField username = new JTextField(fieldSize);
        final JPasswordField password = new JPasswordField(fieldSize);

        username.setToolTipText(java.util.ResourceBundle.getBundle("BrowserStrings").getString("Enter_username"));
        password.setToolTipText(java.util.ResourceBundle.getBundle("BrowserStrings").getString("Enter_password"));
        nameLabel.setLabelFor(username);
        nameLabel.setDisplayedMnemonic('u');
        pwdLabel.setLabelFor(password);
        pwdLabel.setDisplayedMnemonic('p');
        
        final JDialog credentialsDialog =
            new JDialog(RegistryBrowser.getInstance(),
                ResourceBundle.getBundle("BrowserStrings").getString("Enter_authentication_information"),
                true);
        
        Container container = credentialsDialog.getContentPane();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        container.setLayout(gbl);
        
        RegistryBrowser.makeConstraints(gbc, 0, 0, 1, 1, 0, 0,
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        gbl.setConstraints(nameLabel, gbc);
        container.add(nameLabel);
        RegistryBrowser.makeConstraints(gbc, 1, 0, 1, 1, 1, 0,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
        gbc.insets = new Insets(1, 5, 1, 5);
        gbl.setConstraints(username, gbc);
        container.add(username);
        RegistryBrowser.makeConstraints(gbc, 0, 1, 1, 1, 0, 0,
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        gbl.setConstraints(pwdLabel, gbc);
        container.add(pwdLabel);
        RegistryBrowser.makeConstraints(gbc, 1, 1, 1, 1, 1, 0,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
        gbc.insets = new Insets(1, 5, 1, 5);
        gbl.setConstraints(password, gbc);
        container.add(password);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        
        JButton okButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("OK"));
        okButton.setMnemonic('o');
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                passwordAuth =
		    new PasswordAuthentication(username.getText(),
					       password.getPassword());
                credentialsDialog.dispose();
            }
        });
        
        JButton cancelButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Cancel"));
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                credentialsDialog.dispose();
            }
        });
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        RegistryBrowser.makeConstraints(gbc, 0, 2, 2, 1, 0, 1,
            GridBagConstraints.NONE, GridBagConstraints.NORTH);
        gbl.setConstraints(buttonPanel, gbc);
        container.add(buttonPanel);
        
        credentialsDialog.pack();
        credentialsDialog.setLocation(200, 200);
        credentialsDialog.setVisible(true);
        
        // after dialog completes
        RegistryBrowser.getInstance().invalidate();
	PasswordAuthentication tempAuth = passwordAuth;
        passwordAuth = null;
        return tempAuth;
    }

}	
