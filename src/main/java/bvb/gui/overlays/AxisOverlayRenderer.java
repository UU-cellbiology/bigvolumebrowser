package bvb.gui.overlays;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Arrays;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import bdv.ui.UIUtils;
import bdv.util.Affine3DHelpers;
import bdv.viewer.AbstractViewerPanel;
import bdv.viewer.OverlayRenderer;

public class AxisOverlayRenderer implements OverlayRenderer
{
	private AbstractViewerPanel viewer = null;
	
	final Color [] axisColors = new Color[] 
			{new Color(255,54,83), new Color(118,178,23), new Color(48,121,204)};
	final String [] axisTitle = new String [] {"X","Y","Z"};
	
	boolean isEnabled = false;
	
	public void bindViewer(final AbstractViewerPanel viewer_)
    {
    	this.viewer = viewer_;
    }
    
	@Override
	public void drawOverlays( Graphics g )
	{
		if(viewer != null && isEnabled)
		{
			Graphics2D graphics = (Graphics2D) g;
			final Rectangle clipBounds = g.getClipBounds();
			final int xPos =  ( int ) ( clipBounds.getWidth() - 80 );
			final int yPos =  100 ;
			graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			// Get current viewer transform
			AffineTransform3D transform = new AffineTransform3D();
			viewer.state().getViewerTransform(transform);
			final double [] qRotation = new double[4];
			Affine3DHelpers.extractRotationAnisotropic( transform, qRotation );
			final double [][] dAxis = new double [3][3];
			final double [][] axisOrder = new double [3][2];
			for(int d = 0; d < 3; d++)
			{
				dAxis[d][d] = 40.0;
				LinAlgHelpers.quaternionApply( qRotation, dAxis[d], dAxis[d] );
				//z-coordinate
				axisOrder[d][0] = dAxis[d][2]; 
				//index
				axisOrder[d][1] = d; 				
			}
			//sort by depth (z-coord)
			Arrays.sort(axisOrder, (a, b) -> (-1) * Double.compare(a[0], b[0]));
			graphics.setStroke(new BasicStroke(2));
			graphics.setFont( UIUtils.getFont( "mini.font" ) );
			for( int d = 0; d < 3; d++)
			{
				// Example: draw fixed screen-space text
				final int index = (int)axisOrder[d][1];
				graphics.setColor(axisColors[index]);
				int x = (int)Math.round( xPos + dAxis[index][0]);
				int y = (int) Math.round( yPos +  dAxis[index][1]);
				graphics.drawLine( xPos, yPos, x, y);
				graphics.fillOval( x - 10, y - 10, 20, 20);
				graphics.setPaint( Color.BLACK );
				graphics.drawString( axisTitle[index], x - 4, y + 5);
			}

		}
	}

    @Override
    public void setCanvasSize(int width, int height) 
    {
    }  
	
	public void setEnabled (boolean bEnabled)
	{
		isEnabled = bEnabled;
	}
	
	public boolean isEnabled()
	{
		return isEnabled;
	}

}
