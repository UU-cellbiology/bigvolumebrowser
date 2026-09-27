/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
	final RenderSettings renderParams;
	public JPanel glass = null;
	
	public AnimationSnapshot(final BigVolumeBrowser bvb_, AnimationPanel aPanel_)
	{
		this.bvb = bvb_;
		this.aPanel = aPanel_;
		this.renderParams = aPanel.renderSettings;
	}
	@Override
	protected Void doInBackground() throws Exception
	{
		bvb.multiBoxOverlayBVB.setEnabled( renderParams.bRenderMultiBox );
		Prefs.showScaleBar(renderParams.bRenderScaleBar);
		Prefs.showScaleBarInMovie( renderParams.bRenderScaleBar );
		bvb.axisOverlay.setEnabled( renderParams.bRenderAxesGizmo );
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
		final long nTimeLimitmS = renderParams.nRenderFrameTimeLimit * 1000;
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
