package bvb.animation;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import bdv.util.Prefs;
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.io.dto.SerializationIO;
import bvvpg.core.render.VolumeRenderer.RepaintType;

import ij.ImagePlus;
import ij.process.ColorProcessor;

public class AnimationSnapshot extends SwingWorker<Void, String>
{
	final BigVolumeBrowser bvb;
	final AnimationPanel aPanel;
	public JPanel glass = null;
	
	public AnimationSnapshot(final BigVolumeBrowser bvb_, AnimationPanel aPanel_)
	{
		this.bvb = bvb_;
		this.aPanel = aPanel_;
	}
	@Override
	protected Void doInBackground() throws Exception
	{
		bvb.multiBoxOverlayBVB.setEnabled( aPanel.bRenderMultiBox );
		Prefs.showScaleBar(aPanel.bRenderScaleBar);
		Prefs.showScaleBarInMovie( aPanel.bRenderScaleBar );
		bvb.axisOverlay.setEnabled( aPanel.bRenderAxesGizmo );
		bvb.bvvViewer.setRenderMode( true );

		Prefs.showTextOverlay(false);
		if(glass != null)
		{
			bvb.bvvFrame.setGlassPane( glass );
			glass.setVisible(true);
			glass.requestFocusInWindow();
		}
		
		
		SwingUtilities.invokeAndWait( ()->
		{
			bvb.bvvFrame.setResizable( false );
		});
		
		Component component = bvb.bvvViewer;	
		
		Rectangle rect = bvb.bvvViewer.getDisplayComponent().getBounds();
		final BufferedImage bi =
                new BufferedImage(rect.width, rect.height,
                                    BufferedImage.TYPE_INT_ARGB);
		
		RepaintType status;
		//refresh frame 
		SwingUtilities.invokeAndWait( ()->
		{
			bvb.repaintBVV();
		});
		
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
			}
			if(isCancelled())
			{
				return null;	
			}	
			component.paint(bi.getGraphics());
		}
        component.paint(bi.getGraphics());
        final ColorProcessor cp = new ColorProcessor(bi);
        final ImagePlus imp = new ImagePlus("BVB_Snapshot_" + SerializationIO.getTimestamp(), cp);
        imp.show();
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
    		String msg = String.format("Unexpected error during snapshot render: %s", 
    				e.getCause().toString());
    		System.out.println(msg);
    	} 
    	catch (InterruptedException e) 
    	{
    		e.getCause().printStackTrace();
    		String msg = String.format("Unexpected error during snapshot render: %s", 
    				e.getCause().toString());
    		System.out.println(msg);
    	}
    	catch (Exception e)
    	{
    		System.out.println("Snapshot render interrupted by user.");

    	}	
     	
    	bvb.bvvViewer.setRenderMode( false );
		bvb.bvvFrame.setResizable( true );
        if(glass != null)
        {
        	glass.setVisible(false);
        }
    	
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
