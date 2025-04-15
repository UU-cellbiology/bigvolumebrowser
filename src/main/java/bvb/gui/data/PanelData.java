package bvb.gui.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;


import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import bvb.core.BigVolumeBrowser;
import bvb.gui.PanelTitle;


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
		

        //create the tree by passing in the data model
        treeData = new JTree(bvb.dataTreeModel);
        
        treeData.setRootVisible(false);
        
        DataTreeCellRenderer renderer = new DataTreeCellRenderer();

        renderer.setLeafIcon( bvb.dataTreeModel.getSourceIcon() );
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
