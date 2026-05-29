package bvb.illustration;

import bdv.ViewerImgLoader;
import bdv.ViewerSetupImgLoader;
import bdv.AbstractViewerSetupImgLoader;
import bdv.cache.CacheControl;
import bdv.cache.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import bvb.core.BVVSettings;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;

import net.imglib2.img.array.ArrayImgs;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;

import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;


public class ColoredLODLoader  implements ViewerImgLoader
{
	final SharedQueue queue; 

	final ColoredLODImgLoader loader;
	
	public ColoredLODLoader(final long [] dims, final int numScales)
	{
		int numThreads =
		        Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
		
		queue = new SharedQueue(numThreads);

		loader = new ColoredLODImgLoader(dims, numScales);
		
	}
	@Override
	public ViewerSetupImgLoader< ?, ? > getSetupImgLoader( int setupId )
	{
		return loader;
	}

	@Override
	public CacheControl getCacheControl()
	{
		return queue;
	}
	
	public class ColoredLODImgLoader extends AbstractViewerSetupImgLoader<UnsignedShortType, VolatileUnsignedShortType> {
		
		final int numScales;
		
		final AffineTransform3D [] mipmapTransforms;
		
		final double [][] mipmapResolutions; 
		final long [] zeroDims;
		
		public ColoredLODImgLoader( final long [] dims, final int numScales )
		{
			super( new UnsignedShortType(), new VolatileUnsignedShortType() );
			
			zeroDims = dims;
			this.numScales = numScales;
			mipmapResolutions = new double[ numScales ][];
			mipmapTransforms = new AffineTransform3D[numScales];
			
			for(int i = 0; i < numScales; i++)
			{
				double dScale = Math.pow( 2, i ) ;
				AffineTransform3D transform = new AffineTransform3D();
				transform.scale(dScale);
				double dShift = 0.5 * dScale - 0.5;
				transform.translate( dShift, dShift, dShift );
				mipmapTransforms[ i ] = transform;
				
				mipmapResolutions[i] = new double [3];
				for(int d = 0; d < 3; d++)
				{
					mipmapResolutions[i][d] = dScale;
				}		
			}
		}

		@Override
		public RandomAccessibleInterval< VolatileUnsignedShortType > getVolatileImage( int timepointId, int level, ImgLoaderHint... hints )
		{
			return VolatileViews.wrapAsVolatile( prepareCachedImage( level), queue);
		}

		@Override
		public RandomAccessibleInterval< UnsignedShortType > getImage( int timepointId, int level, ImgLoaderHint... hints )
		{	        
			return getRAI(level);
		}
		
		RandomAccessibleInterval< UnsignedShortType > getRAI(int level)
		{
			final long [] currDims = new long[3];
			for(int d = 0; d < 3; d++)
				currDims [d] = ( long ) ( zeroDims[d] /Math.pow(2,level) );
			final RandomAccessibleInterval< UnsignedShortType > rai = ArrayImgs.unsignedShorts(  currDims );
	        int value = (int)Math.round( 65535.0 - (1+level)*(65535.0/(numScales + 1.0)));
	        rai.forEach( t -> t.set( value ) );
	        return rai;
		}
		
		protected CachedCellImg<UnsignedShortType, ?>
		prepareCachedImage(final int level)
		{
			final RandomAccessibleInterval< UnsignedShortType > rai = getRAI(level);
		    final long[] dimensions =
		            rai.dimensionsAsLongArray();
			 final int[] cellDimensions = new int[] {BVVSettings.cacheBlockSize};
			    BlockSupplier< UnsignedShortType > blocks = BlockSupplier.of( rai );

			    return new ReadOnlyCachedCellImgFactory().create(
						dimensions,
						blocks.getType(),
						cellLoader( blocks ),
						ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions ) );
		}

		@Override
		public double[][] getMipmapResolutions()
		{
			return mipmapResolutions;
		}

		@Override
		public AffineTransform3D[] getMipmapTransforms()
		{
			return mipmapTransforms;
		}

		@Override
		public int numMipmapLevels()
		{
			return numScales;
		}
	
	}
	public static < T extends NativeType< T > >
	CellLoader< T > cellLoader( final BlockSupplier< T > blocks )
	{
		final BlockSupplier< T > ts = blocks.threadSafe();
		return cell -> ts.copy( cell, cell.getStorageArray() );
	}

}
