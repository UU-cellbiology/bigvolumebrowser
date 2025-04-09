package bvb.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextField;

import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;
import net.imglib2.util.LinAlgHelpers;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.Affine3DHelpers;
import bdv.viewer.SourceAndConverter;
import bvb.geometry.Line2D;
import bvb.gui.AnisotropicTransformAnimator3D;
import bvb.gui.Rotate3DViewerStyle;
import bvb.scene.VisPolyLineAA;
import bvb.utils.Misc;
import bvvpg.core.util.MatrixMath;
import bvvpg.source.converters.GammaConverterSetup;
import bvvpg.vistools.BvvHandle;

public class BVBActions
{
	final BigVolumeBrowser bvb;
	
	final Actions actions;
	final Behaviours behaviours;
	
	public BVBActions(final BigVolumeBrowser bvb_) 
	{
		bvb = bvb_;
		actions = new Actions( new InputTriggerConfig() );
		behaviours = new Behaviours( new InputTriggerConfig() );
		installBehaviors();
		installActions();
	}
	
	/** install smoother rotation **/
	void installBehaviors()
	{
		final BvvHandle handle = bvb.bvv.getBvvHandle();
		//change drag rotation for navigation "3D Viewer" style
		final Rotate3DViewerStyle dragRotate = new Rotate3DViewerStyle( 0.75, handle);
		final Rotate3DViewerStyle dragRotateFast = new Rotate3DViewerStyle( 2.0, handle);
		final Rotate3DViewerStyle dragRotateSlow = new Rotate3DViewerStyle( 0.1, handle);
		
		behaviours.behaviour( dragRotate, "drag rotate", "button1" );
		behaviours.behaviour( dragRotateFast, "drag rotate fast", "shift button1" );
		behaviours.behaviour( dragRotateSlow, "drag rotate slow", "ctrl button1" );
		behaviours.install( handle.getTriggerbindings(), "BigTrace Behaviours" );
	}
	
	void installActions()
	{
		actions.runnableAction(() -> actionCenterView(), "center view (zoom out)", "C" );
		actions.install( bvb.bvvHandle.getKeybindings(), "BigTrace actions" );
	}
	
	public ActionMap getActionMap()
	{		
		return actions.getActionMap();
	}
	
	public InputMap getInputMap()
	{
		return actions.getInputMap();
	}
	
	void actionCenterView()
	{
		Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		//solution for now, to not interfere with typing
		if(!bvb.bLocked && !(c instanceof JTextField))
		{
			bvb.bvvViewer.setTransformAnimator(getCenteredViewAnim(getAllSelectedVisibleSourcesBoundindBox(),1.0));
		}
	}
	
	
	/** returns a bounding box with all selected/visible sources, taking clipping into account.
	 *  if there is no selected sources, returns all visible sources bbox.
	 *  of there is no visible or no sources, returns null **/
	FinalRealInterval getAllSelectedVisibleSourcesBoundindBox()
	{
		
		FinalRealInterval allInt = null;		

		final Set< SourceAndConverter< ? > > visibleSet = bvb.bvvViewer.state().getVisibleSources();
		//no visible sources
		if(visibleSet.size() == 0)
			return null;
		
		final ArrayList<SourceAndConverter< ? >> allSources = new ArrayList<>();		
		//selected sources
		final List< ConverterSetup > csList = bvb.selectedSources.getSelectedSources();
		for(ConverterSetup cs : csList)
		{
			//is it visible?
			SourceAndConverter< ? > sac = bvb.bvvHandle.getConverterSetups().getSource( cs );
			if(bvb.bvvViewer.state().isSourceVisible( sac ))
			{
				allSources.add( sac );	
			}
		}
		//nothing visible in selected, let's add all visible
		if(allSources.size()==0)
		{
			for(SourceAndConverter< ? > sac :visibleSet)
			{
				allSources.add( sac );
			}
		}
		
		//just in case
		if(allSources.size()>0)
		{
			for(SourceAndConverter< ? > sac : allSources)
			{
				
				final GammaConverterSetup cs = (GammaConverterSetup)bvb.bvvHandle.getConverterSetups().getConverterSetup( sac );
				final FinalRealInterval sourceInt = Misc.getSourceBoundingBox( sac.getSpimSource(), bvb.bvvViewer.state().getCurrentTimepoint(), 0 );
				FinalRealInterval clipInt = cs.getClipInterval() ;
				//no clipping
				if(!cs.clipActive() || clipInt == null)
				{
					allInt = appendIntervals(allInt, sourceInt);
				}
				//clipping is on
				else
				{
					//get clipping transform
					final AffineTransform3D clipTr = new AffineTransform3D();
					cs.getClipTransform( clipTr );
					clipInt = clipTr.estimateBounds( clipInt );
					clipInt = Intervals.intersect( clipInt, sourceInt );
					//clipping interval could be outside of source, let's check it
					boolean bIntersectOk = true;
					for(int d = 0; d<3; d++)
					{
						if(clipInt.realMin( d )>=clipInt.realMax( d ))
						{
							bIntersectOk = false;
							break;
						}
					}
					if(!bIntersectOk)
					{
						clipInt = sourceInt;
					}
					allInt = appendIntervals(allInt,clipInt);
				}
			}
		}
		
		return allInt;
		
	}
	
