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
import bvb.core.BVBActions.AlignPlaneBVB;

public class AxesOverlayRenderer implements OverlayRenderer
{
	private AbstractViewerPanel viewer = null;
	
	final Color [] axesColors = new Color[] 
//			{new Color(255,54,83,255), new Color(118,178,23,255), new Color(48,121,204,255)};
			{new Color(255,54,83,255), new Color(118,178,23,255), new Color(48,121,204,255),
			new Color(255,54,83,128), new Color(118,178,23,128), new Color(48,121,204,128)};

	//final String [] axisTitle = new String [] {"X","Y","Z"};
	
	final Color hoverBGColor = new Color( 0xb0bbbbbb, true );
	final BasicStroke vectorStroke = new BasicStroke(2.5f);
	final BasicStroke ovalStroke = new BasicStroke(1);
	final BasicStroke letterStroke = new BasicStroke(1.3f);
	
	private volatile boolean isEnabled = false;
	
	boolean isHover = false;
	
	final double [] qRotation = new double[4];
	
	final Point center = new Point();
	
	final int vectRadius = 40;
	final int circleAxisRadius = 9;	
	final int fullRadiusSquared = (vectRadius + circleAxisRadius) * (vectRadius + circleAxisRadius);
	final double [][] dAxisCircleCenter = new double [6][3];
	final double [][] dAxisVector = new double [6][3];

	final double [][] axisOrder = new double [6][2];

	int nHighlightedAxis = -1;
	
	private final static int xLetterHalf = 3;
	private final static int yLetterHalf = 4;
	
	private final double [][] quaterLibrary = new double[6][4];
	
	private int nAlignIndex;
	
	public AxesOverlayRenderer()
	{
		super();
		//there should be an easier way?!
		for(int d = 0; d < 4; d++)
		{
			LinAlgHelpers.quaternionInvert( AlignPlaneBVB.ZY.qAlign, quaterLibrary[0] );
			LinAlgHelpers.quaternionInvert( AlignPlaneBVB.XZ.qAlign, quaterLibrary[1] );
			LinAlgHelpers.quaternionInvert( AlignPlaneBVB.XY.qAlign, quaterLibrary[2] );
			LinAlgHelpers.quaternionInvert( AlignPlaneBVB.YZ.qAlign, quaterLibrary[3] );
			LinAlgHelpers.quaternionInvert( AlignPlaneBVB.ZX.qAlign, quaterLibrary[4] );
			LinAlgHelpers.quaternionInvert( AlignPlaneBVB.YX.qAlign, quaterLibrary[5] );
		}
	}
	
	public void bindViewer(final AbstractViewerPanel viewer_)
    {
    	this.viewer = viewer_;
    }
    
