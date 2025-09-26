package bvb.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.FinalRealInterval;

import bvb.gui.shapes.SpotsShapeDialog;
import bvb.io.shapes.SpotsParser;
import bvb.scene.AbstractClipTransformVis;
import bvb.scene.VisSpots;

public class MultiSpots extends AbstractClipTransformMulti implements BasicSpots
{
	float pointSize;
	
	Color pointColor;
	
	int renderType;
	
	int pointShape;
	
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
		for(int d=0;d<3;d++)
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

	void initFromSpotParser(final SpotsParser parser, final SpotsShapeDialog sptShape)
	{
		pointShape = sptShape.nShape;
		setColor(sptShape.spotColor);
		renderType = sptShape.nFill;
		pointSize = -1.0f;
		if(!parser.parseSize)
		{
			pointSize = sptShape.fSpotSize;
		}
		
	}
	
}
