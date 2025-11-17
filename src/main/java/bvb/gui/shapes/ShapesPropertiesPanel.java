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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import bvb.core.BigVolumeBrowser;
import bvb.shapes.BasicMeshShape;
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
				panelSpotsProperties.updateGUI();
				bSpotsUpdate = false;
			}
			if(sh instanceof BasicMeshShape && bMeshUpdate )
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
