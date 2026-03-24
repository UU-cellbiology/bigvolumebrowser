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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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
	
	boolean bSliderUpdate;
	int nSliderValue;
	
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

		bvb.bvbActions.turnOffManualTransform(false);
		bvb.multiBoxOverlayBVB.setEnabled( aPanel.bRenderMultiBox );
		Prefs.showScaleBar(aPanel.bRenderScaleBar);
		Prefs.showScaleBarInMovie( aPanel.bRenderScaleBar );
		bvb.axisOverlay.setEnabled( aPanel.bRenderAxesGizmo );

		Prefs.showTextOverlay(false);
		bvb.bvvViewer.setRenderMode( true );
		bSliderUpdate = aPanel.bUpdateSlider;
		nSliderValue = aPanel.timeSlider.getValue();
		
		aPanel.bUpdateSlider = false;
		
		if(!aPanel.bRenderCurrentWindowSize)
		{
			bvb.bvvFrame.setExtendedState(Frame.NORMAL);

			//resize the canvas and close the splitpanel
			SplitPanel splitPanel =  bvb.bvvFrame.getSplitPanel();
			
			if(!splitPanel.isCollapsed())
			{
				splitPanel.setCollapsed( true );
			}
					
			int nHeight = aPanel.nRenderHeight;
			
			//check if there is time slider => +25 in height
			if(bvb.bvvViewer.state().getNumTimepoints() > 1)
			{
				nHeight += 25;
			}
			
			Dimension nRenderDim = new Dimension(aPanel.nRenderWidth, nHeight);
	
			bvb.bvvFrame.getContentPane().setPreferredSize( null );
			final Point bvv_p = bvb.bvvFrame.getLocation();
	
			bvb.bvvFrame.setBounds( new Rectangle(bvv_p.x, bvv_p.y, nRenderDim.width, nRenderDim.height) );
		}

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
		
		boolean bResize = false;
		if(!aPanel.bRenderCurrentWindowSize)
		{
			if(aPanel.nRenderWidth != rect.width || aPanel.nRenderHeight != rect.height)
			{
				bResize = true;
			}
		}
		RepaintType status;
		//refresh first frame 
		SwingUtilities.invokeAndWait( ()->
		{
			bvb.repaintBVV();
		});
		
		for(int nFr = 0; nFr < nTotFrames; nFr++)
		{
			setProgress(Math.round(  nFr * 100 / (nTotFrames - 1)));

			publish("rendering frame (" + Integer.toString( nFr + 1 ) + "/" + Integer.toString(nTotFrames)+")");
			
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
	        BufferedImage biOut;
	        if(!bResize)
	        {
	        	biOut = bi;
	        }
	        else
	        {
	        	biOut = resizeCenterCrop(bi,aPanel.nRenderWidth, aPanel.nRenderHeight);
	        }			
			ImageIO.write( biOut, "png", new File( aPanel.sRenderSavePath + 
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
    	
    	aPanel.timeSlider.setValue(nSliderValue);
    	aPanel.bUpdateSlider = bSliderUpdate;
    	
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

    	IJ.log( "BVB: rendering is finished." );
        //restore the panel
    	if(!aPanel.bRenderCurrentWindowSize)
        	bvvWindowState.restoreBvvWindowState();
    	else
        	bvvWindowState.setViewerTransformAfterResizeIsDone();
    	

    	//unlock user interaction
//    	bt.bInputLock = false;
//    	bt.setLockMode(false);

    }
    public static BufferedImage resize(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, original.getType());
        Graphics2D g = resized.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();

        return resized;
    }
    
    public static BufferedImage resizeWithPadding(
            BufferedImage original,
            int targetWidth,
            int targetHeight,
            Color background) {

        double scale = Math.min(
                targetWidth  / (double) original.getWidth(),
                targetHeight / (double) original.getHeight());

        int newW = (int) (original.getWidth() * scale);
        int newH = (int) (original.getHeight() * scale);

        BufferedImage canvas = new BufferedImage(
                targetWidth,
                targetHeight,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = canvas.createGraphics();

        g.setColor(background);
        g.fillRect(0, 0, targetWidth, targetHeight);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int x = (targetWidth - newW) / 2;
        int y = (targetHeight - newH) / 2;

        g.drawImage(original, x, y, newW, newH, null);
        g.dispose();

        return canvas;
    }
    public static BufferedImage resizeCenterCrop(
            BufferedImage original,
            int targetWidth,
            int targetHeight) {

        double scale = Math.max(
                targetWidth  / (double) original.getWidth(),
                targetHeight / (double) original.getHeight());

        int scaledW = (int) Math.round(original.getWidth() * scale);
        int scaledH = (int) Math.round(original.getHeight() * scale);

        BufferedImage scaled = new BufferedImage(
                scaledW,
                scaledH,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = scaled.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(original, 0, 0, scaledW, scaledH, null);
        g.dispose();
        int x = (scaledW - targetWidth) / 2;
        int y = (scaledH - targetHeight) / 2;

        return scaled.getSubimage(x, y, targetWidth, targetHeight);
    }
}
