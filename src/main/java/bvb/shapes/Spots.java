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

import bvb.scene.VisSpots;

/** Spots of arbitrary color and size **/

public class Spots extends AbstractClipTransformSingleShape implements BasicSpots
{
	
	float pointSize;
	
	Color pointColor;
	
	int renderType;
	
	int pointShape;
	
	FinalRealInterval boundBox = null;
	
	public Spots(final float pointSize_, final Color pointColor_, final int nShape_, final int nRenderType_)
	{
		pointSize = pointSize_;		
		renderType = nRenderType_;
		pointColor = new Color(pointColor_.getRed(), pointColor_.getGreen(), pointColor_.getBlue(),pointColor_.getAlpha());
		pointShape = nShape_;
		defineTransparency();
	}
	
	
	public void setPoints(final ArrayList<RealPoint> vertices)
	{
		setPoints(vertices, null);
	}

	public void setPoints(final ArrayList<RealPoint> vertices, final float[] spotSizes)
	{
		if(visRender == null)
		{
			visRender = new VisSpots(pointSize, pointColor, pointShape, renderType);
		}
		if(spotSizes == null || spotSizes.length != vertices.size())
		{	
			((VisSpots)visRender).setVertices(vertices);	
		}
		else
		{
			((VisSpots)visRender).setVertices(vertices, spotSizes);				
		}
		setBoundingBox(vertices, spotSizes);
	}
	
	void setBoundingBox(final ArrayList<RealPoint> vertices, final float[] spotSizes)
	{		
		boundBox =  getBBoxSpots(vertices, spotSizes, pointSize);		
	}
	
	public static FinalRealInterval getBBoxSpots(final ArrayList<RealPoint> vertices, final float[] spotSizes, final float pointSize)
	{
		final double[] boundingBox = new double[] { Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
		
		if(spotSizes == null)
		{
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
			
		}
		else
		{
			final float [] sortedSize = new float[spotSizes.length];
			for ( int v=0; v < vertices.size(); v++ )
			{
				final double x = vertices.get( v ).getDoublePosition(0), y = vertices.get( v ).getDoublePosition(1), z = vertices.get( v ).getDoublePosition(2);
				final double pSize = spotSizes[v];
				sortedSize[v] = spotSizes[v];
				if ( x - pSize < boundingBox[ 0 ] )
					boundingBox[ 0 ] = x - pSize;
				if ( y - pSize < boundingBox[ 1 ] )
					boundingBox[ 1 ] = y - pSize;
				if ( z - pSize < boundingBox[ 2 ] )
					boundingBox[ 2 ] = z - pSize;
				if ( x + pSize > boundingBox[ 3 ] )
					boundingBox[ 3 ] = x + pSize;
				if ( y + pSize > boundingBox[ 4 ] )
					boundingBox[ 4 ] = y + pSize;
				if ( z + pSize > boundingBox[ 5 ] )
					boundingBox[ 5 ] = z + pSize;
				
			}

		}
		
		return Intervals.createMinMaxReal( boundingBox[ 0 ], boundingBox[ 1 ], boundingBox[ 2 ], boundingBox[ 3 ], boundingBox[ 4 ], boundingBox[ 5 ] );
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
	
	void defineTransparency()
	{

		bTransparent = false;
		if(renderType >= VisSpots.RENDER_GAUSS)
		{
			bTransparent = true;
		}
		if(pointColor.getAlpha() < BasicShape.TRANSPARENCY_THRESHOLD)
		{
			bTransparent = true;
		}
	}
	
	@Override
	public void setPointSize (final float pointSize_)
	{
		
		if(pointSize < 0)
		{
			System.err.println("This Spots Shape object has different spots size.");
			return;
		}
		
		final double [][] bb = new double[2][3];
		boundBox.realMin( bb[0] );
		boundBox.realMax( bb[1] );
		//adjust bbox dimensions
		final double diff = 0.5*( pointSize - pointSize_);
		for(int d=0;d<3;d++)
		{
			bb[0][d]+=diff;
			bb[1][d]-=diff;
		}
		boundBox = FinalRealInterval.wrap( bb[0], bb[1]);
		pointSize = pointSize_;
		((VisSpots)visRender).fSpotSize = pointSize_;
		
	}
	
	@Override
	public float getPointSize()
	{
		return pointSize;
	}
	
	@Override
	public void setColor(Color pointColor_) 
	{

		pointColor = new Color(pointColor_.getRed(),pointColor_.getGreen(),pointColor_.getBlue(),pointColor_.getAlpha());
		
		if(visRender != null)
		{
			((VisSpots)visRender).setColor(pointColor);	
			defineTransparency();
		}
	}
	
	@Override
	public Color getColor()
	{
		return pointColor;

	}
	
	public void setLUT(String sLUTName)
	{
		((VisSpots)visRender).setLUT( sLUTName );
	}
	
	@Override
	public void setRenderType(int nRenderType)
	{
		renderType = nRenderType;
		((VisSpots)visRender).setRenderType( renderType );
		defineTransparency();
		return;
	}	
	
	@Override
	public int getRenderType()
	{
		return renderType;
	}
	
	@Override
	public void setPointShape(int nShape)
	{
		pointShape = nShape;
		((VisSpots)visRender).setShape(pointShape);
		
		return;
	}	
	
	@Override
	public int getPointShape()
	{		
		return pointShape;
	}
	
	
//	public void setSMLMNorm(final float fNormGauss_)
//	{
//		((VisSpots)visRender).setSMLMNorm( fNormGauss_ );
//	}
//	
//	public float getSMLMNorm()
//	{
//		return ((VisSpots)visRender).getSMLMNorm( );
//	}
//	
//	public void setSMLMGamma(final float fNormGamma_)
//	{
//		((VisSpots)visRender).setSMLMGamma( fNormGamma_ );
//	}
//	
//	public float getSMLMGamma()
//	{
//		return ((VisSpots)visRender).getSMLMGamma( );
//	}
		
	@Override
	public String toString()
	{
		if(sName.equals( "" ))
		{
			return "spots"+Integer.toString(this.hashCode());
		}
		return sName;
	}


}
