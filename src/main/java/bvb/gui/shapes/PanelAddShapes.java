package bvb.gui.shapes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import javax.swing.JPanel;
import javax.swing.SwingWorker.StateValue;
import javax.swing.filechooser.FileNameExtensionFilter;

import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.io.shapes.SpotsParser;
import bvb.shapes.MeshColor;
import bvb.shapes.Spots;
import bvb.shapes.SpotsSame;
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
		//this.setBorder(new PanelTitle(" Add shapes "));
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

//	    gbc.gridx++;
//	    this.add( butBDVXML,gbc);
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
				bAskForSize = false;

			}
			if(!sptShape.showSelectionDialog( bAskForSize ))
			{
				return;
			}
			

			
			
			sptParser.addPropertyChangeListener( (e)->
			{
				if(sptParser.getState() == StateValue.DONE)
				{
					IJ.showStatus( "Uploading " +Long.toString( sptParser.nTotSpots )+" to GPU...");
					if(!sptParser.parseSize)
					{
						
						final SpotsSame importedSpots = new SpotsSame(sptShape.fSpotSize, sptShape.spotColor, sptShape.nShape, sptShape.nFill);
						importedSpots.setPoints( sptParser.vertices );
						bvb.addShape( importedSpots );
					}
					else
					{
						final Spots importedSpots = new Spots(10.0f, sptShape.spotColor, sptShape.nShape, sptShape.nFill);
						importedSpots.setPoints( sptParser.vertices, sptParser.sizes);
						bvb.addShape( importedSpots );						
					}
					IJ.showStatus( "Uploading " +Long.toString( sptParser.nTotSpots )+" to GPU...done.");
				}
			}
			);
			
			sptParser.execute();
		}
	}
	
	void loadMeshDialog()
	{
        JFileChooser chooser = new JFileChooser(BVBSettings.lastDir);
        chooser.setDialogTitle( "Open Mesh Data" );
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Mesh files", "stl", "ply");
        chooser.setFileFilter(filter);
        
        int returnVal = chooser.showOpenDialog(null);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
            BVBSettings.lastDir = chooser.getSelectedFile().getParent();
            Prefs.set( "BVB.lastDir",  BVBSettings.lastDir );
            
            final MeshColor loadedMesh = new MeshColor(chooser.getSelectedFile().getPath());
            
            bvb.addShape( loadedMesh );
            
            bvb.focusOnRealInterval( loadedMesh.boundingBox() );
        }
	}
}
