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
import bvb.shapes.BasicSpots;

public class ShapesPropertiesPanel extends JPanel 
{
	final BigVolumeBrowser bvb;
	
	final SpotsPropertiesPanel panelSpotsProperties;
	final MeshesPropertiesPanel panelMeshesProperties;
	final GeneralPropertiesPanel panelGeneralProperties;

	final JTabbedPane tabPane;
	
	public ShapesPropertiesPanel(final BigVolumeBrowser bvb_)
	{
		super();
		bvb = bvb_;
		GridBagLayout gridbag = new GridBagLayout();

		setLayout(gridbag);
		
		panelSpotsProperties = new SpotsPropertiesPanel(bvb);
		
		panelMeshesProperties = new MeshesPropertiesPanel(bvb);
		
		panelGeneralProperties = new GeneralPropertiesPanel(bvb);
		
		tabPane = new JTabbedPane(SwingConstants.TOP);
		
		tabPane.addTab( "Spots", panelSpotsProperties );
		tabPane.addTab( "Mesh", panelMeshesProperties );
		tabPane.addTab( "General", panelGeneralProperties );

		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		
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
			if(sh instanceof BasicSpots && bSpotsUpdate )
			{
				panelSpotsProperties.setEnabled( true );
				panelSpotsProperties.updateGUI(true);
				bSpotsUpdate = false;
			}
			if(sh instanceof BasicMeshColor && bMeshUpdate )
			{
				panelMeshesProperties.setEnabled( true );
				panelMeshesProperties.updateGUI();
				bMeshUpdate = false;
			}

		}
		if(bSpotsUpdate)
			panelSpotsProperties.setEnabled( false );
		if(bMeshUpdate)
			panelMeshesProperties.setEnabled( false );

	}
	
	void setPanelsEnabled(boolean bEnabled)
	{
		panelSpotsProperties.setEnabled( bEnabled );
		panelMeshesProperties.setEnabled( bEnabled );
	}
	
	
}
