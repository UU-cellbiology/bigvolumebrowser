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

public class MultiSpots extends AbstractClipTransformMulti implements BasicSpots
{
	float pointSize;
	
	Color pointColor;
	
	int renderType;
	
	int pointShape;
	
	int nMapLUTMode = 0;
	
	String sLUTName = "";
	
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
			bb[0][d]+=diff;
			bb[1][d]-=diff;
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
	public void setMapLUTMode(int nMapLUTMode_)
	{
		nMapLUTMode = nMapLUTMode_;
		if(visRendersTimeMap.size()>0 )
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
	public void setLUT(final IndexColorModel icm_, String sLUTName) 
	{		
		this.sLUTName = sLUTName;
		final LUTUploaderGPU lutGPU = new LUTUploaderGPU();
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
		final LUTUploaderGPU lutGPU = new LUTUploaderGPU();
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
			}
			else
			{
				//add a new spots object
				addSpots(verticesTP, sizesTP, nLoadTimePoint);
				nCurrTimepoint = patTimesIndices[i][0];
				nLoadTimePoint++;
				verticesTP.clear();
				sizesTP.clear();
			}
		}
		if(verticesTP.size() != 0)
		{
			addSpots(verticesTP, sizesTP, nLoadTimePoint);	
		}
		return nLoadTimePoint;
	}
	void addSpots(final ArrayList<RealPoint> verticesTP, final ArrayList<Float> sizesTP, final int nLoadTimePoint)
	{
		//add a new spots object
		VisSpots spotsTP = new VisSpots(pointSize, pointColor, pointShape, renderType);
		if(sizesTP.size() == 0)
		{
			spotsTP.setVertices( verticesTP );
			boundBox = Intervals.union( boundBox, Spots.getBBoxSpots(verticesTP, null, pointSize) );
		}
		else
		{
			final float [] sizesF = new float[sizesTP.size()];
			for (int j = 0; j < sizesTP.size(); j++)
			{
				sizesF[j] = sizesTP.get( j );
			}
			spotsTP.setVertices( verticesTP, sizesF );
			boundBox = Intervals.union( boundBox, Spots.getBBoxSpots(verticesTP, sizesF, pointSize) );

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
}
