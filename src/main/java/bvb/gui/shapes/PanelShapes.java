package bvb.gui.shapes;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import bvb.core.BigVolumeBrowser;
import bvb.gui.PanelTitle;

public class PanelShapes extends JPanel
{
	final BigVolumeBrowser bvb;
	
	boolean bLocked = false;
	
	final private ShapesTable tableShapes;
	
	final JScrollPane tableScroller;
	
	public PanelShapes(final BigVolumeBrowser bvb_)
	{
		super(new GridBagLayout());	
		bvb = bvb_;
		this.setBorder(new PanelTitle(" Loaded shapes "));

		tableShapes = new ShapesTable(bvb, new ShapesTableModel(bvb));

		tableScroller = new JScrollPane(tableShapes);
		tableScroller.setMinimumSize(new Dimension(170, 200));
		tableScroller.setPreferredSize(new Dimension(400, 500));
    	
    	GridBagConstraints gbc = new GridBagConstraints();
	    
	    gbc.insets = new Insets(4,3,4,3);

	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 1;
	    gbc.anchor = GridBagConstraints.NORTHWEST;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(tableScroller,gbc);
		
	}
	
	public void updateShapesTableUI()
	{
		tableShapes.updateUI();
	}
}
