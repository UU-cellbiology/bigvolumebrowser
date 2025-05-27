package bvb.gui.shapes;


import java.awt.Color;
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
import bvb.scene.VisSpotsSame;
import bvb.shapes.MeshColor;
import bvb.shapes.SpotsSame;
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
			final int [] nColInd = new int[3];
			for(int d=0; d<3;d++)
			{
				if(dialSpots.cbColumnsAssign.get( d ).getSelectedIndex()>0)
				{
					nColInd[d] = dialSpots.cbColumnsAssign.get( d ).getSelectedIndex()-1;
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
				sptParser.fScale = 0.001f;
				break;
			//nano
			case 2:
				sptParser.fScale = 1000.0f;
				break;
			default:
				sptParser.fScale = 1.0f;
			}
			sptParser.execute();
			sptParser.addPropertyChangeListener( (e)->
			{
				if(sptParser.getState() == StateValue.DONE)
				{
					SpotsSame importedSpots = new SpotsSame(20.0f, Color.WHITE, VisSpotsSame.SHAPE_ROUND, VisSpotsSame.RENDER_GAUSS);
					importedSpots.setPoints( sptParser.vertices );
					bvb.addShape( importedSpots );
				}
			}
			);
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
            //bvb.loadBDVHDF5( chooser.getSelectedFile().getPath() );
        }
	}
}
