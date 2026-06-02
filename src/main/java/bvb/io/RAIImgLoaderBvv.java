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

import static net.imglib2.img.basictypeaccess.AccessFlags.VOLATILE;

import java.util.HashMap;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.algorithm.blocks.BlockAlgoUtils;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.LoaderCache;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.LoadedCellCacheLoader;
import net.imglib2.cache.ref.WeakRefLoaderCache;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.ArrayDataAccessFactory;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.view.Views;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;
import bdv.cache.CacheControl;
import bdv.cache.SharedQueue;
import bdv.util.volatiles.VolatileTypeMatcher;
import bdv.util.volatiles.VolatileViews;
import bvb.core.BVVSettings;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;


public class RAIImgLoaderBvv<T extends NativeType<T>, 
							V extends Volatile<T> & NativeType<V>, 
							 A extends ArrayDataAccess< A >> 
							implements ViewerImgLoader
{
		
	final RandomAccessibleInterval<T> raiXYZTC;
	
	final long[] dimensions;
	
	final int numScales;
	
	private static final double[][] mipmapResolutions = new double[][] { { 1, 1, 1 } };

	private static final AffineTransform3D[] mipmapTransforms =
			new AffineTransform3D[] { new AffineTransform3D() };
	
	final SharedQueue queue;
	
	private final HashMap<Integer, RAISetupLoader> setupImgLoaders;
	
	@SuppressWarnings( "unchecked" )
	public RAIImgLoaderBvv(final RandomAccessibleInterval<T> rai_, final long [] dims_, final int numSetups)
	{
		
		int numThreads =
		        Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
		
		queue = new SharedQueue(numThreads, 5);
		
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
		//TODO maybe check if it is multires

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
		
		public RAISetupLoader (final int setupId, final T type, final V volatileType)
		{
			super(type, volatileType);
			this.setupId = setupId;
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
			return VolatileViews.wrapAsVolatile(prepareCachedImage(timepointId, level), queue);
		}

		@Override
		public RandomAccessibleInterval< T > getImage( int timepointId, int level, ImgLoaderHint... hints )
		{
			return Views.hyperSlice(Views.hyperSlice( raiXYZTC, 4, setupId), 3, timepointId);
		}
		
		@SuppressWarnings( "hiding" )
		protected CachedCellImg< T, ? >
		prepareCachedImage(final int timepointId, @SuppressWarnings( "unused" ) final int level)
		{
			RandomAccessibleInterval< T > rai =  Views.hyperSlice(Views.hyperSlice( raiXYZTC, 4, setupId), 3, timepointId);
			if(rai instanceof CachedCellImg)
			{
				@SuppressWarnings( { "rawtypes", "unchecked" } )
				final CachedCellImg< T, ? > raiCached = (CachedCellImg)rai;
				final Set< AccessFlags > flags = AccessFlags.ofAccess( raiCached );
				if ( flags.contains( VOLATILE ) )
					return ( CachedCellImg< T, ? > ) rai;				}
			
			final long[] dimensions =
		            rai.dimensionsAsLongArray();

		    final int[] cellDimensions = new int[rai.numDimensions()];
		    for ( int d = 0; d < cellDimensions.length; d++ )
			{
		    	cellDimensions[ d ] = BVVSettings.cacheBlockSize;
			}

	        final CellGrid grid =
	                new CellGrid(dimensions, cellDimensions);

	        final CellLoader<T> cellLoader =  BlockAlgoUtils.cellLoader(BlockSupplier.of(rai, PrimitiveBlocks.OnFallback.ACCEPT ));

	        
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

	}

	
	@Override
	public RAISetupLoader getSetupImgLoader(final int setupId) {
		return setupImgLoaders.get(setupId);
	}
	
	@Override
	public CacheControl getCacheControl()
	{
		return queue;
	}


}
