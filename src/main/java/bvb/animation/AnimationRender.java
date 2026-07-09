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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import bdv.ui.splitpanel.SplitPanel;
import bdv.util.Prefs;
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.BVVWindowState;
import bvb.io.dto.SerializationIO;
import bvvpg.core.render.VolumeRenderer.RepaintType;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;

public class AnimationRender extends SwingWorker<Void, String>
{
	final BigVolumeBrowser bvb;
	final AnimationPanel aPanel;
	final RenderSettings renderParams;
	public JPanel glass = null;
	
	boolean bSliderUpdate;
	
	int nSliderValue;
	
	public final BVVWindowState bvvWindowState;
	
	final DiskCachedCellImg< ARGBType, ? > animStack;
	
	final int nTotFrames;
	
	public AnimationRender(final BigVolumeBrowser bvb_, AnimationPanel aPanel_)
	{
		this.bvb = bvb_;
		this.aPanel = aPanel_;
		this.renderParams = aPanel.renderSettings;
		bvvWindowState = new BVVWindowState(bvb);
		bvvWindowState.saveBvvWindowState();
		
		nTotFrames = aPanel.kfAnim.nTotalTime * renderParams.nRenderFPS;
		// generate stack, if needed
		if(renderParams.nRenderOutput == 0)
		{ 
			if(renderParams.bRenderCurrentWindowSize)
			{
				final Rectangle rect = bvb.bvvViewer.getDisplayComponent().getBounds();
				animStack = createCachedStack(rect.width, rect.height, nTotFrames);
			}
			else
			{
				animStack = createCachedStack(renderParams.nRenderWidth, renderParams.nRenderHeight, nTotFrames);
				
			}
		}
		else
		{
			animStack = null;
		}
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
		//final int nTotFrames = aPanel.kfAnim.nTotalTime * renderParams.nRenderFPS;
		final float dT = aPanel.kfAnim.nTotalTime / (float)( nTotFrames - 1 );		

		//check just in case
		if(renderParams.nRenderOutput == 0 && animStack == null)
		{
			return null;
		}		

		if(renderParams.nRenderOutput == 1 && renderParams.sRenderSavePath == null)
		{
			return null;
		}		

		bvb.bvbActions.turnOffManualTransform(false);
		bvb.multiBoxOverlayBVB.setEnabled( renderParams.bRenderMultiBox );
		Prefs.showScaleBar(renderParams.bRenderScaleBar);
		Prefs.showScaleBarInMovie( renderParams.bRenderScaleBar );
		bvb.axisOverlay.setEnabled( renderParams.bRenderAxesGizmo );

		Prefs.showTextOverlay(false);
		bvb.bvvViewer.setRenderMode( true );
		bSliderUpdate = aPanel.bUpdateSlider;
		nSliderValue = aPanel.timeSlider.getValue();
		
		aPanel.bUpdateSlider = false;
		
		if(!renderParams.bRenderCurrentWindowSize)
		{
			bvb.bvvFrame.setExtendedState(Frame.NORMAL);

			//resize the canvas and close the splitpanel
			SplitPanel splitPanel =  bvb.bvvFrame.getSplitPanel();
			
			if(!splitPanel.isCollapsed())
			{
				splitPanel.setCollapsed( true );
			}
					
			int nHeight = renderParams.nRenderHeight;
			
			//check if there is time slider => +25 in height
			if(bvb.bvvViewer.state().getNumTimepoints() > 1)
			{
				nHeight += 25;
			}
			
			Dimension nRenderDim = new Dimension(renderParams.nRenderWidth, nHeight);
	
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
		if(!renderParams.bRenderCurrentWindowSize)
		{
			if(renderParams.nRenderWidth != rect.width || renderParams.nRenderHeight != rect.height)
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
	        	biOut = resizeCenterCrop(bi, renderParams.nRenderWidth, renderParams.nRenderHeight);
	        }	
	        if(renderParams.nRenderOutput == 0)
	        {
	        	copyFrameToStack(biOut, animStack, nFr);
	        }
	        else
	        {
	        	ImageIO.write( biOut, "png", new File( renderParams.sRenderSavePath + 
	        					String.format("%0" + String.valueOf(nTotFrames).length() + "d", nFr + 1) + ".png") );
	        }
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
		if(renderParams.nRenderOutput == 0 && animStack != null)
		{
			//imagej ordered stack
			IntervalView< ARGBType > out = Views.permute(
						Views.addDimension(  Views.addDimension(animStack, 0, 0), 0, 0),
						2, 4);
			final ImagePlus imp = ImageJFunctions.show( out, "BVB_Animation_" + SerializationIO.getTimestamp() );
			imp.getCalibration().fps = renderParams.nRenderFPS;
			imp.getCalibration().frameInterval = 1./renderParams.nRenderFPS;
			imp.getCalibration().setTimeUnit( "sec" );
		}

    	IJ.log( "BVB: rendering is finished." );
        //restore the panel
    	if(!renderParams.bRenderCurrentWindowSize)
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
    
    public static DiskCachedCellImg< ARGBType, ? > createCachedStack(final int width, final int height, final int frames)
    {
    	// Define the dimensions of the entire 3D stack
        long[] dimensions = new long[]{ width, height, frames };
        
        // Define cell dimensions: One full XY frame per cell slice
        // This ensures disk writing happens cleanly per frame/block
        int[] cellDimensions = new int[]{ width, height, 1 }; 

        DiskCachedCellImgOptions options = DiskCachedCellImgOptions.options().cellDimensions( cellDimensions );
        DiskCachedCellImgFactory<ARGBType> factory = new DiskCachedCellImgFactory<>(new ARGBType(), options);

        // Create an empty, lazily allocated, disk-cached image stack
        return factory.create(dimensions);
    }
    
    public static void copyFrameToStack(final BufferedImage frame, final Img<ARGBType> stack, final int nFrame) 
    {
        
        final RandomAccessibleInterval< ARGBType > slice = Views.flatIterable( Views.hyperSlice(stack, 2, nFrame));
               
        if (frame.getType() != BufferedImage.TYPE_INT_ARGB && frame.getType() != BufferedImage.TYPE_INT_RGB) {
            throw new IllegalArgumentException("BufferedImage must be INT_ARGB or INT_RGB");
        }
        int[] framePixels = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();     

        int i = 0;
        Cursor<ARGBType> cursor = slice.cursor();
        while (cursor.hasNext()) {
            cursor.fwd();
            // Grab the packed ARGB int and set it directly into the ImgLib2 pixel
            cursor.get().set(framePixels[i++]);
        }
    }
    
    public void displayAndRegisterCleanup()
    {
    	//imagej ordered stack
    	IntervalView< ARGBType > out = Views.permute(
    			Views.addDimension(  Views.addDimension(animStack, 0, 0), 0, 0),
    			2, 4);
    	final ImagePlus imp = ImageJFunctions.show( out, "BVB_Animation_" + SerializationIO.getTimestamp() );
    	imp.getCalibration().fps = renderParams.nRenderFPS;
    	imp.getCalibration().frameInterval = 1./renderParams.nRenderFPS;
    	imp.getCalibration().setTimeUnit( "sec" );
    	ImageWindow window = imp.getWindow();
    	if (window != null) {
    		window.addWindowListener(new WindowAdapter() {
    			@Override
    			public void windowClosed(WindowEvent e) {
    				//Shuts down the internal IoSync and releases disk resources safely
    				animStack.shutdown();
    			}
    		});
    	}
    }
}
