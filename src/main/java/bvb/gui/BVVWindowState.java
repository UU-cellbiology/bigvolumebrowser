package bvb.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import net.imglib2.realtransform.AffineTransform3D;

import bvb.core.BigVolumeBrowser;
import bvvpg.core.VolumeViewerPanel;

/** wait for BVV window to finish window resizing 
 * before applying viewer transform **/
public class BVVWindowState
{
	
	final BigVolumeBrowser bvb;
	boolean bInitialized = false;
	int dividerPos;
	Point bvv_p;
	Dimension bvv_d;
	boolean bSplitPanelCollapsed;
	AffineTransform3D viewTransform; 

	public BVVWindowState(final BigVolumeBrowser bvb)
	{
		this.bvb = bvb;
	}
	
	public void saveBvvWindowState()
	{
		dividerPos = bvb.bvvFrame.getSplitPanel().getDividerLocation();
	    bvv_p = bvb.bvvFrame.getLocation();
	    bvv_d = bvb.bvvFrame.getContentPane().getSize();
	    viewTransform = bvb.bvvViewer.state().getViewerTransform();
	    bSplitPanelCollapsed = bvb.bvvFrame.getSplitPanel().isCollapsed();
	    bInitialized = true;
	}
	
	public void restoreBvvWindowState()
	{
		if(bInitialized)
		{
			//restore window position		
			bvb.bvvFrame.setLocation( bvv_p );	
			bvb.bvvFrame.getContentPane().setPreferredSize( bvv_d );	
			bvb.bvvFrame.getSplitPanel().setDividerLocation( dividerPos );
			bvb.bvvFrame.getSplitPanel().setCollapsed( bSplitPanelCollapsed );
			bvb.bvvFrame.pack();
			
			//put back viewer transform after window resizing is finished
			BVVWindowState.setViewerTransformAfterResizeIsDone( bvb.bvvViewer, viewTransform );
		}
	}
	
	public static void setViewerTransformAfterResizeIsDone(final VolumeViewerPanel viewer, final AffineTransform3D viewTransform)
	{
		ComponentListener[] holder = new ComponentListener[1];
		final Dimension lastSize = new Dimension();
				
		Timer resizeTimer = new Timer(200, e -> {
		    //System.out.println("Resize finished.");
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
		        if (!current.equals(lastSize)) {
		            lastSize.setSize(current);
		            resizeTimer.restart();
		        }
		    }
		};
		
		viewer.addComponentListener(holder[0]);
	}
}
