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
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.io.ply.PLYMeshIO;
import net.imglib2.mesh.io.stl.STLMeshIO;
import net.imglib2.realtransform.AffineTransform3D;

import org.apache.commons.io.FilenameUtils;

import bvb.scene.AbstractClipTransformVis;
import bvb.scene.VisMesh;
import bvb.utils.Misc;

public class MeshShape extends AbstractClipTransformSingleShape implements BasicMeshShape
{
	
	RealInterval boundBox = null;
	
	boolean bHasTexture = false;
	
	boolean bUseTexture = false;
	
	public MeshShape(String sFilename)
	{
		//load mesh from file
		Mesh nmesh = loadMeshFromFile( sFilename );
		
		if(nmesh != null && nmesh.vertices().size() > 0)
		{			
			visRender = new VisMesh( nmesh );
			boundBox = Meshes.boundingBox( nmesh );
			setName(Misc.getSourceStyleName( sFilename ));
		}
		else
		{
			System.err.println("Sorry, cannot load this mesh. Something wrong with a file or import.");
		}
	}
	
	public MeshShape(final Mesh nmesh)
	{
		if(nmesh != null)
		{
			visRender = new VisMesh( nmesh );
			boundBox = Meshes.boundingBox( nmesh );
		}
	}
	/** constructor for mesh with texture **/
	public MeshShape(final Mesh nmesh, final BufferedImage imageTexture)
	{
	
		if(nmesh != null)
		{
			visRender = new VisMesh( nmesh, imageTexture );
			bHasTexture = true;
			bUseTexture = true;
			boundBox = Meshes.boundingBox( nmesh );
		}
	}
	
	public static Mesh loadMeshFromFile(String sFilename)
	{
		String fileExt = FilenameUtils.getExtension( sFilename );
				
		if(fileExt.equals( "stl" ))
		{
			try
			{
				return STLMeshIO.open( sFilename );
			}
			catch ( IOException exc )
			{
				exc.printStackTrace();
				return null;
			}
		}
		
		if(fileExt.equals( "ply" ))
		{
			try
			{
				return PLYMeshIO.open( sFilename );				
			}
			catch ( IOException exc )
			{
				exc.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	AbstractClipTransformVis getVisObject()
	{
		return visRender;
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
		if(boundBox == null)
			return null;

			return new FinalRealInterval(boundBox);
	}
	
	/** define if the shape is transparent **/
	void defineTransparency()
	{
		if(visRender != null)
		{
			bTransparent = false;
			if(getRenderType() == VisMesh.MESH && getSurfaceRender() ==  VisMesh.SURFACE_SILHOUETTE)
			{
				bTransparent = true;
			}
			if(((VisMesh)visRender).getColor().getAlpha() < BasicShape.TRANSPARENCY_THRESHOLD)
			{
				bTransparent = true;
			}
		}
	}
	
	@Override
	public void setRenderType(final int nRenderType_)
	{
		
		if(visRender != null )
		{
			((VisMesh)visRender).setRenderType( nRenderType_ );
			defineTransparency();
		}	
	}	

	@Override
	public int getRenderType()
	{		
		return ((VisMesh)visRender).getRenderType();
	}	
	
	@Override
	public void setSurfaceRender(final int nSurfaceRenderType)
	{
		if(visRender != null )
		{
			((VisMesh)visRender).setRenderType( VisMesh.MESH );			
			((VisMesh)visRender).setSurfaceRenderType( nSurfaceRenderType );
			defineTransparency();	
		}
	}
	
	@Override
	public int getSurfaceRender()
	{
		return ((VisMesh)visRender).getSurfaceRenderType();
	}
	
	@Override
	public void setSurfaceGrid(final int nSurfaceGridType)
	{
		if(visRender != null )
		{			
			((VisMesh)visRender).setRenderType( VisMesh.MESH );
			((VisMesh)visRender).setSurfaceGridType( nSurfaceGridType );
			defineTransparency();
		}
	}
	
	@Override
	public int getSurfaceGrid()
	{
		return ((VisMesh)visRender).getSurfaceGridType();
	}
	
	@Override
	public void setCartesianGrid(final float cartesianGridStep_, final float cartesianFraction_)
	{
		if(visRender != null )
		{
			((VisMesh)visRender).setCartesianGrid( cartesianGridStep_, cartesianFraction_ );
		}
	}
	
	@Override
	public void setColor(final Color colorin)
	{
		if(visRender != null )
		{
			((VisMesh)visRender).setColor( colorin );
			defineTransparency();
		}
	}
	

	@Override
	public Color getColor()
	{		
		return ((VisMesh)visRender).getColor();
	}
	

	@Override
	public void setPointSize( float fPointSize )
	{
		((VisMesh)visRender).setPointsSize( fPointSize );
		
	}

	@Override
	public float getPointSize()
	{
		return ((VisMesh)visRender).getPointsSize();
	}
	
	
	@Override
	public void setWireLineWidth(final float fThickness)
	{		
		if(visRender != null )
		{
			((VisMesh)visRender).setWireLineWidth( fThickness );
		}
	}
	
	@Override
	public void setSilhouetteDecay(final float silhouetteDecay_)
	{		
		if(visRender != null )
		{
			((VisMesh)visRender).setSilhouetteDecay( silhouetteDecay_ );
		}
	}
	

	@Override
	public String toString()
	{
		if(sName.equals( "" ))
		{
			if(nTimePoint<0)
			{
				return "mesh" + this.hashCode();
			}
			return "mesh_t" + Integer.toString( nTimePoint ) + "_" + this.hashCode();
		}
		return sName;
	}
	
	@Override
	public boolean hasTexture()
	{
		return bHasTexture;
	}
	
	@Override
	public synchronized void useTexture(boolean bUseTexture_)
	{
		if(bHasTexture)
		{
			bUseTexture = bUseTexture_;
			((VisMesh)visRender).useTexture( bUseTexture );
		}
	}
	
	@Override
	public boolean isTextureUsed()
	{
		return bUseTexture;
	}
}
