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
package bvb.core;

import static com.jogamp.opengl.GL.GL_RGBA8;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.ValuePair;


import org.joml.Matrix4f;

import bdv.util.Prefs;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.TimePointListener;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.base.Entity;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import spimdata.util.Displaysettings;

import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

import bvvpg.core.VolumeViewerFrame;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.core.offscreen.OffScreenFrameBufferWithDepth;
import bvvpg.core.render.RenderData;
import bvvpg.core.util.MatrixMath;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvHandleFrame;
import bvvpg.vistools.BvvStackSource;
import bvb.gui.CenterZoomBVV;
import bvb.gui.ColorTextOverlayAnimator;
import bvb.gui.ColorTextOverlayAnimator.TextPosition;
import bvb.gui.SelectLoadedSource;
import bvb.gui.SelectedObjects;
import bvb.gui.BVVWindowState;
import bvb.gui.ShapeSelectionState;
import bvb.gui.VolumeBBoxes;
import bvb.gui.data.BVBShapeCollectionInfo;
import bvb.gui.data.BVBSpimDataInfo;
import bvb.gui.data.DataTreeModel;
import bvb.gui.data.DataTreeNode;
import bvb.gui.overlays.AxisOverlayRenderer;
import bvb.gui.overlays.MultiBoxOverlayRendererBVB;
import bvb.io.LUTNameFIJI;
import bvb.io.RAIToSpimDataBvv;
import bvb.io.SourceToSpimDataBvv;
import bvb.io.SpimDataWrapper;
import bvb.scene.VisPolyLineAA;
import bvb.scene.VisQuad;
import bvb.shapes.BasicShape;
import bvb.shapes.VolumeBox;
import bvb.utils.Misc;


public class BigVolumeBrowser implements PlugIn, TimePointListener
{
	/** Bvv instance **/
	public Bvv bvv = null;
	
	/** Panel of BigVolumeViewer **/
	public VolumeViewerPanel bvvViewer;

	/** Frame of BigVolumeViewer **/
	public VolumeViewerFrame bvvFrame;
	
	/** Handle of BigVolumeViewer **/
	public BvvHandleFrame bvvHandle;
	
	/** object holding all cards panels **/
	public BVBCards bvbCards = null;
	
	/** actions and behaviors **/
	public BVBActions bvbActions;
	
	/** boxes around volume **/	
	final public VolumeBBoxes volumeBoxes;
	
	/** separate framebuffer for the transparent rendering **/
	OffScreenFrameBufferWithDepth sceneBufTransparent = null;
	
	/** clipping boxes **/	
	public final VolumeBBoxes clipBoxes;

	/** flag to lock BVB while it is busy **/
	boolean bInputLock;
	
	/** status of curretly selected groups + listener **/
	public ShapeSelectionState shapeSelection;
	
	/** currently selected sources and shapes + listener for update **/
	public SelectedObjects selectedObjects;
	
	/** maps bvv sources to the input data **/
	private final ConcurrentHashMap < BvvStackSource<?>, AbstractSpimData<?> > bvvSourceToSpimData;
	
	/** maps input data to bvv sources **/
	private final ConcurrentHashMap < AbstractSpimData<?>, List<BvvStackSource<?> >> spimDataToBVVSourceList;

	/** info about input data (icon, description) **/
	private final ConcurrentHashMap < AbstractSpimData<?>, BVBSpimDataInfo> spimDataToInfo;
	
	public SpimDataWrapper spimDataWrapper;

	/** data sources panel tree model **/
	public DataTreeModel dataTreeModel;
	
	/** hash map of loaded objects **/
	final public ObjectHashStorage objectHashStorage;
	
	/** XYZ axis rotation gizmo **/
	final public AxisOverlayRenderer axisOverlay = new AxisOverlayRenderer();
	
	/** multibox that includes shapes instead of default**/
	public MultiBoxOverlayRendererBVB multiBoxOverlayBVB;
	
	String BVVFrameTitle = "BigVolumeBrowser";
	
	final private ArrayList< Listener > listeners = new ArrayList<>();
	
