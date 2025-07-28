package bvb.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.util.Intervals;

import bvb.scene.AbstractClipTransformVis;
import bvb.scene.VisMeshColor;

/** a collection of meshes with different timepoints assigned to them **/
public class MultiMeshColor extends AbstractClipTransformMulti implements BasicMeshColor
{
	
	/** define if the shape is transparent **/
	void defineTransparency()
	{
		if(visRendersTimeMap.size()>0 )
		{
			bTransparent = false;
			if(getRenderType() == VisMeshColor.MESH && getSurfaceRender() ==  VisMeshColor.SURFACE_SILHOUETTE)
			{
				bTransparent = true;
			}
			if(getColor().getAlpha() < BasicShape.TRANSPARENCY_THRESHOLD)
			{
				bTransparent = true;
			}
		}
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
	public void setRenderType(final int nRenderType)
	{
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisMeshColor)visRender).setRenderType( nRenderType);
			}
			defineTransparency();
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
			defineTransparency();
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
			defineTransparency();
		}
	}
	
	@Override
	public int getSurfaceGrid()
	{
		return ((VisMeshColor)visRendersTimeMap.keySet().toArray()[0]).getSurfaceGridType();
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
			defineTransparency();
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
		return ((VisMeshColor)visRendersTimeMap.keySet().toArray()[0]).getPointsSize();
	}
	
	@Override
	public void setWireLineWidth(final float fThickness)
	{		
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisMeshColor)visRender).setWireLineWidth( fThickness );
			}
		}
	}
	
	
	@Override
	public void setSilhouetteDecay(final float silhouetteDecay_)
	{	
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisMeshColor)visRender).setSilhouetteDecay( silhouetteDecay_ );
			}
		}
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
