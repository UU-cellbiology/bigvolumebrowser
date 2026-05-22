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

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.LoaderCache;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.LoadedCellCacheLoader;
import net.imglib2.cache.ref.WeakRefLoaderCache;
import net.imglib2.converter.Converters;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.ArrayDataAccessFactory;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;

import bdv.cache.CacheControl;
import bdv.cache.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.Source;
import bvb.core.BVVSettings;
import bvb.utils.Misc;

import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.generic.sequence.TypedBasicImgLoader;


/** **/
public class SourcesImgLoaderBvv < T extends RealType< T > & NativeType< T >, 
								  V extends Volatile< T > & NativeType < V >,
								  A extends ArrayDataAccess< A >> 
								  implements ViewerImgLoader, TypedBasicImgLoader<T>
{
	final List<Source<?>> srcs;
	
	private final HashMap<Integer, SourceSetupImgLoader> setupImgLoaders;	
	
	final SharedQueue queue; 
	
	public SourcesImgLoaderBvv(final  List<Source< ? >> srcs, final T type, final V volatileType)
	{
		this.srcs = srcs;	
		
		int numThreads =
		        Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
		
		queue = new SharedQueue(numThreads);

		setupImgLoaders = new HashMap<>();

		for (int setupId = 0; setupId < srcs.size(); setupId++)
		{
			setupImgLoaders.put(setupId, new SourceSetupImgLoader(srcs.get( setupId ), 
					type, volatileType));
		}			
	}
	
	@Override
	public CacheControl getCacheControl()
	{		
		return queue;
	}
	
	@Override
	public SourceSetupImgLoader getSetupImgLoader( int setupId )
	{
		return setupImgLoaders.get(setupId);
	}
	
	public class SourceSetupImgLoader extends AbstractViewerSetupImgLoader< T, V > {

		final Source<?> src;
		
		final int numScales;
		
		final AffineTransform3D [] mipmapTransforms;
		
		final double [][] mipmapResolutions; 
		
		final private boolean bConvertSrc;

		protected SourceSetupImgLoader(final Source< ? > src_, final T type,
								 final V volatileType)
		{
			super(type, volatileType);
			
			this.src = src_; 
			
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
		public RandomAccessibleInterval< V > getVolatileImage(final int timepointId,
															final int level, final ImgLoaderHint... hints)
		{
			return VolatileViews.wrapAsVolatile( prepareCachedImage(timepointId, level), queue);
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

		@SuppressWarnings( "unchecked" )
		protected CachedCellImg< T, ? >
		prepareCachedImage(final int timepointId, final int level)
		{
			final RandomAccessibleInterval< T > rai;
			if(!bConvertSrc)
				rai = ( RandomAccessibleInterval< T > ) src.getSource( timepointId, level );
			else
				rai = ( RandomAccessibleInterval< T > ) convertIntegerRAIToShort(src.getSource( timepointId, level ));
		   
			if(rai instanceof CachedCellImg)
				return ( CachedCellImg< T, ? > ) rai;
			
			
			final long[] dimensions =
		            rai.dimensionsAsLongArray();

		    final int[] cellDimensions = new int[rai.numDimensions()];
		    for ( int d = 0; d < cellDimensions.length; d++ )
			{
		    	cellDimensions[ d ] = BVVSettings.cacheBlockSize;
			}

	        final CellGrid grid =
	                new CellGrid(dimensions, cellDimensions);

	        final CellLoader<T> cellLoader = cell -> 
	        { 
	            LoopBuilder.setImages(cell, Views.interval(rai, cell) )
	            .forEachPixel( ( a, b ) -> a.set( b ));
	        };
	        
	        final LoaderCache<Long, Cell< A >> cache =
	                new WeakRefLoaderCache<>();
	        
	        CacheLoader< Long, Cell< A > > backingLoader = LoadedCellCacheLoader.get(
			        grid,
			        cellLoader,
			        type,
			        AccessFlags.setOf(AccessFlags.VOLATILE)
			);
	        
	        return new CachedCellImg<>(
	                grid,
	                type,
	                cache.withLoader(backingLoader),
	                ArrayDataAccessFactory.get(type, AccessFlags.setOf( AccessFlags.VOLATILE))
	        );
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

}
