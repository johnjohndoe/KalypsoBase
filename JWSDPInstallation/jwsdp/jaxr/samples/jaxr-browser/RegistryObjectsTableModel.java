/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;


/**
 * A JTable that lists
 *
 * @author Jim Glennon
 */
public class RegistryObjectsTableModel extends AbstractTableModel {
    
    String[]                columnNames = {
        ResourceBundle.getBundle("BrowserStrings").getString("Object_Type"),
        ResourceBundle.getBundle("BrowserStrings").getString("Key"),
        ResourceBundle.getBundle("BrowserStrings").getString("Name"),
        ResourceBundle.getBundle("BrowserStrings").getString("Description")
    };
    
    private static final int DESCRIPTION_COLUMN = 3;
    
    // currently the registry objects are Organizations only
    ArrayList           registryObjects = new ArrayList();
    RegistryBrowser	registryBrowser;
    
    String getDescription(int row)  {
        System.err.println(ResourceBundle.getBundle("BrowserStrings").getString("Getting_description_for_row:_") + row);
        return (String)(getValueAt(row, DESCRIPTION_COLUMN));
    }
    
    public RegistryObjectsTableModel() {
        registryBrowser = RegistryBrowser.getInstance();
    }
    
    public int getColumnCount() {
        return columnNames.length;
    }
    
    public int getRowCount() {
        return registryObjects.size();
    }
    
    public Object getValueAt(int row, int col) {
        Organization org = (Organization)registryObjects.get(row);
        Object value = null;
        InternationalString iString = null;
        try {
            switch (col) {
                case 0:
                    // will get object type from org after change
                    value = ResourceBundle.getBundle("BrowserStrings").getString("Organization");
                    break;
                case 1:
                    value = org.getKey().getId();
                    break;
                case 2:
                    iString = org.getName();
                    if (iString != null) {
                        value = iString.getValue();
                    }
                    break;
                case 3:
                    iString = org.getDescription();
                    if (iString != null) {
                        value = iString.getValue();
                    }
                    break;
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        
        return value;
    }
    
    public String getColumnName(int col) {
        return columnNames[col].toString();
    }
    
    public void updateTableModelByName(String businessName) {
        registryObjects.clear();
        
        if (businessName == null || businessName.length() == 0) {
            fireTableDataChanged();
            return;
        }

	JAXRClient client = registryBrowser.client;
        Collection orgs = client.getOrganizations(businessName);

	if (orgs.isEmpty()) {
	    JOptionPane.showMessageDialog(null, ResourceBundle.getBundle("BrowserStrings").getString("No_organizations_were_found."),
					  ResourceBundle.getBundle("BrowserStrings").getString("Registry_Browser"),
					  JOptionPane.INFORMATION_MESSAGE);
	} else {
	    registryObjects.addAll(orgs);
	}
        fireTableDataChanged();
    }
    
    public void updateTableModelByClassifications(TreePath[] treePaths) {
        registryObjects.clear();
        
        if (treePaths == null)  {
            fireTableDataChanged();
            return;
        }
        
        Collection concepts = new ArrayList();
        NodeInfo nodeInfo = null;
        Concept concept = null;

	for (int i = 0; i < treePaths.length; ++i) {
	    nodeInfo = (NodeInfo)(((DefaultMutableTreeNode)treePaths[i].
				   getLastPathComponent()).getUserObject());
	    concepts.add((Concept) nodeInfo.obj);
	}
	
	JAXRClient client = registryBrowser.client;
	Collection orgs = client.getOrganizations(concepts);

	if (orgs.isEmpty()) {
	    JOptionPane.showMessageDialog(null, ResourceBundle.getBundle("BrowserStrings").getString("No_organizations_were_found."),
					  ResourceBundle.getBundle("BrowserStrings").getString("Registry_Browser"),
					  JOptionPane.INFORMATION_MESSAGE);
	} else {
	    registryObjects.addAll(orgs);
	}
	fireTableDataChanged();
    }
    
    /*
     * used by table inside action listener
     */
    ArrayList getRegistryObjects() {
        // currently these are Organizations only
        return registryObjects;
    }
    
}
