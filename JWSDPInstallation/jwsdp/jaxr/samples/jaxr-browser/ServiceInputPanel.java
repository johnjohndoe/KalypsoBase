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

/*
 * Panel used for entering information about
 * an individual Service. ServicesPanel holds
 * these as Services are added or removed.
 */
public class ServiceInputPanel extends JPanel {
    JTextField nameText;
    JTextField idText;
    JTextField descriptionText;
    ClassificationsPanel classificationsPanel;
    ServiceBindingsPanel serviceBindingsPanel;
    
    public ServiceInputPanel() {
        super();
        final ServiceInputPanel instance = this;
        serviceBindingsPanel = new ServiceBindingsPanel();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(layout);

        JLabel nameLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Name:"));
        JLabel idLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Id:"));
        JLabel descriptionLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Description:"));

        nameText = new JTextField();
        idText = new JTextField();
        descriptionText = new JTextField();
        idText.setEnabled(false);

        classificationsPanel = new ClassificationsPanel();

        // panel for utility buttons
        JPanel utilPanel = new JPanel();
        JButton bindingsButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Edit_Bindings"));
        JButton removeButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Remove_Service"));

        bindingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final JDialog binDialog = new JDialog();
                binDialog.setModal(true);
                binDialog.setTitle(ResourceBundle.getBundle("BrowserStrings").getString("Edit_ServiceBindings"));
                Container container = binDialog.getContentPane();
                GridBagLayout binLayout = new GridBagLayout();
                GridBagConstraints binC = new GridBagConstraints();
                container.setLayout(binLayout);
                serviceBindingsPanel.setParentWindow(binDialog);
                
                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
                
                JButton addButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Add_Binding"));
                addButton.setMnemonic('a');
                addButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        serviceBindingsPanel.addBinding();
                        binDialog.pack();
                    }
                });
                
                JButton finishedButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Done"));
                finishedButton.setMnemonic('d');
                finishedButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        binDialog.dispose();
                    }
                }); 
                
                buttonPanel.add(addButton);
                buttonPanel.add(finishedButton);

                RegistryBrowser.makeConstraints(binC, 0, 0, 1, 1,
                    1, 0, GridBagConstraints.HORIZONTAL,
                    GridBagConstraints.NORTH);
                binLayout.setConstraints(serviceBindingsPanel, binC);
                container.add(serviceBindingsPanel);
                RegistryBrowser.makeConstraints(binC, 0, 1, 1, 1,
                    1, 1, GridBagConstraints.NONE,
                    GridBagConstraints.NORTH);
                binLayout.setConstraints(buttonPanel, binC);
                container.add(buttonPanel);
                
                binDialog.pack();
                binDialog.setLocation(200, 100);
                binDialog.setVisible(true);
            }
        });
        
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((ServicesPanel)getParent()).removeService(instance);
            }
        });
        
        GridBagLayout utilLayout = new GridBagLayout();
        utilPanel.setLayout(utilLayout);
        RegistryBrowser.makeConstraints(c, 0, 0, 1, 1, 1, 1,
            GridBagConstraints.NONE, GridBagConstraints.NORTHEAST);
        utilLayout.setConstraints(bindingsButton, c);
        utilPanel.add(bindingsButton);
        RegistryBrowser.makeConstraints(c, 1, 0, 1, 1, 1, 1,
            GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
        utilLayout.setConstraints(removeButton, c);
        utilPanel.add(removeButton);

        // add everything to services panel
        RegistryBrowser.makeConstraints(c, 0, 0, 1, 1, 0, 0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        layout.setConstraints(nameLabel, c);
        add(nameLabel);
        RegistryBrowser.makeConstraints(c, 1, 0, 1, 1, 1, 0,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        c.insets = new Insets(0, 5, 0, 5);
        layout.setConstraints(nameText, c);
        add(nameText);
        RegistryBrowser.makeConstraints(c, 0, 1, 1, 1, 0, 0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        layout.setConstraints(idLabel, c);
        add(idLabel);
        RegistryBrowser.makeConstraints(c, 1, 1, 1, 1, 1, 0,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        c.insets = new Insets(0, 5, 0, 5);
        layout.setConstraints(idText, c);
        add(idText);
        RegistryBrowser.makeConstraints(c, 0, 2, 1, 1, 0, 0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        layout.setConstraints(descriptionLabel, c);
        add(descriptionLabel);
        RegistryBrowser.makeConstraints(c, 1, 2, 1, 1, 1, 0,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        c.insets = new Insets(0, 5, 0, 5);
        layout.setConstraints(descriptionText, c);
        add(descriptionText);
        RegistryBrowser.makeConstraints(c, 0, 3, 2, 1, 0, 1,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST);
        c.insets = new Insets(0, 0, 0, 5);
        layout.setConstraints(utilPanel, c);
        add(utilPanel);
        RegistryBrowser.makeConstraints(c, 2, 0, 1,
            GridBagConstraints.REMAINDER, 0, 1,
            GridBagConstraints.VERTICAL, GridBagConstraints.NORTH);
        layout.setConstraints(classificationsPanel, c);
        add(classificationsPanel);
    }

    ClassificationsPanel getClassificationsPanel() throws JAXRException {
        return classificationsPanel;
    }
    
    ServiceBindingsPanel getBindingsPanel() throws JAXRException {
        return serviceBindingsPanel;
    }
    
    void editService(Service service) throws JAXRException {
        nameText.setText(getName(service));
        Key key = service.getKey();
        if (key != null) {
            idText.setText(key.getId());
        }
        descriptionText.setText(getDescription(service));
        classificationsPanel.editClassifications(service.getClassifications());
        serviceBindingsPanel.editServiceBindings(service.getServiceBindings());
    }
    
    /**
     * Returns the String information entered by the
     * user. This allows the separation of the JAXRClient
     * from the gui details.
     *
     * @return The service's name.
     */
    String getServiceName() {
        return nameText.getText();
    }
    
    /**
     * Returns the String information entered by the
     * user. This allows the separation of the JAXRClient
     * from the gui details.
     *
     * @return The service's description.
     */
    String getServiceDescription() {
        return descriptionText.getText();
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
