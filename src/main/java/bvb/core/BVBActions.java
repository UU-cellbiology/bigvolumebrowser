package bvb.core;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextField;

import net.imglib2.FinalRealInterval;
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
import bvb.geometry.Line3D;
import bvb.gui.AnisotropicTransformAnimator3D;
import bvb.gui.Rotate3DViewerStyle;
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
			bvb.bvvViewer.setTransformAnimator(getCenteredViewAnim(getAllSelectedVisibleSourcesBoundindBox(),0.95));
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
			final int nTimePoint = bvb.bvvViewer.state().getCurrentTimepoint();
			for(SourceAndConverter< ? > sac : allSources)
			{
				if(sac.getSpimSource().isPresent( nTimePoint ))
				{
					final GammaConverterSetup cs = (GammaConverterSetup)bvb.bvvHandle.getConverterSetups().getConverterSetup( sac );
					final FinalRealInterval sourceInt = Misc.getSourceBoundingBox( sac.getSpimSource(), nTimePoint, 0 );
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
	

	public AffineTransform3D getCenteredViewTransform(final AffineTransform3D ini_transform, final RealInterval inInterval, double zoomFraction)
	{
	
		//current window dimensions
		final int sW = bvb.bvvViewer.getWidth();
		final int sH = bvb.bvvViewer.getHeight();
		
		//current view transform
		final AffineTransform3D transform = ini_transform.copy();//new AffineTransform3D(ini_transform);
		
		//center of the interval in the world coordinates
		double [] centerCoord = Misc.getIntervalCenter( inInterval );			
		
		//center of the screen "volume" 		
		double [] centerViewPoint = new double[3];
		
		transform.apply( centerCoord, centerViewPoint );

		//position center of the volume in the center of "screen" volume
		double [] dl = new double[3];
		
		//translation after source transform to new position
		for(int d=0;d<3;d++)
		{
			dl[d] = (-1)*centerViewPoint[d];
		}
		//move to the origin of coordinates
		transform.translate(dl);
		
		//extract view rotation, since we are not going to change it
		final double [] quat = new double[4];
		Affine3DHelpers.extractRotationAnisotropic( transform, quat );
		double [] angles = new double[3];
		for(int d=0;d<3;d++)
		{
			angles[d] = Misc.quaternionToAngle(d, quat);
		}
		final double [][] rotMatrix = new double [3][4];
		LinAlgHelpers.quaternionToR( quat, rotMatrix );
		
		//estimate new bounding box of the input interval after the view rotation
		AffineTransform3D viewRot = new AffineTransform3D();
		viewRot.set( rotMatrix );
		AffineTransform3D viewRotFinal = new AffineTransform3D();
		LinAlgHelpers.scale( centerCoord, (-1), centerCoord );
		viewRotFinal.translate( centerCoord ); 
		viewRotFinal = viewRotFinal.preConcatenate( viewRot );
		LinAlgHelpers.scale( centerCoord, (-1), centerCoord );
		viewRotFinal.translate( centerCoord ); 
		
		final FinalRealInterval rotInterval = viewRotFinal.estimateBounds( inInterval );
		
		//move to the center of the canvas
		dl[0] = 0.5f*sW;
		dl[1] = 0.5f*sH;
		dl[2] = 0.0;
		transform.translate( dl );
	
		Matrix4f matPerspWorld = new Matrix4f();
		MatrixMath.screenPerspective( bvb.bvvViewer.getProjectionType(), BVVSettings.dCam, BVVSettings.dClipNear, BVVSettings.dClipFar, sW, sH, 0, matPerspWorld ).mul( MatrixMath.affine( transform, new Matrix4f() ) );
		Vector3f temp = new Vector3f(); 
		
		final RealPoint [][] camRayLinesWH = new RealPoint[2][2]; 
		final RealPoint [][] camRayLinesNearFar = new RealPoint[2][2]; 
	
		
		//width
		for (int z=0 ; z<2; z++)
		{
			//take coordinates in original data volume space
			matPerspWorld.unproject(0.0f,0.5f*sH,z, //z=1 ->far from camera z=0 -> close to camera
					new int[] { 0, 0, sW, sH },temp);
			camRayLinesWH[0][z] = new RealPoint(temp.x,temp.y,temp.z);
		}
		
		//height
		for (int z=0 ; z<2; z++)
		{
			matPerspWorld.unproject(sW*0.5f,sH,z, 
					new int[] { 0, 0, sW, sH },temp);	
			camRayLinesWH[1][z] = new RealPoint(temp.x,temp.y,temp.z);
		}
		
		//Z view axis
		for (int z=0 ; z<2; z++)
		{
			matPerspWorld.unproject(sW*0.5f,sH*0.5f,z, 
					new int[] { 0, 0, sW, sH },temp);	
			camRayLinesNearFar[z][0] = new RealPoint(temp.x,temp.y,temp.z);
		}
		for(int i=0;i<2;i++)
		{
			camRayLinesNearFar[i][1] = camRayLinesWH[1][i];
		}

		double [] scales = new double[4];
		RealPoint[][] boxRayWH = new RealPoint[2][2];
		
		//width and height
		for(int i=0;i<2;i++)
		{	
			boxRayWH[i] = makeBoxRayLineWH(i, rotInterval.minAsDoubleArray(), centerCoord, viewRotFinal );
			scales[i] = zoomFraction*getMaxScaleFactorIntersect(camRayLinesWH[i], boxRayWH[i]);
		}
		
		//clip near and clip far
		for(int i=0;i<2;i++)
		{	
			scales[i+2] = zoomFraction*getMaxScaleFactorIntersect( camRayLinesNearFar[i], boxRayWH[1]);
		}

		double finScale = Double.MAX_VALUE;
		for(int i=0; i<4; i++)
		{
			finScale = Math.min( finScale, scales[i] );
		}
		
		LinAlgHelpers.scale( dl, (-1.0), dl );
		transform.translate( dl );
		
		transform.scale( finScale );
		
		LinAlgHelpers.scale( dl, (-1.0), dl );
		transform.translate( dl );
		
		return transform;
	}
		
	/** calculates optimal zoom to scale bounding box,
	 * based on provided two rays (lines) **/
	double getMaxScaleFactorIntersect(final RealPoint [] camRayLine, RealPoint [] boxRayLine)
	{

		Line3D camRay = new Line3D(camRayLine[ 0 ],camRayLine[ 1 ]);
		Line3D boxRay = new Line3D(boxRayLine[ 0 ],boxRayLine[ 1 ]);
		
		double [] vals = Line3D.linesIntersect( camRay, boxRay );
		double [] intersectPoint  = new double [3];
		
		boxRay.value( vals[1], intersectPoint );
	
		double [] center = boxRayLine[0].positionAsDoubleArray();
		double [] edge = boxRayLine[1].positionAsDoubleArray();
		LinAlgHelpers.subtract( intersectPoint, center, intersectPoint );
		LinAlgHelpers.subtract( boxRayLine[1].positionAsDoubleArray(), center, edge );
		
		return LinAlgHelpers.length( intersectPoint )/LinAlgHelpers.length( edge);

	}
	
	/** calculates "width" and "height" rays **/
	RealPoint [] makeBoxRayLineWH(final int nAxis, final double [] minInt, final double [] centerCoord, final AffineTransform3D viewRotFinal)
	{
		RealPoint [] boxRayLine = new RealPoint[2];
		boxRayLine[0] =  new RealPoint (centerCoord );
		double [] boxRayPoint = new double[3];
		for (int d=0; d<3; d++)
		{
			boxRayPoint[d] = minInt[d];
		}
		if(nAxis == 0)
		{	
			boxRayPoint[1] = centerCoord[1];
		}
		else
		{
			boxRayPoint[0] = centerCoord[0];	
		}
		viewRotFinal.inverse().apply( boxRayPoint, boxRayPoint);
		boxRayLine[1] = new RealPoint (boxRayPoint);
		
		return boxRayLine;
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
