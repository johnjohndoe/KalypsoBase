/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ResourceBundle;

public class FindByPanel extends JPanel {
    
    JComboBox findByCombo;
    static final String CLASSIFICATION = ResourceBundle.getBundle("BrowserStrings").getString("Classifications:");
    
    /* 
     * This array is used to create the combo box and search panels
     * Changes here should be reflected in RegistryBrowser.SymActionListener.
     *     searchButton_actionPerformed()
     */
    static final String [] findMethods= {
        ResourceBundle.getBundle("BrowserStrings").getString("Name:"),
        CLASSIFICATION //, "Description:", "SQL query"
    };
    
    /**
     *
     */
    public FindByPanel() {
        findByCombo = new JComboBox(findMethods);
        findByCombo.setToolTipText(java.util.ResourceBundle.getBundle("BrowserStrings").getString("Select_the_type_of_search_to_perform"));
        findByCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                if (ev.getStateChange() == ItemEvent.DESELECTED) {
                    return;
                }
                String item = (String) ev.getItem();
                for (int i=0; i<findMethods.length; i++) {
                    if (findMethods[i].equals(item)) {
                        JPanel panel = RegistryBrowser.getInstance().findPanel;
                        CardLayout cLayout =
                        (CardLayout) panel.getLayout();
                        cLayout.show(panel, findMethods[i]);
                    }
                }
            }
        });
        
        setLayout(new FlowLayout());
        JLabel findByLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Find_by:"));
        findByLabel.setDisplayedMnemonic('n');
        findByLabel.setLabelFor(findByCombo);
        add(findByLabel);
        add(findByCombo);
    }

}
