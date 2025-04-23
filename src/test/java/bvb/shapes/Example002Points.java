package bvb.shapes;

import java.awt.Color;
import java.util.ArrayList;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.type.numeric.integer.UnsignedByteType;


import bvb.core.BigVolumeBrowser;
import bvb.scene.VisPointsScaled;
import ij.ImageJ;

public class Example002Points
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

		
		//define point size, color, shape and filling
		PointsSameExample testPoints = new PointsSameExample(nRadius*0.2f, Color.RED, VisPointsScaled.SHAPE_SQUARE, VisPointsScaled.RENDER_OUTLINE);
		
		final ArrayList<RealPoint> vertices = new ArrayList<>();
		
		int nTotNumber = 20;
		
		double nScale = nRadius*2.0;
		
		for(int i=0;i<nTotNumber; i++)
		{
			vertices.add( new RealPoint(new double[] {Math.random()*nScale, Math.random()*nScale, Math.random()*nScale}));
		}
		
		testPoints.setPoints( vertices );
		testBVB.addShape( testPoints );
	}
}
