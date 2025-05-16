/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bvb.io;

import java.util.HashMap;

import net.imglib2.Cursor;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.img.basictypeaccess.DataAccess;
import net.imglib2.img.basictypeaccess.volatiles.VolatileAccess;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileByteArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.img.cell.AbstractCellImg;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;
import bdv.cache.CacheControl;
import bdv.img.cache.CacheArrayLoader;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.util.volatiles.VolatileTypeMatcher;
import ch.epfl.biop.bdv.img.CacheControlOverride;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;


public class RAIImgLoaderBvv<T extends NativeType<T>, V extends Volatile<T> & NativeType<V>, A extends DataAccess & VolatileAccess> implements ViewerImgLoader, CacheControlOverride
{
		
	final RandomAccessibleInterval<T> raiXYZTC;
	
	final long[] dimensions;
	
	final int numScales;
	
	private static final double[][] mipmapResolutions = new double[][] { { 1, 1, 1 } };

	private static final AffineTransform3D[] mipmapTransforms =
			new AffineTransform3D[] { new AffineTransform3D() };
	
	private VolatileGlobalCellCache cache;
	
	private final HashMap<Integer, RAISetupLoader> setupImgLoaders;
	
	@SuppressWarnings( "unchecked" )
	public RAIImgLoaderBvv(final RandomAccessibleInterval<T> rai_, final long [] dims_, final int numSetups)
	{
		
		cache = new VolatileGlobalCellCache( 2, 1 );
		
		dimensions = dims_;
		
		int raiNdim = rai_.numDimensions(); 
		
		//convert to XYZTC
		switch(raiNdim)
		{
			case 3:
				raiXYZTC = Views.addDimension(Views.addDimension( rai_, 0, 0 ), 0, 0 );
				break;
			case 4:
				raiXYZTC = Views.addDimension( rai_, 0, 0 );
				break;
			case 5:
				raiXYZTC = rai_;
				break;
			default:
				raiXYZTC = null;
		}

		numScales = 1;
		setupImgLoaders = new HashMap<>();
		for (int setupId = 0; setupId < numSetups; ++setupId)
			setupImgLoaders.put(setupId, new RAISetupLoader(setupId, 
									raiXYZTC.getType(), 
									(V)VolatileTypeMatcher.getVolatileTypeForType( raiXYZTC.getType() )));

	}
	
	
	class RAISetupLoader extends AbstractViewerSetupImgLoader <T, V> 
	{
		
		private final int setupId;
		
		final RAIArrayLoader<T,A> loader;
		
		public RAISetupLoader (final int setupId, final T type, final V volatileType)
		{
			super(type, volatileType);
			this.setupId = setupId;
			loader =  new RAIArrayLoader<>(raiXYZTC);
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
			return 1;
		}

		@Override
		public RandomAccessibleInterval< V > getVolatileImage( int timepointId, int level, ImgLoaderHint... hints )
		{
			return prepareCachedImage(timepointId, level, LoadingStrategy.VOLATILE, volatileType);

		}

		@Override
		public RandomAccessibleInterval< T > getImage( int timepointId, int level, ImgLoaderHint... hints )
		{
				return Views.hyperSlice(Views.hyperSlice( raiXYZTC, 4, setupId), 3, timepointId);
		}
		
		@SuppressWarnings( "hiding" )
		protected <T extends NativeType<T>> AbstractCellImg<T, A, ?, ?>
		prepareCachedImage(final int timepointId, final int level,
						   final LoadingStrategy loadingStrategy, final T typeCache)
		{
			final int priority = -1;
			
			final CacheHints cacheHints = new CacheHints( loadingStrategy, priority, false );
			
			final int[] cellDimensions = new int [] {32,32,32};
			
			final CellGrid grid = new CellGrid(dimensions, cellDimensions);
			return cache.createImg(grid, timepointId, setupId, level, cacheHints,
					loader, typeCache);
		}
		


	}
	
	static class RAIArrayLoader<T extends NativeType<T>,A extends DataAccess> implements CacheArrayLoader<A> 
	{
		final RandomAccessibleInterval<T> rai;
		
		public RAIArrayLoader (final RandomAccessibleInterval<T> rai_)
		{
			rai = rai_;
		}
		@SuppressWarnings( "unchecked" )
		@Override
		public A loadArray( int timepoint, int setup, int level, int[] dimensions, long[] min ) throws InterruptedException
		{
			final RandomAccessibleInterval< T > raiXYZ = Views.hyperSlice(Views.hyperSlice( rai, 4, setup), 3, timepoint);
						
			final long[][] intRange = new long [2][3];
			
			for(int d=0;d<3;d++)
			{
				intRange[0][d]= min[d];
				intRange[1][d]= min[d]+dimensions[d]-1;
			}

			
			if(raiXYZ.getType() instanceof UnsignedShortType)
			{
				Cursor< UnsignedShortType > cur = 
						( Cursor< UnsignedShortType > ) Views.flatIterable( Views.interval( raiXYZ, new FinalInterval(intRange[0],intRange[1]))).cursor();
				final short[] data = new short[dimensions[0]*dimensions[1]*dimensions[2]];
				int nCount = 0;
				while (cur.hasNext())
				{
					cur.fwd();
					data[nCount] = cur.get().getShort();
					nCount++;
				}
				return ( A ) new VolatileShortArray(data,true);
			}
			
			if(raiXYZ.getType() instanceof UnsignedByteType)
			{
				Cursor< UnsignedByteType > cur = 
						( Cursor< UnsignedByteType > ) Views.flatIterable( Views.interval( raiXYZ, new FinalInterval(intRange[0],intRange[1]))).cursor();
				final byte[] data = new byte[dimensions[0]*dimensions[1]*dimensions[2]];
				int nCount = 0;
				while (cur.hasNext())
				{
					cur.fwd();
					data[nCount] = cur.get().getByte();
					nCount++;
				}
				return ( A ) new VolatileByteArray(data,true);
			}
			
			return null;
		}
		
	}
	
	@Override
	public RAISetupLoader getSetupImgLoader(final int setupId) {
		return setupImgLoaders.get(setupId);
	}
	
	@Override
	public CacheControl getCacheControl()
	{
		return cache;
	}
	
	@Override
	public void setCacheControl( VolatileGlobalCellCache cache )
	{
		CacheControlOverride.Tools.shutdownCacheQueue(this.cache);
		this.cache.clearCache();
		this.cache = cache;
		
	}

}
