package bvb.shapes;

import java.awt.Color;
import java.util.ArrayList;

import net.imglib2.RealPoint;

import bvb.core.BigVolumeBrowser;
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
		testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube.xml" );
		
		Points testPoints = new Points(100.f, Color.RED, Points.RENDER_OUTLINE, Points.SHAPE_SQUARE);
		
		final ArrayList<RealPoint> vertices = new ArrayList<>();
		
		int nTotNumber = 20;
		
		double nScale = 500.;
		
		for(int i=0;i<nTotNumber; i++)
		{
			vertices.add( new RealPoint(new double[] {Math.random()*nScale, Math.random()*nScale, Math.random()*nScale}));
		}
		
		testPoints.setPoints( vertices );
		testBVB.addShape( testPoints );
	}
}
