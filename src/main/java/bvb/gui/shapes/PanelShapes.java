package bvb.gui.shapes;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import bvb.core.BigVolumeBrowser;

public class PanelShapes extends JPanel
{
	final BigVolumeBrowser bvb;
	boolean bLocked = false;
	
	public PanelShapes(final BigVolumeBrowser bvb_)
	{
		super(new GridBagLayout());	
		bvb = bvb_;
	}
}
