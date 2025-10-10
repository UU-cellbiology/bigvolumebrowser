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
import java.awt.image.IndexColorModel;
import java.util.ArrayList;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;

import bvb.scene.LUTUploaderGPU;
import bvb.scene.VisSpots;
import ij.IJ;

/** Spots of arbitrary color and size **/

public class Spots extends AbstractClipTransformSingleShape implements BasicSpots
{
	
	float pointSize;
	
	Color pointColor;
	
	int renderType;
	
	int pointShape;
	
	FinalRealInterval boundBox = null;
	
	int nMapAlphaMode = 0;

	String sLUTName = "";
	
	LUTUploaderGPU lutGPU  = null;
	
	float [] sizeMinMax = null;
	
	float [] propertyMinMax = null;
	
	boolean bHasProperty = false;
	
	public Spots(final float pointSize_, final Color pointColor_, final int nShape_, final int nRenderType_)
	{
		pointSize = pointSize_;		
		renderType = nRenderType_;
		pointColor = new Color(pointColor_.getRed(), pointColor_.getGreen(), pointColor_.getBlue(),pointColor_.getAlpha());
		pointShape = nShape_;
		defineTransparency();
	}
	
	/** any of the last two arguments can be null **/
	public void setPoints(final ArrayList<RealPoint> vertices, final float[] spotSizes, final float[] spotProperty)
	{
		if(visRender == null)
		{
			visRender = new VisSpots(pointSize, pointColor, pointShape, renderType);
		}
		
		((VisSpots)visRender).setVertices(vertices, spotSizes, spotProperty);	
		if(spotSizes != null)
		{
			pointSize = -1.0f;
		}
		if(spotProperty != null)
		{
			bHasProperty = true;
			propertyMinMax = getMinMax(spotProperty);
		}
		
		setBoundingBox(vertices, spotSizes);
	}
	
	/** spotSizes argument can be null **/
	void setBoundingBox(final ArrayList<RealPoint> vertices, final float[] spotSizes)
	{		
		boundBox =  getBBoxSpots(vertices, spotSizes, pointSize, 1.0f);		
		sizeMinMax = getMinMax(spotSizes);
	}
	
	public static float [] getMinMax(final float[] values)
	{
		if(values == null)
			return null;
		final float [] valuesMinMax = new float [2];
		valuesMinMax[0] = Float.POSITIVE_INFINITY;
		valuesMinMax[1] = Float.NEGATIVE_INFINITY;
		for(int v = 0; v < values.length; v++)
		{
			if(values[v] < valuesMinMax[0])
				valuesMinMax[0] = values[v];
			if(values[v] > valuesMinMax[1])
				valuesMinMax[1] = values[v];
		}
		return valuesMinMax;
	}
	