	boolean bShowBGShader = BVBSettings.bShowRandomShader;
	
	final VisQuad bgQuad = new VisQuad((int)Math.ceil(Math.random()*4.0));
	
	public boolean bManualTransformMode;

	public static interface Listener 
	{
		public void bvbRestarted();
	}
	
	//SHAPES FOR NOW	
	//final public Queue<BasicShape> shapes = new ConcurrentLinkedQueue<>();
	public List<BasicShape> shapes = Collections.synchronizedList(new ArrayList<>());
	
	//DEBUG VISUALIZATION
	ArrayList<VisPolyLineAA> helpLines = new ArrayList<>();
	ArrayList<VolumeBox> helpBoxes = new ArrayList<>();
	
	public BigVolumeBrowser()
	{
		bvvSourceToSpimData = new ConcurrentHashMap<>();
		spimDataToBVVSourceList = new ConcurrentHashMap<>();
		spimDataToInfo = new ConcurrentHashMap<>();
		volumeBoxes = new VolumeBBoxes(this, false);
		volumeBoxes.setVisible( BVBSettings.bShowVolumeBoxes );
		clipBoxes = new VolumeBBoxes(this, true);
		objectHashStorage = new ObjectHashStorage();
		
	}
	
	/** starting as plugin from ImageJ/FIJI **/
	@Override
	public void run( String arg )
	{
		
		startBVB("");
		
	}
	
	public void startBVB(String BVVFrameTitle_)
	{
		//switch to FlatLaf theme		
		try {
			String sUIName = UIManager.getLookAndFeel().getDescription();
			//already a flatlaf, let's see if it is dark or not
			if(sUIName.toLowerCase().contains( "flatlaf" ))
			{
				//dark theme, let's change the colors
				if(sUIName.toLowerCase().contains( "dark" ) || sUIName.toLowerCase().contains( "darcula")) 
				{
					BVBSettings.sUITheme = "dark/";
				}
			}
			else
			{
				UIManager.setLookAndFeel( new FlatIntelliJLaf() );
			    FlatLaf.registerCustomDefaultsSource( "flatlaf" );
			    FlatIntelliJLaf.setup();
			}
			//System.out.println(sUIName);

		} catch( Exception ex ) {
		    System.err.println( "Failed to initialize LaF" );
		}
		if(!BVVFrameTitle_.equals( "" ))
		{
			this.BVVFrameTitle = BVVFrameTitle_;
		}
		
		dataTreeModel = new DataTreeModel();
		
		spimDataWrapper = new SpimDataWrapper(this);
		
		if(bvv == null)
		{
			setInputLock( true );
			
			initBVV();		
			
			//setup control panel
			bvbCards = new BVBCards(this);
			
			bvbCards.installCards();
			
			setInputLock( false );

			bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "No data loaded", 5000, TextPosition.CENTER, BVBSettings.canvasOverlayColor )  );

