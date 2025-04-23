package bvb.shapes;

import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.impl.naive.NaiveDoubleMesh;
import net.imglib2.mesh.io.ply.PLYMeshIO;
import net.imglib2.mesh.io.stl.STLMeshIO;

import org.apache.commons.io.FilenameUtils;
import org.joml.Matrix4fc;

import bvb.scene.VisMeshColor;

public class MeshColorExample implements Shape
{
	
	VisMeshColor meshVis = null;
	
	public MeshColorExample(String filename)
	{
		//load mesh from file
		Mesh nmesh = loadMeshFromFile(filename);
		
		//Mesh nmesh = createMeshWithNoise(15);
		
		if(nmesh != null)
		{			
			meshVis = new VisMeshColor(nmesh);
		}
		else
		{
			System.err.println("Sorry, cannot load mesh. Only STL and PLY formats are supported for now.");
		}
	}
	
	public MeshColorExample(final Mesh nmesh)
	{
		if(nmesh != null)
		{
			meshVis = new VisMeshColor(nmesh);
		}
	}
	
	public void setPointsRender(final float fPointsSize_)
	{
		if(meshVis != null )
		{
			meshVis.setRenderType( VisMeshColor.POINTS );
			meshVis.setPointsSize( fPointsSize_ );
		}
	}
	
	public void setSurfaceRender(final int nSurfaceRenderType)
	{
		if(meshVis != null )
		{
			meshVis.setRenderType( VisMeshColor.MESH );
			meshVis.setSurfaceRenderType( nSurfaceRenderType );
		}
	}
	
	public void setSurfaceGrid(final int nSurfaceGridType)
	{
		if(meshVis != null )
		{
			meshVis.setRenderType( VisMeshColor.MESH );
			meshVis.setSurfaceGridType( nSurfaceGridType );
		}
	}
	public void setCartesianGrid(final float cartesianGridStep_, final float cartesianFraction_)
	{
		if(meshVis != null )
		{
			meshVis.setCartesianGrid( cartesianGridStep_, cartesianFraction_ );
		}
	}
	
	public void setColor(final Color colorin)
	{
		if(meshVis != null )
		{
			meshVis.setColor( colorin );
		}
	}
	
	public static Mesh loadMeshFromFile(String filename)
	{
		String fileExt = FilenameUtils.getExtension(filename);
		
		NaiveDoubleMesh nmesh = new NaiveDoubleMesh();
				
		if(fileExt.equals( "stl" ))
		{
			try
			{
				STLMeshIO.read( nmesh, new File( filename ) );
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
				PLYMeshIO.read( new File( filename ), nmesh );
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
		if(meshVis != null)
			meshVis.draw( gl, pvm, vm, screen_size );
	}
	

	@Override
	public void reload()
	{
		meshVis.reload();
		
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
