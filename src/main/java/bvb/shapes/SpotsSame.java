/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
package bvb.shapes;

import java.awt.Color;
import java.util.ArrayList;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;

import bvb.scene.VisSpotsSame;

/** Example class that uses points shader **/

public class SpotsSame extends AbstractClipTransformSingleShape
{	
	float pointSize;
	Color pointColor;
	int renderType;
	int pointShape;
	FinalRealInterval boundBox = null;
	
	public SpotsSame(final float pointSize_, final Color pointColor_, final int nShape_, final int nRenderType_)
	{
		pointSize = Math.abs( pointSize_ );		
		renderType = nRenderType_;
		pointColor = new Color(pointColor_.getRed(),pointColor_.getGreen(),pointColor_.getBlue(),pointColor_.getAlpha());
		pointShape = nShape_;
	}
	
	
	public void setPoints(final ArrayList<RealPoint> vertices)
	{
		if(visRender == null)
		{
			visRender = new VisSpotsSame(vertices, pointSize, pointColor, pointShape, renderType);
		}
		else
		{
			((VisSpotsSame)visRender).setVertices(vertices);
		}
		setBoundingBox(vertices);

	}
	
	void setBoundingBox(final ArrayList<RealPoint> vertices)
	{
		final double[] boundingBox = new double[] { Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
		for ( final RealPoint v : vertices )
		{
			final double x = v.getDoublePosition(0), y = v.getDoublePosition(1), z = v.getDoublePosition(2);
			if ( x - pointSize < boundingBox[ 0 ] )
				boundingBox[ 0 ] = x - pointSize;
			if ( y - pointSize < boundingBox[ 1 ] )
				boundingBox[ 1 ] = y - pointSize;
			if ( z - pointSize < boundingBox[ 2 ] )
				boundingBox[ 2 ] = z - pointSize;
			if ( x + pointSize > boundingBox[ 3 ] )
				boundingBox[ 3 ] = x + pointSize;
			if ( y + pointSize > boundingBox[ 4 ] )
				boundingBox[ 4 ] = y + pointSize;
			if ( z + pointSize > boundingBox[ 5 ] )
				boundingBox[ 5 ] = z + pointSize;
		}
		
		boundBox =  Intervals.createMinMaxReal( boundingBox[ 0 ], boundingBox[ 1 ], boundingBox[ 2 ], boundingBox[ 3 ], boundingBox[ 4 ], boundingBox[ 5 ] );
		
	}
	
	@Override
	public RealInterval boundingBox()
	{
		final AffineTransform3D t = new AffineTransform3D();
		visRender.getTransform( t );
		
		return t.estimateBounds( boundBox );
	}
	
	@Override
	public RealInterval boundingBoxNotTransformed()
	{		
		return new FinalRealInterval(boundBox);
	}
	
	public void setPointsColor(Color pointColor_) 
	{

		pointColor = new Color(pointColor_.getRed(),pointColor_.getGreen(),pointColor_.getBlue(),pointColor_.getAlpha());
		
		if(visRender != null)
		{
			((VisSpotsSame)visRender).setColor(pointColor);			
		}
	}
	
	public void setRenderType(int nRenderType)
	{
		pointShape = nRenderType;
		((VisSpotsSame)visRender).setShape( pointShape );		
		return;
	}	
	
	public void setPointShape(int nShape)
	{
		renderType = nShape;
		((VisSpotsSame)visRender).setRenderType(renderType);
		
		return;
	}	
	
	@Override
	public String toString()
	{
		if(sName.equals( "" ))
		{
			return "spots"+Integer.toString(this.hashCode());
		}
		return sName;

	}

	@Override
	public void setVisible( boolean bVisible_ )
	{
		bVisible = bVisible_;
	}


}
