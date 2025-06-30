package bvb.gui.shapes;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import bdv.tools.brightness.ColorIcon;
import bdv.ui.UIUtils;
import bvb.gui.ColorUserSettings;
import bvb.gui.GBCHelper;
import bvb.gui.NumberField;
import bvb.gui.SelectedObjects;

public class PanelsSpotsProperties extends JPanel
{
	final SelectedObjects selectedObjects;
	final NumberField nfSpSize;
	final JButton butColor;
	final JComboBox<String> cbShape;
	final JComboBox<String> cbRender;
	
	final ArrayList<Component> allComp = new ArrayList<>();
	
	ColorUserSettings selectColors = new ColorUserSettings();
	

	private Color consistentBg = Color.WHITE;

	private Color inConsistentBg = Color.WHITE;
	
	public PanelsSpotsProperties(final SelectedObjects so)
	{
		super();	
		
		selectedObjects = so;
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		nfSpSize = new NumberField(5);
		butColor = new JButton( new ColorIcon( Color.WHITE ) );
		
		String[] sShapes = {"Round", "Square"};
		cbShape = new JComboBox< >(sShapes);

		String[] sRender = {"Filled", "Outline", "Gauss", "Gauss norm"};
		cbRender = new JComboBox< >(sRender);

		allComp.add( butColor );
		allComp.add( nfSpSize );
		allComp.add( cbShape );
		allComp.add( cbRender );
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		GBCHelper.alighLoose(gbc);
		this.add(new JLabel("Color: "), gbc);
		gbc.gridx++;	
		this.add( butColor, gbc );
		
		gbc.gridx = 0;
		gbc.gridy ++;		
		this.add(new JLabel("Size: "), gbc);
		gbc.gridx++;	
		this.add( nfSpSize, gbc );
		
		gbc.gridx = 0;
		gbc.gridy ++;		
		this.add(new JLabel("Shape: "), gbc);
		gbc.gridx++;	
		this.add( cbShape, gbc );
		
		gbc.gridx = 0;
		gbc.gridy ++;		
		this.add(new JLabel("Render: "), gbc);
		gbc.gridx++;	
		this.add( cbRender, gbc );
		
		updateColors();
		
	}
	
	void updateGUI()
	{
		
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(final Component nC:allComp)
		{
			nC.setEnabled( bEnabled );
		}
	}
	

	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}

}
