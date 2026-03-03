/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.imglib2.realtransform.AffineTransform3D;

import bdv.util.BoundedValueDouble;
import bvb.utils.Misc;
import bvb.utils.transform.TransformSetups;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class TransformRotationPanel extends JPanel
{
	final TransformSetups transformSetups;

	private final BoundedValuePanelPG [] trRotationPanels = new BoundedValuePanelPG[3];

	private boolean blockUpdates = false;
	
	private double dRange = 180.;
	
	public TransformRotationPanel(final TransformSetups transformSetups_) 
	{
		super();
		
		transformSetups = transformSetups_;
		
		GridBagLayout gridbag = new GridBagLayout();
		
		GridBagConstraints gbc = new GridBagConstraints();

		setLayout(gridbag);
		
		gbc.gridwidth = 0;
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		String [] sAxes = new String[] {"X", "Y", "Z"};
		for(int d = 0; d < 3; d++)
		{
			gbc.gridy++;
			trRotationPanels[d] = new BoundedValuePanelPG( new BoundedValueDouble( -dRange, dRange, 0.0 ));
			trRotationPanels[d].setToolTipText( "Angle around " + sAxes[d] );
			this.add(trRotationPanels[d], gbc);
		}
		
		trRotationPanels[0].changeListeners().add( () -> updateAxisRotation(0));
		trRotationPanels[1].changeListeners().add( () -> updateAxisRotation(1));
		trRotationPanels[2].changeListeners().add( () -> updateAxisRotation(2));

		updateGUI();
	}
	
	synchronized void updateGUI()
	{
		
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		double [] angles = new double[3];
		boolean bFirstObj = true;
		boolean [] allAnglesEqual = new boolean [3];
		for (int d = 0; d < 3; d++)
		{
			allAnglesEqual[ d ] = true;
		}
		
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			if(bFirstObj)
			{
				angles = transformSetups.transformRotation.getAngles( obj );
				bFirstObj = false;
			}
			else
			{
				final double[] currAngles = transformSetups.transformRotation.getAngles( obj );

				for (int d = 0; d < 3; d++)
				{
					allAnglesEqual[d] &= (Math.abs( angles[d]-currAngles[d] )<0.00001);
				}
			}
		}
		
		final double [] finalAngles = angles;
		final boolean [] isConsistent = allAnglesEqual;
		
		for(int d = 0; d < 3; d++)
		{
			finalAngles[d] = Misc.angleToMinusPiPlusPi( finalAngles[d]  ); 
		}
			
		SwingUtilities.invokeLater( () -> {
			synchronized ( TransformRotationPanel.this )
			{
				blockUpdates = true;
				for (int d = 0; d < 3; d++)
				{

					trRotationPanels[d].setConsistent( isConsistent[d] );
					trRotationPanels[d].setValue( new BoundedValueDouble( -dRange, dRange, finalAngles[d]*180/Math.PI ) );
				}
				blockUpdates = false;
			}
		} );
	}
	
	void updateAxisRotation(int nAxis)
	{
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		blockUpdates = true;
		final AffineTransform3D viewTr = transformSetups.bvb.bvvViewer.state().getViewerTransform();
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{			
			final double [] eAngles = transformSetups.transformRotation.getAngles( obj );
			final double [] prevAngles = new double[3];
			for(int d = 0; d < 3; d++)
			{
				prevAngles[d] = eAngles[d];
			}
			
			eAngles[nAxis] = trRotationPanels[nAxis].getValue().getCurrentValue()*Math.PI/180.;

			transformSetups.transformRotation.setAngles( obj, eAngles );
			
			if(!transformSetups.bLocalCoordinates)
			{
				
				//get new rotation center
				double [] viewCenter = new double [] {transformSetups.bvb.bvvHandle.getViewerPanel().getDisplay().getWidth()*0.5,
						transformSetups.bvb.bvvHandle.getViewerPanel().getDisplay().getHeight()*0.5,
						0.0};
				
				//new center of the FOV
				viewTr.applyInverse( viewCenter, viewCenter );
				
				final double [] oldCenters = transformSetups.transformCenters.getCenters( obj );
				final double [] newCenters = new double [3];
				for(int d = 0; d < 3; d++)
				{
					newCenters[d] = oldCenters[d] - viewCenter[d];
				}
				
				//build rotation transform
				final AffineTransform3D rotationTr = new AffineTransform3D();
				for(int d = 0; d < 3; d++)
				{
					rotationTr.rotate( d, eAngles[d] -  prevAngles[d]);
				}
				rotationTr.apply( newCenters, newCenters );
				for(int d = 0; d < 3; d++)
				{
					newCenters[d] += viewCenter[d];
				}

				transformSetups.transformCenters.setCenters( obj, newCenters );	
			}			
			
			transformSetups.updateTransform( obj, prevAngles);

		}
		
		blockUpdates = false;
		
		updateGUI();
		
		if( !transformSetups.bLocalCoordinates )
		{
			transformSetups.bvb.bvbCards.transformPanel.transformCentersPanel.updateGUI();
		}
	}
	
	public void resetRotation()
	{
		
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		blockUpdates = true;
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final double [] prevAngles =  new double[3];
			final double [] eAngles = transformSetups.transformRotation.getAngles( obj );
			for(int d = 0; d < 3; d++)
			{
				prevAngles[ d ] = eAngles [ d ];
			}
			transformSetups.transformRotation.setAngles( obj,  new double [3] );
			transformSetups.updateTransform( obj, prevAngles );		
		}
		blockUpdates = false;
		updateGUI();
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(int d = 0; d < 3; d++)
		{
			trRotationPanels[ d ].setEnabled( bEnabled );
		}
	}
	
	void setSliderColors(Color [] colors)
	{
		for(int d = 0; d < 3; d++)
		{
			trRotationPanels[ d ].setSliderForeground( colors[ d ] );	
		}
	}
}
