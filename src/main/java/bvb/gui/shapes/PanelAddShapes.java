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
package bvb.gui.shapes;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker.StateValue;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.imglib2.mesh.Mesh;

import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.ColorTextOverlayAnimator;
import bvb.gui.ColorTextOverlayAnimator.TextPosition;
import bvb.gui.GBCHelper;
import bvb.gui.NumberField;
import bvb.io.shapes.GltfImporter;
import bvb.io.shapes.LASImport;
import bvb.io.shapes.SpotsParser;
import bvb.io.shapes.WRLParser;
import bvb.shapes.BasicShape;
import bvb.shapes.MeshShape;
import bvb.shapes.MultiMeshShape;
import bvb.shapes.MultiSpots;
import bvb.shapes.Spots;
import bvb.utils.Misc;
import ij.IJ;
import ij.Prefs;

public class PanelAddShapes extends JPanel
{
	final JButton butSpots;
	
	final JButton butMesh;
	
	final BigVolumeBrowser bvb;
	
	public PanelAddShapes(final BigVolumeBrowser bvb_)
	{
		super(new GridBagLayout());	
		
		bvb = bvb_;

	    GridBagConstraints gbc = new GridBagConstraints();
		URL icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "spots.png");
	    ImageIcon tabIcon = new ImageIcon(icon_path);
	    butSpots = new JButton(tabIcon);
	    butSpots.setToolTipText("Import spots/point cloud\n from text file");
	    butSpots.addActionListener( (e)->loadSpotsDialog());				
	    
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "mesh.png");
	    tabIcon = new ImageIcon(icon_path);
	    butMesh = new JButton(tabIcon);
	    butMesh.setToolTipText("Import meshes");
	    butMesh.addActionListener( (e)->loadMeshDialog());				
	    
	    gbc.insets = new Insets(4,3,4,3);

	    gbc.gridx = 0;
	    gbc.gridy = 0;

	    this.add( butSpots,gbc);

	    gbc.gridx++;
	    this.add( butMesh,gbc);

	}
	
	void loadSpotsDialog()
	{
		SpotsLoadDialog dialSpots = new SpotsLoadDialog(bvb);
		dialSpots.show();
		
		//get the values
		if(dialSpots.bAllSuccess)
		{
			if(!dialSpots.bLAZfile)
			{
				parseSpots(dialSpots);
			}
			else
			{
				importLAS(dialSpots.fileSpots);
			}
		}
	}
	void importLAS (final File filein)
	{
		//show spots size dialog
		JPanel pSpotsParams = new JPanel(new GridBagLayout());
		
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		DecimalFormat df3 = new DecimalFormat ("#.##", symbols);
		
		NumberField nfSpotSize = new NumberField(5);
		nfSpotSize.setText(df3.format( Prefs.get( "BVB.spotSizeLAS", 30.0 ) ));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;	
		GBCHelper.alighLoose(gbc);
		pSpotsParams.add(new JLabel("Spots size: "), gbc);
		gbc.gridx++;
		pSpotsParams.add(nfSpotSize, gbc);
		
		int reply = JOptionPane.showConfirmDialog(null, pSpotsParams, "Import spots from LAS/LAZ", 
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (reply == JOptionPane.OK_OPTION) 
		{
			float fSpotSize = Float.parseFloat( nfSpotSize.getText() );
			Prefs.set("BVB.spotSizeLAS", fSpotSize);
			LASImport importLAS = new LASImport();
			importLAS.fPointSize = fSpotSize;
			importLAS.filein = filein;

			importLAS.addPropertyChangeListener( (e)->
			{			
				if(importLAS.getState() == StateValue.DONE)
				{
					if(!importLAS.bSpotsRead )
					{
						importLAS.bSpotsRead = true; 
						final Spots importedSpots = importLAS.spotsLAS;
						importedSpots.setName( Misc.getSourceStyleName( filein ) );
						bvb.addShape( importedSpots );
					}
				}
			});
			
			importLAS.execute();
			
		}
	}
	
	void parseSpots(final SpotsLoadDialog dialSpots)
	{
		SpotsParser sptParser = new SpotsParser();
		sptParser.fileSpots = dialSpots.fileSpots;
		sptParser.bHeader = dialSpots.cbHasHeader.isSelected();
		Prefs.set( "BVB.bSpotsImportHasHeader", sptParser.bHeader);
		String[] sSeparators = { ",", ";", " ", "\t" };
		sptParser.sSeparator = sSeparators[dialSpots.cbSeparator.getSelectedIndex()];
		Prefs.set( "BVB.nSpotsSeparator", dialSpots.cbSeparator.getSelectedIndex());
		//column indices
		final int [] nColInd = new int[8];
		//whether size was provided
		int nSizeCols = 0;
		sptParser.parseTime = false;
		sptParser.parseProperty = false;

		for(int d = 0; d < 8; d++)
		{
			if(dialSpots.cbColumnsAssign.get( d ).getSelectedIndex() > 0)
			{
				nColInd[d] = dialSpots.cbColumnsAssign.get( d ).getSelectedIndex()-1;
				if(d > 3 && d < 7)
				{
					nSizeCols++;
				}
				if(d == 3)
				{
					sptParser.parseTime = true;
				}
				if(d == 7)
				{
					sptParser.parseProperty = true;
				}
				
			}
			else
			{
				nColInd[d] = -1;
			}
		}
		
		sptParser.nColIndices = nColInd;
		
		//scale factor, convert to micrometers
		switch (dialSpots.cbUnits.getSelectedIndex())
		{
			//milli
			case 0:
				sptParser.fScale = 1000.0f;
				break;
			//nano
			case 2:
				sptParser.fScale = 0.001f;
				break;
			//micro
			default:
				sptParser.fScale = 1.0f;
		}
		
		Prefs.set( "BVB.nSpotsUnits", dialSpots.cbUnits.getSelectedIndex());
		
		SpotsShapeDialog sptShape = new SpotsShapeDialog();
		sptShape.fileSpots = dialSpots.fileSpots;
		
		//parse sizes
		if(nSizeCols > 0)
		{
			sptParser.parseSize = true;
			switch(dialSpots.cbSize.getSelectedIndex())
			{
				//radius
				case 1:
					sptParser.fSizeScale = 2.0f;
					break;
				//SD
				case 2:
					sptParser.fSizeScale = 6.0f;
					break;
				
			}	
			Prefs.set( "BVB.nSpotsSize", dialSpots.cbSize.getSelectedIndex());

		}
		if(!sptShape.showSelectionDialog( sptParser.parseSize, sptParser.parseProperty ))
		{
			return;
		}
		
		if(sptShape.bSpotDataCleanUp)
		{
			sptParser.bDataCleanup = true;
			sptParser.dPercMin = sptShape.dSpotsPercMin;
			sptParser.dPercMax = sptShape.dSpotsPercMax;
			sptParser.bCleanupCols = sptShape.bCleanupCols;
		}
		if(sptShape.bExportCleanData)
		{
			sptParser.bExportCleanData = true;
			sptParser.sExportFilename = sptShape.sExportFilename;
		}

	
		sptParser.addPropertyChangeListener( (e)->
		{
			if(sptParser.getState() == StateValue.DONE)
			{
				if(!sptParser.bSpotsAdded )
				{
					sptParser.bSpotsAdded = true; 
					IJ.showStatus( "Uploading " + Long.toString( sptParser.nTotSpots )+" to GPU...");
					BasicShape spots = null;
					if(!sptParser.parseTime)
					{
						Spots importedSpots = new Spots(sptShape.fSpotSize, sptShape.spotColor, sptShape.nShape, sptShape.nFill);
						importedSpots.setPoints( sptParser.vertices, sptParser.sizes, sptParser.property);
						spots = importedSpots;
					}
					else
					{
						MultiSpots importedSpots = new MultiSpots();
						int nMaxTP = importedSpots.initFromSpotParser( sptParser, sptShape );
						spots = importedSpots;
						
						//update time points
						if(nMaxTP > 0)
						{
							bvb.bvvViewer.setNumTimepoints( Math.max( nMaxTP,  bvb.bvvViewer.state().getNumTimepoints()));			
						}
					}	
					spots.setName( Misc.getSourceStyleName( sptParser.fileSpots ) );
					
					bvb.addShape( spots );						
					
					IJ.showStatus( "Uploading " + Long.toString( sptParser.nTotSpots ) + " to GPU...done.");
				}
			}
		
		}
		);
		bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "Loading spots, please wait...", 5000, TextPosition.CENTER, BVBSettings.canvasOverlayColor )  );
		
		sptParser.execute();
	}
	
	void loadMeshDialog()
	{
        JFileChooser chooser = new JFileChooser(BVBSettings.lastDir);
        chooser.setDialogTitle( "Open Mesh Data" );
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Mesh files", "stl", "ply", "wrl", "glb", "gltf");
        chooser.setFileFilter(filter);
        
        int returnVal = chooser.showOpenDialog(null);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
            BVBSettings.lastDir = chooser.getSelectedFile().getParent();
            Prefs.set( "BVB.lastDir",  BVBSettings.lastDir );
            String sFilename = chooser.getSelectedFile().getPath();
            String extension = "";
            int i = sFilename.lastIndexOf('.');
            if (i > 0) {
                extension = sFilename.substring(i+1);
            }
            switch (extension)
            {
            //if it is stl or ply
            case "stl":
            case "ply":
                final MeshShape loadedMesh = new MeshShape(sFilename);
                //weird way to check if loading went well, but let's keep it for now. 
                if(loadedMesh.boundingBoxNotTransformed() != null)
                {
                	bvb.addShape( loadedMesh );
                }
            	break;
            //Imaris wrl files
            case "wrl":
        		new Thread(() -> {
            	loadWRLfile( sFilename );
        		}).start();
            	break;
            //Gltf files
            case "gltf":
            case "glb":
            	new Thread(() -> {
            	loadGLTfile( sFilename );
            	}).start();
            	break;

            default:
            	IJ.log( "Unsupported mesh file format for ."+ extension + " files, aborted.");
            
            }

        }
	}
	
	void loadGLTfile(String sFilename)
	{
		bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "Loading meshes from glTF, please wait...", 3000, TextPosition.CENTER, BVBSettings.canvasOverlayColor )  );
		GltfImporter gltfImporter = new GltfImporter();
		final List< BasicShape > meshes = gltfImporter.loadGLTF( sFilename );
		
		if (meshes != null)
		{
			if(meshes.size() > 0)
			{
				if(meshes.size() == 1)
				{
					bvb.addShapes( meshes, Misc.getSourceStyleName( sFilename ) );
				}
				else
				{
					JPanel pGLTSettings = new JPanel(new GridBagLayout());
					
					GridBagConstraints gbc = new GridBagConstraints();
					String[] sOptions = { "Group meshes into single object", "Each mesh separately" };
					JComboBox<String> cbMultiMesh = new JComboBox<>(sOptions);
					cbMultiMesh.setSelectedIndex(Prefs.get( "BVB.bGroupMesGLT", true) ? 0 : 1);
					gbc.gridx = 0;
					gbc.gridy = 0;	
					GBCHelper.alighLoose(gbc);
					pGLTSettings .add(new JLabel("Multiple meshes:"), gbc);
					gbc.gridx++;
					pGLTSettings .add( cbMultiMesh, gbc );
					boolean bGroupMesh = true;
					
					int reply = JOptionPane.showConfirmDialog(null, pGLTSettings, "Loading glTG mesh file options", 
					        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
					if (reply == JOptionPane.OK_OPTION) 
					{
						bGroupMesh = cbMultiMesh.getSelectedIndex() == 0;
						Prefs.set( "BVB.BVB.bGroupMesGLT", bGroupMesh);
						
					}
					else
					{
						return;
					}
					if(bGroupMesh)
					{
						MultiMeshShape mmGLT = new MultiMeshShape();
						for(BasicShape sh : meshes)
						{
							mmGLT.addMeshShape( (MeshShape)sh );
						}
						mmGLT.setName( Misc.getSourceStyleName( sFilename )  );
						bvb.addShape(mmGLT);
					}
					else
					{
						bvb.addShapes( meshes, Misc.getSourceStyleName( sFilename ) );
					}
				}
			}
		}
	}
	
	void loadWRLfile(String sFilename)
	{
		//loading dialog
		
		JPanel pWRLSettings = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		String[] sOptions = { "Group meshes by color", "Each mesh separately" };
		JComboBox<String> cbMultiMesh = new JComboBox<>(sOptions);
		cbMultiMesh.setSelectedIndex(Prefs.get( "BVB.bGroupMeshColorWRL", true) ? 0 : 1);
		gbc.gridx = 0;
		gbc.gridy = 0;	
		GBCHelper.alighLoose(gbc);
		pWRLSettings.add(new JLabel("Multiple meshes:"), gbc);
		gbc.gridx++;
		pWRLSettings.add( cbMultiMesh, gbc );
		boolean bGroupMesh = true;
		
		int reply = JOptionPane.showConfirmDialog(null, pWRLSettings, "Loading WRL file options", 
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (reply == JOptionPane.OK_OPTION) 
		{
			bGroupMesh = cbMultiMesh.getSelectedIndex() == 0;
			Prefs.set( "BVB.BVB.bGroupMeshColorWRL", bGroupMesh);
			
		}
		else
		{
			return;
		}

		bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "Loading meshes, please wait...", 5000, TextPosition.CENTER, BVBSettings.canvasOverlayColor )  );
		
		final WRLParser loaderWRT = new WRLParser();
		//loaderWRT.nMaxMeshes = 4;
		//loaderWRT.nMaxTimePoints = 1;
		final ArrayList< Mesh > loadedMeshes = loaderWRT.readWRL(sFilename);
		int nMaxTP = -1;
		if(bGroupMesh)
		{
			IJ.showStatus( "Uploading " + Integer.toString( loadedMeshes.size() ) + " meshes." );
			if(loaderWRT.containsColorInfo())
			{
				Set<Color> uniqueColors = new HashSet<>(loaderWRT.meshColors);
				HashMap<Color,MultiMeshShape> meshGroups = new HashMap<>();
				for (final Color color:uniqueColors)
				{
					meshGroups.put( color,  new MultiMeshShape() );
				}
				for(int i = 0; i < loadedMeshes.size(); i++)
				{
					int nTP = -1;

					if(loaderWRT.isTimeData())
					{
						nTP = loaderWRT.timePoints.get( i );
						nMaxTP = Math.max( nMaxTP, nTP );
					}
					Color meshColor = loaderWRT.meshColors.get( i );
					meshGroups.get( meshColor ).addMesh( loadedMeshes.get( i ), null, nTP, meshColor );
				}
				int nC = 0;
				for (Entry< Color, MultiMeshShape > pair : meshGroups.entrySet()) 
				{
					pair.getValue().setName( "c" +Integer.toString( nC )+"_"+Misc.getSourceStyleName( sFilename )  ); 
					bvb.addShape( pair.getValue() ); 
					nC++;
				}
					
			}
			else
			{
				MultiMeshShape mmColor = new MultiMeshShape();
				Color meshColor = null;
				for(int i = 0; i < loadedMeshes.size(); i++)
				{
					int nTP = -1;

					if(loaderWRT.isTimeData())
					{
						nTP = loaderWRT.timePoints.get( i );
						nMaxTP = Math.max( nMaxTP, nTP );
					}
					mmColor.addMesh( loadedMeshes.get( i ), null, nTP, meshColor );					
				}
				mmColor.setName( Misc.getSourceStyleName( sFilename )  );
				bvb.addShape(mmColor);
			}
		}
		///load all meshes separately
		else
		{
			IJ.showStatus( "Uploading " + Integer.toString( loadedMeshes.size() ) + " meshes." );
			final ArrayList<BasicShape> finMeshesShapes = new ArrayList<>();
			for(int i = 0; i < loadedMeshes.size(); i++)
			{		
				final MeshShape meshBVB = new MeshShape(loadedMeshes.get( i ));
				int nTP = -1;
				if(loaderWRT.isTimeData())
				{
					nTP = loaderWRT.timePoints.get( i );
					meshBVB.setTimePoint( nTP );
					nMaxTP = Math.max( nMaxTP, nTP );
				}
				if(loaderWRT.containsColorInfo())
				{
					meshBVB.setColor( loaderWRT.meshColors.get( i ) );
				}	
				
				finMeshesShapes.add( meshBVB );
			}
			bvb.addShapes( finMeshesShapes, Misc.getSourceStyleName( sFilename ) );
		}
		//update time points
		if(nMaxTP > 0)
		{
			bvb.bvvViewer.setNumTimepoints( Math.max( nMaxTP,  bvb.bvvViewer.state().getNumTimepoints()));			
		}
	}
}
