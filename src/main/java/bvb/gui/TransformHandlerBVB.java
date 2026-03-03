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
package bvb.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.TransformEventHandler3D;
import bdv.TransformState;
import bvb.core.BigVolumeBrowser;
import bvb.utils.Misc;
import bvb.utils.transform.TransformSetups;
import bvvpg.vistools.BvvHandle;

public class TransformHandlerBVB 
{
	
	private static final double[] speed = { 0.75, 2.0, 0.1 };
	
	final private static double step = Math.PI / 180;
	
	/**
	 * Copy of transform when mouse dragging started.
	 */
	private final AffineTransform3D affineDragStart = new AffineTransform3D();

	/**
	 * Current transform during mouse dragging.
	 */
	private final AffineTransform3D affineDragCurrent = new AffineTransform3D();
	
	/**
	 * Coordinates where mouse dragging started.
	 */
	private double oX, oY;
	
	/**
	 * Screen coordinates to keep centered while zooming or rotating with the
	 * keyboard. These are set to <em>(canvasW/2, canvasH/2)</em>
	 */
	private int centerX = 0, centerY = 0;
	
	/** orientation of X and Y axis in the current view **/
	final double [][] vXY = new double [2][3]; 
	
	/** rotation angles from mouse displacement **/
	final double [] rotationXY = new double [2];
	
	private final Map< Object, double[] > objToOldCenters = new HashMap<>();
	
	private final TransformState transform;
	
	final BigVolumeBrowser bvb;
	
	final BvvHandle bvvHandle;
	
	// -- behaviours --
	
	private final TranslateXY dragTranslate;
	private final Rotate dragRotate;
	private final Rotate dragRotateFast;
	private final Rotate dragRotateSlow;
	
	public TransformHandlerBVB( final BigVolumeBrowser bvb)
	{
		this.bvb = bvb;
		bvvHandle = bvb.bvvHandle;
		this.transform = TransformState.from( bvb.bvvViewer.state()::getViewerTransform, bvb.bvvViewer.state()::setViewerTransform );
		
		dragTranslate = new TranslateXY();
		dragRotate = new Rotate( speed[ 0 ] );
		dragRotateFast = new Rotate( speed[ 1 ] );
		dragRotateSlow = new Rotate( speed[ 2 ] );

	}
	
	public void install( final Behaviours behaviours )
	{
		behaviours.behaviour( dragTranslate, TransformEventHandler3D.DRAG_TRANSLATE, TransformEventHandler3D.DRAG_TRANSLATE_KEYS);
		behaviours.behaviour( dragRotate, TransformEventHandler3D.DRAG_ROTATE, TransformEventHandler3D.DRAG_ROTATE_KEYS);
		behaviours.behaviour( dragRotateFast, TransformEventHandler3D.DRAG_ROTATE_FAST, TransformEventHandler3D.DRAG_ROTATE_FAST_KEYS );
		behaviours.behaviour( dragRotateSlow, TransformEventHandler3D.DRAG_ROTATE_SLOW, TransformEventHandler3D.DRAG_ROTATE_SLOW_KEYS );
	}
	
	private class Rotate implements DragBehaviour
	{
		@SuppressWarnings( "hiding" )
		private final double speed;
		
		private boolean isDrag;

		public Rotate( final double speed )
		{
			this.speed = speed;
		}
		
		@Override
		public void init( final int x, final int y )
		{
			oX = x;
			oY = y;
			centerX = bvvHandle.getViewerPanel().getDisplay().getWidth()/2;
			centerY = bvvHandle.getViewerPanel().getDisplay().getHeight()/2;
			
			transform.get( affineDragStart );
			
			for (int d = 0; d < 3; d++)
			{
				vXY[0][d] = 0.0;
				vXY[1][d] = 0.0;			
			}
			vXY[0][0] = 1.0;
			vXY[1][1] = 1.0;
			
			final AffineTransform3D viewTransform = affineDragStart.copy();
			transform.get( viewTransform );
			//let's remove translation
			for(int d = 0; d < 3; d++)
			{
				viewTransform.set( 0, d, 3);					
			}
			
			for (int i = 0; i < 2; i++)
			{
				viewTransform.applyInverse( vXY[i], vXY[i]);
				LinAlgHelpers.normalize( vXY[i] );
			}
			isDrag = false;
			
		}

