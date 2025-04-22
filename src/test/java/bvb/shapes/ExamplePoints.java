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
import net.imglib2.img.basictypeaccess.array.ShortArray;
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
		//start BVB
		
		new ImageJ();

		//ij.command().run(ConfigureBVVRenderWindow.class,true).get();
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 
		
		testBVB.startBVB();
		//load some data
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube.xml" );
		
		int nRadius = 35;
		
		//Let's make a hyperSphere (3D ball) with random intensity values 
		long [] dim = new long[] {2*nRadius+2,2*nRadius+2,2*nRadius+2};
		Point center = new Point( 3 );
		center.setPosition( nRadius+1 , 0 );
		center.setPosition( nRadius+1 , 1 );
		center.setPosition( nRadius+1 , 2 );
		
		ArrayImg< UnsignedShortType, ShortArray > sphereRai1 = ArrayImgs.unsignedShorts(dim);
		HyperSphere< UnsignedShortType > hyperSphere1 =
				new HyperSphere<>( sphereRai1, center, nRadius);			

		ArrayImg< UnsignedShortType, ShortArray > sphereRai2 = ArrayImgs.unsignedShorts(dim);
		HyperSphere< UnsignedShortType > hyperSphere2 =
				new HyperSphere<>( sphereRai2, center, nRadius);	
		
		HyperSphereCursor< UnsignedShortType > cursor1 = hyperSphere1.localizingCursor();
		HyperSphereCursor< UnsignedShortType > cursor2 = hyperSphere2.localizingCursor();
		
		while ( cursor1.hasNext() )
		{
			cursor1.fwd();
			cursor2.fwd();
			cursor1.get().setInteger( Math.round(Math.random()*255.0) );
			cursor2.get().setInteger( Math.round(Math.random()*255.0) );
		}
		ArrayList<RandomAccessibleInterval<?>> both = new ArrayList<>();
		both.add( ( RandomAccessibleInterval< ? > ) Views.addDimension( sphereRai1,0,0 ));
		both.add( ( RandomAccessibleInterval< ? > ) Views.addDimension( sphereRai2,0,0));
		RandomAccessibleInterval< ? > sphereRai = Views.stack( both );

		testBVB.addRAI( sphereRai );
		//final BvvSource source = BvvFunctions.show( sphereRai, "sphere_left" );
		//source.setDisplayRange( 0, 255 );
//		//define point size, color, shape and filling
//		Points testPoints = new Points(100.f, Color.RED, Points.SHAPE_SQUARE, Points.RENDER_OUTLINE);
//		
//		final ArrayList<RealPoint> vertices = new ArrayList<>();
//		
//		int nTotNumber = 20;
//		
//		double nScale = 500.;
//		
//		for(int i=0;i<nTotNumber; i++)
//		{
//			vertices.add( new RealPoint(new double[] {Math.random()*nScale, Math.random()*nScale, Math.random()*nScale}));
//		}
//		
//		testPoints.setPoints( vertices );
//		testBVB.addShape( testPoints );
	}
}