	FinalRealInterval appendIntervals(final FinalRealInterval current, final FinalRealInterval newInt)
	{
		if(current == null)
		{
			return newInt;
		}
		return Intervals.union( current, newInt );
	}
	
//	public AffineTransform3D getCenteredViewTransform(final AffineTransform3D ini_transform, final RealInterval inInterval_, double zoomFraction)
//	{
//		
//		double [] quat = new double[4];
//		Affine3DHelpers.extractRotationAnisotropic(ini_transform, quat);
//		double [][] mat = new double[3][4];
//		LinAlgHelpers.quaternionToR( quat, mat );
//		AffineTransform3D rotTr = new AffineTransform3D();
//		rotTr.set( mat );
//		
//		FinalRealInterval inInterval = rotTr.estimateBounds( inInterval_ ); 
//		double [][] dBox = new double[2][3];
//		
//		dBox[0] = inInterval.minAsDoubleArray();
//		dBox[1] = inInterval.maxAsDoubleArray();
//		final double nW = (dBox[1][0]-dBox[0][0]);
//		final double nH = (dBox[1][1]-dBox[0][1]);
//		final double nWoff = 2.0*dBox[0][0];
//		final double nHoff = 2.0*dBox[0][1];
//		final double nDoff = 2.0*dBox[0][2];
//		//current window dimensions
//		final int sW = bvb.bvvViewer.getWidth();
//		final int sH = bvb.bvvViewer.getHeight();
//		
//		double scale = Math.max( sW/nW, sH/nH );
//		
//		AffineTransform3D t = new AffineTransform3D();
//		t.identity();
//		t.concatenate( rotTr );
//		t.scale(scale);
//		t.translate(0.5*(sW-scale*(nW+nWoff)),0.5*(sH-scale*(nH+nHoff)),(-0.5)*scale*(nDoff));
//		return t;
//		
//	}
	public AffineTransform3D getCenteredViewTransform(final AffineTransform3D ini_transform, final RealInterval inInterval, double zoomFraction)
	{
		int i;

		final double [] minDim = inInterval.minAsDoubleArray();
		final double [] maxDim = inInterval.maxAsDoubleArray();
		final double [] sizeBox = new double[3];
		for(int d=0;d<3;d++)
		{
			sizeBox[d]=maxDim[d]-minDim[d];
		}
		
		double [] centerCoord = new double[3];		
		
		//center of the interval in the world coordinates
		for(int d=0; d<3; d++)
		{
			centerCoord[d] = 0.5*(maxDim[d] + minDim[d]);
		}
		
		//current window dimensions
		final int sW = bvb.bvvViewer.getWidth();
		final int sH = bvb.bvvViewer.getHeight();
		
		//current view transform
		final AffineTransform3D transform = ini_transform.copy();//new AffineTransform3D(ini_transform);
		
		//center of the screen "volume" 		
		double [] centerViewPoint = new double[3];
		
		transform.apply( centerCoord, centerViewPoint );

		//position center of the volume in the center of "screen" volume
		double [] dl = transform.getTranslation();
		
		//translation after source transform to new position
		for(int d=0;d<3;d++)
		{
			//dl[d] += (centerViewPoint[d]-centerCoordWorldOld[d]);
			dl[d] -= centerViewPoint[d];
		}
		//dl[0] += 0.5f*sW;
		//dl[1] += 0.5f*sH;
		//move to the origin
		transform.setTranslation(dl);
//		double [] quat = new double [4];
//		Affine3DHelpers.extractRotationAnisotropic( transform, quat);
//		LinAlgHelpers.quaternionToR( quat, null );
		
		//get
	
		//let's figure out the scale		
//		FinalRealInterval scaledInt = transform.estimateBounds( inInterval );
		
		
//		double [] dScale = new double [3];
//		for(int d=0;d<3;d++)
//		{
//			//transform
//			dScale[d] = scaledInt.realMax( d )-scaledInt.realMin( d );
//		}
//		dScale[0] = sW*1.0/dScale[0];
//		dScale[1] = sH*1.0/dScale[1];
//		transform.scale( Math.min( dScale[0], dScale[1] ));
//		//move to the center of the canvas
		dl[0] = 0.5f*sW;
		dl[1] = 0.5f*sH;
		dl[2] = 0.0;
		transform.translate( dl );
		FinalRealInterval scaledInt = transform.estimateBounds( inInterval );
		final double [] sizeBoxScaled = new double[3];
		for(int d=0;d<3;d++)
		{
			sizeBoxScaled[d]=scaledInt.realMax( d )-scaledInt.realMin( d );
		}
		

		
		Matrix4f matPerspWorld = new Matrix4f();
		MatrixMath.screenPerspective( BVVSettings.dCam, BVVSettings.dClipNear, BVVSettings.dClipFar, sW, sH, 0, matPerspWorld ).mul( MatrixMath.affine( transform, new Matrix4f() ) );
		double [][] mainLinePoints = new double[2][2];
		Vector3f temp = new Vector3f(); 
		ArrayList<RealPoint> camRayLine = new ArrayList<>();
		for (int z =0 ; z<2; z++)
		{
			//take coordinates in original data volume space
			matPerspWorld.unproject((float)(sW*0.5),sH,z, //z=1 ->far from camera z=0 -> close to camera
					new int[] { 0, 0, sW, sH },temp);
			
			mainLinePoints[z][0]= temp.y;
			mainLinePoints[z][1]= temp.z;
			camRayLine.add( new RealPoint(temp.x,temp.y,temp.z));
			//mainLinePoints[z] = new RealPoint(temp.x,temp.y,temp.z);			
		}
		
		bvb.helpLines.add( new VisPolyLineAA(camRayLine, 8, Color.WHITE) );
		Vector3f newtemp = new Vector3f(); 
		newtemp.set( centerCoord[0], centerCoord[1], centerCoord[2] );
		matPerspWorld.project( newtemp, new int[] { 0, 0, sW, sH }, temp );
		
		Line2D camRay = new Line2D(mainLinePoints[0],mainLinePoints[1]);
		
		
		ArrayList<RealPoint> boxRayLine = new ArrayList<>();
		
		boxRayLine.add( new RealPoint (centerCoord ));
		
		double [] other = new double[3];
		for (int d=0; d<3; d++)
		{
			other[d] = inInterval.realMin( d );
		}
		other[0] = centerCoord[0];
		boxRayLine.add( new RealPoint (other) );
		bvb.helpLines.add( new VisPolyLineAA(boxRayLine, 8, Color.GREEN) );
		
//		mainLinePoints[0][0] *=(-1);
//		mainLinePoints[1][0] *=(-1);
//		Line2D camRay2 = new Line2D(mainLinePoints[0],mainLinePoints[1]);
		for (int d=0; d<2; d++)
		{
			mainLinePoints[0][d] = centerCoord[d+1];
		}
		for (int d=0; d<2; d++)
		{
			mainLinePoints[1][d] = inInterval.realMin( d );
		}
//		mainLinePoints[1][0] = -sizeBox[1]*0.5;
//		mainLinePoints[1][1] = -sizeBox[2]*0.5;
//		mainLinePoints[1][0] = -sizeBoxScaled[1]*0.5;
//		mainLinePoints[1][1] = -sizeBoxScaled[2]*0.5;		
		//sizeBoxScaled
		
		Line2D boxRay = new Line2D(mainLinePoints[0],mainLinePoints[1]);
		
		double [] intersectPoint = Line2D.intersectionLines2D( camRay, boxRay );
		boxRay.value( intersectPoint[1], intersectPoint);
		
		for(int d=0;d<2;d++)
		{
			intersectPoint[d] -= centerCoord[d+1];
			mainLinePoints[1][d] -= centerCoord[d+1];
		}
		
//		coeffScale = Line2D.intersectionLines2D( camRay2, boxRay );
//		boxRay.value( coeffScale[1], coeffScale );
		//double finScale = (-1.0)*coeffScale[0]/(sizeBox[1]*0.5); 
		//double finScale = (-1.0)*coeffScale[0]/(sizeBox[1]*0.5);//(sizeBoxScaled[1]*0.5); 
		double finScale = LinAlgHelpers.length( intersectPoint )/LinAlgHelpers.length( mainLinePoints[1] );//*(sizeBoxScaled[1]*0.5);

		//double finScale2 = (-1.0)*coeffScale[1]/(sizeBoxScaled[2]*0.5); 
		LinAlgHelpers.scale( dl, (-1.0), dl );
		transform.translate( dl );
		transform.scale( finScale );
		LinAlgHelpers.scale( dl, (-1.0), dl );
		transform.translate( dl );
		return transform;
	}
	
	public AnisotropicTransformAnimator3D getCenteredViewAnim(final RealInterval inInterval, double zoomFraction)
	{
		final AffineTransform3D transform = new AffineTransform3D();
		bvb.bvvViewer.state().getViewerTransform(transform);
		
		final AffineTransform3D transform_scale = getCenteredViewTransform(inInterval,zoomFraction);
		
		final AnisotropicTransformAnimator3D anim = new AnisotropicTransformAnimator3D(transform,transform_scale,0,0, BVBSettings.nTransformAnimationDuration);			
		
		return anim;
	}
	
	public AffineTransform3D getCenteredViewTransform(final RealInterval inInterval, double zoomFraction)
	{
		
		final AffineTransform3D transform = new AffineTransform3D();
		
		bvb.bvvViewer.state().getViewerTransform(transform);
		
		return getCenteredViewTransform(transform, inInterval, zoomFraction);
	}
}
