package bvb.gui.shapes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import bvb.core.BigVolumeBrowser;
import bvb.shapes.BasicMeshColor;
import bvb.shapes.BasicShape;
import bvb.shapes.Spots;

public class ShapesPropertiesPanel extends JPanel 
{
	final BigVolumeBrowser bvb;
	
	final SpotsPropertiesPanel panelsSpotsProperties;
	final MeshesPropertiesPanel panelsMeshesProperties;

	final JTabbedPane tabPane;
	
	public ShapesPropertiesPanel(final BigVolumeBrowser bvb_)
	{
		super();
		bvb = bvb_;
		GridBagLayout gridbag = new GridBagLayout();

		setLayout(gridbag);
		
		panelsSpotsProperties = new SpotsPropertiesPanel(bvb);
		
		panelsMeshesProperties = new MeshesPropertiesPanel(bvb);
		
		tabPane = new JTabbedPane(SwingConstants.TOP);
		
		tabPane.addTab( "Spots", panelsSpotsProperties );
		tabPane.addTab( "Mesh", panelsMeshesProperties );
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		this.add( tabPane, gbc );
		bvb.selectedObjects.addObjectSelectionListener( () -> updateGUI());
		updateGUI();
	}
	
	
	public synchronized void updateGUI()
	{
		if(!bvb.selectedObjects.areShapesSelected())
		{
			setPanelsEnabled(false);
			return;
		}
		boolean bSpotsUpdate = true;
		boolean bMeshUpdate = true;
		final List< BasicShape > shapes = bvb.selectedObjects.getSelectedShapes();
		for(final BasicShape sh:shapes)
		{
			if(sh instanceof Spots && bSpotsUpdate )
			{
				panelsSpotsProperties.setEnabled( true );
				panelsSpotsProperties.updateGUI();
				bSpotsUpdate = false;
			}
			if(sh instanceof BasicMeshColor && bMeshUpdate )
			{
				panelsMeshesProperties.setEnabled( true );
				panelsMeshesProperties.updateGUI();
				bMeshUpdate = false;
			}

		}
		if(bSpotsUpdate)
			panelsSpotsProperties.setEnabled( false );
		if(bMeshUpdate)
			panelsMeshesProperties.setEnabled( false );

	}
	
	void setPanelsEnabled(boolean bEnabled)
	{
		panelsSpotsProperties.setEnabled( bEnabled );
		panelsMeshesProperties.setEnabled( bEnabled );
	}
	
	
}