	@SuppressWarnings( "null" )
	@Override
	public void drawOverlays( Graphics g )
	{
		if(viewer == null || !isEnabled)
			return;

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

		Affine3DHelpers.extractRotationAnisotropic( transform, qRotation );
		for(int d = 0; d < 6; d++)
		{
			for(int i = 0; i < 3; i ++)
			{
				dAxisCircleCenter[d][i] = 0.0;
				dAxisVector[d][i] = 0.0;
			}
			if(d < 3)
			{
				dAxisCircleCenter[d][d] = vectRadius;
				dAxisVector[d][d] = vectRadius - circleAxisRadius - 2;
			}
			else
			{
				dAxisCircleCenter[d][d - 3] = -vectRadius;
				dAxisVector[d][d - 3] = circleAxisRadius - vectRadius + 2 ;
			}
			LinAlgHelpers.quaternionApply( qRotation, dAxisCircleCenter[d], dAxisCircleCenter[d] );
			LinAlgHelpers.quaternionApply( qRotation, dAxisVector[d], dAxisVector[d] );
			//z-coordinate
			axisOrder[d][0] = dAxisCircleCenter[d][2]; 
			//index
			axisOrder[d][1] = d; 	
			dAxisCircleCenter[d][0] += center.x;
			dAxisCircleCenter[d][1] += center.y;
			dAxisVector[d][0] += center.x;
			dAxisVector[d][1] += center.y;
		}

		//highlight axis if hover is active
		if(isHover)
		{
			float dx, dy;
			dx = (float)dAxisCircleCenter[0][0] - pMouse.x;
			dy = (float)dAxisCircleCenter[0][1] - pMouse.y;
			float fDistance = dx * dx + dy * dy;
			nHighlightedAxis = 0;
			for(int d = 1; d < 6; d++)
			{
				dx = (float)dAxisCircleCenter[d][0] - pMouse.x;
				dy = (float)dAxisCircleCenter[d][1] - pMouse.y;
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
		nAlignIndex = isAlignedWithPlanes(qRotation);
		for( int d = 0; d < 6; d++)
		{
			graphics.setStroke(vectorStroke);
			final int index = (int)axisOrder[d][1];

			int x = (int) Math.round( dAxisCircleCenter[index][0]);
			int y = (int) Math.round( dAxisCircleCenter[index][1]);
			//positive axes
			if(index < 3)
			{
				//vector's body
				graphics.setColor(axesColors[index]);					 
				graphics.drawLine(center.x, center.y, x, y);
				graphics.setStroke(ovalStroke);
				graphics.fillOval( x - circleAxisRadius, y - circleAxisRadius , 2 * circleAxisRadius + 2 , 2 * circleAxisRadius + 2);
				drawLetter(graphics, x + 1 , y + 1, index);
			}
			//negative axes
			else
			{
				graphics.setColor(axesColors[index].darker());
				if(nAlignIndex >= 0)
				{
					graphics.drawLine(center.x, center.y, (int) Math.round( dAxisVector[index][0]), (int) Math.round( dAxisVector[index][1]));
					//graphics.drawLine(center.x, center.y, x, y);

					if(nAlignIndex == index)
						graphics.setColor(axesColors[index - 3]);
				}
					
				graphics.setStroke(ovalStroke);
				graphics.fillOval( x - circleAxisRadius, y - circleAxisRadius , 2 * circleAxisRadius + 2 , 2 * circleAxisRadius + 2);
				graphics.setColor(axesColors[index - 3]);
				graphics.drawOval(x - circleAxisRadius, y - circleAxisRadius , 2 * circleAxisRadius + 2 , 2 * circleAxisRadius + 2);					
				drawLetter(graphics, x + 1 , y + 1, index);
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
		int nShiftedX = x;
		int nFinIndex = index;
		if (index > 2)
		{
			if(nHighlightedAxis == index)
			{
				//draw minus
				graphics.drawLine( nShiftedX -2*xLetterHalf, y, nShiftedX - xLetterHalf, y);

				nFinIndex = index - 3;
				nShiftedX += 3;
			}
			else if(nAlignIndex == index )
			{
				//draw minus
				graphics.drawLine( nShiftedX -2*xLetterHalf, y, nShiftedX - xLetterHalf, y);

				nFinIndex = index - 3;
				nShiftedX += 3;
				
			}
			else
			{
				return;
			}
		}
		switch (nFinIndex)
		{
		//x
		case 0:
			graphics.drawLine( nShiftedX - xLetterHalf, y - yLetterHalf, nShiftedX + xLetterHalf, y + yLetterHalf);
			graphics.drawLine( nShiftedX - xLetterHalf, y + yLetterHalf, nShiftedX + xLetterHalf, y - yLetterHalf);
			break;
		//y
		case 1:
			graphics.drawLine( nShiftedX - xLetterHalf, y - yLetterHalf, nShiftedX, y + 1 );
			graphics.drawLine( nShiftedX + xLetterHalf, y - yLetterHalf, nShiftedX, y + 1);
			graphics.drawLine( nShiftedX, y + 1,nShiftedX, y + yLetterHalf);
			break;
		//z
		case 2:
			graphics.drawLine( nShiftedX - xLetterHalf, y - yLetterHalf, nShiftedX + xLetterHalf, y - yLetterHalf);
			graphics.drawLine( nShiftedX - xLetterHalf, y + yLetterHalf, nShiftedX + xLetterHalf, y - yLetterHalf);
			graphics.drawLine( nShiftedX - xLetterHalf, y + yLetterHalf, nShiftedX + xLetterHalf, y + yLetterHalf);
			break;

		default:
			break;
		}
	}

    @Override
    public void setCanvasSize(int width, int height) 
    {
    }  
	
	public synchronized void setEnabled (final boolean bEnabled)
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
	/** function checks if provided quaternion is close
	 * to the ones defining the alignment planes.
	 * Returns the plane number or -1 if quaternion is far**/
	int isAlignedWithPlanes (final double [] qCurrent)
	{
		int nIndex = -1;
		for(int i = 0; i < 6; i++)
		{
			double dot = Math.abs( LinAlgHelpers.dot( qCurrent, quaterLibrary[i] ));
			if(dot > 0.99999)
			{
				nIndex = i;
			}
		}
		return nIndex;
	}
}
