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
package bvb.gui.clip;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.BoundedValueDouble;
import bvb.gui.SelectedSources;
import bvb.utils.clip.ClipSetups;
import bvvpg.source.converters.GammaConverterSetup;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class ClipRotationPanel extends JPanel
{

	final ClipSetups clipSetups;

	private BoundedValuePanelPG [] clipRotationPanels = new BoundedValuePanelPG[3];

	private boolean blockUpdates = false;
	
	private double dRange = 180.;
	
	public ClipRotationPanel(final ClipSetups clipSetups_) 
	{
		super();
		
		clipSetups = clipSetups_;
		
		GridBagLayout gridbag = new GridBagLayout();
		
		GridBagConstraints cd = new GridBagConstraints();

		setLayout(gridbag);
		
		cd.gridwidth = 0;
		cd.gridy = 0;
		cd.gridx = 0;
		cd.fill = GridBagConstraints.BOTH;
		cd.weightx = 1.0;
		for(int d=0;d<3;d++)
		{
			cd.gridy++;
			clipRotationPanels[d] = new BoundedValuePanelPG( new BoundedValueDouble( -dRange, dRange, 0.0 ));

			this.add(clipRotationPanels[d],cd);
		}
		
		clipRotationPanels[0].changeListeners().add( () -> updateClipAxisRotation(0));
		clipRotationPanels[1].changeListeners().add( () -> updateClipAxisRotation(1));
		clipRotationPanels[2].changeListeners().add( () -> updateClipAxisRotation(2));
		
		//add source selection listener
		clipSetups.selectedSources.addSourceSelectionListener(  new SelectedSources.Listener()
		{			
			@Override
			public void selectedSourcesChanged()
			{
				updateGUI();
			}
		} );
	
		
		//add listener in case number of sources, etc change
		clipSetups.converterSetups.listeners().add( s -> updateGUI() );
		
		updateGUI();
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(int i=0;i<3;i++)
		{
			clipRotationPanels[i].setEnabled( bEnabled );
		}
	}
	
	
	synchronized void updateGUI()
	{
		final List< ConverterSetup > csList = clipSetups.selectedSources.getSelectedSources();
		if ( blockUpdates || csList == null || csList.isEmpty() )
			return;	
		
		
		double [] angles = new double[3];
		boolean bFirstCS = true;
		boolean [] allAnglesEqual = new boolean [3];
		for (int d=0;d<3;d++)
		{
			allAnglesEqual[d] = true;
		}
		
		for ( final ConverterSetup cs: csList)
		{
			if(bFirstCS)
			{
				angles = clipSetups.clipRotationAngles.getAngles( cs );
				bFirstCS = false;
			}
			else
			{
				final double[] currAngles = clipSetups.clipRotationAngles.getAngles( cs );

				for (int d=0; d<3; d++)
				{
					allAnglesEqual[d] &= (Double.compare( angles[d], currAngles[d] )==0);
				}
			}
		}
		final double [] finalAngles = angles;
		final boolean [] isConsistent = allAnglesEqual;
		SwingUtilities.invokeLater( () -> {
			synchronized ( ClipRotationPanel.this )
			{
				blockUpdates = true;
				for (int d=0;d<3;d++)
				{

					clipRotationPanels[d].setConsistent( isConsistent[d] );
					clipRotationPanels[d].setValue( new BoundedValueDouble( -dRange, dRange, finalAngles[d]*180/Math.PI ) );
				}
				blockUpdates = false;
			}
		} );
	}
	
	synchronized void updateClipAxisRotation(int nAxis)
	{
		final List< ConverterSetup > csList = clipSetups.selectedSources.getSelectedSources();
		if ( blockUpdates || csList == null || csList.isEmpty() )
			return;
		blockUpdates = true;
		for ( final ConverterSetup cs : csList )
		{
			
			final double [] eAngles = clipSetups.clipRotationAngles.getAngles( cs );
			eAngles[nAxis] = clipRotationPanels[nAxis].getValue().getCurrentValue()*Math.PI/180.;
			
			clipSetups.clipRotationAngles.setAngles( cs, eAngles );
			clipSetups.updateClipTransform( ( GammaConverterSetup ) cs );

		}
		blockUpdates = false;
		updateGUI();
	}
	
	void setSliderColors(Color [] colors)
	{
		for(int i=0;i<3;i++)
		{
			clipRotationPanels[i].setSliderForeground( colors[i] );	
		}
	}

}