			if(bShowBGShader)
			{
				showNoise();
			}
			bvvFrame.getSplitPanel().setDividerLocation( 400 );
			bvvViewer.addTimePointListener(this);
		}
	}
	
	void initBVV()
	{
	
		//start empty bvv
		bvv = BvvFunctions.show( Bvv.options().
				dCam(BVVSettings.dCam).
				dClipNear(BVVSettings.dClipNear).
				dClipFar(BVVSettings.dClipFar).				
				renderWidth( BVVSettings.renderWidth).
				renderHeight( BVVSettings.renderHeight).
				numDitherSamples( BVVSettings.numDitherSamples ).
				cacheBlockSize( BVVSettings.cacheBlockSize ).
				maxCacheSizeInMB( BVVSettings.maxCacheSizeInMB ).
				ditherWidth(BVVSettings.ditherWidth).
				frameTitle(BVVFrameTitle)				
				);
		
		bvvHandle = ( BvvHandleFrame ) bvv.getBvvHandle();
		
		bvvViewer = bvvHandle.getViewerPanel();
		
		sceneBufTransparent = new OffScreenFrameBufferWithDepth( BVVSettings.renderWidth, BVVSettings.renderHeight, GL_RGBA8, false); 

		//get renderScene
		bvvViewer.setRenderScene(this::renderOpaque);
		bvvViewer.setRenderSceneTransparent(this::renderTransparent);

		bvvFrame = bvvHandle.getBigVolumeViewer().getViewerFrame();
		
		bvbActions = new BVBActions(this);
		setCanvasBGColor(BVBSettings.canvasBGColor);
		Prefs.showScaleBar( BVBSettings.bShowScaleBar);
		
		axisOverlay.bindViewer( bvvViewer );
		bvvViewer.getDisplay().overlays().add( axisOverlay );
		axisOverlay.setEnabled( BVBSettings.bShowAxisOverlay );
		
		Prefs.showMultibox( false );
		multiBoxOverlayBVB = new MultiBoxOverlayRendererBVB(this);
		bvvViewer.getDisplay().overlays().add( multiBoxOverlayBVB );
		multiBoxOverlayBVB.setEnabled( BVBSettings.bShowMultiBox );
		objectHashStorage.clear();
	}
	
	public void shutDownAll()
	{
		closeBVV();
	}
	
	void closeBVV()
	{
		bvvViewer.stop();
		bvvFrame.dispose();		
	}
	
	public synchronized void setInputLock(boolean bLockMode)
	{
		bInputLock = bLockMode;
	}
	
	public boolean getInputLock()
	{
		return bInputLock;
	}
	
	public void repaintBVV()
	{
		bvvViewer.requestRepaint();
	}
	
	public ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> loadBDVHDF5(String xmlFileName)
	{
		return loadFromDiskBDVorBF(xmlFileName, 0);	
	}
	

	public ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> loadBioFormats(String imageFileName)
	{

		return loadFromDiskBDVorBF(imageFileName, 1);
	}
	
	public ValuePair<AbstractSpimData<?>, List< BvvStackSource< ? > >> addSource(final Source<?> src)
	{		
		return addSource(src, src.getName(), dataTreeModel.getIconDataDefault());
	}

	public ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> addSource(final Source<?> src, final ImageIcon icon)
	{		
		return addSource(src, src.getName(), icon);
	}
	
	public ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> addSource(final Source<?> src, String sourceName, final ImageIcon icon)
	{
		final AbstractSpimData<?> spimData = SourceToSpimDataBvv.spimDataSourceWrap( src );
		final BVBSpimDataInfo info = new BVBSpimDataInfo(sourceName, icon);
		return addSpimData(spimData, info);
	}
	
	public ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> addRAI(final RandomAccessibleInterval<?> rai, String raiName, final ImageIcon icon)
	{
		final AbstractSpimData<?> spimData = RAIToSpimDataBvv.getSpimData( rai );
		final BVBSpimDataInfo info = new BVBSpimDataInfo(raiName, icon);
		final ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> out = addSpimData(spimData, info);
		
		if(rai.getType() instanceof UnsignedByteType)
		{
			for(BvvStackSource< ? > bvvSrc : out.getB())
			{
				bvvSrc.setDisplayRange( 0, 255 );
				bvvSrc.setDisplayRangeBounds( 0, 255 );
			}
		}
		return out;
	}
	
	public ValuePair<AbstractSpimData<?>, List< BvvStackSource< ? > >> addRAI(final RandomAccessibleInterval<?> rai)
	{
		String raiName = "RAI_"+Integer.toString(BVBSettings.nAddedRAINumber);
		BVBSettings.nAddedRAINumber++;
		return addRAI(rai, raiName, dataTreeModel.getIconDataDefault());
	}
	
	public ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> addImagePlus(final ImagePlus imp)
	{
		final ValuePair< AbstractSpimData< ? >, BVBSpimDataInfo > spimDataInfo = spimDataWrapper.createSpimDataImagePlus(imp);
		return addSpimData(spimDataInfo.getA(), spimDataInfo.getB());
	}
	
	public ValuePair<AbstractSpimData<?>, List< BvvStackSource< ? > >> addSpimData(final AbstractSpimData<?> spimData, final BVBSpimDataInfo info)
	{
		final ValuePair<AbstractSpimData<?>, List< BvvStackSource< ? > >> out = addSpimData(spimData);
		if(out != null)
		{
			spimDataToInfo.put( spimData, info );
			final DataTreeNode dataNode = dataTreeModel.addData( spimData, out.getB(), info);
			if(info.sourceSettings.size() != 0)
			{
				info.applySourceSettings( out.getB() );
			}
			objectHashStorage.addBVVSources( out.getB(), info );
			SelectLoadedSource selector = new SelectLoadedSource(this, dataNode, out.getB());
			selector.start();
			//this.bvbCards.panelData.selectDataNode( dataNode );
		}
		return out;
	}
	
	/** main method adding spimdata to BVV **/	
	ValuePair<AbstractSpimData<?>, List< BvvStackSource< ? > >> addSpimData(final AbstractSpimData<?> spimData)
	{
	
		if(spimData == null)
			return null;

		if(bvv == null)
		{
			startBVB(BVVFrameTitle);
		}
		//let's check it we have some timepoints already (if only shapes are loaded)
		final int nPrevTPN = bvvViewer.state().getNumTimepoints();
		//add data to BVV
		List< BvvStackSource< ? > > bvvSources = BvvFunctions.show(spimData, Bvv.options().addTo( bvv ));
		
		//see if we reset timepoint number
		if(bvvViewer.state().getNumTimepoints() < nPrevTPN)
		{
			bvvViewer.state().setNumTimepoints( nPrevTPN );
		}
		//check for display settings stored in spimdata
		@SuppressWarnings( "unchecked" )
		List<BasicViewSetup> views = ( List< BasicViewSetup > ) (spimData.getSequenceDescription().getViewSetupsOrdered());
		int nSetup = 0;
		for(BasicViewSetup view : views)
		{	
			boolean bLutSet = false;
			
			Map< String, Entity > attr = view.getAttributes();
			
			for (Map.Entry<String, Entity> entry : attr.entrySet()) 				
			{			
				if(entry.getKey().equals( "displaysettings"))
				{
					Displaysettings sett = ( Displaysettings ) entry.getValue();
					bvvSources.get( nSetup ).setDisplayRange( sett.min, sett.max );
					if(!bLutSet && views.size() > 1)
						bvvSources.get( nSetup ).setColor(new ARGBType(ARGBType.rgba( sett.color[0], sett.color[1], sett.color[2], 255 ) ));
				}
				//check if there is a FIJI lut name stored
				if(entry.getKey().equals( "lutnamefiji"))
				{
					LUTNameFIJI lutName = ( LUTNameFIJI ) entry.getValue();
					if(!lutName.sLUTName.equals( "" ))
					{
						bvvSources.get( nSetup ).setLUT( lutName.sLUTName );
						bLutSet = true;
					}
				}
			}
			nSetup ++;
		}
		
		spimDataToBVVSourceList.put( spimData, bvvSources );
		
		for (BvvStackSource< ? > bvvSource : bvvSources) 
		{
			bvvSourceToSpimData.put( bvvSource, spimData );
		}
		bShowBGShader = false;
		updateSceneRender();
		
		if( BVBSettings.bFocusOnSourcesOnLoad )
		{
			this.focusOnSources( bvvSources );			
		}

		return new ValuePair< >( spimData, bvvSources);
	}

	/** nType 0 - BDV, nType 1 - BioFormats/TIF **/
	ValuePair<AbstractSpimData<?>, List< BvvStackSource< ? > >> loadFromDiskBDVorBF(String sFilename, final int nType)
	{		
		final ValuePair< AbstractSpimData< ? >, BVBSpimDataInfo > spimDataInfo = 
				spimDataWrapper.createSpimDataBDVorBF(sFilename, nType);
		return addSpimData(spimDataInfo.getA(), spimDataInfo.getB());

	}

	
	public synchronized void addShape(final BasicShape shape)
	{
		final ArrayList<BasicShape> shapesadd = new ArrayList<>();
		shapesadd.add( shape );
		addShapes(shapesadd, shape.toString());
		
	}
	
	public synchronized void addShapes(final List<BasicShape> shapes_in, String shapeGroupName)
	{
		for(final BasicShape sh:shapes_in)
		{
			shapes.add( sh );
		}
		bvbCards.panelShapes.updateShapesTableUI();
		DataTreeNode shapeNode = dataTreeModel.addData( shapes_in, shapeGroupName, dataTreeModel.getIconGroupShape() );
		objectHashStorage.addShapes( shapes_in, shapeGroupName );
		updateSceneRender();
		
		bShowBGShader = false;
		
		if( BVBSettings.bFocusOnSourcesOnLoad )
		{						
			this.focusOnRealInterval( CenterZoomBVV.getIntervalFromObjectsList( this, shapes_in ) );
			this.bvbCards.panelData.selectDataNode( shapeNode );
		}
		
	}
	
