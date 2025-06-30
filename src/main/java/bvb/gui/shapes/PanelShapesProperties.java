package bvb.gui.shapes;

import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import bvb.core.BigVolumeBrowser;

public class PanelShapesProperties extends JPanel 
{
	final BigVolumeBrowser bvb;

	final JTabbedPane tabPane;
	
	public PanelShapesProperties(final BigVolumeBrowser bvb_)
	{
		super();
		bvb = bvb_;
		GridBagLayout gridbag = new GridBagLayout();

		setLayout(gridbag);
		
		tabPane = new JTabbedPane(SwingConstants.TOP);
	}
}
