/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/**
 * The JAXR Registry Browser
 *
 */
public class RegistryBrowser extends JFrame {

    static final String selectAnItem = ResourceBundle.getBundle("BrowserStrings").getString("[enter_URL_or_choose_from_menu]");
    static Cursor defaultCursor;
    static RegistryBrowser instance;
    static JAXRClient client;

    boolean conceptsTreeModelUpdated = false;

    // used outside constructor
    FindByPanel findByPanel;
    ConceptsTree conceptsTree;
    RegistryObjectsTableModel registryObjectsTableModel;
    RegistryObjectsTable registryObjectsTable;
    JPanel findPanel;
    SubmissionPanel submissionPanel;
    JMenuBar    menuBar;
    JMenuItem   newItem;
    JMenuItem   openItem;
    JMenuItem   saveItem;
    JMenuItem   saveAsItem;
    JMenuItem   exitItem;
    JMenuItem   cutItem;
    JMenuItem   copyItem;
    JMenuItem   pasteItem;
    JMenuItem   aboutItem;

    // move inside constructor later
    FileDialog     saveFileDialog = new java.awt.FileDialog(this);
    FileDialog     compositionDialog = new java.awt.FileDialog(this);
    JFileChooser	openFileDialog = new javax.swing.JFileChooser();
    JTabbedPane	tabbedPane = new javax.swing.JTabbedPane();
    JPanel		browsePanel = new javax.swing.JPanel();
    JPanel		topPanel = new javax.swing.JPanel();
    JComboBox	registryCombo = new javax.swing.JComboBox();
    JPanel      toolbarPanel = new javax.swing.JPanel();
    JToolBar    browseToolBar = new javax.swing.JToolBar();
    JToolBar	submissionToolBar = new javax.swing.JToolBar();
    JToolBar.Separator      toolBarSeparator1 = new JToolBar.Separator();
    JToolBar.Separator      toolBarSeparator2 = new JToolBar.Separator();
    JButton     searchButton = new javax.swing.JButton();
    JPanel      registryObjectsPanel = new javax.swing.JPanel();
    FindInputPanel [] findInputPanels;

    /**
     * Returns an instance of the GUI admin application. Creates
     * one the first time it is called.
     *
     * @return instance of JMSAdminApp
     *
     * @exception JMSException
     *
     * @see
     */
    public static RegistryBrowser getInstance() {
        if (instance == null) {
	    instance = new RegistryBrowser();
        }
        return instance;
    }

    /**
     * Class Constructor.
     *
     *
     * @see
     */
    private RegistryBrowser() {
        instance = this;
	client = new JAXRClient();

        menuBar = new javax.swing.JMenuBar();
        JMenu       fileMenu = new javax.swing.JMenu();
        JSeparator  JSeparator1 = new javax.swing.JSeparator();
        newItem = new javax.swing.JMenuItem();
        openItem = new javax.swing.JMenuItem();
        saveItem = new javax.swing.JMenuItem();
        saveAsItem = new javax.swing.JMenuItem();
        exitItem = new javax.swing.JMenuItem();
        cutItem = new javax.swing.JMenuItem();
        copyItem = new javax.swing.JMenuItem();
        pasteItem = new javax.swing.JMenuItem();
        aboutItem = new javax.swing.JMenuItem();

        setJMenuBar(menuBar);
        setTitle(ResourceBundle.getBundle("BrowserStrings").getString("Registry_Browser"));
        setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(0, 0));

