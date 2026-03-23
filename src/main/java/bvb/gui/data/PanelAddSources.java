/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.ValuePair;

import bvb.core.BVBSettings;
import bvb.core.BVVSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.ColorTextOverlayAnimator;
import bvb.gui.ColorTextOverlayAnimator.TextPosition;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;

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
		//this.setBorder(new PanelTitle(" Add data "));
	    GridBagConstraints gbc = new GridBagConstraints();
	    
		URL icon_path = this.getClass().getResource(BVBSettings.sIconPath + "bioformats.png");
	    ImageIcon tabIcon = new ImageIcon(icon_path);
	    butBioFormats = new JButton(tabIcon);
	    butBioFormats.setToolTipText("Load TIF/BioFormats");
	    butBioFormats.addActionListener( (e) -> loadBioFormatsDialog());				

		icon_path = this.getClass().getResource(BVBSettings.sIconPath + "fiji-logo.png");
	    tabIcon = new ImageIcon(icon_path);
	    butFIJI = new JButton(tabIcon);
	    butFIJI.setToolTipText("Load Current Image");
	    
	    butFIJI.addActionListener((e) -> loadImagePlus());	
	    
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + "bdv-logo.png");
	    tabIcon = new ImageIcon(icon_path);
	    butBDVXML = new JButton(tabIcon);
	    butBDVXML.setToolTipText("Load BDV XML/HDF5");
	    butBDVXML.addActionListener( (e) ->	loadBDVXMLDialog());	
	    
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
            String sFilename = chooser.getSelectedFile().getPath();
			final File f = new File(sFilename);
			bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "Loading " + f.getName() + ",\nplease wait...", 3000, TextPosition.CENTER, BVBSettings.canvasOverlayColor )  );
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(() -> {
				final ValuePair< AbstractSpimData< ? >, BVBSpimDataInfo > spimDataInfo = 
						bvb.spimDataWrapper.createSpimDataBDVorBF( sFilename, 0 );
			    SwingUtilities.invokeLater(() -> {
			    	bvb.addSpimData( spimDataInfo.getA(), spimDataInfo.getB() );
			    });
			});
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
            String sPathFilenameIni = chooser.getSelectedFile().getPath();
			final File f = new File(sPathFilenameIni);
			String sFilename = f.getName();
			final int [] nMode = new int [1];
			nMode[0] = 1;
			if(sFilename.endsWith( "xml" ) || sFilename.endsWith( "h5" ))
			{
				nMode[0] = showXMLsuspectedMessage();
				if(nMode[0] == 0 && sFilename.endsWith( "h5" ))
				{
					sPathFilenameIni = sPathFilenameIni.substring( 0, sPathFilenameIni.length() - 2 );
					sPathFilenameIni = sPathFilenameIni + "xml";
					String sFilenameh5 = sFilename;
					sFilename = sFilename.substring( 0, sFilename.length() - 2 );
					sFilename = sFilename + "xml";
					IJ.log( "Opening " + sFilename + " instead of " + sFilenameh5 + ".");
				}
					
			}
			String sPathFilename = sPathFilenameIni;
			bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "Loading " + sFilename + ",\nplease wait...", 3000, TextPosition.CENTER, BVBSettings.canvasOverlayColor )  );
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(() -> {
				final ValuePair< AbstractSpimData< ? >, BVBSpimDataInfo > spimDataInfo = 
						bvb.spimDataWrapper.createSpimDataBDVorBF( sPathFilename, nMode[0] );
			    SwingUtilities.invokeLater(() -> 
			    {
			    	if(BVBSettings.bShowFileSizeDialog && nMode[0] == 1 )
			    		{ fileSizeWarningDialog(spimDataInfo.getA()); }
					//System.out.println(estimateSizeSingleTimeFrameInMB(spimDataInfo.getA()));
			    	bvb.addSpimData( spimDataInfo.getA(), spimDataInfo.getB() );
			    });
			});
        }
	}
	
	public void loadImagePlus()
	{	
		final ImagePlus imp;
		try
		{
			imp = IJ.getImage();
		}
		catch(RuntimeException exc)
		{
			return;
		}
		
		if (imp.getType() != ImagePlus.GRAY8 && imp.getType() != ImagePlus.GRAY16 && imp.getType() != ImagePlus.GRAY32 ) 
		{
		    IJ.error("Only 8-, 16- or 32-bit grayscale images are currently supported.");
		    return;
		}
		bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "Loading " + imp.getTitle() + ",\nplease wait...", 3000, TextPosition.CENTER, BVBSettings.canvasOverlayColor )  );

		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			final ValuePair< AbstractSpimData< ? >, BVBSpimDataInfo > spimDataInfo = 
					bvb.spimDataWrapper.createSpimDataImagePlus(imp);
		    SwingUtilities.invokeLater(() -> 
		    {
		    	if(BVBSettings.bShowFileSizeDialog)
		    		fileSizeWarningDialog(spimDataInfo.getA());
		    	//System.out.println(estimateSizeSingleTimeFrameInMB(spimDataInfo.getA()));
		    	bvb.addSpimData( spimDataInfo.getA(), spimDataInfo.getB() );
		    });
		});
	}
	
	int showXMLsuspectedMessage()
	{
		
		if (JOptionPane.showConfirmDialog(null, "Looks like you are opening XML file with BioFormats.\n"
				+ "If it is BDV XML, only one resolution level will be available.\nDo you want to open it in full multi-res mode instead?", "Loading sources",
		        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
		    return 0;
		} 
		return 1;
		
	}
	
	void fileSizeWarningDialog(final AbstractSpimData< ? > spimData)
	{
		String sReason = "";
		final int nDatasetSize = estimateSizeSingleTimeFrameInMB(spimData);
		final String sDatasetSizeGB = Integer.toString((int)Math.round( nDatasetSize/1000. ));

		if(nDatasetSize >= 4000)
		{
			sReason = " is pretty large ("+ sDatasetSizeGB +" GB).</html>";
		}
		if(nDatasetSize >= BVVSettings.maxCacheSizeInMB)
		{
			sReason = " size ("+Integer.toString( nDatasetSize )+") MB<br />is larger than available GPU memory ("+Integer.toString( BVVSettings.maxCacheSizeInMB )+" MB).</html>";
		}
		if(!sReason.equals( "" ))
		{
			JPanel pWarning = new JPanel(new GridBagLayout());
		
			String message  = "<html>Looks like the loaded dataset"+ sReason;
					
			String[] options = {"OK"};
			
			JCheckBox cbShowAgain = new JCheckBox("Do not show this message again");
			cbShowAgain.setSelected( false );
			
			JLabel hyperlink = new JLabel("converting it to multi-res BDV/XML");
			hyperlink.setForeground(Color.BLUE.darker());
			hyperlink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			hyperlink.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) 
				{
					try
					{
						Desktop.getDesktop().browse(new URI("https://github.com/UU-cellbiology/bigvolumebrowser/wiki/Load-volumetric-data#loading-large-files"));
					}
					catch ( IOException | URISyntaxException exc )
					{
						exc.printStackTrace();
					}

				}
			});

			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(8,0,8,0);
			gbc.gridx = 0;
			gbc.gridy = 0;
			pWarning.add( new JLabel(message), gbc );
			gbc.insets = new Insets(0,0,0,0);
			gbc.gridy++;
			pWarning.add( new JLabel("For a smooth browsing experience, we recommend"), gbc );
			gbc.gridy++;
			pWarning.add( hyperlink, gbc );
			gbc.gridy++;
			gbc.insets = new Insets(8,0,8,0);
			gbc.anchor = GridBagConstraints.EAST;
			pWarning.add( cbShowAgain, gbc );
			
			JOptionPane.showOptionDialog(null, pWarning, "Loading large file warning", 
					JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			BVBSettings.bShowFileSizeDialog = !cbShowAgain.isSelected();
			Prefs.get( "BVB.bShowFileSizeDialog", BVBSettings.bShowFileSizeDialog );
		}
	}
	
	int estimateSizeSingleTimeFrameInMB(final AbstractSpimData< ? > spimData)
	{
		final BasicImgLoader imgLoader = spimData.getSequenceDescription().getImgLoader();
		long nNumberChannels = spimData.getSequenceDescription().getViewSetups().size();
		RandomAccessibleInterval< ? > rai = imgLoader.getSetupImgLoader( 0 ).getImage( 0 );
		final long [] dims = rai.dimensionsAsLongArray();
		Object type = rai.getType();
		int nBytesCoeff = 0;
		if(type instanceof UnsignedByteType)
			nBytesCoeff = 1;
		if(type instanceof UnsignedShortType)
			nBytesCoeff = 2;
		if(type instanceof Float)
			nBytesCoeff = 4;
		long lSize = dims[0] * dims[1] *dims[2] * nNumberChannels * nBytesCoeff;
		return (int)Math.round( lSize /(1024.0 * 1024.0) );
	}
}
