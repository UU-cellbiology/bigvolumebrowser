package bvb.shapes;


import java.awt.Color;
import java.io.File;

import net.imglib2.RandomAccessibleInterval;
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
		
		//load and show bunny mesh
		File resource = new File("src/test/resources/mesh/bunny.stl");
		
		MeshExample meshBunny = new MeshExample(resource.toString());
		meshBunny.setPointsRender( 0.3f );
		meshBunny.setColor( Color.CYAN );
		
		testBVB.addShape( meshBunny );
		
	}
}
