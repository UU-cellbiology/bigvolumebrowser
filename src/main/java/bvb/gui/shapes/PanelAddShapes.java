package bvb.gui.shapes;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
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

import bdv.viewer.animate.TextOverlayAnimator;
import bdv.viewer.animate.TextOverlayAnimator.TextPosition;
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.GBCHelper;
import bvb.io.shapes.GltfImporter;
import bvb.io.shapes.SpotsParser;
import bvb.io.shapes.WRLParser;
import bvb.shapes.BasicShape;
import bvb.shapes.MeshColor;
import bvb.shapes.MultiMeshColor;
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
		URL icon_path = this.getClass().getResource("/icons/spots.png");
	    ImageIcon tabIcon = new ImageIcon(icon_path);
	    butSpots = new JButton(tabIcon);
	    butSpots.setToolTipText("Import spots from text file");
	    butSpots.addActionListener( (e)->loadSpotsDialog());				
	    
		icon_path = this.getClass().getResource("/icons/mesh.png");
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
			SpotsParser sptParser = new SpotsParser();
			sptParser.fileSpots = dialSpots.fileSpots;
			sptParser.bHeader = dialSpots.cbHasHeader.isSelected();
			sptParser.sSeparator = (String)dialSpots.cbSeparator.getSelectedItem();
			Prefs.set( "BVB.nSpotsSeparator", dialSpots.cbSeparator.getSelectedIndex());
			//column indices
			final int [] nColInd = new int[6];
			//whether size was provided
			int nSizeCols = 0;
			for(int d=0; d<6;d++)
			{
				if(dialSpots.cbColumnsAssign.get( d ).getSelectedIndex()>0)
				{
					nColInd[d] = dialSpots.cbColumnsAssign.get( d ).getSelectedIndex()-1;
					if(d>2)
					{
						nSizeCols++;
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
			
			boolean bAskForSize = true;
			
			//parse sizes
			if(nSizeCols>0)
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
				bAskForSize = false;

			}
			if(!sptShape.showSelectionDialog( bAskForSize ))
			{
				return;
			}
			if(sptShape.bSpotDataCleanUp)
			{
				sptParser.bDataCleanup = true;
				sptParser.dPercMin = sptShape.dSpotsPercMin;
				sptParser.dPercMax = sptShape.dSpotsPercMax;
			}

		
			sptParser.addPropertyChangeListener( (e)->
			{
				if(sptParser.getState() == StateValue.DONE)
				{
					IJ.showStatus( "Uploading " +Long.toString( sptParser.nTotSpots )+" to GPU...");
					final Spots importedSpots = new Spots(sptShape.fSpotSize, sptShape.spotColor, sptShape.nShape, sptShape.nFill);
					if(!sptParser.parseSize)
					{
						importedSpots.setPoints( sptParser.vertices );
					}
					else
					{
						importedSpots.setPoints( sptParser.vertices, sptParser.sizes);
					}
					importedSpots.setName( Misc.getSourceStyleName( sptParser.fileSpots ) );
					bvb.addShape( importedSpots );						
					
					IJ.showStatus( "Uploading " +Long.toString( sptParser.nTotSpots )+" to GPU...done.");
				}
			}
			);
			bvb.bvvViewer.addOverlayAnimator( new TextOverlayAnimator( "Loading spots, please wait...", 5000, TextPosition.CENTER )  );
			
			sptParser.execute();
		}
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
                final MeshColor loadedMesh = new MeshColor(sFilename);
                bvb.addShape( loadedMesh );
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
		final List< BasicShape > meshes = GltfImporter.loadGLTF( sFilename );
		
		if (meshes != null)
		{
			if(meshes.size()>0)
			{
				bvb.addShapes( meshes, Misc.getSourceStyleName( sFilename ) );
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
		cbMultiMesh.setSelectedIndex(Prefs.get( "BVB.bGroupMesh", true)?0:1);
		gbc.gridx=0;
		gbc.gridy=0;	
		GBCHelper.alighLoose(gbc);
		pWRLSettings.add(new JLabel("Multiple meshes:"), gbc);
		gbc.gridx++;
		pWRLSettings.add( cbMultiMesh, gbc );
		boolean bGroupMesh = true;
		
		int reply = JOptionPane.showConfirmDialog(null, pWRLSettings, "Loading WRL file options", 
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (reply == JOptionPane.OK_OPTION) 
		{
			bGroupMesh = cbMultiMesh.getSelectedIndex()==0;
			Prefs.set( "BVB.bGroupMesh", bGroupMesh);
			
		}
		else
		{
			return;
		}

		bvb.bvvViewer.addOverlayAnimator( new TextOverlayAnimator( "Loading meshes, please wait...", 5000, TextPosition.CENTER )  );
		
		final WRLParser loaderWRT = new WRLParser();
		//loaderWRT.nMaxMeshes = 4;
		//loaderWRT.nMaxTimePoints = 1;
		loaderWRT.bEnableWireGrid = true;
		//loaderWRT.bEnableWireGrid = false;
		final ArrayList< Mesh > loadedMeshes = loaderWRT.readWRL(sFilename);
		
		if(bGroupMesh)
		{
			IJ.showStatus( "Uploading " + Integer.toString( loadedMeshes.size() ) + " meshes." );
			if(loaderWRT.containsColorInfo())
			{
				Set<Color> uniqueColors = new HashSet<>(loaderWRT.meshColors);
				HashMap<Color,MultiMeshColor> meshGroups = new HashMap<>();
				for (final Color color:uniqueColors)
				{
					meshGroups.put( color,  new MultiMeshColor() );
				}
				for(int i=0;i<loadedMeshes.size();i++)
				{
					int nTP = -1;

					if(loaderWRT.isTimeData())
					{
						nTP = loaderWRT.timePoints.get( i );
					}
					Color meshColor = loaderWRT.meshColors.get( i );
					meshGroups.get( meshColor ).addMesh( loadedMeshes.get( i ), nTP, meshColor );
				}
				int nC = 0;
				for (Entry< Color, MultiMeshColor > pair : meshGroups.entrySet()) 
				{
					pair.getValue().setName( "c" +Integer.toString( nC )+"_"+Misc.getSourceStyleName( sFilename )  ); 
					bvb.addShape( pair.getValue() ); 
					nC++;
				}
					
			}
			else
			{
				MultiMeshColor mmColor = new MultiMeshColor();
				Color meshColor = null;
				for(int i=0;i<loadedMeshes.size();i++)
				{
					int nTP = -1;

					if(loaderWRT.isTimeData())
					{
						nTP = loaderWRT.timePoints.get( i );
					}
					mmColor.addMesh( loadedMeshes.get( i ), nTP, meshColor );					
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
			for(int i=0;i<loadedMeshes.size();i++)
			{		
				final MeshColor meshBVB = new MeshColor(loadedMeshes.get( i ));
			
				if(loaderWRT.isTimeData())
				{
					meshBVB.setTimePoint( loaderWRT.timePoints.get( i ) );
				}
				if(loaderWRT.containsColorInfo())
				{
					meshBVB.setColor( loaderWRT.meshColors.get( i ) );
				}	
				
				finMeshesShapes.add( meshBVB );
			}
			bvb.addShapes( finMeshesShapes, Misc.getSourceStyleName( sFilename ) );
		}
	}
}