//	public void removeShape(final BasicShape shape)
//	{
//		shapes.remove( shape );
//	}
	
	public void renderOpaque(final GL3 gl, final RenderData data)
	{
		//set canvas background color
		if(!bShowBGShader)
		{
			gl.glClearColor(BVBSettings.canvasBGColor.getRed()/255.0f, BVBSettings.canvasBGColor.getGreen()/255.0f, BVBSettings.canvasBGColor.getBlue()/255.0f, 1.0f);
		}
		else
		{
			gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);			
		}
		//clear buffer with color
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glDepthFunc( GL.GL_LESS);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
		//get viewport size and transform matrices 
		int [] screen_size = new int [] {(int)data.getScreenWidth(), (int) data.getScreenHeight()};
		final Matrix4f pvm = new Matrix4f( data.getPv() );
		final Matrix4f view = MatrixMath.affine( data.getRenderTransformWorldToScreen(), new Matrix4f() );
		final Matrix4f vm = MatrixMath.screen( data.getDCam(), screen_size[0], screen_size[1], new Matrix4f() ).mul( view );
		
		final int nTimePoint = bvvViewer.state().getCurrentTimepoint();
		
		//draw boxes around volume
//		volumeBoxes.draw( gl, pvm, vm, screen_size, nTimePoint, false );
		//draw clip boxes
	//	clipBoxes.draw( gl, pvm, vm, screen_size, nTimePoint, false );

		int shapeN = shapes.size();
		for(int i = 0; i < shapeN; i++)
		{
			final BasicShape sh = shapes.get( i );			
			if(!sh.isTransparent())
				sh.draw( gl, pvm, vm, screen_size, nTimePoint, false  );
		}

		//BG
		if(bShowBGShader)
		{
			bgQuad.drawQuad( gl );
		}
		//DEBUG
