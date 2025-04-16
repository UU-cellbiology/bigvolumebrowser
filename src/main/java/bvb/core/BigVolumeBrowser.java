package bvb.core;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
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

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.ValuePair;

import org.joml.Matrix4f;

import bdv.viewer.Source;
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
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvHandleFrame;
import bvvpg.vistools.BvvStackSource;
import bvb.gui.SelectedSources;
import bvb.gui.VolumeBBoxes;
import bvb.gui.data.BVBSpimDataInfo;
import bvb.gui.data.DataTreeModel;
import bvb.io.ImagePlusToSpimDataBVV;
import bvb.io.LUTNameFIJI;
import bvb.io.SourceToSpimDataWrapperBvv;
import bvb.io.SpimDataLoader;
import bvb.scene.VisPolyLineAA;
import bvb.shapes.VolumeBox;
import bvb.utils.Misc;


public class BigVolumeBrowser  implements PlugIn, TimePointListener
{
	/** Bvv instance **/
	public Bvv bvv = null;
	
	/** Panel of BigVolumeViewer **/
	public VolumeViewerPanel bvvViewer;

	/** Frame of BigVolumeViewer **/
	public VolumeViewerFrame bvvFrame;
	
	public BvvHandleFrame bvvHandle;
	
	/** control panel **/
	public BVBControlPanel controlPanel;
	
	/** actions and behaviors **/
	public BVBActions bvbActions;
	
	/** boxes around volume **/	
	public final VolumeBBoxes volumeBoxes;
	
	/** boxes around volume **/	
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
	
	//DEBUG VISUALIZATION
	public ArrayList<VisPolyLineAA> helpLines = new ArrayList<>();
	public ArrayList<VolumeBox> helpBoxes = new ArrayList<>();
	
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
		
		startBVB();
		
	}
	
	public void startBVB()
	{
		//switch to FlatLaf theme		
		try {
		    UIManager.setLookAndFeel( new FlatIntelliJLaf() );
		    FlatLaf.registerCustomDefaultsSource( "flatlaf" );
		    FlatIntelliJLaf.setup();
		} catch( Exception ex ) {
		    System.err.println( "Failed to initialize LaF" );
		}
		
		if(bvv == null)
		{
			bLocked = true;
			
			initBVV();
			
			//setup control panel
			controlPanel = new BVBControlPanel(this);
			controlPanel.cpFrame = new JFrame("BVB Control Panel");
			controlPanel.cpFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			controlPanel.cpFrame.add(controlPanel);
			
	        //Display the window.
			controlPanel.cpFrame.setSize(BVBSettings.nDefaultWidthControlPanel, BVBSettings.nDefaultHeightControlPanel);
			controlPanel.cpFrame.setVisible(true);
		    java.awt.Point bvv_p = bvvFrame.getLocationOnScreen();
		    java.awt.Dimension bvv_d = bvvFrame.getSize();
		
		    controlPanel.cpFrame.setLocation(bvv_p.x + bvv_d.width, bvv_p.y);
			
			controlPanel.cpFrame.addWindowListener( closeWA );
		    bvvFrame.addWindowListener(	closeWA );
		    
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
				frameTitle("BigVolumeBrowser")
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
		
		bvvFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		bvbActions = new BVBActions(this);

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
		final AbstractSpimData<?> spimData = SourceToSpimDataWrapperBvv.spimDataSourceWrap( src );
		return addSpimData(spimData);
	}
	
	public ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> addImagePlus(final ImagePlus imp)
	{
		final AbstractSpimData<?> spimData = ImagePlusToSpimDataBVV.getSpimData( imp );
		final ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> out = addSpimData(spimData);
		final BVBSpimDataInfo info = new BVBSpimDataInfo(imp.getTitle(),dataTreeModel.getFIJIIcon());
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
		}
		return out;
	}
	
	ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> addSpimData(final AbstractSpimData<?> spimData)
	{
	
		if(spimData == null)
			return null;

		if(bvv == null)
		{
			startBVB();
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
			spimDataIcon = dataTreeModel.getBDVIcon();
		}
		else
		{
			spimData = SpimDataLoader.loadBioFormats( sFilename );
			spimDataIcon = dataTreeModel.getBioformatsIcon();
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
		
		for(VisPolyLineAA line:helpLines)
		{
			line.draw( gl, pvm );
		}
		for(VolumeBox box:helpBoxes)
			box.draw( gl, pvm, vm, screen_size );
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
			final ValuePair<AbstractSpimData<?>,List< BvvStackSource< ? > >> out = addSpimData(spimData);
			dataTreeModel.addData( out.getA(), out.getB(), spimDataToInfo.get( spimData ));			
		}
		//sync GUI		
		controlPanel.tabPanelDataSources.updateBVVlisteners();
		controlPanel.tabPanelView.resetClipPanel();
		
		//put back viewer transform
		bvvViewer.state().setViewerTransform( viewTransform );

	}
	
	public static void main(String... args) throws Exception
	{
		
		new ImageJ();
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 
		
		testBVB.run("");
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube_2ch.xml" );

		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/ExM_MT.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/2_channels.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/HyperStack.xml" );
		testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/trace1514947168.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/cliptest.xml" );
	}

}