		@Override
		public void drag( final int x, final int y )
		{
			isDrag = true;
			rotationXY[0]  = (y - oY) *  step * speed;
			rotationXY[1]  = (oX - x) * step * speed;

			affineDragCurrent.set( affineDragStart );

			// center shift
			affineDragCurrent.set( affineDragCurrent.get( 0, 3 ) - centerX, 0, 3 );
			affineDragCurrent.set( affineDragCurrent.get( 1, 3 ) - centerY, 1, 3 );	

			affineDragCurrent.rotate( 0, rotationXY[0] );
			affineDragCurrent.rotate( 1, rotationXY[1] );

			// center un-shift
			affineDragCurrent.set( affineDragCurrent.get( 0, 3 ) + centerX, 0, 3 );
			affineDragCurrent.set( affineDragCurrent.get( 1, 3 ) + centerY, 1, 3 );
			//apply rotation to the view transform
			if(!bvb.bManualTransformMode)
			{
				transform.set( affineDragCurrent );
			}		
			//apply rotation to selected objects
			else
			{
				//slower moving
				oX = x;
				oY = y;
				if(bvb.selectedObjects.isAnythingSelected())
				{
					//get rotation around current X and Y axes
					final double [][] qXY = new double[2][4];
					final double [][] anglesRot = new double[2][3];
					for (int i = 0; i < 2; i++)
					{
						LinAlgHelpers.quaternionFromAngleAxis( vXY[i], rotationXY[i], qXY[i] );
						
						for(int d = 0; d < 3; d++)
						{
							anglesRot[i][d] = Misc.quaternionToAngle( d, qXY[i] );
						}
					}

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
						for(int d = 0; d < 3; d++)
						{
							eAngles[d] += anglesRot[0][d] + anglesRot[1][d];
						}
						
						transformSetups.transformRotation.setAngles( obj, eAngles );
					
						//update center positions if needed
						if(!transformSetups.bLocalCoordinates)
						{
							final double [] oldCenters = transformSetups.transformCenters.getCenters( obj );
							final double [] newCenters = new double [3];
							for(int d = 0; d < 3; d++)
							{
								newCenters[d] = oldCenters[d];
							}
							affineDragCurrent.apply( newCenters, newCenters );
							affineDragStart.applyInverse( newCenters, newCenters );
							transformSetups.transformCenters.setCenters( obj, newCenters );					
						}
						transformSetups.updateTransform( obj, prevAngles );
					}
					transformSetups.updateBVV();
					bvb.bvbCards.transformPanel.updateGUI();
				}
			}
		}

		@Override
		public void end( final int x, final int y )
		{
			if(!isDrag)
			{
				final int nAxis = bvb.axisOverlay.getHighlightedAxis();
				if( nAxis >= 0 )
				{
					bvb.bvbActions.alignToAxis(nAxis);
				}
			}
		}

	}
	
	private class TranslateXY implements DragBehaviour
	{
		
		@Override
		public void init( final int x, final int y )
		{
			oX = x;
			oY = y;

			transform.get( affineDragStart );

			if(bvb.bManualTransformMode)
			{
				objToOldCenters.clear();
				final List< Object > objList = bvb.selectedObjects.getSelectedObjects();
				for ( final Object obj: objList)
				{
					final double [] oldCenters = bvb.bvbCards.transformPanel.transformSetups.transformCenters.getCenters( obj );
					final double [] oldCentersCopy = new double [3];
					for(int d = 0; d < 3; d++)
					{
						oldCentersCopy[d] = oldCenters[d];
					}
					affineDragStart.apply( oldCentersCopy, oldCentersCopy ); 
					objToOldCenters.put( obj, oldCentersCopy );
				}	
			}
		}

		@Override
		public void drag( final int x, final int y )
		{
			double dX = oX - x;
			double dY = oY - y;
//			
//			if(bvb.bvvViewer.getProjectionType() == 1)
//			{
//				dX = Math.signum( dX )*Math.sqrt( Math.abs( dX ) );
//				dY = Math.signum( dY )*Math.sqrt( Math.abs( dY ) );
//			}
			
			//apply rotation to the view transform
			if(!bvb.bManualTransformMode)
			{
				affineDragCurrent.set( affineDragStart );
				affineDragCurrent.set( affineDragCurrent.get( 0, 3 ) - dX, 0, 3 );
				affineDragCurrent.set( affineDragCurrent.get( 1, 3 ) - dY, 1, 3 );	
				transform.set( affineDragCurrent );
			}
			else
			{
			
				if(bvb.selectedObjects.isAnythingSelected())
				{
					final TransformSetups transformSetups = bvb.bvbCards.transformPanel.transformSetups;
					final List< Object > objList = bvb.selectedObjects.getSelectedObjects();
					
					for ( final Object obj: objList)
					{
						final double [] oldCentersTr = objToOldCenters.get( obj );
						final double [] newCenters = new double [3];
						for(int d = 0; d < 3; d++)
						{
							newCenters[d] = oldCentersTr[d] ;
						}
						//apply transform in the view coordinates
						newCenters[0]-= dX;
						newCenters[1]-= dY;
						//go back to the world coordinates
						affineDragStart.applyInverse( newCenters, newCenters );						
						transformSetups.transformCenters.setCenters( obj, newCenters );
						transformSetups.updateTransform( obj, null );
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
}
