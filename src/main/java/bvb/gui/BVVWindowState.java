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
package bvb.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import bdv.ui.splitpanel.SplitPanel;
import bvb.animation.SceneView;
import bvb.core.BigVolumeBrowser;
import bvvpg.core.InteractiveGLDisplayCanvas.CanvasSizeListener;

/** wait for BVV window to finish window resizing 
 * before applying viewer transform **/
public class BVVWindowState
{
	
	final BigVolumeBrowser bvb;
	boolean bInitialized = false;
	double dividerProportion;
	Rectangle frameBounds;
	
	boolean bSplitPanelCollapsed;
	SceneView sceneView;
	int frameState;
	
	int nCount = 0;


	public BVVWindowState(final BigVolumeBrowser bvb)
	{
		this.bvb = bvb;
	}
	
	public void saveBvvWindowState()
	{
		final SplitPanel splitPanel = bvb.bvvFrame.getSplitPanel();
		dividerProportion = (double)splitPanel.getDividerLocation()/splitPanel.getWidth();
		sceneView = SceneView.getCurrentSceneView( bvb.bvvViewer );
	    bSplitPanelCollapsed = bvb.bvvFrame.getSplitPanel().isCollapsed();
	    bInitialized = true;
	    frameBounds = bvb.bvvFrame.getBounds(); 
	    frameState = bvb.bvvFrame.getExtendedState();
	}
	
	public void restoreBvvWindowState()
	{
		if(bInitialized)
		{

			if(frameState == Frame.MAXIMIZED_BOTH)
			{				
				//restore window position		
				SwingUtilities.invokeLater(() -> 
				{
					final Timer resizeTimer = new Timer(150, e ->
					{
						bvb.bvvFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
						setSplitPanelAfterResizeIsDone();
					});
					resizeTimer.setRepeats( false );
					resizeTimer.start();
				});			
			}
			else
			{
				//restore window position		
				SwingUtilities.invokeLater(() -> 
				{
					
					final Timer resizeTimer = new Timer(200, e ->
					{
						bvb.bvvFrame.setBounds( new Rectangle(frameBounds));
						setSplitPanelAfterResizeIsDone();
					});
					resizeTimer.setRepeats( false );
					resizeTimer.start();
				});
			}
			
		}
	}

	public void setSplitPanelAfterResizeIsDone()
	{
		final ComponentListener[] holder = new ComponentListener[1];
		final Dimension lastSize = new Dimension();

		final Timer splitPanelTimer = new Timer(200, e -> 
		{
				bvb.bvvViewer.removeComponentListener(holder[0]);
				bvb.bvvFrame.getSplitPanel().setCollapsed( bSplitPanelCollapsed );
				bvb.bvvFrame.getSplitPanel().setDividerLocation( dividerProportion );
				setViewerTransformAfterResizeIsDone();

		});
		splitPanelTimer.setRepeats(false);
		lastSize.setSize( bvb.bvvViewer.getSize());

		holder[0] = new ComponentAdapter() 
		{
			@Override
			public void componentResized(ComponentEvent e) {
				Dimension current = bvb.bvvViewer.getSize();
				if (!current.equals(lastSize) ) 
				{
					lastSize.setSize(current);
					splitPanelTimer.restart();
				}
			}
		};

		bvb.bvvViewer.addComponentListener(holder[0]);
		splitPanelTimer.start();
	}

	public void setViewerTransformAfterResizeIsDone()
	{	
		if(bInitialized)
		{
			final Timer transformTimer ;
			final CanvasListenerWithTimer canvasListener = 
					new CanvasListenerWithTimer();
			
			transformTimer = new Timer(200, e -> 
			{	
				bvb.bvvViewer.getDisplay().canvasSizeListeners().remove( canvasListener );
				SceneView.setSceneView( bvb.bvvViewer, sceneView );
			});
	
			transformTimer.setRepeats(false);
			canvasListener.setTimer(transformTimer);
	
			bvb.bvvViewer.getDisplay().canvasSizeListeners().add( canvasListener );
			transformTimer.start();
		}
	}
	public class CanvasListenerWithTimer implements CanvasSizeListener
	{
		Timer transformTimerCanvas;
		
		public void setTimer (final Timer timer)
		{
			transformTimerCanvas = timer;
		}
		@Override
		public void setCanvasSize( int width, int height )
		{
			transformTimerCanvas.restart();		
		}
		
	}
}
