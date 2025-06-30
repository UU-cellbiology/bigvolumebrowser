package bvb.gui;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.UIManager;

import bdv.ui.UIUtils;


public class JPanelConsistent extends JPanel
{	
	private Color consistentBg = Color.WHITE;

	private Color inConsistentBg = Color.WHITE;
	
	public JPanelConsistent( final GridBagLayout gridBagLayout )
	{
		super(gridBagLayout);
		updateColors();
	}

	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}
	
	public void setConsistent( final boolean isConsistent )
	{
		setBackground( isConsistent ? consistentBg : inConsistentBg );
	}
}
