package bvb.gui;

import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

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
	
	private final TransformState transform;
	
	final BigVolumeBrowser bvb;
	
	final BvvHandle bvvHandle;
	
	// -- behaviours --
	
	private final Rotate dragRotate;
	private final Rotate dragRotateFast;
	private final Rotate dragRotateSlow;
	
	public TransformHandlerBVB( final BigVolumeBrowser bvb)
	{
		this.bvb = bvb;
		bvvHandle = bvb.bvvHandle;
		this.transform = TransformState.from( bvb.bvvViewer.state()::getViewerTransform, bvb.bvvViewer.state()::setViewerTransform );
		
		dragRotate = new Rotate( speed[ 0 ] );
		dragRotateFast = new Rotate( speed[ 1 ] );
		dragRotateSlow = new Rotate( speed[ 2 ] );

	}
	
	public void install( final Behaviours behaviours )
	{
		behaviours.behaviour( dragRotate, "drag rotate", "button1" );
		behaviours.behaviour( dragRotateFast, "drag rotate fast", "shift button1" );
		behaviours.behaviour( dragRotateSlow, "drag rotate slow", "ctrl button1" );
	}
	
	private class Rotate implements DragBehaviour
	{
		@SuppressWarnings( "hiding" )
		private final double speed;

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
		}

		@Override
		public void drag( final int x, final int y )
		{
			final double [] rotationXY = new double [2];
			rotationXY[0]  = (y - oY) *  step * speed;
			rotationXY[1]  = (oX - x) * step * speed ;
			
			//apply rotation to the view transform
			if(!bvb.bManualTransformMode)
			{
				affineDragCurrent.set( affineDragStart );

				// center shift
				affineDragCurrent.set( affineDragCurrent.get( 0, 3 ) - centerX, 0, 3 );
				affineDragCurrent.set( affineDragCurrent.get( 1, 3 ) - centerY, 1, 3 );	

				affineDragCurrent.rotate( 0, rotationXY[0] );
				affineDragCurrent.rotate( 1, rotationXY[1] );

				// center un-shift
				affineDragCurrent.set( affineDragCurrent.get( 0, 3 ) + centerX, 0, 3 );
				affineDragCurrent.set( affineDragCurrent.get( 1, 3 ) + centerY, 1, 3 );
				transform.set( affineDragCurrent );

			}		
			//apply rotation to selected objects
			else
			{
				oX = x;
				oY = y;
				if(bvb.selectedObjects.isAnythingSelected())
				{
					//see where x and y axis of the current view are looking
					final double [][] vXY = new double [2][3]; 
					vXY[0][0] = 1.0;
					vXY[1][1] = 1.0;
					
					final AffineTransform3D viewTransform = new AffineTransform3D();
					transform.get( viewTransform );
					//let's remove translation
					for(int d = 0; d < 3; d++)
					{
						viewTransform.set( 0, d, 3);					
					}
					final double [][] qXY = new double[2][4];
					final double [][] anglesRot = new double[2][3];
					for (int i = 0; i < 2; i++)
					{
						viewTransform.applyInverse( vXY[i], vXY[i]);
						LinAlgHelpers.normalize( vXY[i] );
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
}
