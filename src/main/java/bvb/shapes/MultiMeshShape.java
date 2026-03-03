/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;

import bvb.scene.AbstractClipTransformVis;
import bvb.scene.VisMesh;

/** a collection of meshes with different timepoints assigned to them **/
public class MultiMeshShape extends AbstractClipTransformMulti implements BasicMeshShape
{
	boolean bHasTexture = false;
	
	boolean bUseTexture = false;
	
	final AffineTransform3D transform = new AffineTransform3D();
	
	/** in the case meshes are transformed, we store initial transform **/
	final ConcurrentHashMap<AbstractClipTransformVis, AffineTransform3D> visRendersTransform = new ConcurrentHashMap<>();

	/** define if the shape is transparent **/
	void defineTransparency()
	{
		if( visRendersTimeMap.size() > 0 )
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
			visRendersTransform.put(meshShape, new AffineTransform3D());
		}
	}
	
	public void addMeshShape(final MeshShape meshShape)
	{
		if(meshShape != null)
		{
			bHasTexture = bHasTexture || meshShape.hasTexture();
			AffineTransform3D meshTransform = new AffineTransform3D();
			meshShape.getTransform( meshTransform );
			
			boundBox = Intervals.union( boundBox, meshTransform.estimateBounds( meshShape.boundingBoxNotTransformed() ));
			visRendersTransform.put(  meshShape.getVisObject(), meshTransform );
			visRendersTimeMap.put( meshShape.getVisObject(), meshShape.getTimePoint() );
		}
		if(visRendersTimeMap.size() == 1 )
		{
			defineTransparency();
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
		if( visRendersTimeMap.size() > 0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisMesh)visRender).setWireLineWidth( fThickness );
			}
		}
	}
	
	@Override
	public float getWireLineWidth()
	{
		return ((VisMesh)visRendersTimeMap.keySet().toArray()[0]).getWireLineWidth();
	}
	
	@Override
	public void setSilhouetteDecay(final float silhouetteDecay_)
	{	
		if( visRendersTimeMap.size() > 0 )
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{		
				((VisMesh)visRender).setSilhouetteDecay( silhouetteDecay_ );
			}
		}
	}
	
	@Override
	public float getSilhouetteDecay()
	{
		return ((VisMesh)visRendersTimeMap.keySet().toArray()[0]).getSilhouetteDecay();
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
	
	@Override
	public void getTransform(final AffineTransform3D t)
	{
		if(visRendersTimeMap.size() == 0)
		{
			t.set( new AffineTransform3D());
			return;
		}
		t.set( transform );
	}
	
	@Override
	public void setTransform(final AffineTransform3D t)
	{
		transform.set( t );
		final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			final AffineTransform3D trObj = visRendersTransform.get( visRender );
			final AffineTransform3D trObjCum = new AffineTransform3D();
			trObjCum.set( trObj );
			trObjCum.preConcatenate( transform );
			visRender.setTransform( trObjCum );
		}
	}
	
	@Override
	public RealInterval boundingBox()
	{
		if(visRendersTimeMap.size() == 0)
			return null;
				
		return transform.estimateBounds( boundBox );
	}	

}
