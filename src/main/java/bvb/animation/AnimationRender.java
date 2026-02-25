package bvb.animation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import bdv.ui.splitpanel.SplitPanel;
import bdv.util.Prefs;
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.BVVWindowState;
import bvvpg.core.render.VolumeRenderer.RepaintType;
import ij.IJ;

public class AnimationRender extends SwingWorker<Void, String>
{
	final BigVolumeBrowser bvb;
	final AnimationPanel aPanel;
	public JPanel glass = null;
	
	public final BVVWindowState bvvWindowState;
	
	public AnimationRender(final BigVolumeBrowser bvb_, AnimationPanel aPanel_)
	{
		this.bvb = bvb_;
		this.aPanel = aPanel_;
		bvvWindowState = new BVVWindowState(bvb);
		bvvWindowState.saveBvvWindowState();	
	}
	
	@Override
    protected void process(List<String> chunks) 
	{
		String message = chunks.get( chunks.size() - 1 );
		if(message.startsWith( "Notice" ))
		{
			IJ.log( "BVB:" + message );	
		}
		else
		{
			IJ.showStatus( "BVB:" + message );
		}
    }

	@Override
	protected Void doInBackground() throws Exception
	{
		final int nTotFrames = aPanel.kfAnim.nTotalTime * aPanel.nRenderFPS;
		final float dT = aPanel.kfAnim.nTotalTime / (float)( nTotFrames - 1 );		

		if(aPanel.sRenderSavePath == null)
		{
			return null;
		}		

		bvb.multiBoxOverlayBVB.setEnabled( aPanel.bRenderMultiBox );
		
		Prefs.showScaleBar(aPanel.bRenderScaleBar);
		Prefs.showScaleBarInMovie( aPanel.bRenderScaleBar );
		
		bvb.axisOverlay.setEnabled( false );

		Prefs.showTextOverlay(false);
		bvb.bvvFrame.setExtendedState(Frame.NORMAL);
		bvb.bvvViewer.setRenderMode( true );
		
		SplitPanel splitPanel =  bvb.bvvFrame.getSplitPanel();
		
		if(!splitPanel.isCollapsed())
		{
			splitPanel.setCollapsed( true );
		}
		
		Component component = bvb.bvvViewer;	
		
		int nHeight = aPanel.nRenderHeight;
		
		//check if there is time slider => +25 in height
		if(bvb.bvvViewer.state().getNumTimepoints() > 1)
		{
			nHeight += 25;
		}
		
		Dimension nRenderDim = new Dimension(aPanel.nRenderWidth, nHeight);
		if(glass != null)
		{
			bvb.bvvFrame.setGlassPane( glass );
			glass.setVisible(true);
			glass.requestFocusInWindow();
		}

		bvb.bvvFrame.getContentPane().setPreferredSize( null );
		final Point bvv_p = bvb.bvvFrame.getLocation();
		bvb.bvvFrame.setBounds( new Rectangle(bvv_p.x, bvv_p.y, nRenderDim.width, nRenderDim.height) );
		
		SwingUtilities.invokeAndWait( ()->
		{
			bvb.bvvFrame.setResizable( false );
		});
		
		Rectangle rect = bvb.bvvViewer.getDisplayComponent().getBounds();
		final BufferedImage bi =
                new BufferedImage(rect.width, rect.height,
                                    BufferedImage.TYPE_INT_ARGB);
		RepaintType status;
		//refresh first frame 
		SwingUtilities.invokeAndWait( ()->
		{
			bvb.repaintBVV();
		});
		
		for(int nFr = 0; nFr < nTotFrames; nFr++)
		{
			setProgress(Math.round(  nFr * 100 / (nTotFrames - 1)));

			publish("rendering frame ("+Integer.toString( nFr+1 )+"/"+Integer.toString(nTotFrames)+")");
			
			final float fTimePoint = nFr * dT;

			SwingUtilities.invokeAndWait( ()->
			{
				aPanel.updateScene( fTimePoint );
			} );

			long nTotalTime = 0;
			final long nWaitTime = 30;
			final long nTimeLimitmS = aPanel.nRenderFrameTimeLimit * 1000;
			boolean bWait = (bvb.bvvViewer.getRepaintStatus() != RepaintType.NONE);

			while(bWait)
			{			
				Thread.sleep( nWaitTime );
				status = bvb.bvvViewer.getRepaintStatus();
				//System.out.println(status);
				nTotalTime += nWaitTime;
				if(status == RepaintType.NONE)
				{
					bWait = false;
				}
				if (nTotalTime > nTimeLimitmS)
				{
					bWait = false;
					publish( "Notice: rendering of frame " + Integer.toString( nFr + 1 ) + " took more than a minute, proceeding with current result." );
				}
				if(isCancelled())
				{
					return null;	
				}	
			}
	        component.paint(bi.getGraphics());
	        //Img< FloatType > img = bvb.bvvViewer.getOffscreenIMG();
	        //ImageJFunctions.show( Views.hyperSlice( img, 0, 2 ));
			ImageIO.write( bi, "png", new File( aPanel.sRenderSavePath + 
			String.format("%0" + String.valueOf(nTotFrames).length() + "d", nFr + 1) + ".png") );
			if(isCancelled())
			{
				return null;	
			}	
		}
		
		return null;
	}
	
	
    @Override
    public void done() 
    {
    	//see if we have some errors
    	try {

    		get();
    	} 
    	catch (ExecutionException e) 
    	{
    		e.getCause().printStackTrace();
    		String msg = String.format("Unexpected error during animation render: %s", 
    				e.getCause().toString());
    		System.out.println(msg);
    	} 
    	catch (InterruptedException e) 
    	{
    		e.getCause().printStackTrace();
    		String msg = String.format("Unexpected error during animation render: %s", 
    				e.getCause().toString());
    		System.out.println(msg);
    	}
    	catch (Exception e)
    	{
    		System.out.println("Animation render interrupted by user.");

    	}	
    	setProgress(100);    	
    	bvb.bvvViewer.setRenderMode( false );
		bvb.bvvFrame.setResizable( true );
        if(glass != null)
        {
        	glass.setVisible(false);
        }
        
        bvvWindowState.restoreBvvWindowState();
    	IJ.log( "BVB: rendering is finished." );

    	bvb.axisOverlay.setEnabled( BVBSettings.bShowAxisOverlay );

    	bvb.multiBoxOverlayBVB.setEnabled( BVBSettings.bShowMultiBox );	
		Prefs.showScaleBar( BVBSettings.bShowScaleBar );
		Prefs.showScaleBarInMovie( BVBSettings.bShowScaleBar );
		Prefs.showTextOverlay(true);
		
    	//unlock user interaction
//    	bt.bInputLock = false;
//    	bt.setLockMode(false);

    }

}