//		for(VisPolyLineAA line:helpLines)
//		{
//			line.draw( gl, pvm );
//		}
//		
//		for(VolumeBox box:helpBoxes)
//		{
//			box.draw( gl, pvm, vm, screen_size, nTimePoint );
//		}
		
//		System.out.println(gl.glGetString( GL.GL_VENDOR ));
//		System.out.println(gl.glGetString( GL.GL_RENDERER ));
	}
	
	public void renderTransparent(final GL3 gl, final RenderData data, final OffScreenFrameBufferWithDepth sceneVolBuffer)
	{
		
		
		//gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		//get viewport size and transform matrices 
		int [] screen_size = new int [] {(int)data.getScreenWidth(), (int) data.getScreenHeight()};
		final Matrix4f pvm = new Matrix4f( data.getPv() );
		final Matrix4f view = MatrixMath.affine( data.getRenderTransformWorldToScreen(), new Matrix4f() );
		final Matrix4f vm = MatrixMath.screen( data.getDCam(), screen_size[0], screen_size[1], new Matrix4f() ).mul( view );

		final int nTimePoint = bvvViewer.state().getCurrentTimepoint();

		//to be able to change point size in shader
		gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
		if(BVBSettings.bWeightedOIT)
		{
			sceneBufTransparent.bind( gl );
			gl.glDepthMask(true);
			sceneVolBuffer.drawQuadDepth( gl, true );
			gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE); // Additive RGB + alpha
			gl.glBlendEquation(GL.GL_FUNC_ADD);
		}
		int shapeN = shapes.size();
		//disable depth writing
		gl.glDepthMask(false);
		for(int i = 0; i < shapeN; i++)
		{
			final BasicShape sh = shapes.get( i );			
			if(sh.isTransparent())
				sh.draw( gl, pvm, vm, screen_size, nTimePoint, BVBSettings.bWeightedOIT  );
		}
		//draw boxes around volume
		volumeBoxes.draw( gl, pvm, vm, screen_size, nTimePoint, BVBSettings.bWeightedOIT );
		//draw clip boxes
		clipBoxes.draw( gl, pvm, vm, screen_size, nTimePoint, BVBSettings.bWeightedOIT );
		
		gl.glDepthMask(true);
		if(BVBSettings.bWeightedOIT)
		{
			sceneBufTransparent.unbind( gl,false );
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			sceneBufTransparent.drawQuadAlpha( gl );
		}
	}
	
	public void showVolumeBoxes(boolean bShow)
	{
		if(bShow)
		{
			volumeBoxes.updateVolumeBoxes();
		}
		volumeBoxes.setVisible( bShow );
		ij.Prefs.set("BVB.bShowVolumeBoxes", bShow);
		
		repaintBVV();
	}
	
	
	public void updateSceneRender()
	{
		volumeBoxes.updateVolumeBoxes();
		clipBoxes.updateClipBoxes();
		
		bvvViewer.requestRepaint();
	}
	
	@Override
	public void timePointChanged( int timePointIndex )
	{
		updateSceneRender();
		
		//in case the scale changed (for BDV XML)
		if(bvbCards != null)
		{
			if(bvbCards.transformPanel.transformSetups != null)
				bvbCards.transformPanel.updateGUI();
		}
	}
	
	public void settingsDialogBVV()
	{
		this.bvbActions.runSettingsCommand();
	}
	
	public void focusOnSources(final List< BvvStackSource< ? > > bvvSources)
	{
		final ArrayList<SourceAndConverter< ? >> sacList = Misc.bvvSourcesToSaCList(bvvSources);
		
		final RealInterval interval = CenterZoomBVV.getIntervalFromSourcesList(this,sacList);

		focusOnRealInterval(interval);
	}
	
	public void focusOnRealInterval(RealInterval interval)
	{
		if(interval != null)
		{
			if(Misc.checkInterval(interval))
			{
				CenterZoomBVV.focusAnimateOnInterval(this, interval, BVBSettings.dFocusScreenFraction);
			}
		}
	}
	
	/** restarts BVV. Main purpose is to update rendering parameters. **/
	public void restartBVV()
	{
		
		//save Bvv window location and view
		final BVVWindowState bvvWindowState = new BVVWindowState(this);
		bvvWindowState.saveBvvWindowState();
		
		//gather all the spimdata
		ArrayList<AbstractSpimData<?>> spimDataAll = Collections.list( spimDataToBVVSourceList.keys() );

		//save settings
		updateSpimDataInfo();
		
		final ArrayList<BVBShapeCollectionInfo> shapesInfo = BVBShapeCollectionInfo.assembleCurrentShapes(this);
		shapes.clear();
		
		boolean focusStore = BVBSettings.bFocusOnSourcesOnLoad;
		BVBSettings.bFocusOnSourcesOnLoad = false;

		//now restart	
		closeBVV();
		clipBoxes.setVisible( false );
		volumeBoxes.setVisible( false );

	    dataTreeModel.clearAllSources();
	    bvvSourceToSpimData.clear();
	    spimDataToBVVSourceList.clear();

        initBVV();
        clipBoxes.reload();
        volumeBoxes.reload();
        bgQuad.reload();
		clipBoxes.setVisible( true );
		volumeBoxes.setVisible( true);
		
		bvbCards.setupListeners();

		bvbCards.resetClipTransformPanels();
	    bvbCards.installCards();

		//sync GUI	
		this.selectedObjects = new SelectedObjects(this);
		
		BVBSettings.bFocusOnSourcesOnLoad = focusStore;		

		//put back spimdata
		for(AbstractSpimData<?> spimData:spimDataAll)
		{
			addSpimData(spimData, spimDataToInfo.get( spimData ));
		}
		
		//put back shapes
		for(final BVBShapeCollectionInfo shInf: shapesInfo)
		{
			//reload shapes
			for(BasicShape shape : shInf.shapes)
			{
				shape.reload();
			}
			//add back
			this.addShapes( shInf.shapes, shInf.collectionDescription );
			
		}
		//restore window location
		bvvWindowState.restoreBvvWindowState();
		bvvViewer.addTimePointListener(this);
		
		//notify listener that BVB finished restarting
		for(Listener l : listeners)
			l.bvbRestarted();
	}
	
	void updateSpimDataInfo()
	{		
		for (Map.Entry<AbstractSpimData<?>, BVBSpimDataInfo> spimdataE : spimDataToInfo.entrySet()) 
		{
			List< BvvStackSource< ? > > bvvSList = spimDataToBVVSourceList.get( spimdataE.getKey() );
			spimdataE.getValue().storeSourceSettings( bvvSList );
		}
	}
	
	public void setCanvasBGColor(final Color bgColor)
	{
		BVBSettings.canvasBGColor = new Color(bgColor.getRed(),bgColor.getGreen(),bgColor.getBlue(),bgColor.getAlpha());		
		ij.Prefs.set("BVB.canvasBGColor", bgColor.getRGB());
		final Color bbFrameColor = BVBSettings.getInvertedColor(bgColor);
		BVBSettings.canvasOverlayColor = new Color(bbFrameColor.getRed(),bbFrameColor.getGreen(),bbFrameColor.getBlue(),bbFrameColor.getAlpha());		
		ij.Prefs.set("BVB.canvasOverlayColor", bbFrameColor.getRGB());
		volumeBoxes.setLineColor( bbFrameColor );
		clipBoxes.setLineColor( bbFrameColor );
	}
	
	public List<BvvStackSource<?> > getBVVSourcesList(final AbstractSpimData<?> spimData)
	{
		return spimDataToBVVSourceList.get( spimData );
	}
	
	public void addBVBListener(Listener l) 
	{
        listeners.add(l);
    }
	
	void showNoise()
	{
		new Thread(() -> {
		    while(bShowBGShader)
		    {
		    	try
				{
					Thread.sleep(25);
				}
				catch ( InterruptedException exc )
				{
					exc.printStackTrace();
				}
		    	repaintBVV();
		    }
		}).start();
	}
	
	public static void main(String... args) throws Exception
	{	
		
		new ImageJ();

		//ij.command().run(ConfigureBVVRenderWindow.class,true).get();
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 
		
		testBVB.startBVB("");
		//testBVB.run("");
		
		
		//testBVB.loadBioFormats( "/home/eugene/Desktop/projects/BVB/HyperStack_cliptest.tif" );
		//testBVB.loadBioFormats( "/home/eugene/Desktop/projects/BigTrace/BT_time_Oane/tracefile.tif" );
		
		//transform test
//		ValuePair< AbstractSpimData< ? >, List< BvvStackSource< ? > > > in = testBVB.loadBioFormats( "/home/eugene/Desktop/projects/BVB/HyperStack_test.tif" );
//		BvvStackSource< ? > sourceBVV = in.getB().get( 0 );
//		Source< ? > src = sourceBVV.getSources().get( 0 ).getSpimSource();
//		
//		AffineTransform3D testT = new AffineTransform3D();
//		testT.setTranslation( 50.,50.,0. );
//		testT.scale( 0.1, 0.2, 0.3 );
//		(( TransformedSource< ? > )src).setFixedTransform( testT );
		
		
		
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube_2ch.xml" );

		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/ExM_MT.xml" );
		//testBVB.loadBioFormats( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/ExM_MT.tif" );

		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/2_channels.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/HyperStack.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/trace1514947168.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/cliptest.xml" );
	}

}
