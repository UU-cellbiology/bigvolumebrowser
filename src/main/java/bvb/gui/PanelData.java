package bvb.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import bvb.core.BigVolumeBrowser;


public class PanelData extends JPanel
{
	final BigVolumeBrowser bvb;
	
	final private JTree treeData;
	
	final JScrollPane treeScroller;
	
	
	public PanelData(final BigVolumeBrowser bvb_)
	{
		super(new GridBagLayout());	
		bvb = bvb_;
		this.setBorder(new PanelTitle(" Loaded data "));
		
		
        //create the root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        //create the child nodes
        DefaultMutableTreeNode vegetableNode = new DefaultMutableTreeNode("Vegetables");
        DefaultMutableTreeNode fruitNode = new DefaultMutableTreeNode("Fruits");
        //add the child nodes to the root node
        root.add(vegetableNode);
        root.add(fruitNode);
        vegetableNode.add(new DefaultMutableTreeNode("Tomato"));
        vegetableNode.add(new DefaultMutableTreeNode("Potato"));
        fruitNode.add(new DefaultMutableTreeNode("Banana"));
        fruitNode.add(new DefaultMutableTreeNode("Mango"));
        fruitNode.add(new DefaultMutableTreeNode("Apple"));
        //create the tree by passing in the root node
        treeData = new JTree(bvb.dataTreeModel);
        
        treeData.setRootVisible(false);
        
        ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("/icons/bioformats.png"));
        DataTreeCellRenderer renderer = new DataTreeCellRenderer();
        renderer.setLeafIcon(imageIcon);
        renderer.setOpenIcon( imageIcon );
        renderer.setClosedIcon( imageIcon );

        treeData.setCellRenderer(renderer);
        treeData.setShowsRootHandles(true);
    	treeScroller = new JScrollPane(treeData);
    	treeScroller.setMinimumSize(new Dimension(170, 250));
    	treeScroller.setPreferredSize(new Dimension(400, 500));
    	
    	GridBagConstraints gbc = new GridBagConstraints();
	    
	    gbc.insets = new Insets(4,3,4,3);

	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 1;
	    gbc.anchor = GridBagConstraints.NORTHWEST;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(treeScroller,gbc);
	}
}
