package bvb.core;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpinnerModel;

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
import ij.Prefs;

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
		actions.runnableAction(() -> runSettingsCommand(), "settings", "F10" );
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

	void runSettingsCommand()
	{
		JPanel pViewSettings = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbcL = new GridBagConstraints();
		GridBagConstraints gbcR = new GridBagConstraints();
		
		SpinnerModel smW = new SpinnerNumberModel(BVVSettings.renderWidth, 10, 10000, 1);		
		JSpinner renderWidth = new JSpinner(smW);
		renderWidth.setEditor(new JSpinner.NumberEditor(renderWidth, "#"));
		renderWidth.setToolTipText( "Viewport render width"  );
		
		SpinnerModel smH = new SpinnerNumberModel(BVVSettings.renderHeight, 10, 10000, 1);		
		JSpinner renderHeight = new JSpinner(smH);
		renderHeight.setEditor(new JSpinner.NumberEditor(renderHeight, "#"));
		renderHeight.setToolTipText( "Viewport render height"  );
			
		
		String[] sDitherWidths = { "none (always render full resolution)", "2x2", "3x3", "4x4", "5x5", "6x6", "7x7", "8x8" };
		JComboBox<String> ditherWidthsList = new JComboBox<>(sDitherWidths);
		ditherWidthsList.setToolTipText( "Dither window size" );
		ditherWidthsList.setSelectedIndex(BVVSettings.ditherWidth-1);

		JSlider slNumDitherSamples = new JSlider(SwingConstants.HORIZONTAL,
                1, 8, BVVSettings.numDitherSamples);
		slNumDitherSamples.setToolTipText( "Pixels are interpolated from this many nearest neighbors when dithering. This is not very expensive, it's fine to turn it up to 8." );
		slNumDitherSamples.setMinorTickSpacing(1);
		Hashtable< Integer, JLabel > labelTable = new Hashtable<>();
		labelTable.put( new Integer( 1 ), new JLabel("1") );
		for(int i=1; i<=4; i++)
		{
			labelTable.put( new Integer( i*2 ), new JLabel(Integer.toString( i*2 )) );
			
		}
		slNumDitherSamples.setLabelTable( labelTable );
		slNumDitherSamples.setPaintTicks(true);
		slNumDitherSamples.setPaintLabels(true);
		
		SpinnerModel cacheBlockSizeM = new SpinnerNumberModel(BVVSettings.cacheBlockSize, 10, 1024, 1);		
		JSpinner cacheBlockSize = new JSpinner(cacheBlockSizeM);
		cacheBlockSize.setEditor(new JSpinner.NumberEditor(cacheBlockSize, "#"));
		SpinnerModel maxCacheSizeInMBM = new SpinnerNumberModel(BVVSettings.maxCacheSizeInMB, 10, Integer.MAX_VALUE, 1);		
		JSpinner maxCacheSizeInMB = new JSpinner(maxCacheSizeInMBM);
		maxCacheSizeInMB.setToolTipText( "The size of the GPU cache texture. Increase it to the max available."  );
		
		SpinnerModel dCamM = new SpinnerNumberModel(BVVSettings.dCam, BVVSettings.dClipNear+5, Integer.MAX_VALUE, 1);		
		JSpinner dCam = new JSpinner(dCamM);
		dCam.setEditor(new JSpinner.NumberEditor(dCam, "#"));
		dCam.setToolTipText( "Distance from camera to z=0 plane (in space units)."  );
		
		
		SpinnerModel dClipFarM = new SpinnerNumberModel(BVVSettings.dClipFar, 10, Integer.MAX_VALUE, 1);		
		JSpinner dClipFar = new JSpinner(dClipFarM);
		dClipFar.setEditor(new JSpinner.NumberEditor(dClipFar, "#"));
		dClipFar.setToolTipText( "Visible depth from z=0 further away from the camera (in space units)."  );
		
		SpinnerModel dClipNearM = new SpinnerNumberModel(BVVSettings.dClipNear, 10, Integer.MAX_VALUE, 1);		
		JSpinner dClipNear = new JSpinner(dClipNearM);
		dClipNear.setEditor(new JSpinner.NumberEditor(dClipNear, "#"));
		dClipNear.setToolTipText( "Visible depth from z=0 closer to the camera (in space units). MUST BE SMALLER THAN CAMERA DISTANCE!"  );
		dClipNear.addChangeListener( new ChangeListener()
				{

					@Override
					public void stateChanged( ChangeEvent arg0 )
					{
						int currNear =  ((Double)dClipNear.getValue()).intValue();
						((SpinnerNumberModel)dCam.getModel()).setMinimum( new Double(currNear+5) );
						if(currNear > ((Double)dCam.getValue()).intValue())
						{
							dCam.setValue( currNear+5 );
							//(dCam.getModel()).setMinimum( new Integer(currNear+5) );
						}
					}

			
				}
				);
		
		gbcL.insets = new Insets(5,5,5,5);
		gbcR.insets = new Insets(5,5,5,5);
		gbcL.anchor = GridBagConstraints.EAST;
		gbcR.fill = GridBagConstraints.HORIZONTAL;
		gbcR.weightx = 1.0;
		
		gbcL.gridx=0;
		gbcR.gridx=1;
		gbcL.gridy=0;
		gbcR.gridy=0;
		pViewSettings.add( new JLabel("Render width"), gbcL );	
		pViewSettings.add( renderWidth,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Render height"), gbcL );
		pViewSettings.add( renderHeight,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Dither window size"), gbcL );
		pViewSettings.add( ditherWidthsList, gbcR );

		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Number of dither samples"), gbcL );
		pViewSettings.add( slNumDitherSamples,gbcR );

		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("GPU cache tile size"), gbcL );
		pViewSettings.add( cacheBlockSize,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("GPU cache size (in MB)"), gbcL );
		pViewSettings.add( maxCacheSizeInMB,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Camera distance"), gbcL );
		pViewSettings.add( dCam,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Clip distance far"), gbcL );
		pViewSettings.add( dClipFar,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Clip distance near"), gbcL );
		pViewSettings.add( dClipNear,gbcR );
		
		
		int reply = JOptionPane.showConfirmDialog(null, pViewSettings, "BVV canvas settings", 
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (reply == JOptionPane.OK_OPTION) 
		{
			boolean bRestartBVV = false;
			BVVSettings.dCam = ((Double)dCam.getValue()).doubleValue();
			Prefs.set("BVB.dCam", BVVSettings.dCam);
			BVVSettings.dClipFar = ((Double)dClipFar.getValue()).doubleValue();
			Prefs.set("BVB.dClipFar", BVVSettings.dClipFar);
			BVVSettings.dClipNear = ((Double)dClipNear.getValue()).doubleValue();
			Prefs.set("BVB.dClipNear", BVVSettings.dClipNear);
			
			int nTempInt =  ((Integer)renderWidth.getValue()).intValue();
			if(BVVSettings.renderWidth != nTempInt)
			{
				BVVSettings.renderWidth = nTempInt;
				bRestartBVV = true;
				Prefs.set("BVB.renderWidth", BVVSettings.renderWidth);
			}
			
			nTempInt =  ((Integer)renderHeight.getValue()).intValue();
			if(BVVSettings.renderHeight != nTempInt)
			{
				BVVSettings.renderHeight = nTempInt;
				bRestartBVV = true;
				Prefs.set("BVB.renderHeight", BVVSettings.renderHeight);
			}
			
			nTempInt = ditherWidthsList.getSelectedIndex()+1;
			if(BVVSettings.ditherWidth != nTempInt)
			{
				BVVSettings.ditherWidth = nTempInt;
				bRestartBVV = true;
				Prefs.set("BVB.ditherWidth", BVVSettings.ditherWidth);
			}
			
			nTempInt = slNumDitherSamples.getValue();
			if(BVVSettings.numDitherSamples != nTempInt)
			{
				BVVSettings.numDitherSamples = nTempInt;
				bRestartBVV = true;
				Prefs.set("BVB.numDitherSamples", BVVSettings.numDitherSamples);
			}
			
			nTempInt = ((Integer)cacheBlockSizeM.getValue()).intValue();
			if(BVVSettings.cacheBlockSize != nTempInt)
			{
				BVVSettings.cacheBlockSize = nTempInt;
				bRestartBVV = true;
				Prefs.set("BVB.cacheBlockSize", BVVSettings.cacheBlockSize);
			}
			
			nTempInt = ((Integer)maxCacheSizeInMB.getValue()).intValue();
			if(BVVSettings.maxCacheSizeInMB != nTempInt)
			{
				BVVSettings.maxCacheSizeInMB = nTempInt;
				bRestartBVV = true;
				Prefs.set("BVB.maxCacheSizeInMB", BVVSettings.maxCacheSizeInMB);
			}
			
			if(!bRestartBVV)
			{
				bvb.bvvViewer.setCamParams( BVVSettings.dCam, BVVSettings.dClipNear, BVVSettings.dClipFar );
				bvb.repaintBVV();
			}
			else
			{
				bvb.restartBVV();
			}
		}
	}

//	void runSettingsCommandSciJava()
//	{	
//		
//		//final Context ctx = new Context(); // you need to have one of these; make one with new if you don't already		
//		//CommandService cs = ctx.service(CommandService.class);
//		HelloWorld cn = new HelloWorld();
//		Services.commandService.context().inject( cn );
//		Future<CommandModule> f = Services.commandService.run(HelloWorld.class, true);
//
////		Future<CommandModule> f = Services.commandService.run(ConfigureBVVRenderWindow.class, true);
//		try
//		{
//			Module m = f.get();
//		}
//		catch ( InterruptedException exc )
//		{
//			// TO DO Auto-generated catch block
//			exc.printStackTrace();
//		}
//		catch ( ExecutionException exc )
//		{
//			// TO DO Auto-generated catch block
//			exc.printStackTrace();
//		} 
//		// wait for command to complete
//	//	Map<String, Object> outputs = m.getOutputs();
////		System.out.println("Processed data = " + outputs.get("processedData");
//
//	}
	
	void actionCenterView()
	{
		Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		//solution for now, to not interfere with typing
		if(!bvb.bLocked && !(c instanceof JTextField))
		{
			final FinalRealInterval focusInt = getAllSelectedVisibleSourcesBoundindBox();
			if(focusInt != null)
			{
				bvb.bvvViewer.setTransformAnimator(getCenteredViewAnim(focusInt,0.95));
			}
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
