package bvb.shapes;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.impl.naive.NaiveDoubleMesh;
import net.imglib2.mesh.impl.nio.BufferMesh;
import net.imglib2.mesh.io.stl.STLMeshIO;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import bvb.core.BigVolumeBrowser;
import ij.ImageJ;

public class Example002Mesh
{
	public static void main( final String[] args )
	{
				
		new ImageJ();

		//start BVB
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 		
		testBVB.startBVB();
		
		//load some data
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube.xml" );
		
		
		//add sphere with random values as background
		
		int nRadius = 35;
		int maxInt = 200;
		final RandomAccessibleInterval< UnsignedByteType > sphereRai = RandomHyperSphere.generateRandomSphere(nRadius, maxInt);
		
		testBVB.addRAI( sphereRai );
		
		//load mesh
		String fn = "/home/eugene/Desktop/StanfordBunny_fixed.stl";	
		
		MeshExample meshBVB = new MeshExample(fn);
		
		testBVB.addShape( meshBVB );
		
	}
}
