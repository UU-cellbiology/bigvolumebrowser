package bvb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import ij.Prefs;
import ij.io.OpenDialog;

public class PanelLoadSources extends JPanel
{
	
	JButton butBioFormats;
	
	JButton butBDVXML;
	
	JButton butFIJI;
	
	final BigVolumeBrowser bvb;
	
	public PanelLoadSources(final BigVolumeBrowser bvb_)
	{
		super(new GridBagLayout());	
		
		bvb = bvb_;
		this.setBorder(new PanelTitle(" Add data "));
	    GridBagConstraints gbc = new GridBagConstraints();
	    
		URL icon_path = this.getClass().getResource("/icons/bioformats.png");
	    ImageIcon tabIcon = new ImageIcon(icon_path);
	    butBioFormats = new JButton(tabIcon);
	    butBioFormats.setToolTipText("Load TIF/BioFormats");

		icon_path = this.getClass().getResource("/icons/fiji-logo.png");
	    tabIcon = new ImageIcon(icon_path);
	    butFIJI = new JButton(tabIcon);
	    butFIJI.setToolTipText("Load Current Image");
	    
		icon_path = this.getClass().getResource("/icons/bdv-logo.png");
	    tabIcon = new ImageIcon(icon_path);
	    butBDVXML = new JButton(tabIcon);
	    butBDVXML.setToolTipText("Load BDV XML/HDF5");
	    butBDVXML.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e )
			{
				loadBDVXML();				
			}
	    	
	    } );
	    
	    gbc.insets = new Insets(4,3,4,3);
	    //View
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    //gbc.weightx = 1.0;
	   // gbc.gridwidth = 1;
	    //gbc.anchor = GridBagConstraints.NORTHWEST;
	    //gbc.fill = GridBagConstraints.HORIZONTAL;
	    this.add( butBioFormats,gbc);
	    gbc.gridx++;
	    this.add( butFIJI,gbc);
	    gbc.gridx++;

	//    gbc.anchor = GridBagConstraints.CENTER;
	    //this.add( new JLabel("Add TIF/BioFormats"),gbc);
	    //gbc.gridx = 0;
	    //gbc.gridy++;
	    this.add( butBDVXML,gbc);
	   // gbc.gridx++;
	    //this.add( new JLabel("Add BDV XML/HDF5"),gbc);
	}
	
	void loadBDVXML()
	{
		OpenDialog openDial = new OpenDialog("Load BDV XML/HDF5", BVBSettings.lastDir, "*.xml");
        String path = openDial.getDirectory();
        if (path==null)
        	return;
        
        BVBSettings.lastDir = path;
        Prefs.set( "BVB.lastDir",  BVBSettings.lastDir );
        String filename = path+openDial.getFileName();
        
        bvb.loadBDVHDF5( filename );
	}
}
