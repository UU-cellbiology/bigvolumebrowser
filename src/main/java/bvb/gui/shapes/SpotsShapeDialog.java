package bvb.gui.shapes;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import bdv.tools.brightness.ColorIcon;
import bvb.gui.ColorUserSettings;
import bvb.gui.GBCHelper;
import bvb.gui.NumberField;
import bvb.scene.VisSpots;
import ij.Prefs;

public class SpotsShapeDialog
{

	public float fSpotSize = 10.0f;
	public Color spotColor = Color.WHITE;
	public int nShape = VisSpots.SHAPE_ROUND;
	public int nFill = VisSpots.RENDER_FILLED;
	
	public ColorUserSettings selectColors = new ColorUserSettings();
	
	public boolean showSelectionDialog(boolean bAskForSize)
	{
		JPanel pSpotsParams = new JPanel(new GridBagLayout());
		
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		DecimalFormat df3 = new DecimalFormat ("#.##", symbols);
		String[] sShape = { "Round", "Square"};
		JComboBox<String> cbShape = new JComboBox<>(sShape);

		String[] sFill = null;
		if(bAskForSize)
		{
			sFill = new String[3];					
		}
		else
		{
			sFill = new String[4];
		}
		sFill[0] = "Filled";
		sFill[1] = "Outline";
		if(bAskForSize)
		{
			sFill[2] = "Gaussian";			
		}
		else
		{
			sFill[2] = "Gauss Const";			
			sFill[3] = "Gauss Norm";			
		}
		
		JComboBox<String> cbFill = new JComboBox<>(sFill);
		
		JButton butSpotsColor = new JButton( new ColorIcon( Color.WHITE ) );	
		butSpotsColor.addActionListener( e -> {
			Color newColor = JColorChooser.showDialog(pSpotsParams, "Choose spots color", Color.WHITE);
			if (newColor != null)
			{
				selectColors.setColor(newColor, 0);

				butSpotsColor.setIcon(new ColorIcon(newColor));
			}
			
		});
		
		NumberField nfSpotSize = new NumberField(5);
		nfSpotSize.setText(df3.format( Prefs.get( "BVB.spotSize", 10.0 ) ));
		
		//assemble everything
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx=0;
		gbc.gridy=0;	
		GBCHelper.alighLoose(gbc);
		pSpotsParams.add(new JLabel("Spots color: "), gbc);
		gbc.gridx++;
		pSpotsParams.add(butSpotsColor, gbc);
		
		if(bAskForSize)
		{
			gbc.gridx=0;
			gbc.gridy++;
			pSpotsParams.add(new JLabel("Spots size: "), gbc);
			gbc.gridx++;
			pSpotsParams.add(nfSpotSize, gbc);			
		}
		
		gbc.gridx=0;
		gbc.gridy++;
		pSpotsParams.add(new JLabel("Spots shape: "), gbc);
		gbc.gridx++;
		pSpotsParams.add(cbShape, gbc);

		gbc.gridx=0;
		gbc.gridy++;
		pSpotsParams.add(new JLabel("Spots filling: "), gbc);
		gbc.gridx++;
		pSpotsParams.add(cbFill, gbc);

		
		int reply = JOptionPane.showConfirmDialog(null, pSpotsParams, "Spots render", 
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (reply == JOptionPane.OK_OPTION) 
		{
			Color tempC;
			
			tempC = selectColors.getColor(0);
			if(tempC != null)
			{
				spotColor = new Color(tempC.getRed(),tempC.getGreen(),tempC.getBlue(),tempC.getAlpha());						
			}
			nShape = cbShape.getSelectedIndex();
			nFill = cbFill.getSelectedIndex();
			if(bAskForSize)
			{
				
				fSpotSize = Float.parseFloat( nfSpotSize.getText() );
				Prefs.set("BVB.spotSize", fSpotSize);
			}
			
			return true;
		}

		return false;

	}
	
}