        // Scale window to be centered using 70% of screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((int)(dim.getWidth() * .15),(int)(dim.getHeight() * .1),
        (int)(dim.getWidth() * .7), (int)(dim.getHeight() * .75));
        setVisible(false);

        GridBagLayout gb = new GridBagLayout();
        topPanel.setLayout(gb);
        getContentPane().add("North", topPanel);
        GridBagConstraints  c = new GridBagConstraints();

        toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toolbarPanel.setBounds(0, 0, 488, 29);
        browseToolBar.setAlignmentY(0.222222F);
        toolbarPanel.add(browseToolBar);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);
        gb.setConstraints(toolbarPanel, c);
        topPanel.add(toolbarPanel);

        JPanel locationPanel = new JPanel();
        GridBagLayout gb1 = new GridBagLayout();
        locationPanel.setLayout(gb1);

        JLabel locationLabel = new JLabel(ResourceBundle.getBundle("BrowserStrings").getString("Registry_Location:"));
        locationLabel.setLabelFor(registryCombo);
        locationLabel.setDisplayedMnemonic('r');
        //		locationLabel.setPreferredSize(new Dimension(80, 23));

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 5, 4, 3);
        gb1.setConstraints(locationLabel, c);
        //locationPanel.setBackground(Color.green);
        locationPanel.add(locationLabel);

        String pathSep = System.getProperty("file.separator");

        registryObjectsTableModel = new RegistryObjectsTableModel();
        registryObjectsTable =
            new RegistryObjectsTable(registryObjectsTableModel);
        JScrollPane registryObjectsTablePane =
            new JScrollPane(registryObjectsTable);
        conceptsTree = new ConceptsTree(false);

	    registryCombo.addItem(selectAnItem);

        registryCombo.addItem("http://uddi.ibm.com/ubr/inquiryapi");
        registryCombo.addItem("https://uddi.ibm.com/ubr/publishapi");
        registryCombo.addItem("http://uddi.ibm.com/testregistry/inquiryapi");
        registryCombo.addItem("https://uddi.ibm.com/testregistry/publishapi");
        registryCombo.addItem("http://uddi.microsoft.com/inquire");
        registryCombo.addItem("https://uddi.microsoft.com/publish");
        registryCombo.addItem("http://test.uddi.microsoft.com/inquire");
        registryCombo.addItem("https://test.uddi.microsoft.com/publish");


        registryCombo.setEditable(true);
        registryCombo.setEnabled(true);
        registryCombo.setToolTipText(java.util.ResourceBundle.getBundle("BrowserStrings").getString("Select_URL_for_connection."));

        registryCombo.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e)  {
		String url = (String) registryCombo.getSelectedItem();
		if (url.equals(selectAnItem)) {
		    return;
		}

		Cursor oldCursor = RegistryBrowser.this.getCursor();
		RegistryBrowser.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		connectToRegistry(url);

		/*
		 * we want to be able to change registries
		 * without erasing table
		 */
		if (conceptsTreeModelUpdated == false) {
		    ((ConceptsTreeModel)conceptsTree.getModel()).update();
		    conceptsTreeModelUpdated = true;
		}

		RegistryBrowser.this.setCursor(oldCursor);
            }
        });

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.9;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 5, 0);
        gb1.setConstraints(registryCombo, c);
        locationPanel.add(registryCombo);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.9;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 0, 0);
        gb.setConstraints(locationPanel, c);
        topPanel.add(locationPanel, c);

        browseToolBar.setBounds(0, 0, 199, 29);
        searchButton.setText(ResourceBundle.getBundle("BrowserStrings").getString("Search"));
        searchButton.setToolTipText(ResourceBundle.getBundle("BrowserStrings").getString("Search"));
        searchButton.setMnemonic((int) 'S');
        browseToolBar.add(searchButton);
        searchButton.setBounds(141, 4, 23, 23);

        // submission toolbar
        JButton	submitButton = new javax.swing.JButton();
        submitButton.setText(ResourceBundle.getBundle("BrowserStrings").getString("Submit"));
        submitButton.setMnemonic((int) 'S');
        submissionToolBar.add(submitButton);

        JButton	addServiceButton = new javax.swing.JButton();
        addServiceButton.setText(ResourceBundle.getBundle("BrowserStrings").getString("Add_service"));
        addServiceButton.setMnemonic((int) 'A');
        submissionToolBar.add(addServiceButton);

        getContentPane().add("Center", tabbedPane);
        tabbedPane.setBounds(0, 29, 488, 280);

        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                toolbarPanel.removeAll();
                validate();
                if (e.getSource() == tabbedPane) {
                    if (((JTabbedPane)e.getSource()).getSelectedIndex() == 0)
                        toolbarPanel.add(browseToolBar);
                    else
                        toolbarPanel.add(submissionToolBar);
                }
            }
        });

        // set up the Browse panel
        browsePanel.setLayout(new BorderLayout());
        tabbedPane.add(browsePanel);

        registryObjectsPanel.setLayout(new BorderLayout());
        JScrollPane conceptsTreePane = new JScrollPane(conceptsTree);

        // added to findSplitPane
        findByPanel = new FindByPanel();
        JScrollPane findByPane = new JScrollPane(findByPanel);
        findPanel = new JPanel();

        // add panels that take user input for searching
        findPanel.setLayout(new CardLayout());
        int numPanes = FindByPanel.findMethods.length;
        findInputPanels = new FindInputPanel [numPanes];
        for (int i=0; i<numPanes; i++) {
            if (FindByPanel.findMethods[i].equals(FindByPanel.CLASSIFICATION)) {
                findInputPanels[i] = null; // for classification tree
                findPanel.add(FindByPanel.findMethods[i], conceptsTreePane);
            } else {
                findInputPanels[i] = new FindInputPanel(FindByPanel.findMethods[i]);
                findPanel.add(FindByPanel.findMethods[i],
                new JScrollPane(findInputPanels[i]));
            }
        }

        //add findByPanel and findPanel to findSplitPane and add to browserSplitPane
        JSplitPane findSplitPane =
            new JSplitPane(JSplitPane.VERTICAL_SPLIT, findByPane, findPanel);
        findSplitPane.setDividerLocation(100);
        JSplitPane browserSplitPane =
            new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, findSplitPane,
                registryObjectsTablePane);
        browserSplitPane.setDividerLocation(200);
        browsePanel.add(browserSplitPane, "Center");

        // set up the Submission panel
        submissionPanel = new SubmissionPanel();
        tabbedPane.add(submissionPanel);
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (registryCombo.getSelectedItem().equals(selectAnItem)) {
                    JOptionPane.showMessageDialog(null, ResourceBundle.getBundle("BrowserStrings").getString("Please_select_a_registry_first."),
                    ResourceBundle.getBundle("BrowserStrings").getString("Error:"),
                    JOptionPane.ERROR_MESSAGE);
                    return;
                }
		String id = client.doSubmission(submissionPanel);
		if (id != null) {
                    JOptionPane.showMessageDialog(null,
                        ResourceBundle.getBundle("BrowserStrings").getString("Organization_submitted_successfully._Key_is\n") + id);
                } // else: client cancelled submission
            }
        });
        addServiceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                submissionPanel.getServicesPanel().addService();
            }
        });
        try {
            tabbedPane.setTitleAt(0, ResourceBundle.getBundle("BrowserStrings").getString("Browse"));
            tabbedPane.setTitleAt(1, ResourceBundle.getBundle("BrowserStrings").getString("Submissions"));
        } catch (ArrayIndexOutOfBoundsException e) {}

        fileMenu.setText(ResourceBundle.getBundle("BrowserStrings").getString("File"));
        fileMenu.setActionCommand(ResourceBundle.getBundle("BrowserStrings").getString("File"));
        fileMenu.setMnemonic((int) 'F');
        menuBar.add(fileMenu);
        exitItem.setText(ResourceBundle.getBundle("BrowserStrings").getString("Exit"));
        exitItem.setActionCommand(ResourceBundle.getBundle("BrowserStrings").getString("Exit"));
        exitItem.setMnemonic((int) 'X');
        fileMenu.add(exitItem);
        tabbedPane.setSelectedIndex(0);

        //REGISTER_LISTENERS
        SymWindow   aSymWindow = new SymWindow();

        this.addWindowListener(aSymWindow);
        SymAction   lSymAction = new SymAction();

        openItem.addActionListener(lSymAction);
        saveItem.addActionListener(lSymAction);
        exitItem.addActionListener(lSymAction);
        aboutItem.addActionListener(lSymAction);
        searchButton.addActionListener(lSymAction);

        SwingUtilities.updateComponentTreeUI(getContentPane());
        SwingUtilities.updateComponentTreeUI(menuBar);
        SwingUtilities.updateComponentTreeUI(submissionToolBar);
        SwingUtilities.updateComponentTreeUI(openFileDialog);
    }

    public void connectToRegistry(String url) {
	client.makeNewConnection(url);
    }

    /**
     * Used to kick off a search as if the search button had
     * been pressed.
     */
    void performSearch(ActionEvent ev) {
        searchButton.doClick();
    }

    /**
     * Allows editing of a registry object for submission. Sets
     * tabbed pane to the Submission pane and fills out fields with
     * existing information contained in the registry object.
     */
    void editRegistryObject(RegistryObject regObject) throws JAXRException {
        if (regObject instanceof Organization) {
            tabbedPane.setSelectedIndex(1);
            submissionPanel.editOrganization((Organization) regObject);
        } else {
            throw new JAXRException(ResourceBundle.getBundle("BrowserStrings").getString("Can_only_edit_Organizations."));
        }
    }

    SubmissionPanel getSubmissionPanel() {
        return submissionPanel;
    }

    /**
     * Delete an object from the registry.
     */
    void deleteRegistryObject(RegistryObject regObject) throws JAXRException {
        if (regObject instanceof Organization) {
            boolean result =
                client.deleteOrganization((Organization) regObject);
            if (result == true) {
                JOptionPane.showMessageDialog(null,
                    ResourceBundle.getBundle("BrowserStrings").getString("Organization_removed_from_registry."),
                    ResourceBundle.getBundle("BrowserStrings").getString("Delete_Organization"),
                    JOptionPane.INFORMATION_MESSAGE);
            } // else: client cancelled delete
        } else {
            throw new JAXRException(ResourceBundle.getBundle("BrowserStrings").getString("Can_only_delete_Organizations."));
        }
    }

    /*
     * Utility method for components that use
     * GridBagLayout.
     */
    static void makeConstraints(GridBagConstraints gbc,
        int gridx, int gridy, int gridwidth, int gridheight,
        double weightx, double weighty, int fill, int anchor) {
            gbc.gridx = gridx;
            gbc.gridy = gridy;
            gbc.gridwidth = gridwidth;
            gbc.gridheight = gridheight;
            gbc.weightx = weightx;
            gbc.weighty = weighty;
            gbc.fill = fill;
            gbc.anchor = anchor;
            gbc.insets = new Insets(0, 0, 0, 0);
    }

    /**
     * Helper method to let browser subcomponents set a
     * wait cursor while performing long operations.
     */
    static void setWaitCursor() {
        defaultCursor = instance.getCursor();
        instance.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /**
     * Helper method for browser subcomponents to set the
     * cursor back to its default version.
     */
    static void setDefaultCursor() {
        instance.setCursor(defaultCursor);
    }

    static void displayError(String message) {
	JOptionPane.showMessageDialog(null, message, ResourceBundle.getBundle("BrowserStrings").getString("Error:"),
				      JOptionPane.ERROR_MESSAGE);
    }

    static void displayError(String message, Throwable t) {
	JOptionPane.showMessageDialog(null, (message + "\n" + t),
				      ResourceBundle.getBundle("BrowserStrings").getString("Error:"),
                                      JOptionPane.ERROR_MESSAGE);
    }

    static void displayError(Throwable t) {
        JOptionPane.showMessageDialog(null, t.toString(), ResourceBundle.getBundle("BrowserStrings").getString("Exception:"),
            JOptionPane.ERROR_MESSAGE);
    }

    /**
     * The entry point for this application.
     * Sets the Look and Feel to the System Look and Feel.
     * Creates a new RegistryBrowser and makes it visible.
     */
    static public void main(String[] args) {
        try {
	    if ((args.length == 1) && (args[0].equalsIgnoreCase("help"))) {
		System.err.println(ResourceBundle.getBundle("BrowserStrings").getString("\nWith_no_firewall,_usage:_java_RegistryBrowser\nWith_firewall,_usage:_java_RegistryBrowser_proxyHost_proxyPort\nor:_java_RegistryBrowser_httpProxyHost_httpProxyPort_httpsProxyHost_httpsProxyPort\n"));
		System.exit(0);
	    } else
        if ((args.length == 1) && (args[0].equalsIgnoreCase("help_unix"))) {
System.err.println(ResourceBundle.getBundle("BrowserStrings").getString("\n_script_unix:With_no_firewall,_usage:_java_RegistryBrowser\nWith_firewall,_usage:_java_RegistryBrowser_proxyHost_proxyPort\nor:_java_RegistryBrowser_httpProxyHost_httpProxyPort_httpsProxyHost_httpsProxyPort\n"));
		System.exit(0);
        } else
        if ((args.length == 1) && (args[0].equalsIgnoreCase("help_windows"))) {
System.err.println(ResourceBundle.getBundle("BrowserStrings").getString("\n_script_windows:With_no_firewall,_usage:_java_RegistryBrowser\nWith_firewall,_usage:_java_RegistryBrowser_proxyHost_proxyPort\nor:_java_RegistryBrowser_httpProxyHost_httpProxyPort_httpsProxyHost_httpsProxyPort\n"));
		System.exit(0);
        }
            RegistryBrowser browser = getInstance();
            // Fetch system properties for proxies.
            browser.client.httpProxyHost = System.getProperty("http.proxyHost");
            if (browser.client.httpProxyHost != null)
                browser.client.httpProxyPort = System.getProperty("http.proxyPort");
            else
                browser.client.httpProxyHost = "";
            browser.client.httpsProxyHost = browser.client.httpProxyHost;
            browser.client.httpsProxyPort = browser.client.httpProxyPort;
            // Override proxy properties with command line arguments
        if (args.length >= 2) {
		browser.client.httpProxyHost = args[0];
		browser.client.httpProxyPort = args[1];
		browser.client.httpsProxyHost = args[0];
		browser.client.httpsProxyPort = args[1];
		System.out.println(ResourceBundle.getBundle("BrowserStrings").getString("\nBrowser_will_use_proxy_") + args[0] +
				   ResourceBundle.getBundle("BrowserStrings").getString("_on_port_") + args[1]);
            }
	    if (args.length == 4) {
		browser.client.httpsProxyHost = args[2];
		browser.client.httpsProxyPort = args[3];
		System.out.println(ResourceBundle.getBundle("BrowserStrings").getString("Browser_will_use_proxy_") + args[2] +
				   ResourceBundle.getBundle("BrowserStrings").getString("_on_port_") + args[3] + ResourceBundle.getBundle("BrowserStrings").getString("_for_ssl"));
	    }
	    browser.setVisible(true);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
            System.exit(1);
        }
    }

    /**
     * Method Declaration.
     *
     *
     * @param doConfirm
     * @param exitStatus
     *
     */
    void exitApplication(boolean doConfirm, int exitStatus) {
        boolean doExit = true;

        if (doConfirm) {
            try {

                // Show a confirmation dialog
                int reply = JOptionPane.showConfirmDialog(this, ResourceBundle.getBundle("BrowserStrings").getString("Do_you_really_want_to_exit?"),
                ResourceBundle.getBundle("BrowserStrings").getString("Registry_Browser"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

                // If the confirmation was affirmative, handle exiting.
                if (reply == JOptionPane.YES_OPTION) {
                    this.setVisible(false);     // hide the Frame
                    this.dispose();             // free the system resources
                    exitStatus = 0;
                } else {
                    doExit = false;
                }
            } catch (Exception e) {}
        }

        if (doExit) {
            System.exit(exitStatus);
        }

    }

    /**
     * Class Declaration.
     *
     *
     * @see
     *
     * @author
     * @version   1.17, 03/29/00
     */
    class SymWindow extends WindowAdapter {

        /**
         * Method Declaration.
         *
         *
         * @param event
         *
         * @see
         */
        public void windowClosing(WindowEvent event) {
            Object  object = event.getSource();

            if (object == RegistryBrowser.this) {
                RegistryBrowser_windowClosing(event);
            }
        }

    }

    /**
     * Method Declaration.
     *
     *
     * @param event
     *
     * @see
     */
    void RegistryBrowser_windowClosing(WindowEvent event) {

        // to do: code goes here.
        RegistryBrowser_windowClosing_Interaction1(event);
    }

    /**
     * Method Declaration.
     *
     *
     * @param event
     *
     * @see
     */
    void RegistryBrowser_windowClosing_Interaction1(WindowEvent event) {
        try {
            this.exitApplication(true, 0);
        } catch (Exception e) {}
    }

    /**
     * Class Declaration.
     *
     *
     * @see
     *
     * @author
     * @version   1.17, 03/29/00
     */
    class SymAction implements ActionListener {

        /**
         * Method Declaration.
         *
         *
         * @param event
         *
         * @see
         */
        public void actionPerformed(ActionEvent event) {
            Object  object = event.getSource();

            if (object == openItem) {
                openItem_actionPerformed(event);
            } else if (object == saveItem) {
                saveItem_actionPerformed(event);
            } else if (object == exitItem) {
                exitItem_actionPerformed(event);
            } else if (object == aboutItem) {
                aboutItem_actionPerformed(event);
            } else if (object == searchButton) {
                searchButton_actionPerformed(event);
            }
        }

    }

    /**
     * Method Declaration.
     *
     *
     * @param event
     *
     * @see
     */
    void openItem_actionPerformed(ActionEvent event) {

        // to do: code goes here.
        openItem_actionPerformed_Interaction1(event);
    }

    /**
     * Method Declaration.
     *
     *
     * @param event
     *
     * @see
     */
    void openItem_actionPerformed_Interaction1(ActionEvent event) {
        try {
            openFileDialog.setVisible(true);
        } catch (Exception e) {}
    }

    /**
     * Method Declaration.
     *
     *
     * @param event
     *
     * @see
     */
    void saveItem_actionPerformed(ActionEvent event) {

        // to do: code goes here.
        saveItem_actionPerformed_Interaction1(event);
    }

    /**
     * Method Declaration.
     *
     *
     * @param event
     *
     * @see
     */
    void saveItem_actionPerformed_Interaction1(ActionEvent event) {
        try {
            saveFileDialog.setVisible(true);
        } catch (Exception e) {}
    }

    /**
     * Method Declaration.
     *
     *
     * @param event
     *
     * @see
     */
    void exitItem_actionPerformed(ActionEvent event) {

        // to do: code goes here.
        exitItem_actionPerformed_Interaction1(event);
    }

    /**
     * Method Declaration.
     *
     *
     * @param event
     *
     * @see
     */
    void exitItem_actionPerformed_Interaction1(ActionEvent event) {
        try {
            this.exitApplication(true, 0);
        } catch (Exception e) {}
    }

    /**
     * Method Declaration.
     *
     *
     * @param event
     *
     * @see
     */
    void aboutItem_actionPerformed(ActionEvent event) {

        // to do: code goes here.
        aboutItem_actionPerformed_Interaction1(event);
    }

    /**
     * Method Declaration.
     *
     *
     * @param event
     *
     * @see
     */
    void aboutItem_actionPerformed_Interaction1(ActionEvent event) {
/* jimbog
        try {

            // JAboutDialog Create with owner and show as modal
            {
                JAboutDialog    JAboutDialog1 = new JAboutDialog(this);

                JAboutDialog1.setModal(true);
                JAboutDialog1.show();
            }
        } catch (Exception e) {}
 */
    }

    /**
     * Determine which search criteria was used and
     * call the proper method to update the table.
     *
     * @param event
     *
     * @see
     */
    public void searchButton_actionPerformed(ActionEvent event) {
        int panelNumber = findByPanel.findByCombo.getSelectedIndex();

        Cursor oldCursor = RegistryBrowser.this.getCursor();
        RegistryBrowser.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        switch (panelNumber) {
            case 0: // search by name
                searchButton_actionPerformed_Interaction2(event, panelNumber);
                break;
            case 1: // search by tree
                searchButton_actionPerformed_Interaction1(event);
                break;
        }
        RegistryBrowser.this.setCursor(oldCursor);
    }

    /**
     * This method updates the table based on the
     * concepts tree.
     *
     */
    void searchButton_actionPerformed_Interaction1(ActionEvent event) {
        ((RegistryObjectsTableModel)registryObjectsTable.getModel()).
        updateTableModelByClassifications(conceptsTree.getSelectionPaths());
    }

    /**
     * This method updates the table based on a
     * search by name.
     */
    void searchButton_actionPerformed_Interaction2(ActionEvent event, int x) {
        String url = (String) registryCombo.getSelectedItem();
        if (url.equals(selectAnItem)) {
            JOptionPane.showMessageDialog(null,
                ResourceBundle.getBundle("BrowserStrings").getString("Please_select_a_registry_URL."));
            return;
        }
        FindInputPanel panel = (FindInputPanel)
            ((JScrollPane) findPanel.getComponent(x)).getViewport().getView();
        String name = panel.inputField.getText();
        ((RegistryObjectsTableModel)registryObjectsTable.getModel()).
            updateTableModelByName(name);
    }
}


