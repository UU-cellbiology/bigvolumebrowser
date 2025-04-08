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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.imglib2.FinalRealInterval;

import org.joml.Matrix4f;

import bdv.viewer.TimePointListener;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.SpimDataException;

import ij.ImageJ;
import ij.plugin.PlugIn;

import bvvpg.core.VolumeViewerFrame;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.core.render.RenderData;
import bvvpg.core.util.MatrixMath;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvHandleFrame;
import bvvpg.vistools.BvvStackSource;
import bvb.gui.VolumeBBoxes;
import bvb.io.BDVHDF5Loader;


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

	
	@SuppressWarnings( "rawtypes" )
	private final ConcurrentHashMap < BvvStackSource<?>, AbstractSpimData > bvvSourceToSpimData;
	
	@SuppressWarnings( "rawtypes" )
	private final ConcurrentHashMap < AbstractSpimData, List<BvvStackSource<?> >> spimDataTobvvSourceList;
	
	public BigVolumeBrowser()
	{
		bvvSourceToSpimData = new ConcurrentHashMap<>();
		spimDataTobvvSourceList = new ConcurrentHashMap<>();
		volumeBoxes = new VolumeBBoxes(this);
		volumeBoxes.setVisible( BVBSettings.bShowVolumeBoxes );
		clipBoxes = new VolumeBBoxes(this);
		
	}
	/** starting as plugin from ImageJ/FIJI **/
	@Override
	public void run( String arg )
	{
		
		startBVB();
		bvbActions = new BVBActions(this);
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
			
			bvvFrame = bvvHandle.getBigVolumeViewer().getViewerFrame();
			
			bvvFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			
			
			
			//setup control panel
			controlPanel = new BVBControlPanel(this);
			controlPanel.cpFrame = new JFrame("BVB Control Panel");
			controlPanel.cpFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			controlPanel.cpFrame.add(controlPanel);
			
	        //Display the window.
			controlPanel.cpFrame.setSize(controlPanel.nDefaultWidth, controlPanel.nDefaultHeight);
			controlPanel.cpFrame.setVisible(true);
		    java.awt.Point bvv_p = bvvFrame.getLocationOnScreen();
		    java.awt.Dimension bvv_d = bvvFrame.getSize();
		
		    controlPanel.cpFrame.setLocation(bvv_p.x + bvv_d.width, bvv_p.y);
		    
		    //sync closing
		    final WindowAdapter closeWA = new WindowAdapter()
			{
				@Override
				public void windowClosing( WindowEvent ev )
				{
					closeWindows();
				}
			};
			
			controlPanel.cpFrame.addWindowListener( closeWA );
		    bvvFrame.addWindowListener(	closeWA );
		    
		    bvvHandle.getConverterSetups().listeners().add( s -> clipBoxes.updateClipBoxes() );
		}
	}
	
	public void closeWindows()
	{
		bvvViewer.stop();
		bvvFrame.dispose();		
		controlPanel.cpFrame.dispose();
	}
	
	/** switches to full screen mode **/
	public void makeFullScreen()
	{
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		int nCPWidth = controlPanel.cpFrame.getWidth();
				
		bvvFrame.getContentPane().setPreferredSize(  new Dimension( width - nCPWidth, height ) ) ;	
		bvvFrame.setSize(width - nCPWidth, height  );
		bvvFrame.setLocation( 0, 0 );
		
		controlPanel.cpFrame.setSize( nCPWidth, height );
		controlPanel.cpFrame.setLocation(width - nCPWidth, 0);	
		
		if(bvvFrame.getSplitPanel() != null)
		{
			bvvFrame.getSplitPanel().setCollapsed( false );
		}

	
	}
	
	public void repaintBVV()
	{
		bvvViewer.requestRepaint();
	}
	
	@SuppressWarnings( "rawtypes" )
	public void loadBDVHDF5(String xmlFileName)
	{
		AbstractSpimData spimData;
		try
		{
			spimData = BDVHDF5Loader.loadHDF5( xmlFileName );
			if(bvv == null)
			{
				startBVB();
			}
			List< BvvStackSource< ? > > sourcesSPIM = BvvFunctions.show(spimData, Bvv.options().addTo( bvv ));
			
			spimDataTobvvSourceList.put( spimData, sourcesSPIM );
			for (BvvStackSource< ? > bvvSource : sourcesSPIM) 
			{
				bvvSourceToSpimData.put( bvvSource, spimData );
			}
			updateSceneRender();
			
//			double [] minI = new double[3];
//			double [] maxI = new double[3];
//			for(int i=0;i<3;i++)
//				maxI[i]=30;
//			sourcesSPIM.get( 0 ).setClipInterval(new FinalRealInterval(minI,maxI)  );
			
//			double [] minI = new double[3];
//			double [] maxI = new double[3];
//			for(int i=0;i<2;i++)
//			{
//				minI[i] = 225;
//				maxI[i] = 275;
//			}
//			minI[2] = 200;
//			maxI[2] = 300;
//
//			sourcesSPIM.get( 0 ).setClipInterval(new FinalRealInterval(minI,maxI)  );	
//			sourcesSPIM.get( 1 ).setClipInterval(new FinalRealInterval(minI,maxI)  );	
//			sourcesSPIM.get( 0 ).setLUT( "Green" );
//			sourcesSPIM.get( 1 ).setLUT( "Red" );	
		}
		catch ( SpimDataException exc )
		{
			exc.printStackTrace();
		}
		
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
		final Matrix4f camview = MatrixMath.screen( data.getDCam(), screen_size[0], screen_size[1], new Matrix4f() ).mul( view );
		
		//draw boxes around volume
		volumeBoxes.draw( gl, pvm, camview, screen_size );
		//draw clip boxes
		clipBoxes.draw( gl, pvm, camview, screen_size );
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
	
	public static void main(String... args) throws Exception
	{
		
		new ImageJ();
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 
		
		testBVB.run("");
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube_2ch.xml" );

		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/ExM_MT.xml" );
		testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/2_channels.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/HyperStack.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/trace1514947168.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/cliptest.xml" );
	}

}
