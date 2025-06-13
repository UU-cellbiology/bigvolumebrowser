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
import java.io.File;
import java.io.IOException;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.impl.naive.NaiveDoubleMesh;
import net.imglib2.mesh.io.ply.PLYMeshIO;
import net.imglib2.mesh.io.stl.STLMeshIO;
import net.imglib2.realtransform.AffineTransform3D;

import org.apache.commons.io.FilenameUtils;

import bvb.scene.VisMeshColor;
import bvb.utils.Misc;

public class MeshColor extends AbstractClipTransformSingleShape
{
	
	String sName = "";
	
	RealInterval boundBox = null;
	
	public MeshColor(String sFilename)
	{
		//load mesh from file
		Mesh nmesh = loadMeshFromFile( sFilename );
		
		//Mesh nmesh = createMeshWithNoise(15);
		
		if(nmesh != null)
		{			
			visRender = new VisMeshColor( nmesh );
			boundBox = Meshes.boundingBox( nmesh );
			setName(Misc.getSourceStyleName( sFilename ));
		}
		else
		{
			System.err.println("Sorry, cannot load mesh. Only STL and PLY formats are supported for now.");
		}
	}
	
	public MeshColor(final Mesh nmesh)
	{
		
		if(nmesh != null)
		{
			visRender = new VisMeshColor( nmesh );
			boundBox = Meshes.boundingBox( nmesh );
		}
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
	
	public void setPointsRender(final float fPointsSize_)
	{
		if(visRender != null )
		{
			((VisMeshColor)visRender).setRenderType( VisMeshColor.POINTS );
			((VisMeshColor)visRender).setPointsSize( fPointsSize_ );
		}
	}
	
	
	public void setSurfaceRender(final int nSurfaceRenderType)
	{
		if(visRender != null )
		{
			((VisMeshColor)visRender).setRenderType( VisMeshColor.MESH );
			((VisMeshColor)visRender).setSurfaceRenderType( nSurfaceRenderType );
		}
	}
	
	public void setSurfaceGrid(final int nSurfaceGridType)
	{
		if(visRender != null )
		{
			((VisMeshColor)visRender).setRenderType( VisMeshColor.MESH );
			((VisMeshColor)visRender).setSurfaceGridType( nSurfaceGridType );
		}
	}
	public void setCartesianGrid(final float cartesianGridStep_, final float cartesianFraction_)
	{
		if(visRender != null )
		{
			((VisMeshColor)visRender).setCartesianGrid( cartesianGridStep_, cartesianFraction_ );
		}
	}
	
	public void setColor(final Color colorin)
	{
		if(visRender != null )
		{
			((VisMeshColor)visRender).setColor( colorin );
		}
	}
	
	public static Mesh loadMeshFromFile(String sFilename)
	{
		String fileExt = FilenameUtils.getExtension( sFilename );
		
		NaiveDoubleMesh nmesh = new NaiveDoubleMesh();
				
		if(fileExt.equals( "stl" ))
		{
			try
			{
				STLMeshIO.read( nmesh, new File( sFilename ) );
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
				//not sure what is better
				//BufferMesh bmesh = null;
				//bmesh = PLYMeshIO.open( filename );				
				PLYMeshIO.read( new File( sFilename ), nmesh );
			}
			catch ( IOException exc )
			{
				exc.printStackTrace();
				return null;
			}
		}
		return nmesh;
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
			if(nTimePoint<0)
			{
				return "mesh"+this.hashCode();
			}
			return "mesh_t" + Integer.toString( nTimePoint ) + "_" + this.hashCode();
		}
		return sName;
	}
	
	
}
