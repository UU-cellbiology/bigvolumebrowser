package bvb.examples;

import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.ValuePair;

import bvb.core.BigVolumeBrowser;
import bvvpg.vistools.BvvStackSource;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import mpicbg.spim.data.generic.AbstractSpimData;

public class Example001AddingData
{
	public static void main( final String[] args )
	{
				
		new ImageJ();

		//start BVB
		BigVolumeBrowser bvbTest = new BigVolumeBrowser(); 		
		bvbTest.startBVB("");
		
		
		//make and add ImagePlus
		ImagePlus imp = IJ.createHyperStack( "test ImageJ", 100, 100, 2, 100, 3, 8 );
		ij.plugin.HyperStackMaker.labelHyperstack( imp );
		bvbTest.addImagePlus( imp );
		
		//add BDV xml
		final ValuePair< AbstractSpimData< ? >, List< BvvStackSource< ? > > > spimOut = bvbTest.loadBDVHDF5( "src/test/resources/images/t1-head.xml" );
		//adjust brightness
		final BvvStackSource< ? > bvvSource = spimOut.getB().get( 0 );
		bvvSource.setDisplayRange( 0, 500 );
		//set LUT
		bvvSource.setLUT( "Fire" );
		
		//make and add RAI (sphere with random values)
		int nRadius = 35;
		int maxInt = 200;
		final RandomAccessibleInterval< UnsignedByteType > sphereRai = RandomHyperSphere.generateRandomSphere(nRadius, maxInt);		
		bvbTest.addRAI( sphereRai );
		
		//focus on everything
		bvbTest.bvbActions.actionCenterView();

	}
}
