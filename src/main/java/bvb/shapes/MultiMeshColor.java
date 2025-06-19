package bvb.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;

import bvb.scene.AbstractClipTransformVis;
import bvb.scene.VisMeshColor;

public class MultiMeshColor extends AbstractClipTransformMulti
{

	String sName = "";
	
	RealInterval boundBox = Intervals.createMinMaxReal( Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY  );
	
	@Override
	public RealInterval boundingBox()
	{
		if(visRendersTimeMap.size() ==0)
			return null;
		
		final AffineTransform3D t = new AffineTransform3D();
		((AbstractClipTransformVis)visRendersTimeMap.keySet().toArray()[0]).getTransform( t );
		
		return t.estimateBounds( boundBox );
	}
	
	@Override
	public RealInterval boundingBoxNotTransformed()
	{		
		return new FinalRealInterval(boundBox);
	}
	
	public void setPointsRender(final float fPointsSize_)
	{
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisMeshColor)visRender).setRenderType( VisMeshColor.POINTS );
				((VisMeshColor)visRender).setPointsSize( fPointsSize_ );
			}
		}
	}
	
	
	public void setSurfaceRender(final int nSurfaceRenderType)
	{
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisMeshColor)visRender).setRenderType( VisMeshColor.MESH );
				((VisMeshColor)visRender).setSurfaceRenderType( nSurfaceRenderType );	
			}
		}
	}
	
	public void setSurfaceGrid(final int nSurfaceGridType)
	{
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisMeshColor)visRender).setRenderType( VisMeshColor.MESH );
				((VisMeshColor)visRender).setSurfaceGridType( nSurfaceGridType );
			}
		}
	}
	public void setCartesianGrid(final float cartesianGridStep_, final float cartesianFraction_)
	{
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisMeshColor)visRender).setCartesianGrid( cartesianGridStep_, cartesianFraction_ );
			}
		}
	}
	
	public void setColor(final Color colorin)
	{
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisMeshColor)visRender).setColor( colorin );
			}
		}
	}
	
	public void setName(String sName_)
	{
		sName = sName_;
	}
	
	@Override
	public String toString()
	{
		if(sName.equals( "" ))
		{
			return "mesh" + this.hashCode();
		}
		
		return sName;
	}

}
