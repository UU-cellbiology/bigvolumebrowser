package bvb.shapes;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import net.imglib2.view.Views;

public class RAIdummy
{

	/** make a dummy empty interval **/
	public static RandomAccessibleInterval<UnsignedShortType> dummyRAI(final RealInterval interval)
	{
		double [] min = interval.minAsDoubleArray();
		double [] max = interval.maxAsDoubleArray();
		long [][] minmax = new long[2][3];
		for(int d=0; d<3;d++)
		{
			minmax[0][d] = Math.round( Math.floor( min[d] ) );
			minmax[1][d] = Math.round( Math.ceil( max[d] ) );
		}
		
	    ArrayImg< UnsignedShortType, ShortArray > center = ArrayImgs.unsignedShorts(new long [] {1,1,1 });
	    return Views.interval( Views.extendZero( center ), minmax[0], minmax[1] );

	}
}
