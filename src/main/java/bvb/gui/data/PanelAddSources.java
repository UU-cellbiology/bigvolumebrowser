package bvb.gui.data;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.PanelTitle;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;

public class PanelAddSources extends JPanel
{
	
	JButton butBioFormats;
	
	JButton butBDVXML;
	
	JButton butFIJI;
	
	final BigVolumeBrowser bvb;
	
	public PanelAddSources(final BigVolumeBrowser bvb_)
	{
		super(new GridBagLayout());	
		
		bvb = bvb_;
		this.setBorder(new PanelTitle(" Add data "));
	    GridBagConstraints gbc = new GridBagConstraints();
	    
		URL icon_path = this.getClass().getResource("/icons/bioformats.png");
	    ImageIcon tabIcon = new ImageIcon(icon_path);
	    butBioFormats = new JButton(tabIcon);
	    butBioFormats.setToolTipText("Load TIF/BioFormats");
	    
	    butBioFormats.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e )
			{
				loadBioFormatsDialog();				
			}
	    	
	    } );

		icon_path = this.getClass().getResource("/icons/fiji-logo.png");
	    tabIcon = new ImageIcon(icon_path);
	    butFIJI = new JButton(tabIcon);
	    butFIJI.setToolTipText("Load Current Image");
	    
	    butFIJI.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e )
			{
				loadImagePlus();				
			}
	    	
	    } );
	    
		icon_path = this.getClass().getResource("/icons/bdv-logo.png");
	    tabIcon = new ImageIcon(icon_path);
	    butBDVXML = new JButton(tabIcon);
	    butBDVXML.setToolTipText("Load BDV XML/HDF5");
	    butBDVXML.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e )
			{
				loadBDVXMLDialog();				
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
	
	public void loadBDVXMLDialog()
	{		
        JFileChooser chooser = new JFileChooser(BVBSettings.lastDir);
        chooser.setDialogTitle( "Open BigDataViewer XML/HDF5" );
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
	public void loadBioFormatsDialog()
	{		
        JFileChooser chooser = new JFileChooser(BVBSettings.lastDir);
        chooser.setDialogTitle( "Open TIF or BioFormats readable files" );
        
        int returnVal = chooser.showOpenDialog(null);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
            BVBSettings.lastDir = chooser.getSelectedFile().getParent();
            Prefs.set( "BVB.lastDir",  BVBSettings.lastDir );
            bvb.loadBioFormats(chooser.getSelectedFile().getPath() );
        }
	}
	
	public void loadImagePlus()
	{	
		ImagePlus imp = null;
		try
		{
			imp = IJ.getImage();
		}
		catch(RuntimeException exc)
		{
			return;
		}
		
		if (imp.getType() != ImagePlus.GRAY8 && imp.getType() != ImagePlus.GRAY16 ) 
		{
		    IJ.error("Only 8- or 16-bit grayscale images are currently supported.");
		    return;
		}
		bvb.addImagePlus( imp );
	}
}
