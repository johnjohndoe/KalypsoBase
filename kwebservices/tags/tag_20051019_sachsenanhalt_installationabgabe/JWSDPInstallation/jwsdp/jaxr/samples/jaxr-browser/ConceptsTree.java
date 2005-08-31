/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.tree.*;
import java.util.ResourceBundle;

/**
 * A JTree that lists
 *
 *
 * @author Jim Glennon
 */
public class ConceptsTree extends JTree {
    
    /**
     * Class Constructor.
     *
     *
     * @see
     */
    public ConceptsTree(boolean updateOnCreate) {
        super(new ConceptsTreeModel(updateOnCreate));
        setCellRenderer(new ConceptsTreeCellRenderer());
        setRootVisible(false);
        setShowsRootHandles(true);
        addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		    getLastSelectedPathComponent();
                if (node == null) return;
                Component c = SwingUtilities.getRoot(ConceptsTree.this);
                Cursor oldCursor = c.getCursor();
                c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                ((ConceptsTreeModel)getModel()).expandTree(node);
                c.setCursor(oldCursor);
            }
        }
				 );
        
        ((ConceptsTreeModel) getModel()).addTreeModelListener(new TreeModelListener() {
            
            public void treeNodesChanged(TreeModelEvent e) {}
            
            public void treeNodesInserted(TreeModelEvent e) {
                //                expandPath(e.getTreePath());
            }
            
            public void treeNodesRemoved(TreeModelEvent e) {}
            
            public void treeNodesStructureChanged(TreeModelEvent e) {}
            
            public void treeStructureChanged(TreeModelEvent e) {}
            
        }
							      );
        
        setToolTipText(ResourceBundle.getBundle("BrowserStrings").getString("Classification_Schemes"));
    }
    
    public class ConceptsTreeCellRenderer extends JLabel implements TreeCellRenderer {
        
        /**
         * Is the value currently selected.
         */
        protected boolean	 selected;
        
        // These two ivars will be made protected later.
        
        /**
         * True if has focus.
         */
        private boolean		 hasFocus;
        
        /**
         * True if draws focus border around icon as well.
         */
        private boolean		 drawsFocusBorderAroundIcon;
        
        // Icons
        
        /**
         * Icon used to show non-leaf nodes that aren't expanded.
         */
        transient protected Icon closedIcon;
        
        /**
         * Icon used to show leaf nodes.
         */
        transient protected Icon leafIcon;
        
        /**
         * Icon used to show non-leaf nodes that are expanded.
         */
        transient protected Icon openIcon;
        
        // Colors
        
        /**
         * Color to use for the foreground for selected nodes.
         */
        protected Color		 textSelectionColor;
        
        /**
         * Color to use for the foreground for non-selected nodes.
         */
        protected Color		 textNonSelectionColor;
        
        /**
         * Color to use for the background when a node is selected.
         */
        protected Color		 backgroundSelectionColor;
        
        /**
         * Color to use for the background when the node isn't selected.
         */
        protected Color		 backgroundNonSelectionColor;
        
        /**
         * Color to use for the background when the node isn't selected.
         */
        protected Color		borderSelectionColor;
        
        DefaultMutableTreeNode	node = null;
        Object				obj = null;
        
        /**
         * Returns a new instance of DefaultTreeCellRenderer.  Alignment is
         * set to left aligned. Icons and text color are determined from the
         * UIManager.
         */
        public ConceptsTreeCellRenderer() {
            
            setHorizontalAlignment(JLabel.LEFT);
            setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
            setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
            setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
            setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
            setBorderSelectionColor(UIManager.getColor("Tree.selectionBorderColor"));
            Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");
            
            drawsFocusBorderAroundIcon = (value != null
					  && ((Boolean) value).booleanValue());
            
        }
        
        /**
         * Sets the color the text is drawn with when the node is selected.
         */
        public void setTextSelectionColor(Color newColor) {
            textSelectionColor = newColor;
        }
        
        /**
         * Returns the color the text is drawn with when the node is selected.
         */
        public Color getTextSelectionColor() {
            return textSelectionColor;
        }
        
        /**
         * Sets the color the text is drawn with when the node isn't selected.
         */
        public void setTextNonSelectionColor(Color newColor) {
            textNonSelectionColor = newColor;
        }
        
        /**
         * Returns the color the text is drawn with when the node isn't selected.
         */
        public Color getTextNonSelectionColor() {
            return textNonSelectionColor;
        }
        
        /**
         * Sets the color to use for the background if node is selected.
         */
        public void setBackgroundSelectionColor(Color newColor) {
            backgroundSelectionColor = newColor;
        }
        
        /**
         * Returns the color to use for the background if node is selected.
         */
        public Color getBackgroundSelectionColor() {
            return backgroundSelectionColor;
        }
        
        /**
         * Sets the background color to be used for non selected nodes.
         */
        public void setBackgroundNonSelectionColor(Color newColor) {
            backgroundNonSelectionColor = newColor;
        }
        
        /**
         * Returns the background color to be used for non selected nodes.
         */
        public Color getBackgroundNonSelectionColor() {
            return backgroundNonSelectionColor;
        }
        
        /**
         * Sets the color to use for the border.
         */
        public void setBorderSelectionColor(Color newColor) {
            borderSelectionColor = newColor;
        }
        
        /**
         * Returns the color the border is drawn.
         */
        public Color getBorderSelectionColor() {
            return borderSelectionColor;
        }

        /**
	 * Sublcassed to only accept the font if it isn't
	 * a FonUIResource
	 */
	public void setFont(Font font) {
	    if (font instanceof FontUIResource) {
		font = null;
	    }
	    super.setFont(font);
	}

        /**
         * Configures the renderer based on the passed in components.
         * The value is set from messaging value with toString().
         * The foreground color is set based on the selection and the icon
         * is set based on on leaf and expanded.
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value,
						      boolean sel, boolean expanded,
						      boolean leaf, int row,
						      boolean hasFocus) {
            
            node = (DefaultMutableTreeNode) value;
            ImageIcon		   icon = null;
            String		   text = node.toString();
            
            if (node == getModel().getRoot()) {
                setText(text);
                return this;
            }
            
            obj = ((NodeInfo)node.getUserObject()).obj;
            String stringValue = tree.convertValueToText(value, sel, expanded, leaf,
							 row, hasFocus);
            
            this.hasFocus = hasFocus;
            
            setText(stringValue);
            if (sel) {
                setForeground(getTextSelectionColor());
            } else {
                setForeground(getTextNonSelectionColor());
            }
            
            // There needs to be a way to specify disabled icons.
            if (!tree.isEnabled()) {
                setEnabled(false);
            } else {
                setEnabled(true);
            }
            selected = sel;
            
            return this;
        }
        
	/**
	 * Paints the value.  The background is filled based on selected.
	 */
        public void paint(Graphics g) {
            Color bColor;
            
            if (selected) {
                bColor = getBackgroundSelectionColor();
            } else {
                bColor = getBackgroundNonSelectionColor();
                if (bColor == null) {
                    bColor = getBackground();
                }
            }
            int imageOffset = -1;
            
            if (bColor != null) {
                Icon currentI = getIcon();
                
                imageOffset = getLabelStart();
                g.setColor(bColor);
                g.fillRect(imageOffset, 0, getWidth() - 1 - imageOffset,
			   getHeight());
            }
            if (hasFocus) {
                if (drawsFocusBorderAroundIcon) {
                    imageOffset = 0;
                } else if (imageOffset == -1) {
                    imageOffset = getLabelStart();
                }
                Color bsColor = getBorderSelectionColor();
                
                if (bsColor != null) {
                    g.setColor(bsColor);
                    g.drawRect(imageOffset, 0, getWidth() - 1 - imageOffset,
			       getHeight() - 1);
                }
            }
            
            super.paint(g);
        }
        
        /**
         * Method Declaration.
         *
         *
         * @return
         *
         * @see
         */
        private int getLabelStart() {
            Icon currentI = getIcon();
            
            if (currentI != null && getText() != null) {
                return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
            }
            return 0;
        }
        
	/**
	 * Overrides <code>JComponent.getPreferredSize</code> to
	 * return slightly wider preferred size value.
	 */
        public Dimension getPreferredSize() {
            Dimension retDimension = super.getPreferredSize();
            
            if (retDimension != null) {
                retDimension = new Dimension(retDimension.width + 3,
					     retDimension.height);
            }
            return retDimension;
        }
        
    }    
}
