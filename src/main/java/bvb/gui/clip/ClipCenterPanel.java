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

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;


import bdv.tools.brightness.ConverterSetup;
import bdv.util.BoundedValueDouble;
import bvb.gui.SelectedSources;
import bvb.utils.BoundedValueDoubleBVB;
import bvb.utils.Bounds3D;
import bvb.utils.clip.ClipSetups;
import bvvpg.source.converters.GammaConverterSetup;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class ClipCenterPanel extends JPanel
{

	final ClipSetups clipSetups;

	private BoundedValuePanelPG [] clipCenterPanels = new BoundedValuePanelPG[3];

	private boolean blockUpdates = false;	
	
	public ClipCenterPanel( final ClipSetups clipSetups_ ) 
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
		final JPopupMenu [] menus = new JPopupMenu[3];
		for(int d=0;d<3;d++)
		{
			cd.gridy++;
			
			clipCenterPanels[d] = new BoundedValuePanelPG( new BoundedValueDouble( 0.0, 1.0, 0.5 ));
			menus[d] = new JPopupMenu();
			menus[d].add( runnableItem(  "set bounds ...", clipCenterPanels[d]::setBoundsDialog ) );
			this.add(clipCenterPanels[d],cd);
		}

		menus[0].add( runnableItem(  "reset bounds", () -> resetBounds(0)));
		menus[1].add( runnableItem(  "reset bounds", () -> resetBounds(1)));
		menus[2].add( runnableItem(  "reset bounds", () -> resetBounds(2)));
	
		clipCenterPanels[0].setPopup( () -> menus[0] );
		clipCenterPanels[1].setPopup( () -> menus[1] );
		clipCenterPanels[2].setPopup( () -> menus[2] );
		
		clipCenterPanels[0].changeListeners().add( () -> updateClipCenter(0));
		clipCenterPanels[1].changeListeners().add( () -> updateClipCenter(1));
		clipCenterPanels[2].changeListeners().add( () -> updateClipCenter(2));
		
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
			clipCenterPanels[i].setEnabled( bEnabled );
		}
	}
	
	synchronized void updateGUI()
	{
		final List< ConverterSetup > csList = clipSetups.selectedSources.getSelectedSources();
		if ( blockUpdates || csList == null || csList.isEmpty() )
			return;	
		
		
		BoundedValueDoubleBVB [] boundValue = new BoundedValueDoubleBVB[3];
		boolean bFirstCS = true;
		boolean [] allCenterEqual = new boolean [3];
		for (int d=0;d<3;d++)
		{
			allCenterEqual[d] = true;
		}

		for ( final ConverterSetup cs: csList)
		{
			final Bounds3D bounds = clipSetups.clipCenterBounds.getBounds( cs );
			final double [] minBound = bounds.getMinBound();
			final double [] maxBound = bounds.getMaxBound();
			
			double [] center = new double [3];
			center = clipSetups.clipCenters.getCenters( cs );
			if(bFirstCS)
			{
				for (int d=0; d<3; d++)
				{
					boundValue[d] = new BoundedValueDoubleBVB( minBound[d], maxBound[d], center[d]);
				}
				bFirstCS = false;
			}
			else
			{
				for (int d=0; d<3; d++)
				{
					final BoundedValueDoubleBVB centerRange = new BoundedValueDoubleBVB( minBound[d], maxBound[d], center[d]);
					allCenterEqual[d] &= boundValue[d].equals( centerRange );
					boundValue[d] = boundValue[d].join( centerRange );
				}
			}
		}
		
		
		final BoundedValueDoubleBVB [] finalCenter = boundValue;
		final boolean [] isConsistent = allCenterEqual;
		SwingUtilities.invokeLater( () -> {
			synchronized ( ClipCenterPanel.this )
			{
				blockUpdates = true;
				for (int d=0;d<3;d++)
				{

					clipCenterPanels[d].setConsistent( isConsistent[d] );
					clipCenterPanels[d].setValue( finalCenter[d] );
				}
				blockUpdates = false;
			}
		} );
	}
	
	synchronized void updateClipCenter(int nAxis)
	{
		final List< ConverterSetup > csList = clipSetups.selectedSources.getSelectedSources();
		if ( blockUpdates || csList == null || csList.isEmpty() )
			return;
		blockUpdates = true;
		double currVal = clipCenterPanels[nAxis].getValue().getCurrentValue();
		double minBound = clipCenterPanels[nAxis].getValue().getRangeMin();
		double maxBound = clipCenterPanels[nAxis].getValue().getRangeMax();
		minBound = Math.min( currVal, minBound );
		maxBound = Math.max( currVal, maxBound );
		for ( final ConverterSetup cs : csList )
		{
			final Bounds3D bounds = clipSetups.clipCenterBounds.getBounds( cs );
			
			if(minBound != bounds.getMinBound()[nAxis] || maxBound != bounds.getMaxBound()[nAxis])
			{
				bounds.getMinBound()[nAxis] = minBound;
				bounds.getMaxBound()[nAxis] = maxBound;
				clipSetups.clipCenterBounds.setBounds( cs, bounds );
			}
			final double [] newCenter = clipSetups.clipCenters.getCenters( cs );
			newCenter[nAxis] = currVal;
			
			clipSetups.clipCenters.setCenters( cs, newCenter );
			clipSetups.updateClipTransform( ( GammaConverterSetup ) cs );
			//update bounds
			

		}
		blockUpdates = false;
		updateGUI();
	}
	
	/** sets bounds along the axis including all selected sources **/
	public void resetBounds(int nAxis)
	{
		final List< ConverterSetup > csList = clipSetups.selectedSources.getSelectedSources();
		if ( blockUpdates || csList== null || csList.isEmpty() )
			return;
		Bounds3D range3D = null;
		for ( final ConverterSetup cs : csList )
		{
			if(range3D == null)
				range3D = clipSetups.clipCenterBounds.getDefaultBounds( cs );
			else
				range3D = range3D.join( clipSetups.clipCenterBounds.getDefaultBounds( cs ) );			
		}
		if(range3D != null)
		{
			double currVal = clipCenterPanels[nAxis].getValue().getCurrentValue();
			double bmin = range3D.getMinBound()[nAxis];
			double bmax = range3D.getMaxBound()[nAxis];
			currVal = Math.min( bmax, currVal );
			currVal = Math.max( bmin, currVal );
			clipCenterPanels[nAxis].setValue( new BoundedValueDouble(bmin, bmax, currVal) );
			updateClipCenter(nAxis);
		}
	}
	
	void setSliderColors(Color [] colors)
	{
		for(int i=0;i<3;i++)
		{
			clipCenterPanels[i].setSliderForeground( colors[i] );	
		}
	}
	
	private JMenuItem runnableItem( final String text, final Runnable action )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> action.run() );
		return item;
	}
}
