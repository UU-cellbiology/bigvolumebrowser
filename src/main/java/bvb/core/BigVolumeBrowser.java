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

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.imglib2.FinalRealInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.ValuePair;


import org.joml.Matrix4f;

import bdv.tools.transformation.TransformedSource;
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
import bvvpg.core.render.RenderData;
import bvvpg.core.util.MatrixMath;
import bvvpg.pgcards.BVVPGDefaultCards;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvHandleFrame;
import bvvpg.vistools.BvvStackSource;
import bvb.gui.CenterZoomBVV;
import bvb.gui.SelectedSources;
import bvb.gui.VolumeBBoxes;
import bvb.gui.data.BVBSpimDataInfo;
import bvb.gui.data.DataTreeModel;
import bvb.io.ImagePlusToSpimDataBvv;
import bvb.io.LUTNameFIJI;
import bvb.io.RAIToSpimDataBvv;
import bvb.io.SourceToSpimDataBvv;
import bvb.io.SpimDataLoader;
import bvb.scene.VisPolyLineAA;
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
	
	/** control panel **/
	public BVBControlPanel controlPanel = null;
	
	/** actions and behaviors **/
	public BVBActions bvbActions;
	
	/** boxes around volume **/	
	final VolumeBBoxes volumeBoxes;
	
	/** clipping boxes **/	
	public final VolumeBBoxes clipBoxes;

	/** flag to lock BVB while it is busy **/
	public boolean bLocked;
	
	/** currently selected source + listener for update **/
	public SelectedSources selectedSources;
	
	/** maps bvv sources to the input data **/
	private final ConcurrentHashMap < BvvStackSource<?>, AbstractSpimData<?> > bvvSourceToSpimData;
	
	/** maps input data to bvv sources **/
	private final ConcurrentHashMap < AbstractSpimData<?>, List<BvvStackSource<?> >> spimDataToBVVSourceList;

	/** info about input data (icon, description) **/
	private final ConcurrentHashMap < AbstractSpimData<?>, BVBSpimDataInfo> spimDataToInfo;

	/** data sources panel tree model **/
	public DataTreeModel dataTreeModel = new DataTreeModel();
	
	final WindowAdapter closeWA;
	
	String BVVFrameTitle = "BigVolumeBrowser";
	
	final private ArrayList<Listener> listeners =	new ArrayList<>();

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
		volumeBoxes = new VolumeBBoxes(this);
		volumeBoxes.setVisible( BVBSettings.bShowVolumeBoxes );
		clipBoxes = new VolumeBBoxes(this);
		
	    //sync BVV and Control Panel window closing
	    closeWA = new WindowAdapter()
		{
			@Override
			public void windowClosing( WindowEvent ev )
			{
				shutDownAll();
			}
		};
		
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
		    UIManager.setLookAndFeel( new FlatIntelliJLaf() );
		    FlatLaf.registerCustomDefaultsSource( "flatlaf" );
		    FlatIntelliJLaf.setup();
		} catch( Exception ex ) {
		    System.err.println( "Failed to initialize LaF" );
		}
		if(!BVVFrameTitle_.equals( "" ))
		{
			this.BVVFrameTitle = BVVFrameTitle_;
		}
		
		if(bvv == null)
		{
			bLocked = true;
			
			initBVV();
			
			
			//setup control panel
			controlPanel = new BVBControlPanel(this);
//			controlPanel.cpFrame = new JFrame("BVB");
//			controlPanel.cpFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//			controlPanel.cpFrame.add(controlPanel);
//			
//	        //Display the window.
//			controlPanel.cpFrame.setSize(BVBSettings.nDefaultWidthControlPanel, BVBSettings.nDefaultHeightControlPanel);
//			controlPanel.cpFrame.setVisible(true);
//		    java.awt.Point bvv_p = bvvFrame.getLocationOnScreen();
//		    java.awt.Dimension bvv_d = bvvFrame.getSize();
//		
//		    controlPanel.cpFrame.setLocation(bvv_p.x + bvv_d.width, bvv_p.y);
//			
//			controlPanel.cpFrame.addWindowListener( closeWA );
//			
//
//		    bvvFrame.addWindowListener(	closeWA );

			final Dimension tableViewPrefSize = new Dimension( 340, 285 );
			//bvvFrame.getSplitPanel().setCollapsed( false );
		    bvvFrame.getCardPanel().removeCard( BVVPGDefaultCards.DEFAULT_VIEWERMODES_CARD );
		    bvvFrame.getCardPanel().removeCard( BVVPGDefaultCards.DEFAULT_SOURCEGROUPS_CARD );
		    bvvFrame.getCardPanel().setCardExpanded( BVVPGDefaultCards.DEFAULT_SOURCES_CARD, false );

		    controlPanel.tabPanelShapes.panelShapes.setPreferredSize( tableViewPrefSize );
		    controlPanel.tabPanelDataSources.panelData.setPreferredSize( tableViewPrefSize );
		    bvvFrame.getCardPanel().addCard("Shapes", controlPanel.tabPanelShapes.panelShapes, false, new Insets( 0, 0, 0, 0 ) );
		    bvvFrame.getCardPanel().addCard("All objects", controlPanel.tabPanelDataSources.panelData, true, new Insets( 0, 0, 0, 0 ) );
		    bvvFrame.getCardPanel().addCard("Render sources", controlPanel.tabPanelView.sourcesRenderPanel, false, new Insets( 0, 0, 0, 0 ) );
		    bvvFrame.getCardPanel().addCard("View", controlPanel.tabPanelView.viewPanel, false, new Insets( 0, 0, 0, 0 ) );
		    bvvFrame.getCardPanel().addCard("Clipping", controlPanel.tabPanelView.clipPanel, false, new Insets( 0, 0, 0, 0 ) );
		    bvvFrame.getCardPanel().addCard("Transform", controlPanel.tabPanelView.transformPanel, false, new Insets( 0, 0, 0, 0 ) );		   
		    bvvFrame.getCardPanel().addCard("Add volumes", controlPanel.tabPanelDataSources.panelAddSources, true, new Insets( 0, 0, 0, 0 ) );		   
		    bvvFrame.getCardPanel().addCard("Add shapes", controlPanel.tabPanelShapes.panelAddShapes, true, new Insets( 0, 0, 0, 0 ) );		   

		    //		    controlPanel.tabPanelView.viewPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
//		    controlPanel.tabPanelView.viewPanel.butFullScreen.setVisible( false );
//		    controlPanel.tabPanelView.sourcesRenderPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
//		    controlPanel.tabPanelView.clipPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
//		    bvvFrame.getCardPanel().setCardExpanded( BVVPGDefaultCards.DEFAULT_SOURCEGROUPS_CARD, false );
//		    bvvFrame.getCardPanel().addCard( "View", controlPanel.tabPanelView.viewPanel, false);
//		    bvvFrame.getCardPanel().addCard( "Sources render", controlPanel.tabPanelView.sourcesRenderPanel, false);
//		    bvvFrame.getCardPanel().addCard( "Clipping", controlPanel.tabPanelView.clipPanel, true );
		    bvvFrame.getSplitPanel().setCollapsed( false );
		    bvvHandle.getConverterSetups().listeners().add( s -> clipBoxes.updateClipBoxes() );
			bLocked = false;
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
		
		//get renderScene
		bvvViewer.setRenderScene(this::renderScene);
		
		//listen to timepoint change
		bvvViewer.addTimePointListener(this);
		
		//bind updater on selected sources
		selectedSources = new SelectedSources(bvvViewer);
		
		bvvFrame = bvvHandle.getBigVolumeViewer().getViewerFrame();
		
		//bvvFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		bvbActions = new BVBActions(this);
		setCanvasBGColor(BVBSettings.canvasBGColor);
		Prefs.showMultibox( BVBSettings.bShowMultiBox);
		Prefs.showScaleBar( BVBSettings.bShowScaleBar);

	}
	
	public void shutDownAll()
	{
		closeBVV();
		controlPanel.cpFrame.dispose();
	}
	
	void closeBVV()
	{
		bvvViewer.stop();
		bvvFrame.dispose();		
	}
	
	/** switches to full screen mode **/
	public void makeFullScreen()
	{
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		int nCPWidth = controlPanel.cpFrame.getWidth();
				
		bvvFrame.getContentPane().setPreferredSize(  new Dimension( width - nCPWidth, height ) ) ;	
		//bvvFrame.setPreferredSize(new Dimension( width - nCPWidth, height ));
		bvvFrame.pack();
		bvvFrame.setLocation( 0, 0 );
		
		controlPanel.cpFrame.setSize( nCPWidth, height );
		controlPanel.cpFrame.setLocation(width - nCPWidth, 0);	
		
		if(bvvFrame.getSplitPanel() != null)
		{
			//bvvFrame.getSplitPanel().setDividerLocation( -0.1 );
			bvvFrame.getSplitPanel().setCollapsed( false );
		}
	
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
	
	public ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> addSource(final Source<?> src)
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
		final ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> out = addSpimData(spimData);
		final BVBSpimDataInfo info = new BVBSpimDataInfo(sourceName, icon);
		spimDataToInfo.put( spimData, info );
		dataTreeModel.addData( spimData, out.getB(), info);
		return out;
	}
	
	public ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> addRAI(final RandomAccessibleInterval<?> rai, String raiName, final ImageIcon icon)
	{
		final AbstractSpimData<?> spimData = RAIToSpimDataBvv.getSpimData( rai );
		final ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> out = addSpimData(spimData);
		final BVBSpimDataInfo info = new BVBSpimDataInfo(raiName, icon);
		spimDataToInfo.put( spimData, info );
		dataTreeModel.addData( spimData, out.getB(), info);
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
	
	public ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> addRAI(final RandomAccessibleInterval<?> rai)
	{
		String raiName = "RAI_"+Integer.toString(BVBSettings.nAddedRAINumber);
		BVBSettings.nAddedRAINumber++;
		return addRAI(rai, raiName, dataTreeModel.getIconDataDefault());
	}
	
	public ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> addImagePlus(final ImagePlus imp)
	{
		final AbstractSpimData<?> spimData = ImagePlusToSpimDataBvv.getSpimData( imp );
		final ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> out = addSpimData(spimData);
		final BVBSpimDataInfo info = new BVBSpimDataInfo(imp.getTitle(), dataTreeModel.getIconFIJI());
		spimDataToInfo.put( spimData, info );
		dataTreeModel.addData( spimData, out.getB(), info);

		return out;
	}

	
	public ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> addSpimData(final AbstractSpimData<?> spimData, final BVBSpimDataInfo info)
	{
		final ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> out = addSpimData(spimData);
		if(out != null)
		{
			spimDataToInfo.put( spimData, info );
			dataTreeModel.addData( spimData, out.getB(), info);
			if(info.sourceSettings.size()!=0)
			{
				info.applySourceSettings( out.getB() );
			}
		}
		return out;
	}
	
	ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> addSpimData(final AbstractSpimData<?> spimData)
	{
	
		if(spimData == null)
			return null;

		if(bvv == null)
		{
			startBVB(BVVFrameTitle);
		}
		
		List< BvvStackSource< ? > > bvvSources = BvvFunctions.show(spimData, Bvv.options().addTo( bvv ));

		
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
					if(!bLutSet)
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
		
		updateSceneRender();
		
		if(BVBSettings.bFocusOnSourcesOnLoad)
		{
			this.focusOnSources( bvvSources );
		}

		return new ValuePair< >( spimData, bvvSources);
	}

	/** nType 0 - BDV, nType 1 - BioFormats/TIF **/
	ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> loadFromDiskBDVorBF(String sFilename, int nType)
	{
		AbstractSpimData<?> spimData;
		final ImageIcon spimDataIcon;
		if(nType == 0 )
		{
			spimData = SpimDataLoader.loadHDF5( sFilename );
			spimDataIcon = dataTreeModel.getIconBDV();
		}
		else
		{
			spimData = SpimDataLoader.loadBioFormats( sFilename );
			spimDataIcon = dataTreeModel.getIconBioformats();
		}
		final ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> out = addSpimData(spimData);
		
		if(out != null)
		{
			final BVBSpimDataInfo info = new BVBSpimDataInfo(Misc.getSourceStyleName(sFilename),spimDataIcon);
			spimDataToInfo.put( spimData, info );
			dataTreeModel.addData( spimData, out.getB(), info);
		}
		return out;
	}
	
	public void renderScene(final GL3 gl, final RenderData data)
	{
		//set canvas background color
		gl.glClearColor(BVBSettings.canvasBGColor.getRed()/255.0f, BVBSettings.canvasBGColor.getGreen()/255.0f, BVBSettings.canvasBGColor.getBlue()/255.0f, 0.0f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		
		//get viewport size and transform matrices 
		int [] screen_size = new int [] {(int)data.getScreenWidth(), (int) data.getScreenHeight()};
		final Matrix4f pvm = new Matrix4f( data.getPv() );
		final Matrix4f view = MatrixMath.affine( data.getRenderTransformWorldToScreen(), new Matrix4f() );
		final Matrix4f vm = MatrixMath.screen( data.getDCam(), screen_size[0], screen_size[1], new Matrix4f() ).mul( view );
		
		//draw boxes around volume
		volumeBoxes.draw( gl, pvm, vm, screen_size );
		//draw clip boxes
		clipBoxes.draw( gl, pvm, vm, screen_size );

		//to be able to change point size in shader
		gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
		int shapeN = shapes.size();
		for(int i=0; i<shapeN; i++)
		{
			shapes.get( i ).draw( gl, pvm, vm, screen_size );
		}

		//DEBUG
		for(VisPolyLineAA line:helpLines)
		{
			line.draw( gl, pvm );
		}
		
		for(VolumeBox box:helpBoxes)
		{
			box.draw( gl, pvm, vm, screen_size );
		}
	}
	
	public void showVolumeBoxes(boolean bShow)
	{
		volumeBoxes.setVisible( bShow );
		ij.Prefs.set("BVB.bShowVolumeBoxes", bShow);
		repaintBVV();
	}
	
	
	public synchronized void addShape(final BasicShape shape)
	{
		shapes.add( shape );
		controlPanel.tabPanelShapes.panelShapes.updateShapesTableUI();
	}
	
	public void removeShape(final BasicShape shape)
	{
		shapes.remove( shape );
	}
	
	public void updateSceneRender()
	{
		volumeBoxes.updateVolumeBoxes();
		
		bvvViewer.requestRepaint();
	}
	
	@Override
	public void timePointChanged( int timePointIndex )
	{
		updateSceneRender();
		if(controlPanel != null)
			controlPanel.tabPanelView.transformPanel.updateGUI();
	}
	
	public void settingsDialogBVV()
	{
		this.bvbActions.runSettingsCommand();
	}
	
	public void focusOnSources(List< BvvStackSource< ? > > bvvSources)
	{
		final ArrayList<SourceAndConverter< ? >> sacList = new ArrayList<>();
		for (BvvStackSource< ? > bvvS : bvvSources)
		{
			for(SourceAndConverter< ? > sac : bvvS.getSources())
			{
				sacList.add( sac );
			}
		}
		
		final FinalRealInterval interval = CenterZoomBVV.getIntervalFromSourcesList(this,sacList);

		focusOnRealInterval(interval);
	}
	
	public void focusOnRealInterval(RealInterval interval)
	{
		if(interval != null)
		{
			CenterZoomBVV.focusAnimateOnInterval(this, interval, 0.95);
		}
	}
	
	/** restarts BVV. Main purpose is to update rendering parameters. **/
	public void restartBVV()
	{
		
		//gather all the spimdata
		ArrayList<AbstractSpimData<?>> spimDataAll = Collections.list( spimDataToBVVSourceList.keys() );
		//save window position and size on the screen
	    final java.awt.Point bvv_p = bvvFrame.getLocation();
	    final java.awt.Dimension bvv_d = bvvFrame.getContentPane().getSize();
	    //final java.awt.Point bvv_pf= bvvFrame.getRootPane().getLocationOnScreen();
		
	    //let's save viewer transform
		AffineTransform3D viewTransform = bvvViewer.state().getViewerTransform();
		
		//save settings
		updateSpimDataInfo();
		
		//save shapes
		final ArrayList<BasicShape> tempShapes = new ArrayList<>();
		for(BasicShape shape : shapes)
		{
			tempShapes.add( shape );
		}
		shapes.clear();
		
		boolean focusStore = BVBSettings.bFocusOnSourcesOnLoad;
		BVBSettings.bFocusOnSourcesOnLoad = false;

		//now restart	
		closeBVV();

        initBVV();
		
	    bvvFrame.addWindowListener(	closeWA );
	    
	    dataTreeModel.clearAllSources();
	    bvvSourceToSpimData.clear();
	    spimDataToBVVSourceList.clear();
	    
	    bvvHandle.getConverterSetups().listeners().add( s -> clipBoxes.updateClipBoxes() );
		
		//restore window position		
		bvvFrame.setLocation( bvv_p );	
		bvvFrame.getContentPane().setPreferredSize( bvv_d );	
		bvvFrame.pack();
		
		//put back spimdata
		for(AbstractSpimData<?> spimData:spimDataAll)
		{
			addSpimData(spimData, spimDataToInfo.get( spimData ));
		}
		
		//sync GUI		
		controlPanel.tabPanelDataSources.updateBVVlisteners();
		controlPanel.tabPanelView.resetClipPanel();
		
		BVBSettings.bFocusOnSourcesOnLoad = focusStore;
		
		//put back viewer transform
		bvvViewer.state().setViewerTransform( viewTransform );
		
		//reload shapes
		for(BasicShape shape : tempShapes)
		{
			shape.reload();
		}
		//add back
		for(BasicShape shape : tempShapes)
		{
			shapes.add( shape );
		}
		
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
		volumeBoxes.setLineColor( bbFrameColor );
		clipBoxes.setLineColor( bbFrameColor.darker() );
	}
	
	public List<BvvStackSource<?> > getBVVSourcesList(final AbstractSpimData<?> spimData)
	{
		return spimDataToBVVSourceList.get( spimData );
	}
	
	public void addBVBListener(Listener l) 
	{
        listeners.add(l);
    }
	
	public static void main(String... args) throws Exception
	{
		
		
		new ImageJ();

		//ij.command().run(ConfigureBVVRenderWindow.class,true).get();
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 
		
		testBVB.startBVB("");
		//testBVB.run("");
		ValuePair< AbstractSpimData< ? >, List< BvvStackSource< ? > > > in = testBVB.loadBioFormats( "/home/eugene/Desktop/projects/BVB/HyperStack_test.tif" );
		BvvStackSource< ? > sourceBVV = in.getB().get( 0 );
		Source< ? > src = sourceBVV.getSources().get( 0 ).getSpimSource();
		
		AffineTransform3D testT = new AffineTransform3D();
		testT.setTranslation( 50.,50.,0. );
		testT.scale( 0.1, 0.2, 0.3 );
		(( TransformedSource< ? > )src).setFixedTransform( testT );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube_2ch.xml" );

		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/ExM_MT.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/2_channels.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/HyperStack.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/trace1514947168.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/cliptest.xml" );
	}

}
