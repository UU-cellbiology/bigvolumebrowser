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
package bvb.gui.transform;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.utils.transform.TransformSetups;
import ij.Prefs;

public class TransformPanel extends JPanel 
{
	final BigVolumeBrowser bvb;
	
	final public TransformSetups transformSetups;
	
	final TransformScalePanel transformScalePanel;
	
	final TransformCenterPanel transformCentersPanel;
	
	final TransformRotationPanel transformRotationPanel;
	
	final JCheckBox cbTransformClip;
	
	final JButton butResetCurrent;
	
	final JButton butResetAll;
	
	JButton butCoordSystem;
	
	final ImageIcon [] coordIcon = new ImageIcon[2];
	
	final String[] coordToolTip = new String[2];
	
	final JTabbedPane tabTrPane;
	
	public TransformPanel(final BigVolumeBrowser bvb_)
	{
		super();
		bvb = bvb_;
		
		GridBagLayout gridbag = new GridBagLayout();

		setLayout(gridbag);

		transformSetups = new TransformSetups(bvb);
		
		transformScalePanel = new TransformScalePanel(transformSetups);
		
		transformCentersPanel = new TransformCenterPanel(transformSetups);
		
		transformRotationPanel = new TransformRotationPanel(transformSetups);

		
		tabTrPane = new JTabbedPane(SwingConstants.TOP);

		tabTrPane.addTab( "Center", transformCentersPanel );
		tabTrPane.addTab( "Rotate(L)", transformRotationPanel );
		tabTrPane.addTab( "Scale", transformScalePanel );
		
		if(!transformSetups.bLocalCoordinates)
		{
			tabTrPane.setTitleAt( 0, "Translate" );
			tabTrPane.setTitleAt( 1, "Rotate(W)" );

		}	
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0.3;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		
		
		cbTransformClip = new JCheckBox("Modify clip", true);
		cbTransformClip.setSelected( transformSetups.bTransformClip );
		cbTransformClip.addItemListener((e)->
		{
			transformSetups.bTransformClip = cbTransformClip.isSelected();
			Prefs.set( "BVB.bTransformClip", transformSetups.bTransformClip );
		} );
		this.add(cbTransformClip,gbc);
		
		
		coordToolTip[0] = "Global world coordinates";
		coordToolTip[1] = "Local volume coordinates";

		URL icon_path = this.getClass().getResource("/icons/" + BVBSettings.sUITheme + "frame_global.png");
		coordIcon[0] = new ImageIcon(icon_path);
		icon_path = this.getClass().getResource("/icons/" + BVBSettings.sUITheme + "frame_local.png");
		coordIcon[1] = new ImageIcon(icon_path);
		gbc.gridx ++;
		gbc.weightx = 0.6;
		butCoordSystem = new JButton(coordIcon[transformSetups.bLocalCoordinates?1:0]);
		butCoordSystem.setToolTipText(coordToolTip[transformSetups.bLocalCoordinates?1:0]);

		butCoordSystem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent arg0 )
			{
				transformSetups.bLocalCoordinates = !transformSetups.bLocalCoordinates;
				int ind = 0;
				
				if(transformSetups.bLocalCoordinates)
				{
					tabTrPane.setTitleAt( 0, "Center" );
					tabTrPane.setTitleAt( 1, "Rotate(L)" );
					ind = 1;
				}
				else
				{
					tabTrPane.setTitleAt( 0, "Translate" );
					tabTrPane.setTitleAt( 1, "Rotate(W)" );
				}
				butCoordSystem.setIcon( coordIcon[ind]  );
				butCoordSystem.setToolTipText(coordToolTip[ind]);
				Prefs.set( "BVB.bCenterPanel", transformSetups.bLocalCoordinates );
				transformCentersPanel.updateGUI();
				//updateGUI();
			}
	
		});
		this.add(butCoordSystem,gbc);			
		
		gbc.weightx = 0.05;
		gbc.fill = GridBagConstraints.NONE;
		icon_path = this.getClass().getResource("/icons/red_cross.png");
		ImageIcon icon = new ImageIcon(icon_path);
		butResetCurrent = new JButton(icon);
		butResetCurrent.setToolTipText( "Reset current panel" );
		butResetCurrent.addActionListener((e)->	resetPanelTransform()); 
		gbc.gridx ++;
		this.add(butResetCurrent,gbc);	
		
		icon_path = this.getClass().getResource("/icons/red_crossx2.png");
		icon = new ImageIcon(icon_path);
		butResetAll = new JButton(icon);
		butResetAll.setToolTipText( "Reset all transforms" );
		butResetAll.addActionListener((e)->	resetFullTransform()); 

		gbc.gridx ++;
		this.add(butResetAll,gbc);	
		
		
		gbc.gridx = 0;
	    gbc.gridy ++;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 4;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
		
	    this.add(tabTrPane,gbc);
	    
	    setupListeners();
	    
	    updateGUI();
	    
	    Color [] colors = new Color[3];
	    colors[0] =  new Color(198,34,0);
	    colors[1] =  new Color(67,154,0);
	    colors[2] =  new Color(0,34,213);

	    this.setSliderColors( colors );
	    

	}
	
	public synchronized void updateGUI()
	{
		if(!transformSetups.selectedObjects.isAnythingSelected())
		{
			setPanelsEnabled(false);
			cbTransformClip.setEnabled( false );
			return;
		}
		
		setPanelsEnabled(true);
		cbTransformClip.setEnabled( true );
		transformScalePanel.updateGUI();
		transformCentersPanel.updateGUI();
		transformRotationPanel.updateGUI();
	}
	
	private void setPanelsEnabled(boolean bEnabled)
	{
		transformScalePanel.setEnabled( bEnabled );
		transformCentersPanel.setEnabled( bEnabled );
		transformRotationPanel.setEnabled( bEnabled );
	}
	
	public void setupListeners()
	{		
		transformSetups.selectedObjects.addObjectSelectionListener( () -> updateGUI());		
	    //add listener in case number of sources, etc change
		transformSetups.converterSetups.listeners().add( s -> updateGUI() );

	}
	
	void setSliderColors(Color [] colors)
	{		
		transformCentersPanel.setSliderColors( colors );
		transformRotationPanel.setSliderColors( colors );
	}
	
	void resetPanelTransform()
	{
		switch(tabTrPane.getSelectedIndex())
		{
		case 0:
			transformCentersPanel.resetCenters();
			break;
		case 1:
			transformRotationPanel.resetRotation();
			break;
		case 2:
			transformScalePanel.resetScale();
			break;
		default:
		}
	}
	
	void resetFullTransform()
	{
		if(!transformSetups.selectedObjects.isAnythingSelected())
			return;
		
		final double [] unitScale = new double [3];
		
		for (int d=0; d<3;d++)
		{
			unitScale [d] = 1.0;
		}
		
		transformCentersPanel.resetCenters();
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final double [] prevAngles =  new double[3];
			final double [] eAngles = transformSetups.transformRotation.getAngles( obj );
			for(int d=0;d<3;d++)
			{
				prevAngles[d] = eAngles [d];
			}
			transformSetups.transformRotation.setAngles( obj,  new double [3] );
			transformSetups.transformScale.setScale( obj, unitScale );
			transformSetups.updateTransform( obj, prevAngles );
		}
		updateGUI();
	}
	
	
}
