/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.converter.Converters;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.img.basictypeaccess.DataAccess;
import net.imglib2.img.basictypeaccess.volatiles.VolatileAccess;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileByteArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileFloatArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;

import bdv.cache.CacheControl;
import bdv.img.cache.CacheArrayLoader;
import bdv.img.cache.VolatileCachedCellImg;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.viewer.Source;
import bvb.utils.Misc;

import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.generic.sequence.TypedBasicImgLoader;


/** **/
public class SourcesImgLoaderBvv < T extends RealType< T > & NativeType< T >, 
								  V extends Volatile< T > & NativeType < V >, 
								  A extends DataAccess & VolatileAccess> 
								  implements ViewerImgLoader, TypedBasicImgLoader<T>
{
	final List<Source<?>> srcs;
	
	private VolatileGlobalCellCache cache;
	
	private final HashMap<Integer, SourceSetupImgLoader> setupImgLoaders;	
	
	public SourcesImgLoaderBvv(final  List<Source< ? >> srcs, final T type, final V volatileType)
	{
		this.srcs = srcs;	

		int nMaxScales = 1;
		
		for(final Source<?> src:srcs)
		{
			nMaxScales = Math.max(nMaxScales, src.getNumMipmapLevels());
		}
		final int numFetcherThreads = Math.max(2,
		        Runtime.getRuntime().availableProcessors() / 2);
		cache = new VolatileGlobalCellCache( nMaxScales + 1, numFetcherThreads );
		
		setupImgLoaders = new HashMap<>();
		
		for (int setupId = 0; setupId < srcs.size(); setupId++)
		{
			setupImgLoaders.put(setupId, new SourceSetupImgLoader(srcs.get( setupId ), 
					setupId, type, volatileType));
		}			
	}
	
	@Override
	public CacheControl getCacheControl()
	{		
		return cache;
	}
	
	@Override
	public SourceSetupImgLoader getSetupImgLoader( int setupId )
	{
		return setupImgLoaders.get(setupId);
	}
	
	
	public class SourceSetupImgLoader extends AbstractViewerSetupImgLoader<T, V> {

		final Source<?> src;
		
		private final int setupId;
		
		final CacheArrayLoader<A> loader;
		
		final int numScales;
		
		final AffineTransform3D [] mipmapTransforms;
		
		final double [][] mipmapResolutions; 
		
		final private boolean bConvertSrc;

		protected SourceSetupImgLoader(final Source<?> src_, final int setupId, final T type,
								 final V volatileType)
		{
			super(type, volatileType);
			this.src = src_; 
			this.setupId = setupId;
			loader = new SourceArrayLoader<>(this.src);
			numScales = src.getNumMipmapLevels();
			
			mipmapTransforms = new AffineTransform3D[numScales];

			mipmapResolutions = new double[ numScales ][];
			
			final AffineTransform3D transformSource = new AffineTransform3D();
			src.getSourceTransform( 0, 0, transformSource );
			
			final double [] zeroScale = Misc.getScale( transformSource );

			for(int i = 0; i < numScales; i++)
			{
				AffineTransform3D transform = new AffineTransform3D();
				src.getSourceTransform( 0, i, transform );			
				mipmapTransforms[ i ] = transform;
				
				double [] currScale = Misc.getScale( transform );
				mipmapResolutions[i] = new double [3];
				for(int d = 0; d < 3; d++)
				{
					mipmapResolutions[i][d] = currScale[d]/zeroScale[d];
				}		
			}
			bConvertSrc = SourcesToSpimDataBvv.needsConvertion(type);
			
		}

		@Override
		public RandomAccessibleInterval<V> getVolatileImage(final int timepointId,
															final int level, final ImgLoaderHint... hints)
		{
			return prepareCachedImage(timepointId, level,
					LoadingStrategy.VOLATILE, volatileType);
		}
		
		@SuppressWarnings( { "unchecked", "rawtypes" } )
		@Override
		public RandomAccessibleInterval getImage(final int timepointId,
													final int level, final ImgLoaderHint... hints)
		{
			final RandomAccessibleInterval< ? > raiXYZ = src.getSource( timepointId, level );
			
			if(!bConvertSrc)
				return raiXYZ;
			
			return convertIntegerRAIToShort(raiXYZ);
		}

		@SuppressWarnings( "hiding" )
		protected <T extends NativeType<T>> VolatileCachedCellImg<T, A>
		prepareCachedImage(final int timepointId, final int level,
						   final LoadingStrategy loadingStrategy, final T typeCache)
		{
			final long[] dimensions = src.getSource( timepointId, level ).dimensionsAsLongArray();
			
			final int priority = numScales - 1 - level;
			
			final CacheHints cacheHints = new CacheHints( loadingStrategy, priority, false );
			
			final int[] cellDimensions = new int [] {32,32,32};
			
			final CellGrid grid = new CellGrid(dimensions, cellDimensions);
			return cache.createImg(grid, timepointId, setupId, level, cacheHints,
					loader, typeCache);
		}

		@Override
		public double[][] getMipmapResolutions() {
			return mipmapResolutions;
		}

		@Override
		public AffineTransform3D[] getMipmapTransforms() {
			return mipmapTransforms;
		}

		@Override
		public int numMipmapLevels() {
			return numScales;
		}
	}
	
	
	static class SourceArrayLoader <A extends DataAccess> implements CacheArrayLoader<A> 
	{	
		final Source<?> src;
		final boolean bConvert;
		int bytesPerElement;
		
		public SourceArrayLoader (final Source<?> source_)
		{
			src = source_;
			final Object type = src.getType();
			bConvert = SourcesToSpimDataBvv.needsConvertion(type);
			bytesPerElement = calculateBytesPerElement(type);				
		}
		
		@Override
		public int getBytesPerElement() {
			return bytesPerElement;
		}

		@SuppressWarnings( { "unchecked" } )
		@Override
		public A loadArray( int timepoint, int setup, int level, int[] dimensions, long[] min ) throws InterruptedException
		{
			final RandomAccessibleInterval< ? > raiXYZ = src.getSource( timepoint, level );
			
			final int numElements = dimensions[0] * dimensions[1] * dimensions[2];
				
			final long[][] intRange = new long [2][3];
			
			for(int d = 0; d < 3; d++)
			{
				intRange[0][d]= min[d];
				intRange[1][d]= min[d] + dimensions[d] - 1;
			}

			final IterableInterval< ? > iterRAI;
			if(!bConvert)
			{
				iterRAI = Views.flatIterable( Views.interval( raiXYZ, new FinalInterval(intRange[0],intRange[1])));				
			}
			else
			{
				iterRAI = Views.flatIterable( convertIntegerRAIToShort(Views.interval( raiXYZ, new FinalInterval(intRange[0],intRange[1]))));				
			}
			
			if(raiXYZ.getType() instanceof UnsignedByteType )
			{
				final byte[] data = new byte[numElements];
				int nCount = 0;
				Cursor< UnsignedByteType > cur = ( Cursor< UnsignedByteType > ) iterRAI.cursor();
				while (cur.hasNext())
				{
					cur.fwd();
					data[nCount] = cur.get().getByte();
					nCount++;
				}
				return ( A ) new VolatileByteArray(data,true);
			}
			else if(raiXYZ.getType() instanceof FloatType )
			{
				final float[] data = new float[numElements];
				int nCount = 0;
				Cursor< FloatType > cur = ( Cursor< FloatType > ) iterRAI.cursor();
				while (cur.hasNext())
				{
					cur.fwd();
					data[nCount] = cur.get().getRealFloat();
					nCount++;
				}
				return ( A ) new VolatileFloatArray(data,true);
			}
			else
			{
				final short[] data = new short[numElements];
				int nCount = 0;
				Cursor< UnsignedShortType > cur = ( Cursor< UnsignedShortType > ) iterRAI.cursor();
				while (cur.hasNext())
				{
					cur.fwd();
					data[nCount] = cur.get().getShort();
					nCount++;
				}
				return ( A ) new VolatileShortArray(data, true);
			}
		}


	}
	@SuppressWarnings( { "rawtypes" } )
	public static RandomAccessibleInterval< UnsignedShortType > convertIntegerRAIToShort(RandomAccessibleInterval< ? > raiXYZ)
	{
			return Converters.convert(
					raiXYZ,
					( i, o ) -> 
					{
						o.setInteger(((IntegerType)i).getInteger());
					},
					new UnsignedShortType( ) );
	}
	
	static int calculateBytesPerElement(Object type) 
	{
		//short or converted (clipped) integer
		int n = 2;
		//System.out.println( "LOADING " + type.getClass().getName() );

		if(type instanceof UnsignedByteType)
		{
			n = 1;
		}
		if(type instanceof FloatType)
		{
			n = 4;
		}		
		return n;
	}

}
