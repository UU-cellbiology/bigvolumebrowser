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

import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.impl.naive.NaiveDoubleMesh;
import net.imglib2.mesh.io.ply.PLYMeshIO;
import net.imglib2.mesh.io.stl.STLMeshIO;

import org.apache.commons.io.FilenameUtils;
import org.joml.Matrix4fc;

import bvb.core.BigVolumeBrowser;
import bvb.scene.VisMeshColor;
import bvb.utils.Misc;

public class MeshColor extends AbstractBasicShape
{
	
	final BigVolumeBrowser bvb;
	
	VisMeshColor meshVis = null;
	
	String sName = "";
	
	RealInterval boundingBox = null;
	
	public MeshColor(String sFilename, BigVolumeBrowser bvb_)
	{
		bvb  = bvb_;
		//load mesh from file
		Mesh nmesh = loadMeshFromFile( sFilename );
		
		//Mesh nmesh = createMeshWithNoise(15);
		
		if(nmesh != null)
		{			
			meshVis = new VisMeshColor( nmesh );
			boundingBox = Meshes.boundingBox( nmesh );
			setName(Misc.getSourceStyleName( sFilename ));
		}
		else
		{
			System.err.println("Sorry, cannot load mesh. Only STL and PLY formats are supported for now.");
		}
	}
	
	public MeshColor(final Mesh nmesh, BigVolumeBrowser bvb_)
	{
		bvb  = bvb_;
		
		if(nmesh != null)
		{
			meshVis = new VisMeshColor( nmesh );
			boundingBox = Meshes.boundingBox( nmesh );
		}
	}
	
	@Override
	public RealInterval boundingBox()
	{
		return boundingBox;
	}
	
	public void setPointsRender(final float fPointsSize_)
	{
		if(meshVis != null )
		{
			meshVis.setRenderType( VisMeshColor.POINTS );
			meshVis.setPointsSize( fPointsSize_ );
			bvb.repaintBVV();
		}
	}
	
	
	public void setSurfaceRender(final int nSurfaceRenderType)
	{
		if(meshVis != null )
		{
			meshVis.setRenderType( VisMeshColor.MESH );
			meshVis.setSurfaceRenderType( nSurfaceRenderType );
			bvb.repaintBVV();
		}
	}
	
	public void setSurfaceGrid(final int nSurfaceGridType)
	{
		if(meshVis != null )
		{
			meshVis.setRenderType( VisMeshColor.MESH );
			meshVis.setSurfaceGridType( nSurfaceGridType );
			bvb.repaintBVV();
		}
	}
	public void setCartesianGrid(final float cartesianGridStep_, final float cartesianFraction_)
	{
		if(meshVis != null )
		{
			meshVis.setCartesianGrid( cartesianGridStep_, cartesianFraction_ );
			bvb.repaintBVV();
		}
	}
	
	public void setColor(final Color colorin)
	{
		if(meshVis != null )
		{
			meshVis.setColor( colorin );
			bvb.repaintBVV();
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

	@Override
	public void draw( GL3 gl, Matrix4fc pvm, Matrix4fc vm, int[] screen_size )
	{
		if(bVisible)
		{
			if(meshVis != null)
			{
				if(nTimePoint<0 || nTimePoint == bvb.bvvViewer.state().getCurrentTimepoint())
				{
					meshVis.draw( gl, pvm, vm, screen_size );
				}
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
			if(nTimePoint<0)
			{
				return "mesh"+this.hashCode();
			}
			return "mesh_t" + Integer.toString( nTimePoint ) + "_" + this.hashCode();
		}
		return sName;
	}

	@Override
	public void reload()
	{
		meshVis.reload();
		
	}

	@Override
	public void setVisible( boolean bVisible_ )
	{
		bVisible = bVisible_;
		bvb.repaintBVV();
		
	}
	
//	private static Mesh createMeshWithNoise(final double dScale)
//	{
//		final RealPoint p1 = new RealPoint( 0., 0., 0. );
//
//		final RealPoint p2 = new RealPoint( dScale, 0., 0. );
//
//		final RealPoint p3 = new RealPoint( dScale, dScale, 0.0 );
//
//	    final RealPoint p4 = new RealPoint( dScale, dScale, dScale );
//		
//	    final Mesh mesh = new NaiveDoubleMesh();
//
//		// Make mesh with two triangles sharing two points with each other.
//		// The points are a bit off in the third decimal digit.
//		mesh.vertices().add( p1.getDoublePosition( 0 ) + 0.001, p1.getDoublePosition( 1 ) - 0.001, p1.getDoublePosition( 2 ) - 0.004 );
//		mesh.vertices().add( p2.getDoublePosition( 0 ) + 0.004, p2.getDoublePosition( 1 ) - 0.000, p2.getDoublePosition( 2 ) + 0.002 );
//		mesh.vertices().add( p3.getDoublePosition( 0 ) - 0.002, p3.getDoublePosition( 1 ) + 0.003, p3.getDoublePosition( 2 ) + 0.001 );
//		mesh.triangles().add( 0, 1, 2 );
//		mesh.vertices().add( p2.getDoublePosition( 0 ) + dScale, p2.getDoublePosition( 1 ) + dScale, p2.getDoublePosition( 2 ) + dScale);
//		mesh.vertices().add( p4.getDoublePosition( 0 ) + 0.004, p4.getDoublePosition( 1 ) - 0.000, p4.getDoublePosition( 2 ) + 0.002 );
//		mesh.vertices().add( p3.getDoublePosition( 0 ) + dScale, p3.getDoublePosition( 1 ) + dScale, p3.getDoublePosition( 2 )+ dScale );
//		mesh.triangles().add( 3, 4, 5 );
//		//mesh.triangles().add( 0, 4, 5 );
//		return mesh;
//	}
}
