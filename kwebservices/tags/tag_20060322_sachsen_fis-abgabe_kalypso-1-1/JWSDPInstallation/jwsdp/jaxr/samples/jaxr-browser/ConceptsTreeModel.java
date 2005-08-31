/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/**
 * A JTable that lists
 *
 * @author Jim Glennon
 */
public class ConceptsTreeModel extends DefaultTreeModel {
    
    RegistryBrowser registryBrowser;
    ConceptsTreeNode     rootNode;
    
    /**
     * Class Constructor.
     *
     *
     * @see
     */
    public ConceptsTreeModel(boolean updateOnCreate) {
        super(new DefaultMutableTreeNode());
        registryBrowser = RegistryBrowser.getInstance();

        // only update if parent component needs to
        if (updateOnCreate == true) {
            update();
        }
    }
    
    public void update()  {
        rootNode = new ConceptsTreeNode(ResourceBundle.getBundle("BrowserStrings").getString("Concepts"));
        setRoot(rootNode);
        
	JAXRClient client = registryBrowser.client;
	Collection rootConcepts = client.getClassificationSchemes();

	// add classification nodes to the tree
	Iterator iter = rootConcepts.iterator();
	ClassificationScheme rootConcept = null;
	NodeInfo nodeInfo = null;
	while (iter.hasNext()) { //update
	    nodeInfo = new NodeInfo();
	    rootConcept = (ClassificationScheme) iter.next();
	    nodeInfo.obj = rootConcept;
	    nodeInfo.loaded = false;
	    ConceptsTreeNode newNode = new ConceptsTreeNode(nodeInfo);
	    rootNode.add(new ConceptsTreeNode(nodeInfo)); 
	}
	reload(rootNode);
    }

    /**
     * fetches children of specified node
     */
    public void expandTree(DefaultMutableTreeNode node) {
        
        //Dont expand hidden root
        if (node.isRoot())
            return;
        
        NodeInfo nodeInfo = (NodeInfo) node.getUserObject();
        
        if (nodeInfo.loaded)
            return;
        
        nodeInfo.loaded = true;

        if (nodeInfo.obj instanceof ClassificationScheme) {
            ClassificationScheme concept =
            (ClassificationScheme) nodeInfo.obj;
            try {
                Collection childConcepts = concept.getChildrenConcepts();
                Iterator iter = childConcepts.iterator();
                ConceptsTreeNode newNode = null;
                Concept childConcept = null;
                while (iter.hasNext()) { //expandTree()
                    nodeInfo = new NodeInfo();
                    childConcept = (Concept) iter.next();
                    nodeInfo.obj = childConcept;
                    nodeInfo.loaded = false;
                    newNode = new ConceptsTreeNode(nodeInfo);
                    node.add(newNode);
                }
            } catch (JAXRException e) {
                e.printStackTrace();
            }
        } else {
            Concept concept = (Concept) nodeInfo.obj;
            try {
                Collection childConcepts = concept.getChildrenConcepts();
                Iterator iter = childConcepts.iterator();
                ConceptsTreeNode newNode = null;
                Concept childConcept = null;
                while (iter.hasNext()) { //expandTree()
                    nodeInfo = new NodeInfo();
                    childConcept = (Concept) iter.next();
                    nodeInfo.obj = childConcept;
                    nodeInfo.loaded = false;
                    newNode = new ConceptsTreeNode(nodeInfo);
                    node.add(newNode);
                }
            } catch (JAXRException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    /**
     * Class Declaration.
     *
     *
     * @see
     *
     * @author
     */
    private class ConceptsTreeNode extends DefaultMutableTreeNode {
        /**
         * Class Constructor.
         *
         *
         * @param userObject
         *
         * @see
         */
        ConceptsTreeNode(Object userObject) {
            super(userObject);
        }
        
        public String toString() {
            
            String str = super.toString();
            
            if (!isRoot()) {
                NodeInfo nodeInfo = (NodeInfo) getUserObject();
                if (nodeInfo.obj instanceof RegistryObject) {
                    str = getName(nodeInfo.obj);
                }
            }
            
            // being friendly
            if (str == null) {
                str = ResourceBundle.getBundle("BrowserStrings").getString("[no_name_info_given]");
            }
            return str;
        }
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
