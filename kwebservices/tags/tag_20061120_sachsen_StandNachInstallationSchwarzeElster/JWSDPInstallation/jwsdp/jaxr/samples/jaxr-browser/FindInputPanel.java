/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Simple class that has a label and field to
 * enter information for searching.
 */
public class FindInputPanel extends javax.swing.JPanel {
    
    String inputType;
    JTextField inputField;
    
    /** Creates new FindInputPanel */
    public FindInputPanel(String value) {
        inputType = value;
        JLabel label = new JLabel(inputType);
        if (inputType.equals(java.util.ResourceBundle.getBundle("BrowserStrings").getString("Name:"))) {
            label.setDisplayedMnemonic('a');
        }
        
        inputField = new JTextField(10);
        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                // pass in the event in case it's needed
                RegistryBrowser.getInstance().performSearch(ev);
            }
        });
        inputField.setToolTipText(java.util.ResourceBundle.getBundle("BrowserStrings").getString("Enter_search_information"));
        label.setLabelFor(inputField);
        
        setLayout(new FlowLayout());
        add(label);
        add(inputField);
    }
    
}
