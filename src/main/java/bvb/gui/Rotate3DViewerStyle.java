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
package bvb.gui;


import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;

import org.scijava.ui.behaviour.DragBehaviour;

import bvb.core.BigVolumeBrowser;
import bvb.utils.transform.TransformSetups;
import bvvpg.vistools.BvvHandle;

public class Rotate3DViewerStyle implements DragBehaviour
{
	/**
	 * Coordinates where mouse dragging started.
	 */
	private double oX, oY;
	
	private final double speed;
	/**
	 * One step of rotation (radian).
	 */
	final private static double step = Math.PI / 180;
	
	private int centerX = 0, centerY = 0;
	final BigVolumeBrowser bvb;
	final BvvHandle bvvHandle;
	private final AffineTransform3D transform = new AffineTransform3D();
	private final AffineTransform3D affineDragStart = new AffineTransform3D();
	private final AffineTransform3D affineDragCurrent = new AffineTransform3D();

	public Rotate3DViewerStyle( final double speed, BigVolumeBrowser bvb_)
	{	
		bvb = bvb_;
		this.bvvHandle = bvb.bvvHandle;
		this.speed = speed;
	}

	@Override
	public void init( final int x, final int y )
	{
		oX = x;
		oY = y;
		centerX = bvvHandle.getViewerPanel().getDisplay().getWidth()/2;
		centerY = bvvHandle.getViewerPanel().getDisplay().getHeight()/2;
		//affineDragStart.set(bvvHandle.getViewerPanel().state().getViewerTransform());
		//transform.get( affineDragStart );
	}

	@Override
	public void drag( final int x, final int y )
	{
		
		final double rotationX = (y - oY) *  step * speed;
		final double rotationY = (oX - x) * step * speed ;
		//final double dX = oX - x;
		//final double dY = oY - y;
		//final double v = step * speed;

		affineDragCurrent.set( affineDragStart );

		// center shift
		affineDragCurrent.set( affineDragCurrent.get( 0, 3 ) - centerX, 0, 3 );
		affineDragCurrent.set( affineDragCurrent.get( 1, 3 ) - centerY, 1, 3 );	

		affineDragCurrent.rotate( 0, rotationX );
		affineDragCurrent.rotate( 1, rotationY );

		// center un-shift
		affineDragCurrent.set( affineDragCurrent.get( 0, 3 ) + centerX, 0, 3 );
		affineDragCurrent.set( affineDragCurrent.get( 1, 3 ) + centerY, 1, 3 );
		
		//does not depend on how far we from initial click
		oX = x;
		oY = y;
		if(!bvb.bManualTransformMode)
		{
			transform.set( bvvHandle.getViewerPanel().state().getViewerTransform() );
			transform.preConcatenate( affineDragCurrent );
			bvvHandle.getViewerPanel().state().setViewerTransform(transform);
		}
		else
		{
			//bvb.bvbCards.transformPanel.transformSetups.s
			if(bvb.selectedObjects.isAnythingSelected())
			{
				final TransformSetups transformSetups = bvb.bvbCards.transformPanel.transformSetups;
				final List< Object > objList = bvb.selectedObjects.getSelectedObjects();
				for ( final Object obj: objList)
				{	
					final double [] eAngles = transformSetups.transformRotation.getAngles( obj );
					final double [] prevAngles =  new double[3];
					for(int d = 0; d < 3; d++)
					{
						prevAngles[d] = eAngles[d];
					}
					
					eAngles[0] += rotationX;
					eAngles[1] += rotationY;

					transformSetups.transformRotation.setAngles( obj, eAngles );
					transformSetups.updateTransform( obj, prevAngles );
				}
				transformSetups.updateBVV();
				bvb.bvbCards.transformPanel.updateGUI();
			}
		}
	}

	@Override
	public void end( final int x, final int y )
	{}

}
