package bvb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

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

	    gbc.gridx = 0;
	    gbc.gridy = 0;

	    this.add( butBioFormats,gbc);

	    gbc.gridx++;
	    this.add( butFIJI,gbc);

	    gbc.gridx++;
	    this.add( butBDVXML,gbc);

	}
	
	void loadBDVXML()
	{
		
        JFileChooser chooser = new JFileChooser(BVBSettings.lastDir);
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "BigDataViewer XML/HDF5", "xml");
        chooser.setFileFilter(filter);
        
        int returnVal = chooser.showOpenDialog(null);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
            BVBSettings.lastDir = chooser.getSelectedFile().getParent();
            Prefs.set( "BVB.lastDir",  BVBSettings.lastDir );
            bvb.loadBDVHDF5( chooser.getSelectedFile().getPath() );
        }
	}
}
