/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/*
 * Panel used for entering information about
 * an individual ServiceBinding.
  * ServicesBindingsPanel holds these as
  * ServiceBindingss are added or removed.
 */
public class ServiceBindingInputPanel extends JPanel {

    JTextField descriptionText;
    JTextField uriText;

    public ServiceBindingInputPanel() {
        super();
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gb);

        final ServiceBindingInputPanel instance = this;
        JLabel descriptionLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Description:"));
        JLabel uriLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Access_URI:"));

        int textFieldLength = 25;
        descriptionText = new JTextField(textFieldLength);
        uriText = new JTextField(textFieldLength);
        
        JButton removeButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Remove_Binding"));
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((ServiceBindingsPanel)getParent()).
                    removeServiceBinding(instance);
            }
        });
        
        // add to panel
        RegistryBrowser.makeConstraints(c, 0, 0, 1, 1, 0, 0,
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        gb.setConstraints(descriptionLabel, c);
        add(descriptionLabel);
        RegistryBrowser.makeConstraints(c, 1, 0, 1, 1, 1, 0,
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        c.insets = new Insets(1, 5, 1, 5);
        gb.setConstraints(descriptionText, c);
        add(descriptionText);
        RegistryBrowser.makeConstraints(c, 0, 1, 1, 1, 0, 0,
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        gb.setConstraints(uriLabel, c);
        add(uriLabel);
        RegistryBrowser.makeConstraints(c, 1, 1, 1, 1, 1, 0,
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        c.insets = new Insets(1, 5, 1, 5);
        gb.setConstraints(uriText, c);
        add(uriText);
        RegistryBrowser.makeConstraints(c, 0, 2, 2, 1, 0, 0,
            GridBagConstraints.NONE, GridBagConstraints.NORTH);
        c.insets = new Insets(5, 0, 0, 0);
        gb.setConstraints(removeButton, c);
        add(removeButton);
    }

    /*
     * Enter information from existing binding into fields
     */
    void editServiceBinding(ServiceBinding binding) throws JAXRException {
        descriptionText.setText(getDescription(binding));
        uriText.setText(binding.getAccessURI());
    }

    /**
     * Returns the String information entered by the
     * user. This allows the separation of the JAXRClient
     * from the gui details.
     *
     * @return The service binding's description.
     */
    String getBindingDescription() {
        return descriptionText.getText();
    }

    /**
     * Returns the String information entered by the
     * user. This allows the separation of the JAXRClient
     * from the gui details.
     *
     * @return The service binding's access URI.
     */
    String getAccessURI() {
        return uriText.getText();
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
