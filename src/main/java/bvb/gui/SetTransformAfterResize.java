package bvb.gui;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Timer;

import net.imglib2.realtransform.AffineTransform3D;

import bvvpg.core.VolumeViewerPanel;

public class SetTransformAfterResize
{

	public static void run(final VolumeViewerPanel viewer, final AffineTransform3D viewTransform)
	{
		
		Dimension lastSize = new Dimension();
				
		Timer resizeTimer = new Timer(200, e -> {
		    //System.out.println("Resize finished.");
		    viewer.state().setViewerTransform( viewTransform );
		});
		resizeTimer.setRepeats(false);

		viewer.addComponentListener(new ComponentAdapter() {
		    @Override
		    public void componentResized(ComponentEvent e) {
		        Dimension current = viewer.getSize();
		        if (!current.equals(lastSize)) {
		            lastSize.setSize(current);
		            resizeTimer.restart();
		        }
		    }
		});
	}
}
