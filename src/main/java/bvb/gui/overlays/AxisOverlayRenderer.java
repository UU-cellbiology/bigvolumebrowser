package bvb.gui.overlays;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Arrays;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import bdv.util.Affine3DHelpers;
import bdv.viewer.AbstractViewerPanel;
import bdv.viewer.OverlayRenderer;

public class AxisOverlayRenderer implements OverlayRenderer
{
	private AbstractViewerPanel viewer = null;
	
	final Color [] axisColors = new Color[] 
//			{new Color(255,54,83,255), new Color(118,178,23,255), new Color(48,121,204,255)};
			{new Color(255,54,83,255), new Color(118,178,23,255), new Color(48,121,204,255),
			new Color(255,54,83,128), new Color(118,178,23,128), new Color(48,121,204,128)};

	//final String [] axisTitle = new String [] {"X","Y","Z"};
	
	final Color hoverBGColor = new Color( 0xb0bbbbbb, true );
	final BasicStroke vectorStroke = new BasicStroke(2.5f);
	final BasicStroke ovalStroke = new BasicStroke(1);
	final BasicStroke letterStroke = new BasicStroke(1.3f);
	
	boolean isEnabled = false;
	
	boolean isHover = false;
	
	final Point center = new Point();
	
	final int vectRadius = 40;
	final int circleAxisRadius = 9;	
	final int fullRadiusSquared = (vectRadius + circleAxisRadius) * (vectRadius + circleAxisRadius);
	
	int nHighlightedAxis = -1;
	
	private final static int xLetterHalf = 3;
	private final static int yLetterHalf = 4;
	
	public void bindViewer(final AbstractViewerPanel viewer_)
    {
    	this.viewer = viewer_;
    }
    
	@SuppressWarnings( "null" )
	@Override
	public void drawOverlays( Graphics g )
	{
		if(viewer != null && isEnabled)
		{
			Graphics2D graphics = (Graphics2D) g;
			final Rectangle clipBounds = g.getClipBounds();
			center.setLocation( clipBounds.getWidth() - 80 , 100 );
			isHover = false;
			final Point pMouse = viewer.getMousePosition();
			if (pMouse != null)
			{
				final float dMouseX = pMouse.x - center.x;
				final float dMouseY = pMouse.y - center.y;
								
				if( dMouseX * dMouseX + dMouseY * dMouseY < fullRadiusSquared)
				{
					graphics.setColor( hoverBGColor );
					graphics.fillOval( center.x - vectRadius - circleAxisRadius, 
									   center.y - vectRadius - circleAxisRadius, 
									   2 * (vectRadius + circleAxisRadius) + 1, 
									   2 * (vectRadius + circleAxisRadius) + 1);
					isHover = true;
				}
			}

			graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			AffineTransform3D transform = new AffineTransform3D();
			viewer.state().getViewerTransform(transform);
			final double [] qRotation = new double[4];
			Affine3DHelpers.extractRotationAnisotropic( transform, qRotation );
			final double [][] dAxis = new double [6][3];
			final double [][] axisOrder = new double [6][2];
			for(int d = 0; d < 6; d++)
			{
				if(d < 3)
					dAxis[d][d] = vectRadius;
				else
					dAxis[d][d - 3] = -vectRadius;
				LinAlgHelpers.quaternionApply( qRotation, dAxis[d], dAxis[d] );
				//z-coordinate
				axisOrder[d][0] = dAxis[d][2]; 
				//index
				axisOrder[d][1] = d; 	
				dAxis[d][0] += center.x;
				dAxis[d][1] += center.y;
			}
					
			//highlight axis if hover is active
			if(isHover)
			{
				float dx, dy;
				dx = (float)dAxis[0][0] - pMouse.x;
				dy = (float)dAxis[0][1] - pMouse.y;
				float fDistance = dx * dx + dy * dy;
				nHighlightedAxis = 0;
				for(int d = 1; d < 3; d++)
				{
					dx = (float)dAxis[d][0] - pMouse.x;
					dy = (float)dAxis[d][1] - pMouse.y;
					if(dx * dx + dy * dy < fDistance)
					{
						nHighlightedAxis = d;
						fDistance = dx * dx + dy * dy;
					}

				}
			}
			else
			{
				nHighlightedAxis = -1;
			}
			//sort by depth (z-coord)
			Arrays.sort(axisOrder, (a, b) -> (-1) * Double.compare(a[0], b[0]));
			
			for( int d = 0; d < 6; d++)
			{
				graphics.setStroke(vectorStroke);
				// Example: draw fixed screen-space text
				final int index = (int)axisOrder[d][1];
				
				int x = (int) Math.round( dAxis[index][0]);
				int y = (int) Math.round( dAxis[index][1]);
				if(index < 3)
				{
					graphics.setColor(axisColors[index]);					 
					graphics.drawLine(center.x, center.y, x, y);
					graphics.setStroke(ovalStroke);
					graphics.fillOval( x - circleAxisRadius, y - circleAxisRadius , 2 * circleAxisRadius + 2 , 2 * circleAxisRadius + 2);
					drawLetter(graphics, x + 1 , y + 1, index);
				}
				else
				{
					graphics.setColor(axisColors[index].darker());
					graphics.setStroke(ovalStroke);
					graphics.fillOval( x - circleAxisRadius, y - circleAxisRadius , 2 * circleAxisRadius + 2 , 2 * circleAxisRadius + 2);
					graphics.setColor(axisColors[index - 3]);
					graphics.drawOval(x - circleAxisRadius, y - circleAxisRadius , 2 * circleAxisRadius + 2 , 2 * circleAxisRadius + 2);					
				}
			}

		}
	}
	
	/** since fonts vary from OS to OS, I will draw axes letter with lines.**/
	void drawLetter(final Graphics2D graphics, final int x, final int y, final int index)
	{
		if( nHighlightedAxis == index )
			graphics.setPaint( Color.WHITE );
		else
			graphics.setPaint( Color.BLACK );
		
		graphics.setStroke(letterStroke);
		switch (index)
		{
		//x
		case 0:
			graphics.drawLine( x - xLetterHalf, y - yLetterHalf, x + xLetterHalf, y + yLetterHalf);
			graphics.drawLine( x - xLetterHalf, y + yLetterHalf, x + xLetterHalf, y - yLetterHalf);
			break;
		//y
		case 1:
			graphics.drawLine( x - xLetterHalf, y - yLetterHalf, x, y + 1 );
			graphics.drawLine( x + xLetterHalf, y - yLetterHalf, x, y + 1);
			graphics.drawLine( x, y + 1, x, y + yLetterHalf);
			break;
		//z
		case 2:
			graphics.drawLine( x - xLetterHalf, y - yLetterHalf, x + xLetterHalf, y - yLetterHalf);
			graphics.drawLine( x - xLetterHalf, y + yLetterHalf, x + xLetterHalf, y - yLetterHalf);
			graphics.drawLine( x - xLetterHalf, y + yLetterHalf, x + xLetterHalf, y + yLetterHalf);
			break;

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
	public int getHighlightedAxis()
	{
		return nHighlightedAxis;
	}

}
