/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/**
 * A JTable that lists
 *
 * @author Jim Glennon
 */
public class RegistryObjectsTable extends JTable {
    int                             selectedRow = -1;
    JPopupMenu                      popup;
    MouseListener                   popupListener;
    final RegistryObjectsTableModel       tableModel;

    /**
     * Class Constructor.
     *
     *
     * @param model
     *
     * @see
     */
    public RegistryObjectsTable(RegistryObjectsTableModel model) {
        super(model);
	tableModel = model;
        setToolTipText(ResourceBundle.getBundle("BrowserStrings").getString("Table_of_Registered_Objects"));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel  rowSM = getSelectionModel();
        setRowHeight(getRowHeight() * 2);
        
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel  lsm = (ListSelectionModel) e.getSource();
                
                if (!lsm.isSelectionEmpty()) {
                    setSelectedRow(lsm.getMinSelectionIndex());
                } else {
                    setSelectedRow(-1);
                }
            }
            
        }
        );

        // Add listener to self so that I can bring up popup menus on right mouse click
        popupListener = new PopupListener();
        addMouseListener(popupListener);
        
        /*
         * Add listener to show popup menu from
         * keyboard. This adds keyboard accessibility
         * for users not using a mouse.
         */
        addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ' ') {
                    Component comp = e.getComponent();
                    popup.show(comp, comp.getX(), comp.getY());
                    popup.requestFocus();
                }
            }
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {}
        });

        // Create popup menu for table
        popup = new JPopupMenu();
        JMenuItem saveMenuItem = new JMenuItem(ResourceBundle.getBundle("BrowserStrings").getString("Save_As_Local_File"));
        JMenuItem editMenuItem = new JMenuItem(ResourceBundle.getBundle("BrowserStrings").getString("Edit_RegistryObject"));
        JMenuItem deleteMenuItem = new JMenuItem(ResourceBundle.getBundle("BrowserStrings").getString("Delete_RegistryObject"));
        JMenuItem drillDownMenuItem = new JMenuItem(java.util.ResourceBundle.getBundle("BrowserStrings").getString("Show_Details"));
        saveMenuItem.setMnemonic('v');
        editMenuItem.setMnemonic('e');
        deleteMenuItem.setMnemonic('d');
        drillDownMenuItem.setMnemonic('h');
        
        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        
        editMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int index = getSelectedRow();
                RegistryObject regObject =
                    (RegistryObject) tableModel.getRegistryObjects().get(index);
                RegistryBrowser browser = RegistryBrowser.getInstance();
                try {
                    browser.editRegistryObject(regObject);
                } catch (JAXRException e) {
                    RegistryBrowser.getInstance().displayError(e);
                    e.printStackTrace();
                }
            }
        });
        
        deleteMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int index = getSelectedRow();
                RegistryObject regObject =
                    (RegistryObject) tableModel.getRegistryObjects().get(index);
                RegistryBrowser browser = RegistryBrowser.getInstance();
                try {
                    browser.deleteRegistryObject(regObject);
                } catch (JAXRException e) {
                    RegistryBrowser.getInstance().displayError(e);
                    e.printStackTrace();
                }
            }
        });
        
        drillDownMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                ((PopupListener) popupListener).showDrillDown();
            }
        });
        
        //popup.add(saveMenuItem);

	/* 
	 * editMenuItem commented out until there is a fix for the
	 * submit vs resubmit issue (probably will add ability to
	 * set two urls in the browser, one for query and one for
	 * life cycle mamagement)
	 */
        //popup.add(editMenuItem);
        popup.add(deleteMenuItem);
        popup.add(drillDownMenuItem);
        
    }
    
    /**
     * Overrides base class behaviour by setting selection when first
     * row (destination) is added to model
     */
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        
        // If no selectedRow, set selectedRow to firstRow
        if ((selectedRow == -1) && (e.getType() == e.INSERT)) {
            
            // Following will result in a software initiated selection
            // of the first row in table
            ListSelectionModel  rowSM = getSelectionModel();
            
            rowSM.setSelectionInterval(0, 0);
        }
        
    }
    
    /**
     * Sets the currently selected row in table
     * Also does firePropertyChange on property "selectedRow"
     */
    private void setSelectedRow(int index) {
        Integer oldIndex = new Integer(selectedRow);
        
        selectedRow = index;
        firePropertyChange("selectedRow", oldIndex, new Integer(index));
    }
    
    /**
     * Class Declaration.
     *
     *
     * @see
     *
     * @author
     * @version   1.9, 03/29/00
     */
    class PopupListener extends MouseAdapter {
        
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
            if ((e.getClickCount() > 1) && (getSelectedRow() > -1)) {
                showDrillDown();
            }
        }
        
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        void maybeShowPopup(MouseEvent e) {
            if ((e.isPopupTrigger()) && (getSelectedRow() > -1)) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
        
        void showDrillDown() {
            Cursor oldCursor = RegistryBrowser.getInstance().getCursor();
            try {
                RegistryBrowser.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                int row = getSelectedRow();
                ArrayList orgs = tableModel.getRegistryObjects();
                Organization org = (Organization) orgs.get(row);
                OrganizationPanel orgPanel = new OrganizationPanel(org);

                DrillDownDialog orgDrillDown =
                    DrillDownDialog.getInstance();
                orgDrillDown.setOrgPanel(orgPanel);
                orgDrillDown.setModal(true); // workaround for focus issue w/1.4
                orgDrillDown.pack();
                orgDrillDown.setVisible(true);

            } finally {
                RegistryBrowser.getInstance().setCursor(oldCursor);
            }
        }
    }

    /**
     * Used for drilling down into object information
     */
    static class DrillDownDialog extends JDialog {

	static DrillDownDialog instance;
	static OrganizationPanel orgPanel;
	static ServicesDialog servicesDialog;

	public static DrillDownDialog getInstance() {
	    if (instance == null) {
		instance = new DrillDownDialog();
	    }
	    return instance;
	}

	private DrillDownDialog() {
	    super();
	    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	    setBounds((int)(dim.getWidth() * .05),
		      (int)(dim.getHeight() * .05),
		      (int)(dim.getWidth() * .4),
		      (int)(dim.getHeight() * .4));
	    this.setVisible(false);
	}

	void setOrgPanel(OrganizationPanel p) {

	    // close other popups to avoid confusion
	    if (servicesDialog != null) {
		servicesDialog.close();
	    }

	    getContentPane().removeAll();

	    orgPanel = p;
	    setTitle(ResourceBundle.getBundle("BrowserStrings").getString("Organization:_") + orgPanel.getOrganizationName());
	    getContentPane().add(orgPanel, BorderLayout.CENTER);

	    // add buttons
	    JButton drillButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Show_Services"));
            drillButton.setMnemonic('s');
            drillButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    servicesDialog = ServicesDialog.getInstance();
		    servicesDialog.setServicesPanel(
		        new ServicesPanel(orgPanel.getOrganization()));
                    servicesDialog.setModal(true);
		    servicesDialog.pack();
		    servicesDialog.setVisible(true);
		}
	    });

	    JButton dismissButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Dismiss"));
            dismissButton.setMnemonic('d');
            dismissButton.requestDefaultFocus();
	    dismissButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    close();
		}
	    });

	    JPanel buttonPanel = new JPanel();
	    buttonPanel.add(drillButton);
	    buttonPanel.add(dismissButton);
	    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}

	/*
	 * dispose child dialogs and then self
	 */
	void close() {
	    if (servicesDialog != null) {
		servicesDialog.close();
	    }
	    dispose();
	}
    }

    /**
     * Used for drilling down into organization services.
     * This dialog only holds the ServicesPanel and some
     * buttons. Will get rid of this if ServicesPanel
     * is worked into DrillDownDialog.
     */
    static class ServicesDialog extends JDialog {

	static ServicesDialog instance;
	static ServicesPanel servicesPanel;
	static ServiceBindingsDialog serviceBindingsDialog;

	public static ServicesDialog getInstance() {
	    if (instance == null) {
		instance = new ServicesDialog();
	    }
	    return instance;
	}

	private ServicesDialog() {
	    super();
	    setTitle(ResourceBundle.getBundle("BrowserStrings").getString("Services"));
	    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	    setBounds((int)(dim.getWidth() * .15),
		      (int)(dim.getHeight() * .15),
		      (int)(dim.getWidth() * .4),
		      (int)(dim.getHeight() * .4));
	    this.setVisible(false);
	}

	void setServicesPanel(ServicesPanel panel) {
	    servicesPanel = panel;
	    getContentPane().removeAll();
	    servicesPanel.setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("BrowserStrings").getString("Services")));
	    getContentPane().add(servicesPanel, BorderLayout.CENTER);

	    // add buttons
	    JButton drillButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Show_ServiceBindings"));
            drillButton.setMnemonic('s');
	    drillButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    serviceBindingsDialog = ServiceBindingsDialog.getInstance();
		    serviceBindingsDialog.setServiceBindingsPanel(
		        new ServiceBindingsPanel(servicesPanel.getSelectedService()));
                    serviceBindingsDialog.setModal(true);
		    serviceBindingsDialog.pack();
		    serviceBindingsDialog.setVisible(true);
		}
	    });

	    JButton dismissButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Dismiss"));
            dismissButton.setMnemonic('d');
	    dismissButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    close();
		}
	    });

	    drillButton.setEnabled(servicesPanel.hasServices());

	    JPanel buttonPanel = new JPanel();
	    buttonPanel.add(drillButton);
	    buttonPanel.add(dismissButton);
	    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}

	/*
	 * dispose child dialogs and then self
	 */
	void close() {
	    if (serviceBindingsDialog != null) {
		serviceBindingsDialog.close();
	    }
	    dispose();
	}
    }

    /**
     * Used for drilling down into organization service bindings.
     * This dialog only holds the ServiceBindingsPanel and some
     * buttons. Will get rid of this if ServiceBindingsPanel
     * is worked into DrillDownDialog.
     */
    static class ServiceBindingsDialog extends JDialog {

	static ServiceBindingsDialog instance;
	static ServiceBindingsPanel serviceBindingsPanel;

	public static ServiceBindingsDialog getInstance() {
	    if (instance == null) {
		instance = new ServiceBindingsDialog();
	    }
	    return instance;
	}

	private ServiceBindingsDialog() {
	    super();
	    setTitle(ResourceBundle.getBundle("BrowserStrings").getString("ServiceBindings"));
	    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	    setBounds((int)(dim.getWidth() * .25),
		      (int)(dim.getHeight() * .25),
		      (int)(dim.getWidth() * .4),
		      (int)(dim.getHeight() * .4));
	    this.setVisible(false);
	}

	void setServiceBindingsPanel(ServiceBindingsPanel panel) {
	    serviceBindingsPanel = panel;
	    getContentPane().removeAll();
	    serviceBindingsPanel.setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("BrowserStrings").getString("ServiceBindings")));
	    getContentPane().add(serviceBindingsPanel, BorderLayout.CENTER);

	    // add buttons
	    JButton dismissButton = new JButton(ResourceBundle.getBundle("BrowserStrings").getString("Dismiss"));
            dismissButton.setMnemonic('d');
	    dismissButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    close();
		}
	    });

	    JPanel buttonPanel = new JPanel();
	    buttonPanel.add(dismissButton);
	    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}

	/*
	 * dispose child dialogs and then self
	 */
	void close() {
	    dispose();
	}
    }
}

