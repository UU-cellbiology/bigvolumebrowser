package bvb.examples;

import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.hypersphere.HyperSphereCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class RandomHyperSphere
{
	public static RandomAccessibleInterval<UnsignedByteType> generateRandomSphere(final int nRadius, final int nMaxInt)
	{
		//Let's make a hyperSphere (3D ball) with random intensity values 
		long [] dim = new long[] {2*nRadius+2,2*nRadius+2,2*nRadius+2};
		Point center = new Point( 3 );
		center.setPosition( nRadius+1 , 0 );
		center.setPosition( nRadius+1 , 1 );
		center.setPosition( nRadius+1 , 2 );
		
		ArrayImg< UnsignedByteType, ByteArray > sphereRai = ArrayImgs.unsignedBytes(dim);
		HyperSphere< UnsignedByteType > hyperSphere =
				new HyperSphere<>( sphereRai, center, nRadius);		
		HyperSphereCursor< UnsignedByteType > cursor = hyperSphere.localizingCursor();
		int nFinMaxInt = Math.min( 255, nMaxInt );
		nFinMaxInt = Math.max( 10, nFinMaxInt );
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.get().setInteger( Math.round(Math.random()*nFinMaxInt) );
		}
		return sphereRai;
	}
}
