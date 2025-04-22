package bvb.shapes;

import java.awt.Color;
import java.util.ArrayList;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.hypersphere.HyperSphereCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import bvb.core.BigVolumeBrowser;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import ij.ImageJ;

public class ExamplePoints
{
	public static void main( final String[] args )
	{
				
		new ImageJ();

		//start BVB
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 		
		testBVB.startBVB();
		
		//load some data
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube.xml" );
		
		int nRadius = 35;
		int maxInt = 200;
		RandomAccessibleInterval< UnsignedByteType > sphereRai = RandomHyperSphere.generateRandomSphere(nRadius, maxInt);

		testBVB.addRAI( sphereRai );

		//define point size, color, shape and filling
		Points testPoints = new Points(nRadius*0.2f, Color.RED, Points.SHAPE_SQUARE, Points.RENDER_OUTLINE);
		
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
