package bvb.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;

import bvb.scene.AbstractClipTransformVis;
import bvb.scene.VisMeshColor;

public class MultiMeshColor extends AbstractClipTransformMulti implements BasicMeshColor
{
	
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


	public void addMesh(final Mesh nmesh, final int nTP, final Color colorin )
	{		
		if(nmesh != null)
		{
			final VisMeshColor meshShape = new VisMeshColor( nmesh );
			if(colorin != null)
			{
				meshShape.setColor( colorin );
			}
			boundBox = Intervals.union( boundBox, Meshes.boundingBox( nmesh ) );
			visRendersTimeMap.put( meshShape, nTP );
		}
	}

	@Override
	public void setRenderType(final int nRenderType_)
	{
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisMeshColor)visRender).setRenderType( nRenderType_);
			}
		}
	}	
	
	@Override
	public int getRenderType()
	{
		return ((VisMeshColor)visRendersTimeMap.keySet().toArray()[0]).getRenderType();
	}
	
	@Override
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
	
	@Override
	public int getSurfaceRender()
	{
		return ((VisMeshColor)visRendersTimeMap.keySet().toArray()[0]).getSurfaceRenderType();
	}
	
	@Override
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
	
	@Override
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
	
	@Override
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
	
	@Override
	public Color getColor()
	{
		return ((VisMeshColor)visRendersTimeMap.keySet().toArray()[0]).getColor();
	}

	@Override
	public void setPointSize( float fPointSize )
	{
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisMeshColor)visRender).setPointsSize( fPointSize );
			}
		}
		
	}
	@Override
	public float getPointSize()
	{
		return ((VisMeshColor)visRendersTimeMap.keySet().toArray()[0]).fPointSize;
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
