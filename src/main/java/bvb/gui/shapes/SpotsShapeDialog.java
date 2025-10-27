package bvb.gui.shapes;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import ij.IJ;
import ij.Prefs;
import ij.io.SaveDialog;

public class SpotsShapeDialog
{

	public float fSpotSize = 10.0f;
	public Color spotColor = Color.WHITE;
	public int nShape = VisSpots.SHAPE_ROUND;
	public int nFill = VisSpots.RENDER_FILLED;
	public boolean bSpotDataCleanUp;
	public double dSpotsPercMin = 1.0;
	public double dSpotsPercMax = 99.0;
	public boolean bExportCleanData = false;
	public File fileSpots = null;
	public String sExportFilename;
	public boolean [] bCleanupCols = new boolean[5];
	public ColorUserSettings selectColors = new ColorUserSettings();
	
	public boolean showSelectionDialog(boolean bHasSize, boolean bHasProperty)
	{
		JPanel pSpotsParams = new JPanel(new GridBagLayout());
		
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		DecimalFormat df3 = new DecimalFormat ("#.##", symbols);
		String[] sShape = { "Round", "Square"};
		JComboBox<String> cbShape = new JComboBox<>(sShape);

		String[] sFill = new String[3];

		sFill[0] = "Filled";
		sFill[1] = "Outline";
		sFill[2] = "Gauss";			
		
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
		
		JCheckBox cbDataCleanUp = new JCheckBox();
		cbDataCleanUp.setSelected( Prefs.get( "BVB.bSpotDataCleanUp", false ) );
		
		//assemble everything
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;	
		GBCHelper.alighLoose(gbc);
		pSpotsParams.add(new JLabel("Spots color: "), gbc);
		gbc.gridx++;
		pSpotsParams.add(butSpotsColor, gbc);
		
		if(!bHasSize)
		{
			gbc.gridx = 0;
			gbc.gridy++;
			pSpotsParams.add(new JLabel("Spots size: "), gbc);
			gbc.gridx++;
			pSpotsParams.add(nfSpotSize, gbc);			
		}
		
		gbc.gridx = 0;
		gbc.gridy++;
		pSpotsParams.add(new JLabel("Spots shape: "), gbc);
		gbc.gridx++;
		pSpotsParams.add(cbShape, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		pSpotsParams.add(new JLabel("Spots render: "), gbc);
		gbc.gridx++;
		pSpotsParams.add(cbFill, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		pSpotsParams.add(new JLabel("Data cleanup: "), gbc);
		gbc.gridx++;
		pSpotsParams.add(cbDataCleanUp, gbc);

		
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
			if(!bHasSize)
			{			
				fSpotSize = Float.parseFloat( nfSpotSize.getText() );
				Prefs.set("BVB.spotSize", fSpotSize);
			}
			bSpotDataCleanUp = cbDataCleanUp.isSelected();
			Prefs.set("BVB.bSpotDataCleanUp", bSpotDataCleanUp);
			
			if(bSpotDataCleanUp)
			{
				//show percentile dialog
				return showDataCleanUpDialog(bHasSize, bHasProperty);
			}
			
			
			return true;
		}

		return false;

	}
	
	boolean showDataCleanUpDialog(boolean bHasSize, boolean bHasProperty)
	{
		JPanel pCleanup = new JPanel(new GridBagLayout());
		
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		DecimalFormat df3 = new DecimalFormat ("#.##", symbols);
		
		
		NumberField nfPercMin = new NumberField(5);
		nfPercMin.setText(df3.format( Prefs.get( "BVB.dSpotsPercMin", 1.0 ) ));
		NumberField nfPercMax = new NumberField(5);
		nfPercMax.setText(df3.format( Prefs.get( "BVB.dSpotsPercMax", 99.0 ) ));
		
		JCheckBox cbExportCleanData = new JCheckBox();
		cbExportCleanData.setSelected( Prefs.get( "BVB.bExportCleanData", true ) );
		
		JPanel pCheckBoxes = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		String [] sAxes = new String []{"X", "Y", "Z"};
		final JCheckBox [] cbAxes = new JCheckBox[3];
		for(int d = 0; d < 3; d++)
		{
			cbAxes[d] = new JCheckBox(sAxes[d]);
			cbAxes[d].setSelected( Prefs.get( "BigTrace.SpotCleanupAxis"+sAxes[d],true) );
		}
		final JCheckBox cbSize = new JCheckBox("Size");
		final JCheckBox cbProperty = new JCheckBox("Property");
		
		cbSize.setSelected( Prefs.get( "BigTrace.SpotCleanupSize",true) );
		cbProperty.setSelected( Prefs.get( "BigTrace.SpotCleanupProperty",true) );
		
		
		gbc.gridx = 0;
		gbc.gridy = 0;	
		for(int d = 0; d < 3; d++)
		{
			pCheckBoxes.add( cbAxes[d], gbc );
			gbc.gridx++;
		}
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		if(bHasSize)
		{
			pCheckBoxes.add( cbSize, gbc );
			
		}
		if(bHasProperty)
		{
			gbc.gridx = 0;
			gbc.gridy++;
			pCheckBoxes.add( cbProperty, gbc );
		}
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;	
		GBCHelper.alighLoose(gbc);
		pCleanup.add(new JLabel("Percentile % min: "), gbc);
		gbc.gridx++;
		pCleanup.add(nfPercMin, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		pCleanup.add(new JLabel("Percentile % max: "), gbc);
		gbc.gridx++;
		pCleanup.add(nfPercMax, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		pCleanup.add(new JLabel("Filter: "), gbc);
		gbc.gridx++;
		pCleanup.add(new JLabel(), gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		pCleanup.add( pCheckBoxes, gbc );
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		pCleanup.add(new JLabel("Save cleaned up data: "), gbc);
		gbc.gridx++;
		pCleanup.add(cbExportCleanData, gbc);
		int reply = JOptionPane.showConfirmDialog(null, pCleanup, "Filter data outliers", 
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (reply == JOptionPane.OK_OPTION) 
		{
			dSpotsPercMin = Double.parseDouble( nfPercMin.getText() );
			dSpotsPercMin = Math.max( dSpotsPercMin, 0.0 );
			Prefs.set("BVB.dSpotsPercMin", dSpotsPercMin);
			dSpotsPercMax = Double.parseDouble( nfPercMax.getText() );
			dSpotsPercMax = Math.min( dSpotsPercMax, 100.0 );
			Prefs.set("BVB.dSpotsPercMax", dSpotsPercMax);
			bExportCleanData = cbExportCleanData.isSelected();
			Prefs.set("BVB.bExportCleanData", bExportCleanData);
			for(int d = 0; d < 3; d++)
			{
				bCleanupCols[d] = cbAxes[d].isSelected();
				Prefs.set( "BigTrace.SpotCleanupAxis"+sAxes[d], bCleanupCols[d]);
			}

			if(bHasSize)
			{
				bCleanupCols[3] = cbSize.isSelected();
				Prefs.set( "BigTrace.SpotCleanupSize",bCleanupCols[3]);
			}
			if(bHasProperty)
			{
				bCleanupCols[4] = cbProperty.isSelected();
				Prefs.set( "BigTrace.SpotCleanupProperty",bCleanupCols[4]);
			}			
			if(dSpotsPercMin > dSpotsPercMax)
			{
				final double temp = dSpotsPercMin;
				dSpotsPercMin = dSpotsPercMax;
				dSpotsPercMax = temp;
			}
			
		}
		else
		{
			return false;
		}
		
		if(bExportCleanData)
		{
			sExportFilename = fileSpots.getAbsolutePath()+"_cleanedup";
			SaveDialog sd = new SaveDialog("Save cleaned up data ", sExportFilename, ".csv");
			sExportFilename = sd.getDirectory();
			if (sExportFilename == null)
			{
				bExportCleanData = false;
				IJ.log( "Exporting cleaned up data is aborted." );
				return true;
			}
			sExportFilename = sExportFilename+ sd.getFileName();
			
			
		}
		return true;
		
	}
	
}
