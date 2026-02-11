package bvb.animation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import bdv.ui.splitpanel.SplitPanel;
import bdv.util.Prefs;
import bvb.core.BigVolumeBrowser;
import bvvpg.core.render.VolumeRenderer.RepaintType;
import ij.IJ;

public class AnimationRender extends SwingWorker<Void, String>
{
	final BigVolumeBrowser bvb;
	final AnimationPanel aPanel;
	Dimension dimsIni = null;
	public JPanel glass = null;
	
	public AnimationRender(final BigVolumeBrowser bvb_, AnimationPanel aPanel_)
	{
		this.bvb = bvb_;
		this.aPanel = aPanel_;
	}
	@Override
	protected Void doInBackground() throws Exception
	{
		if(aPanel.sRenderSavePath == null)
		{
			return null;
		}

		final int nTotFrames = aPanel.kfAnim.nTotalTime * aPanel.nRenderFPS;
		
		if(!aPanel.bRenderMultiBox)
		{
			Prefs.showMultibox(false);
		}
		
		if(aPanel.bRenderScaleBar)
		{
			Prefs.showScaleBar(true);
			Prefs.showScaleBarInMovie( true );
		}

		Prefs.showTextOverlay(false);
		final float dT = aPanel.kfAnim.nTotalTime / (float)( nTotFrames - 1 );		
		
		dimsIni = new Dimension(bvb.bvvFrame.getContentPane().getSize());
		
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
       // bt.bvvFrame.setEnabled( false );	
		bvb.bvvFrame.getContentPane().setPreferredSize( nRenderDim );
		bvb.bvvFrame.pack();	
		SwingUtilities.invokeAndWait( ()->
		{
			bvb.bvvFrame.setResizable( false );
		});
		Rectangle rect = bvb.bvvViewer.getDisplayComponent().getBounds();
		BufferedImage bi =
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
			//setProgress(nFr * 100/ (nTotFrames - 1));
			//setProgressState("rendering frames ("+Integer.toString( nFr+1 )+"/"+Integer.toString(nTotFrames)+")");
			final float fTimePoint = nFr * dT;

			SwingUtilities.invokeAndWait( ()->
			{
				aPanel.updateScene( fTimePoint );
			} );

			long nTotalTime = 0;
			final long nWaitTime = 30;
			final long nTimeLimitmS = aPanel.nRenderFrameTimeLimit * 1000;
			boolean bWait = (bvb.bvvViewer.getRepaintStatus() != RepaintType.NONE);
			//while(bt.viewer.getRepaintStatus() != RepaintType.NONE)
			while(bWait)
			{			
				Thread.sleep( nWaitTime );
				status = bvb.bvvViewer.getRepaintStatus();
				//System.out.println(status);
				nTotalTime += nWaitTime;
				if(status == RepaintType.NONE)
					{bWait = false;}
				if (nTotalTime > nTimeLimitmS)
				{
					bWait = false;
					IJ.log( "Rendering of frame " + Integer.toString( nFr + 1 ) + " took more than a minute, proceeding with current result." );
				}
				if(isCancelled())
				{
					return null;	
				}	
			}
	        component.paint(bi.getGraphics());
			ImageIO.write( bi, "png", new File( aPanel.sRenderSavePath + 
			String.format("%0"+String.valueOf(nTotFrames).length() + "d", nFr + 1) + ".png") );
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
    		bvb.bvvViewer.setRenderMode( false );
    	} 
    	catch (InterruptedException e) 
    	{
    		// Process e here
    	}
    	catch (Exception e)
    	{

    		System.out.println("Animation render interrupted by user.");
    		bvb.bvvViewer.setRenderMode( false );
        	//setProgress(100);	
        	//setProgressState("Render interrupted by user.");
    	}	
    	
    	bvb.bvvViewer.setRenderMode( false );
    	if(dimsIni != null)
    		bvb.bvvFrame.getContentPane().setPreferredSize( dimsIni );

		bvb.bvvFrame.pack();
        
		bvb.bvvFrame.setResizable( true );
        if(glass != null)
        {
        	glass.setVisible(false);
        }
    	
		if(aPanel.butRecord != null && aPanel.tabIconRecord!= null)
    	{
			aPanel.butRecord.setIcon( aPanel.tabIconRecord );
			aPanel.butRecord.setToolTipText( "Render" );
    	}
		if(!aPanel.bRenderMultiBox)
		{
			Prefs.showMultibox(true);
		}
		if(aPanel.bRenderScaleBar)
		{
			Prefs.showScaleBar(false);
			Prefs.showScaleBarInMovie( false);
		}
		Prefs.showTextOverlay(true);
		
    	//unlock user interaction
//    	bt.bInputLock = false;
//    	bt.setLockMode(false);

    }

}
