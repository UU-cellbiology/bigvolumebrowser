package bvb.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import net.imglib2.realtransform.AffineTransform3D;

import bdv.ui.splitpanel.SplitPanel;
import bvb.core.BigVolumeBrowser;
import bvvpg.core.VolumeViewerPanel;

/** wait for BVV window to finish window resizing 
 * before applying viewer transform **/
public class BVVWindowState
{
	
	final BigVolumeBrowser bvb;
	boolean bInitialized = false;
	double dividerProportion;
	Rectangle frameBounds;
	boolean bSplitPanelCollapsed;
	AffineTransform3D viewTransform; 
	boolean bSplitRestored = false;
	boolean bSizeRestored = false;
	int frameState;


	public BVVWindowState(final BigVolumeBrowser bvb)
	{
		this.bvb = bvb;
	}
	
	public void saveBvvWindowState()
	{
		final SplitPanel splitPanel = bvb.bvvFrame.getSplitPanel();
		dividerProportion = (double)splitPanel.getDividerLocation()/splitPanel.getWidth();
	    viewTransform = bvb.bvvViewer.state().getViewerTransform();
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
					bvb.bvvFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
					bSizeRestored = true;
				});
			}
			else
			{
				//restore window position		
				SwingUtilities.invokeLater(() -> 
				{
					bvb.bvvFrame.setBounds( frameBounds );
					bSizeRestored = true;
				});
			}
			bSplitRestored = false;
			setSplitPanelAfterResizeIsDone();
			//put back viewer transform after window resizing is finished
			setViewerTransformAfterResizeIsDone( bvb.bvvViewer, viewTransform );

		}
	}

	public void setSplitPanelAfterResizeIsDone()
	{
		{
			ComponentListener[] holder = new ComponentListener[1];
			final Dimension lastSize = new Dimension();
					
			Timer resizeTimer = new Timer(200, e -> {
				bvb.bvvViewer.removeComponentListener(holder[0]);
				bvb.bvvFrame.getSplitPanel().setCollapsed( bSplitPanelCollapsed );
				bvb.bvvFrame.getSplitPanel().setDividerLocation( dividerProportion );
				bSplitRestored = true;
			});
			resizeTimer.setRepeats(false);
			lastSize.setSize( bvb.bvvViewer.getSize());
			holder[0] = new ComponentAdapter() 
			{
			    @Override
			    public void componentResized(ComponentEvent e) {
			        Dimension current = bvb.bvvViewer.getSize();
			        if (!current.equals(lastSize) || !bSizeRestored ) 
			        {
			            lastSize.setSize(current);
			            resizeTimer.restart();
			        }

			    }
			};
			
			bvb.bvvViewer.addComponentListener(holder[0]);
		}

	}

	public void setViewerTransformAfterResizeIsDone(final VolumeViewerPanel viewer, final AffineTransform3D viewTransform)
	{
		ComponentListener[] holder = new ComponentListener[1];
		final Dimension lastSize = new Dimension();
				
		Timer resizeTimer = new Timer(200, e -> 
		{	
			viewer.removeComponentListener(holder[0]);
		    viewer.state().setViewerTransform( viewTransform );
		});
		resizeTimer.setRepeats(false);
		lastSize.setSize( viewer.getSize());
		
		holder[0] = new ComponentAdapter() 
		{
		    @Override
		    public void componentResized(ComponentEvent e) {
		        Dimension current = viewer.getSize();
		        if (!current.equals(lastSize) && bSplitRestored) {
		            lastSize.setSize(current);
		            resizeTimer.restart();
		        }
		    }
		};
		
		viewer.addComponentListener(holder[0]);
	}
}
