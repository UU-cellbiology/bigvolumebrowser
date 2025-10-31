package bvb.shapes;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.util.Intervals;

import bvb.scene.AbstractClipTransformVis;
import bvb.scene.VisMesh;

/** a collection of meshes with different timepoints assigned to them **/
public class MultiMeshShape extends AbstractClipTransformMulti implements BasicMeshShape
{
	boolean bHasTexture = false;
	
	boolean bUseTexture = false;
	
	/** define if the shape is transparent **/
	void defineTransparency()
	{
		if(visRendersTimeMap.size()>0 )
		{
			bTransparent = false;
			if(getRenderType() == VisMesh.MESH && getSurfaceRender() ==  VisMesh.SURFACE_SILHOUETTE)
			{
				bTransparent = true;
			}
			if(getColor().getAlpha() < BasicShape.TRANSPARENCY_THRESHOLD)
			{
				bTransparent = true;
			}
		}
	}


	/** Adds a mesh to the object.
	 * For meshes without texture imageTexture should be null **/
	@SuppressWarnings( "hiding" )
	public void addMesh(final Mesh nmesh, final BufferedImage imageTexture, final int nTimePoint, final Color colorin )
	{		
		if(nmesh != null)
		{
			final VisMesh meshShape;
			
			if(imageTexture == null)
			{
				meshShape = new VisMesh( nmesh );
			}
			else
			{
				bHasTexture = true;
				meshShape = new VisMesh( nmesh, imageTexture );
			}
			if(colorin != null)
			{
				meshShape.setColor( colorin );
			}
			
			boundBox = Intervals.union( boundBox, Meshes.boundingBox( nmesh ) );
			visRendersTimeMap.put( meshShape, nTimePoint );
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
				((VisMesh)visRender).setRenderType( nRenderType);
			}
			defineTransparency();
		}
	}	
	
	@Override
	public int getRenderType()
	{
		return ((VisMesh)visRendersTimeMap.keySet().toArray()[0]).getRenderType();
	}
	
	@Override
	public void setSurfaceRender(final int nSurfaceRenderType)
	{
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisMesh)visRender).setRenderType( VisMesh.MESH );
				((VisMesh)visRender).setSurfaceRenderType( nSurfaceRenderType );	
			}
			defineTransparency();
		}
	}
	
	@Override
	public int getSurfaceRender()
	{
		return ((VisMesh)visRendersTimeMap.keySet().toArray()[0]).getSurfaceRenderType();
	}
	
	@Override
	public void setSurfaceGrid(final int nSurfaceGridType)
	{
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisMesh)visRender).setRenderType( VisMesh.MESH );
				((VisMesh)visRender).setSurfaceGridType( nSurfaceGridType );
			}
			defineTransparency();
		}
	}
	
	@Override
	public int getSurfaceGrid()
	{
		return ((VisMesh)visRendersTimeMap.keySet().toArray()[0]).getSurfaceGridType();
	}
	
	@Override
	public void setCartesianGrid(final float cartesianGridStep_, final float cartesianFraction_)
	{
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				((VisMesh)visRender).setCartesianGrid( cartesianGridStep_, cartesianFraction_ );
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
				((VisMesh)visRender).setColor( colorin );
			}
			defineTransparency();
		}
	}
	
	@Override
	public Color getColor()
	{
		return ((VisMesh)visRendersTimeMap.keySet().toArray()[0]).getColor();
	}

	@Override
	public void setPointSize( float fPointSize )
	{
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisMesh)visRender).setPointsSize( fPointSize );
			}
		}
		
	}
	
	@Override
	public float getPointSize()
	{
		return ((VisMesh)visRendersTimeMap.keySet().toArray()[0]).getPointsSize();
	}
	
	@Override
	public void setWireLineWidth(final float fThickness)
	{		
		if(visRendersTimeMap.size()>0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisMesh)visRender).setWireLineWidth( fThickness );
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
				((VisMesh)visRender).setSilhouetteDecay( silhouetteDecay_ );
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


	@Override
	public boolean hasTexture()
	{
		return bHasTexture;
	}


	@Override
	public void useTexture( boolean bUseTexture_ )
	{
		if(bHasTexture)
		{
			bUseTexture = bUseTexture_;
			if(visRendersTimeMap.size()>0 )
			{
				final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
				for(final AbstractClipTransformVis visRender:visRenders)
				{		
					((VisMesh)visRender).useTexture( bUseTexture );
				}
			}
		}
		
	}


	@Override
	public boolean isTextureUsed()
	{
		return bUseTexture;
	}

}
