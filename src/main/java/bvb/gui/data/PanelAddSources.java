/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