	public static FinalRealInterval getBBoxSpots(final ArrayList<RealPoint> vertices, final float[] spotSizes, final float pointSize_, final float sizeScale)
	{
		final double[] boundingBox = new double[] { Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
		double pointRadius = 0.5 * pointSize_;
		if(spotSizes == null)
		{
			for ( final RealPoint v : vertices )
			{
				final double x = v.getDoublePosition(0), y = v.getDoublePosition(1), z = v.getDoublePosition(2);
				if ( x - pointRadius < boundingBox[ 0 ] )
					boundingBox[ 0 ] = x - pointRadius;
				if ( y - pointRadius < boundingBox[ 1 ] )
					boundingBox[ 1 ] = y - pointRadius;
				if ( z - pointRadius < boundingBox[ 2 ] )
					boundingBox[ 2 ] = z - pointRadius;
				if ( x + pointRadius > boundingBox[ 3 ] )
					boundingBox[ 3 ] = x + pointRadius;
				if ( y + pointRadius > boundingBox[ 4 ] )
					boundingBox[ 4 ] = y + pointRadius;
				if ( z + pointRadius > boundingBox[ 5 ] )
					boundingBox[ 5 ] = z + pointRadius;
			}
			
		}
		else
		{
			
			for ( int v = 0; v < vertices.size(); v++ )
			{
				final double x = vertices.get( v ).getDoublePosition(0), y = vertices.get( v ).getDoublePosition(1), z = vertices.get( v ).getDoublePosition(2);
				final double pRad = 0.5 * spotSizes[v]*sizeScale;
				if ( x - pRad < boundingBox[ 0 ] )
					boundingBox[ 0 ] = x - pRad;
				if ( y - pRad < boundingBox[ 1 ] )
					boundingBox[ 1 ] = y - pRad;
				if ( z - pRad < boundingBox[ 2 ] )
					boundingBox[ 2 ] = z - pRad;
				if ( x + pRad > boundingBox[ 3 ] )
					boundingBox[ 3 ] = x + pRad;
				if ( y + pRad > boundingBox[ 4 ] )
					boundingBox[ 4 ] = y + pRad;
				if ( z + pRad > boundingBox[ 5 ] )
					boundingBox[ 5 ] = z + pRad;
				
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
		
		if(nMapAlphaMode != 0)
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
		for(int d = 0;d < 3; d++)
		{
			bb[0][d] += diff;
			bb[1][d] -= diff;
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
	
	@Override
	public void setLUT(String sLUTName)
	{
		this.sLUTName = sLUTName;
		lutGPU = new LUTUploaderGPU();
		lutGPU.setLUT( sLUTName );
		((VisSpots)visRender).setLUTUploaderGPU( lutGPU );
	}
	
	@Override
	public void setLUT(final IndexColorModel icm_, String sLUTName) 
	{		
		this.sLUTName = sLUTName;
		lutGPU = new LUTUploaderGPU();
		lutGPU.setLUT( icm_, sLUTName );
		((VisSpots)visRender).setLUTUploaderGPU( lutGPU );

	}

	@Override
	public String getLUTName()
	{
		return sLUTName;
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
	public float[] getSizeRange()
	{
		final float [] sizeMinMaxOut = new float[2];
		for(int i = 0; i < 2; i++)
		{
			sizeMinMaxOut[i] = sizeMinMax[i];
		}
		return sizeMinMaxOut;
	}
	
	@Override
	public float[] getPropertyRange()
	{
		final float [] properyMinMaxOut = new float[2];
		for(int i = 0; i < 2; i++)
		{
			properyMinMaxOut[i] = propertyMinMax[i];
		}
		return properyMinMaxOut;
	}


	@Override
	public float getSizeScale()
	{
		return ((VisSpots)visRender).getSizeScale();
	}


	@Override
	public void setSizeScale( float fSizeScale )
	{
		((VisSpots)visRender).setSizeScale( fSizeScale );
		//adjust bounding box?? too complicated for now	
	}
	@Override
	public void setMapLUTMode(int nMapLUTMode_)
	{
		int nMapMode = nMapLUTMode_;
		//no sizes, cannot map LUT
		if(nMapMode == 4 && pointSize > 0.0f)
		{
			nMapMode = 0;
			System.out.println("No spot size data available for " + this.toString());
		}
		//no property, cannot map LUT
		if(nMapMode == 5 && !bHasProperty)
		{
			nMapMode = 0;
			System.out.println("No spot property data available for " + this.toString());
		}
		if(nMapMode > 0 && lutGPU == null)
		{
			this.setLUT( IJ.getLuts()[0] );
		}
		((VisSpots)visRender).setMapLUTMode( nMapMode ); 
	}
	
	@Override
	public int getMapLUTMode()
	{
		return ((VisSpots)visRender).getMapLUTMode();
	}
	
	@Override
	public void setMapLUTRange(final float fMin, final float fMax)
	{
		((VisSpots)visRender).setMapLUTRange( fMin, fMax );
	}
	
	@Override
	public void setMapLUTGamma(final float fGamma)
	{
		((VisSpots)visRender).setMapLUTGamma( fGamma );		
	}
	
	@Override
	public void setInvertedLUT(boolean bInv)
	{
		((VisSpots)visRender).setInvertedLUT( bInv );
		
	}
	
	@Override
	public boolean isInvertedLUT()
	{
		return ((VisSpots)visRender).isInvertedLUT();
	}


	@Override
	public boolean hasProperty()
	{
		return bHasProperty ;
	}


	@Override
	public void setMapAlphaMode( int nMapAlphaMode_ )
	{
		nMapAlphaMode = nMapAlphaMode_;
		//no sizes, cannot map alpha
		if(nMapAlphaMode == 4 && pointSize > 0.0f)
		{
			nMapAlphaMode = 0;
			System.out.println("No spot size data available for " + this.toString());
		}
		//no property, cannot map alpha
		if(nMapAlphaMode == 5 && !bHasProperty)
		{
			nMapAlphaMode = 0;
			System.out.println("No spot property data available for " + this.toString());
		}
		((VisSpots)visRender).setMapAlphaMode( nMapAlphaMode );
		defineTransparency();
		
	}
	
	@Override
	public int getMapAlphaMode()
	{
		return nMapAlphaMode;
	}

	@Override
	public void setMapAlphaRange( float fMin, float fMax )
	{
		((VisSpots)visRender).setMapAlphaRange( fMin, fMax );	
	}

	@Override
	public void setMapAlphaGamma( float fGamma )
	{
		((VisSpots)visRender).setMapAlphaGamma( fGamma );		
	}

	@Override
	public void setInvertedAlpha( boolean bInv )
	{
		((VisSpots)visRender).setInvertedAlpha( bInv );		
	}

	@Override
	public boolean isInvertedAlpha()
	{
		return ((VisSpots)visRender).isInvertedAlpha();
	}
}
