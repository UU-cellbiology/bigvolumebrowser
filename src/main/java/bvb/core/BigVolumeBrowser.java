package bvb.core;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.ValuePair;

import bvb.gui.BVBControlPanel;
import bvb.io.BDVHDF5Loader;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.SpimDataException;
import bvvpg.core.VolumeViewerFrame;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvHandleFrame;
import bvvpg.vistools.BvvStackSource;
import ij.ImageJ;
import ij.plugin.PlugIn;

public class BigVolumeBrowser < T extends RealType< T > & NativeType< T > > implements PlugIn
{
	/** Bvv instance **/
	public Bvv bvv = null;
	
	/** Panel of BigVolumeViewer **/
	public VolumeViewerPanel bvvViewer;

	/** Frame of BigVolumeViewer **/
	public VolumeViewerFrame bvvFrame;
	
	public BVBControlPanel<T> controlPanel;
	
	@SuppressWarnings( "rawtypes" )
	private final ConcurrentHashMap < BvvStackSource<?>, AbstractSpimData > bvvSourceToSpimData;
	private final ConcurrentHashMap < AbstractSpimData, List<BvvStackSource<?> >> spimDataTobvvSourceList;
	
	public BigVolumeBrowser()
	{
		bvvSourceToSpimData = new ConcurrentHashMap<>();
		spimDataTobvvSourceList = new ConcurrentHashMap<>();
		
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
			//start empty bvv
			bvv = BvvFunctions.show( Bvv.options().
					dCam(BVBSettings.dCam).
					dClipNear(BVBSettings.dClipNear).
					dClipFar(BVBSettings.dClipFar).				
					renderWidth( BVBSettings.renderWidth).
					renderHeight( BVBSettings.renderHeight).
					numDitherSamples( BVBSettings.numDitherSamples ).
					cacheBlockSize( BVBSettings.cacheBlockSize ).
					maxCacheSizeInMB( BVBSettings.maxCacheSizeInMB ).
					ditherWidth(BVBSettings.ditherWidth).
					frameTitle("BigVolumeBrowser")
					);
			bvvViewer = ((BvvHandleFrame)bvv.getBvvHandle()).getViewerPanel();
			bvvFrame = ((BvvHandleFrame)bvv.getBvvHandle()).getBigVolumeViewer().getViewerFrame();
			
			bvvFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			//setup control panel
			
			controlPanel = new BVBControlPanel(this);
			controlPanel.cpFrame = new JFrame("BVB Control Panel");
			controlPanel.cpFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			controlPanel.cpFrame.add(controlPanel);
			
	        //Display the window.
			controlPanel.cpFrame.setSize(400,600);
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
		}
	}
	
	public void closeWindows()
	{
		bvvViewer.stop();
		bvvFrame.dispose();		
		controlPanel.cpFrame.dispose();
	}
	
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
			for (BvvStackSource< ? > bvvSourve : sourcesSPIM) 
			{
				bvvSourceToSpimData.put( bvvSourve, spimData );
			}
			
			
			
		}
		catch ( SpimDataException exc )
		{
			exc.printStackTrace();
		}
		
		
		
	}
	
	@SuppressWarnings("rawtypes")
	public static void main(String... args) throws Exception
	{
		
		new ImageJ();
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 
		
		testBVB.run("");
		testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/HyperStack.xml" );
	}
}
