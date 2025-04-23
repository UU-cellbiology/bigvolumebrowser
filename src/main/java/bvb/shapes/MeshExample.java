package bvb.shapes;

import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import net.imglib2.RealPoint;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.impl.naive.NaiveDoubleMesh;
import net.imglib2.mesh.impl.nio.BufferMesh;
import net.imglib2.mesh.io.stl.STLMeshIO;

import org.joml.Matrix4fc;

import bvb.scene.VisWireMesh;

public class MeshExample implements Shape
{
	Mesh mesh = null;
	VisWireMesh meshVis;
	public MeshExample(String filename)
	{
		//load mesh

		NaiveDoubleMesh nmesh = new NaiveDoubleMesh();
		try
		{
			STLMeshIO.read( nmesh, new File( filename ) );
		}
		catch ( IOException exc )
		{
			// TODO Auto-generated catch block
			exc.printStackTrace();
			return;
		}

		
		//Mesh nmesh = createMeshWithNoise(15);
		mesh = new BufferMesh( nmesh.vertices().size(), nmesh.triangles().size(), true );
		Meshes.calculateNormals( nmesh, mesh );
		meshVis = new VisWireMesh((BufferMesh)mesh, 6.0f, Color.CYAN, VisWireMesh.SURFACE );
	}

	@Override
	public void draw( GL3 gl, Matrix4fc pvm, Matrix4fc vm, int[] screen_size )
	{
		meshVis.draw( gl, pvm, vm );
	}
	

	private static Mesh createMeshWithNoise(final double dScale)
	{
		final RealPoint p1 = new RealPoint( 0., 0., 0. );

		final RealPoint p2 = new RealPoint( dScale, 0., 0. );

		final RealPoint p3 = new RealPoint( dScale, dScale, 0.0 );

	    final RealPoint p4 = new RealPoint( dScale, dScale, dScale );
		
	    final Mesh mesh = new NaiveDoubleMesh();

		// Make mesh with two triangles sharing two points with each other.
		// The points are a bit off in the third decimal digit.
		mesh.vertices().add( p1.getDoublePosition( 0 ) + 0.001, p1.getDoublePosition( 1 ) - 0.001, p1.getDoublePosition( 2 ) - 0.004 );
		mesh.vertices().add( p2.getDoublePosition( 0 ) + 0.004, p2.getDoublePosition( 1 ) - 0.000, p2.getDoublePosition( 2 ) + 0.002 );
		mesh.vertices().add( p3.getDoublePosition( 0 ) - 0.002, p3.getDoublePosition( 1 ) + 0.003, p3.getDoublePosition( 2 ) + 0.001 );
		mesh.triangles().add( 0, 1, 2 );
		mesh.vertices().add( p2.getDoublePosition( 0 ) + dScale, p2.getDoublePosition( 1 ) + dScale, p2.getDoublePosition( 2 ) + dScale);
		mesh.vertices().add( p4.getDoublePosition( 0 ) + 0.004, p4.getDoublePosition( 1 ) - 0.000, p4.getDoublePosition( 2 ) + 0.002 );
		mesh.vertices().add( p3.getDoublePosition( 0 ) + dScale, p3.getDoublePosition( 1 ) + dScale, p3.getDoublePosition( 2 )+ dScale );
		mesh.triangles().add( 3, 4, 5 );
		//mesh.triangles().add( 0, 4, 5 );
		return mesh;
	}

	@Override
	public void reload()
	{
		meshVis.reload();
		
	}
}
