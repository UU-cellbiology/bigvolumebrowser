package bvb.gui;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.Timer;

import net.imglib2.realtransform.AffineTransform3D;

import bvvpg.core.VolumeViewerPanel;

/** wait for BVV window to finish window resizing 
 * before applying viewer transform **/
public class SetTransformAfterResize
{

	public static void run(final VolumeViewerPanel viewer, final AffineTransform3D viewTransform)
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
