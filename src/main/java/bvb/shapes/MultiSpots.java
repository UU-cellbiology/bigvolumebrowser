package bvb.shapes;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealPoint;
import net.imglib2.util.Intervals;

import bvb.gui.shapes.SpotsShapeDialog;
import bvb.io.shapes.SpotsParser;
import bvb.scene.AbstractClipTransformVis;
import bvb.scene.LUTUploaderGPU;
import bvb.scene.VisSpots;
import ij.IJ;

public class MultiSpots extends AbstractClipTransformMulti implements BasicSpots
{
	float pointSize;
	
	Color pointColor;
	
	int renderType;
	
	int pointShape;
	
	float [] sizeMinMax = null;
	
	float [] propertyMinMax = null;
	
	float fSizeScale = 1.0f;
	
	int nMapLUTMode = 0;
	
	String sLUTName = "";
	
	LUTUploaderGPU lutGPU = null;
	
	boolean bInvertedLUT = false;
	
	final float [] fMapLUTMinMax = new float[2];
	
	int nMapAlphaMode = 0;
	
	boolean bInvertedAlpha = false;
	
	final float [] fMapAlphaMinMax = new float[2];
	
	boolean bHasProperty = false;
	
	float fExtraAlpha = 1.0f;
	
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
		
		if(fExtraAlpha < BasicShape.TRANSPARENCY_THRESHOLD / 255.0f)
		{
			bTransparent = true;		
		}
	}
	
	@Override
	public void setPointSize( float pointSize_ )
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
		for(int d = 0; d < 3; d++)
		{
			bb[0][d] += diff;
			bb[1][d] -= diff;
		}
		boundBox = FinalRealInterval.wrap( bb[0], bb[1]);
		pointSize = pointSize_;
		
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisSpots)visRender).fSpotSize = pointSize_;
			}
	
		}
	}

	@Override
	public float getPointSize()
	{
		return pointSize;
	}

	@Override
	public void setColor( Color pointColor_ )
	{
		pointColor = new Color(pointColor_.getRed(),pointColor_.getGreen(),pointColor_.getBlue(),pointColor_.getAlpha());
		
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisSpots)visRender).setColor( pointColor );
			}
			defineTransparency();
		}
		
	}

	@Override
	public Color getColor()
	{
		return pointColor;
	}
	
	@Override
	public void setLUT(final IndexColorModel icm_, String sLUTName) 
	{		
		this.sLUTName = sLUTName;
		lutGPU = new LUTUploaderGPU();
		lutGPU.setLUT( icm_, sLUTName );
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisSpots)visRender).setLUTUploaderGPU( lutGPU );
			}
			
		}
	}
	
	@Override
	public void setLUT( String sLUTName ) 
	{	
		this.sLUTName = sLUTName;
		lutGPU = new LUTUploaderGPU();
		lutGPU.setLUT( sLUTName );
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisSpots)visRender).setLUTUploaderGPU( lutGPU );
			}			
		}
	}
	
	@Override
	public String getLUTName()
	{
		return sLUTName;
	}

	@Override
	public void setRenderType( int nRenderType )
	{
		renderType = nRenderType;
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisSpots)visRender).setRenderType( nRenderType);
			}
			defineTransparency();
		}
		
	}

	@Override
	public int getRenderType()
	{
		return renderType;
	}

	@Override
	public void setPointShape( int nShape )
	{
		pointShape = nShape;
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisSpots)visRender).setShape( nShape );
			}
			defineTransparency();
		}
		
	}

	@Override
	public int getPointShape()
	{
		return pointShape;
	}

	/** returns maximum timepoint value (integer) **/
	public int initFromSpotParser(final SpotsParser sptParser, final SpotsShapeDialog sptShape)
	{
		pointShape = sptShape.nShape;
		setColor(sptShape.spotColor);
		renderType = sptShape.nFill;
		pointSize = -1.0f;
		if(!sptParser.parseSize)
		{
			pointSize = sptShape.fSpotSize;
		}
		defineTransparency();
		
		final int nTotSpotsN = sptParser.times.length;

		float [][] patTimesIndices = new float[nTotSpotsN][2];
		for (int i = 0; i< nTotSpotsN; i++)
		{
			patTimesIndices[i][0] = sptParser.times[i];
			patTimesIndices[i][1] = i;
		}
		Arrays.sort(patTimesIndices, (a, b) -> Float.compare(a[0], b[0]));
		float nCurrTimepoint = patTimesIndices[0][0];
		
		int nLoadTimePoint = 0;
		final ArrayList<RealPoint> verticesTP = new ArrayList<>();
		final ArrayList<Float> sizesTP = new ArrayList<>();
		final ArrayList<Float> propertyTP = new ArrayList<>();
		for(int i = 0; i < nTotSpotsN; i++)
		{
			if(Math.abs(patTimesIndices[i][0] - nCurrTimepoint)<0.00001)
			{
				final int index = ( int ) patTimesIndices[i][1] ;
				verticesTP.add( sptParser.vertices.get( index ) );
				if(sptParser.parseSize)
				{
					sizesTP.add( sptParser.sizes[index] );
				}			
				if(sptParser.parseProperty)
				{
					propertyTP.add( sptParser.property[index] );
				}
			}
			else
			{
				//add a new spots object
				addSpots(verticesTP, sizesTP, propertyTP, nLoadTimePoint);
				nCurrTimepoint = patTimesIndices[i][0];
				nLoadTimePoint++;
				verticesTP.clear();
				sizesTP.clear();
				propertyTP.clear();
			}
		}
		//just a last part of the array
		if(verticesTP.size() != 0)
		{
			addSpots(verticesTP, sizesTP, propertyTP, nLoadTimePoint);	
		}
		return nLoadTimePoint;
	}
	
	public void addSpots(final ArrayList<RealPoint> verticesTP, final ArrayList<Float> sizesTP, final ArrayList<Float> propertyTP, final int nLoadTimePoint)
	{
		//add a new spots object
		VisSpots spotsTP = new VisSpots(pointSize, pointColor, pointShape, renderType);
		
		float [] sizesF = null;
		float [] propertyF = null;

		if(sizesTP.size() != 0)
		{
			sizesF = new float[sizesTP.size()];
			for (int j = 0; j < sizesTP.size(); j++)
			{
				sizesF[j] = sizesTP.get( j );
			}
		}
		
		if(propertyTP.size() != 0)
		{
			bHasProperty = true;
			propertyF = new float[propertyTP.size()];
			for (int j = 0; j < propertyTP.size(); j++)
			{
				propertyF[j] = propertyTP.get( j );
			}
		}
		spotsTP.setVertices( verticesTP, sizesF, propertyF );
		
		boundBox = Intervals.union( boundBox, Spots.getBBoxSpots(verticesTP, sizesF, pointSize, fSizeScale) );
		
		if(sizesTP.size() != 0)
		{
			float [] sizeMinMaxCurr = Spots.getMinMax( sizesF );
			if(sizeMinMax == null)
			{
				sizeMinMax = new float[2];
				sizeMinMax[0] = Float.POSITIVE_INFINITY;
				sizeMinMax[1] = Float.NEGATIVE_INFINITY;
			}
			sizeMinMax[0] = Math.min( sizeMinMax[0], sizeMinMaxCurr[0] );
			sizeMinMax[1] = Math.max( sizeMinMax[1], sizeMinMaxCurr[1] );

		}
		
		if(bHasProperty)
		{
			float [] propertyMinMaxCurr = Spots.getMinMax( propertyF );
			if(propertyMinMax == null)
			{
				propertyMinMax = new float[2];
				propertyMinMax[0] = Float.POSITIVE_INFINITY;
				propertyMinMax[1] = Float.NEGATIVE_INFINITY;
			}
			propertyMinMax[0] = Math.min( propertyMinMax[0], propertyMinMaxCurr[0] );
			propertyMinMax[1] = Math.max( propertyMinMax[1], propertyMinMaxCurr[1] );
		}
			
		visRendersTimeMap.put( spotsTP, nLoadTimePoint );
	}
	
	@Override
	public String toString()
	{
		if(sName.equals( "" ))
		{
			return "spots" + this.hashCode();
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
		final float [] propertyMinMaxOut = new float[2];
		for(int i = 0; i < 2; i++)
		{
			propertyMinMaxOut[i] = propertyMinMax[i];
		}
		return propertyMinMaxOut;
	}

	@Override
	public float getSizeScale()
	{
		return fSizeScale;
	}

	@Override
	public void setSizeScale( float fSizeScale_ )
	{
		fSizeScale = fSizeScale_;
		
		if( visRendersTimeMap.size() > 0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisSpots)visRender).setSizeScale( fSizeScale);
			}
		}
		
	}
	
	@Override
	public void setMapLUTMode(int nMapLUTMode_)
	{
		nMapLUTMode = nMapLUTMode_;
		//no sizes, cannot map LUT
		if(nMapLUTMode == 4 && pointSize > 0.0f)
		{
			nMapLUTMode = 0;
			System.out.println("No spot size data available for " + this.toString());
		}
		//no property, cannot map LUT
		if(nMapLUTMode == 5 && !bHasProperty)
		{
			nMapLUTMode = 0;
			System.out.println("No spot property data available for " + this.toString());
		}
		if(nMapLUTMode > 0 && lutGPU == null)
		{
			this.setLUT( IJ.getLuts()[0] );
		}
		if( visRendersTimeMap.size() > 0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisSpots)visRender).setMapLUTMode( nMapLUTMode );
			}
			
		}
	}
	
	@Override
	public int getMapLUTMode()
	{
		return nMapLUTMode;
	}
	
	@Override
	public void setMapLUTRange(final float fMin, final float fMax)
	{
		fMapLUTMinMax[0] = fMin;
		fMapLUTMinMax[1] = fMax;

		if( visRendersTimeMap.size() > 0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisSpots)visRender).setMapLUTRange( fMapLUTMinMax[0], fMapLUTMinMax[1] );
			}
			
		}
	}
	
	@Override
	public void setMapLUTGamma(final float fGamma)
	{
		if( visRendersTimeMap.size() > 0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisSpots)visRender).setMapLUTGamma( fGamma );	
			}		
		}	
	}
	
	@Override
	public void setInvertedLUT(boolean bInv)
	{
		bInvertedLUT = bInv;
		if( visRendersTimeMap.size() > 0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisSpots)visRender).setInvertedLUT( bInvertedLUT );
			}
		}
		
	}
	
	@Override
	public boolean isInvertedLUT()
	{
		return bInvertedLUT;
	}

	@Override
	public boolean hasProperty()
	{
		return bHasProperty;
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
		if( visRendersTimeMap.size() > 0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisSpots)visRender).setMapAlphaMode( nMapAlphaMode );
			}
			
		}
		
	}
	
	@Override
	public int getMapAlphaMode()
	{
		return nMapAlphaMode;
	}

	@Override
	public void setMapAlphaRange( float fMin, float fMax )
	{
		fMapAlphaMinMax[0] = fMin;
		fMapAlphaMinMax[1] = fMax;

		if( visRendersTimeMap.size() > 0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisSpots)visRender).setMapAlphaRange( fMapAlphaMinMax[0], fMapAlphaMinMax[1] );
			}
			
		}
		
	}

	@Override
	public void setMapAlphaGamma( float fGamma )
	{
		if( visRendersTimeMap.size() > 0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisSpots)visRender).setMapAlphaGamma( fGamma );	
			}		
		}	
		
	}

	@Override
	public void setInvertedAlpha( boolean bInv )
	{
		bInvertedAlpha = bInv;
		if( visRendersTimeMap.size() > 0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisSpots)visRender).setInvertedAlpha( bInvertedAlpha );
			}
		}
		
	}

	@Override
	public boolean isInvertedAlpha()
	{
		return bInvertedAlpha;
	}

	@Override
	public void setExtraAlphaCoefficient( float dCoeff )
	{
		fExtraAlpha = dCoeff;
		if( visRendersTimeMap.size() > 0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisSpots)visRender).setExtraAlphaCoefficient( fExtraAlpha );
			}
		}	
		defineTransparency();
	}
	@Override
	public float getExtraAlphaCoefficient()
	{
		return fExtraAlpha;
	}
}
