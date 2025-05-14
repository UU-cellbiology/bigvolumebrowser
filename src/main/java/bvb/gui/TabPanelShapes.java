package bvb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import bvb.core.BigVolumeBrowser;
import bvb.gui.shapes.PanelAddShapes;

public class TabPanelShapes extends JPanel
{
	final PanelAddShapes panelAddShapes;
	
	public TabPanelShapes(final BigVolumeBrowser bvb)
	{
		super(new GridBagLayout());	
		
		panelAddShapes = new PanelAddShapes(bvb);
	
		
	    GridBagConstraints gbc = new GridBagConstraints();
	    
	    gbc.insets = new Insets(4,3,4,3);

	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 1;
	    gbc.anchor = GridBagConstraints.NORTHWEST;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    this.add( panelAddShapes, gbc);
//	    gbc.gridy++;
//	    this.add( panelData, gbc);
//	    gbc.gridy++;
//	    this.add( panelAddShapes, gbc);

        // Blank/filler component
	    gbc.gridy++;
	    gbc.weightx = 0.01;
	    gbc.weighty = 0.01;
	    this.add(new JLabel(), gbc);
	}
}
