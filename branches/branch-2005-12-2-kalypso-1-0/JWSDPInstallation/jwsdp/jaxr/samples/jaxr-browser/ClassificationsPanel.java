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
import javax.swing.table.*;
import javax.swing.tree.*;

/**
 * Lists classifications for an object
 * and allows classifications
 * to be added and removed.
 */
public class ClassificationsPanel extends JPanel {

    RegistryBrowser registryBrowser;
    RegistryObject registryObject;
    ArrayList classifications;
    JTable classificationsTable;
    
    /**
     * Default constructor used for submission
     */
    public ClassificationsPanel() {
        this(null);
    }
    
    /**
     * Constructor used for listing only
     */
    public ClassificationsPanel(RegistryObject ro) {
        super();
        registryBrowser = RegistryBrowser.getInstance();
        setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("BrowserStrings").getString("Classifications")));
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(layout);
        
        try {
            if (ro == null) {
                
                // change for case of editing existing object
                classifications = new ArrayList();
                
                classificationsTable = new JTable(new AbstractTableModel() {
                    public int getRowCount() {
                        return classifications.size();
                    }
                    public int getColumnCount() {
                        return 1;
                    }
                    public Object getValueAt(int row, int column) {
                        Classification classification = null;
                        Concept concept = null;
                        String value = null;
                        try {
                            classification =
                                (Classification) classifications.get(row);
                            concept = classification.getConcept();
                            if (concept != null) {
                                InternationalString iString = concept.getName();
                                if (iString == null) {
                                    value = concept.getValue();
                                } else {
                                    value = iString.getValue() +
                                        ": " + concept.getValue();
                                }
                            } else {
                                InternationalString iString =
                                    classification.getName();
                                if (iString == null) {
                                    value = classification.getValue();
                                } else {
                                    value = iString.getValue() +
                                        ": " + classification.getValue();
                                }
                            }
                        } catch (Exception e) {
                            
                            // display and return null
                            RegistryBrowser.displayError(e);
                        }
                        return value;
                    }
                });
                classificationsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
                classificationsTable.setBorder(
                BorderFactory.createEtchedBorder());
                
                JButton addButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Add"));
                addButton.setMnemonic('d');
                addButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        RegistryBrowser browser = RegistryBrowser.getInstance();
                        String url =
                            (String) browser.registryCombo.getSelectedItem();
                        if (url.equals(browser.selectAnItem)) {
                            JOptionPane.showMessageDialog(null,
                            ResourceBundle.getBundle("BrowserStrings").getString("Please_select_a_registry_URL."),
                            ResourceBundle.getBundle("BrowserStrings").getString("Error:"), JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        
                        final JDialog classificationsDialog =
                            new JDialog(RegistryBrowser.getInstance(),
                            ResourceBundle.getBundle("BrowserStrings").getString("Select_Classifications"), true);
                        Container container =
                            classificationsDialog.getContentPane();
                        GridBagLayout dLayout = new GridBagLayout();
                        GridBagConstraints dc = new GridBagConstraints();
                        container.setLayout(dLayout);
                        
                        final ConceptsTree conceptsTree = new ConceptsTree(true);
                        JScrollPane treePane = new JScrollPane(conceptsTree);
                        
                        JButton okButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Add"));
                        okButton.setMnemonic('a');
                        okButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e1) {
                                TreePath paths [] =
                                conceptsTree.getSelectionPaths();
                                if (paths == null) {
                                    JOptionPane.showMessageDialog(null,
                                    ResourceBundle.getBundle("BrowserStrings").getString("No_concepts_selected."), ResourceBundle.getBundle("BrowserStrings").getString("Error:"),
                                    JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                NodeInfo nodeInfo = null;
                                Concept concept = null;
                                Classification classification = null;
                                for (int i = 0; i < paths.length; ++i) {
                                    nodeInfo = (NodeInfo)
                                    (((DefaultMutableTreeNode)paths[i].
                                    getLastPathComponent()).
                                    getUserObject());
                                    try {
                                        concept = (Concept)nodeInfo.obj;
                                    } catch (ClassCastException cce) {
                                        // schemes should not be added
                                        return;
                                    }
                                    try {
                                        BusinessLifeCycleManager manager =
                                            registryBrowser.client.
                                            getBusinessLifeCycleManager();
                                        classification =
                                            manager.createClassification(
                                                concept.getClassificationScheme(),
                                                concept.getName(),
                                                concept.getValue());
                                        if (!classificationsContains(
                                            classification)) {
                                            classifications.add(classification);
                                        }
                                    } catch (JAXRException e) {
                                        RegistryBrowser.displayError(e);
                                    } finally {
                                        conceptsTree.requestFocus();
                                    }
                                }
                                classificationsTable.revalidate();
                            }
                        });
                        JButton closeButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Close"));
                        closeButton.setMnemonic('c');
                        closeButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e1) {
                                classificationsDialog.dispose();
                            }
                        });
                        
                        RegistryBrowser.makeConstraints(dc, 0, 0,
                        GridBagConstraints.REMAINDER, 1, 1, 1,
                        GridBagConstraints.BOTH, GridBagConstraints.WEST);
                        dLayout.setConstraints(treePane, dc);
                        container.add(treePane);
                        RegistryBrowser.makeConstraints(dc, 0, 1, 1, 1, 1, 0,
                        GridBagConstraints.NONE, GridBagConstraints.EAST);
                        dLayout.setConstraints(okButton, dc);
                        container.add(okButton);
                        RegistryBrowser.makeConstraints(dc, 1, 1, 1, 1, 1, 0,
                        GridBagConstraints.NONE, GridBagConstraints.WEST);
                        dLayout.setConstraints(closeButton, dc);
                        container.add(closeButton);
                        
                        classificationsDialog.setLocation(300, 200);
                        classificationsDialog.pack();
                        
                        // now double the width
                        Dimension cdSize =
                        classificationsDialog.getPreferredSize();
                        cdSize.width *= 2;
                        classificationsDialog.setSize(cdSize);
                        classificationsDialog.setVisible(true);
                    }
                });
                
                JButton removeButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Remove"));
                removeButton.setMnemonic('r');
                removeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int selectedRow = classificationsTable.getSelectedRow();
                        if (selectedRow < 0 ||
                        selectedRow >= classifications.size()) {
                            JOptionPane.showMessageDialog(null,
                            ResourceBundle.getBundle("BrowserStrings").getString("Select_Classification_to_remove."),
                            ResourceBundle.getBundle("BrowserStrings").getString("Error:"), JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        classifications.remove(selectedRow);
                        classificationsTable.revalidate();
                    }
                });
                
                // add table and buttons
                RegistryBrowser.makeConstraints(c, 0, 0, 1, 1, 1, 1,
                GridBagConstraints.NONE, GridBagConstraints.EAST);
                layout.setConstraints(addButton, c);
                add(addButton);
                RegistryBrowser.makeConstraints(c, 1, 0, 1, 1, 1, 1,
                GridBagConstraints.NONE, GridBagConstraints.WEST);
                layout.setConstraints(removeButton, c);
                add(removeButton);
                RegistryBrowser.makeConstraints(c, 0, 1,
                GridBagConstraints.REMAINDER, 1, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
                c.insets = new Insets(5, 5, 5, 5);
                layout.setConstraints(classificationsTable, c);
                add(classificationsTable);
                
            } else {
                registryObject = ro;
                classifications = new ArrayList(ro.getClassifications());
                Iterator iter = classifications.iterator();
                JLabel classificationLabel = null;
                Classification classification = null;
                Concept concept = null;
                String name = null;
                String value = null;
                int row = 0;
                while (iter.hasNext()) {
                    classification = (Classification) iter.next();
                    concept = classification.getConcept();
                    if (concept != null) {
                        name = getName(concept);
                        value = concept.getValue();
                    } else {
                        name = getName(classification);
                        value = classification.getValue();
                    }
                    classificationLabel = new JLabel(name + ":  " + value);
                    RegistryBrowser.makeConstraints(c, 0, row++, 2, 1, 1, 1,
                    GridBagConstraints.NONE, GridBagConstraints.WEST);
                    c.insets = new Insets(0, 5, 0, 0);
                    layout.setConstraints(classificationLabel, c);
                    add(classificationLabel);
                }
            }
        } catch (JAXRException e) {
            System.err.println(ResourceBundle.getBundle("BrowserStrings").getString("Error_creating_ClassificationsPanel"));
            e.printStackTrace();
        }
    }
    
    /*
     * Checks parameter classification to see if it is already
     * in the classifications arraylist.
     */
    private boolean classificationsContains(Classification classification)
        throws JAXRException{
            boolean result = false;
            Iterator iter = classifications.iterator();
            Classification iterClassification = null;
            Concept concept = null;
            String valueA = null;
            String valueB = null;
            String nameA = null;
            String nameB = null;
            ClassificationScheme schemeA = null;
            ClassificationScheme schemeB = null;
            
            concept = classification.getConcept();
            if (concept != null) {
                valueB = concept.getValue();
                schemeB = concept.getClassificationScheme();
                nameB = getName(concept);
            } else {
                valueB = classification.getValue();
                schemeB = classification.getClassificationScheme();
                nameB = getName(classification);
            }
                
            while (iter.hasNext()) {
                iterClassification = (Classification) iter.next();
                concept = iterClassification.getConcept();
                if (concept != null) {
                    valueA = concept.getValue();
                    schemeA = concept.getClassificationScheme();
                    nameA = getName(concept);
                } else {
                    valueA = iterClassification.getValue();
                    schemeA = iterClassification.getClassificationScheme();
                    nameA = getName(iterClassification);
                }
                
                if (valueA == null) {
                    if (nameA.equals(nameB)) {
                        result = true;
                        break;
                    }
                } else if (valueA.equalsIgnoreCase(valueB)) {
                    String idA = schemeA.getKey().getId();
                    String idB = schemeB.getKey().getId();
                    if (idA.equalsIgnoreCase(idB)) {
                        result = true;
                        break;
                    }
                }
            }
            return result;
    }
    
    Collection getClassifications() throws JAXRException {
        return classifications;
    }
    
    void editClassifications(Collection classifications) throws JAXRException {
        this.classifications = new ArrayList(classifications);
        classificationsTable.revalidate();
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
}
