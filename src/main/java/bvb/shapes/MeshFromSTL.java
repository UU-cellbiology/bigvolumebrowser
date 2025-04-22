package bvb.shapes;

import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.impl.naive.NaiveDoubleMesh;
import net.imglib2.mesh.impl.nio.BufferMesh;
import net.imglib2.mesh.io.stl.STLMeshIO;

import org.joml.Matrix4fc;

import bvb.scene.VisWireMesh;

public class MeshFromSTL implements Shape
{
	Mesh mesh = null;
	VisWireMesh meshVis;
	public MeshFromSTL(String filename)
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

		mesh = new BufferMesh( nmesh.vertices().size(), nmesh.triangles().size(), true );
		Meshes.calculateNormals( nmesh, mesh );
		meshVis = new VisWireMesh((BufferMesh)mesh, 6.0f, Color.CYAN, VisWireMesh.WIRE );
	}

	@Override
	public void draw( GL3 gl, Matrix4fc pvm, Matrix4fc vm, int[] screen_size )
	{
		meshVis.draw( gl, pvm, vm );
	}
}
