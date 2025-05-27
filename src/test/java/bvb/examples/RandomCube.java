package bvb.examples;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;



public class RandomCube
{
	public static RandomAccessibleInterval<UnsignedByteType> generateRandomCubeByte(final int nEdgeSize, final int nMaxInt)
	{
		long [] dim = new long[] {nEdgeSize,nEdgeSize,nEdgeSize};
		ArrayImg< UnsignedByteType, ByteArray > cubeRai = ArrayImgs.unsignedBytes(dim);
		
		ArrayCursor< UnsignedByteType > cursor = cubeRai.cursor();
		int nFinMaxInt = Math.min( 255, nMaxInt );
		nFinMaxInt = Math.max( 10, nFinMaxInt );
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.get().setInteger( Math.round(Math.random()*nFinMaxInt) +1);
		}
		return cubeRai;
	}
	
	public static RandomAccessibleInterval<UnsignedShortType> generateRandomCubeShort(final int nEdgeSize, final int nMaxInt)
	{
		long [] dim = new long[] {nEdgeSize,nEdgeSize,nEdgeSize};
		ArrayImg< UnsignedShortType, ShortArray > cubeRai = ArrayImgs.unsignedShorts(dim);
		
		ArrayCursor< UnsignedShortType > cursor = cubeRai.cursor();
		int nFinMaxInt = Math.min( 255, nMaxInt );
		nFinMaxInt = Math.max( 10, nFinMaxInt );
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.get().setInteger( Math.round(Math.random()*nFinMaxInt) );
		}
		return cubeRai;
	}

}
