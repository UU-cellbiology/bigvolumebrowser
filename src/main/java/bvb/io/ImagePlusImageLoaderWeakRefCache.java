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
import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.ArrayDataAccessFactory;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;
import bdv.cache.CacheControl;
import bdv.cache.SharedQueue;
import bdv.util.volatiles.VolatileTypeMatcher;
import bdv.util.volatiles.VolatileViews;
import bvb.core.BVVSettings;
import ij.ImagePlus;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.generic.sequence.TypedBasicImgLoader;

public class ImagePlusImageLoaderWeakRefCache < T extends RealType< T > & NativeType< T >, 
										V extends Volatile< T > & NativeType < V >,
										A extends ArrayDataAccess< A >> 
										implements ViewerImgLoader, TypedBasicImgLoader<T>
{

	final SharedQueue queue; 
	
	final RandomAccessibleInterval< ? > raiWrap;
	
	private final HashMap<Integer, ImpSetupImgLoader> setupImgLoaders;	
	
	private static final double[][] mipmapResolutions = new double[][] { { 1, 1, 1 } };

	private static final AffineTransform3D[] mipmapTransforms =
			new AffineTransform3D[] { new AffineTransform3D() };

	
	@SuppressWarnings( "unchecked" )
	public ImagePlusImageLoaderWeakRefCache(final ImagePlus imp, final T type)
	{
		int numThreads =
		        Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
		
		queue = new SharedQueue(numThreads);
		
		
		final int numSetups = imp.getNChannels();
		//let's convert it to XYZTC
		raiWrap = wrapImagePlusToRAIXYZTC( imp );
		
		setupImgLoaders = new HashMap<>();
		
		final Volatile<T> volatileType = ( Volatile< T > ) VolatileTypeMatcher.getVolatileTypeForType(type);
		
		for (int setupId = 0; setupId < numSetups; ++setupId)
			setupImgLoaders.put(setupId, new ImpSetupImgLoader(setupId, type,
					(V)volatileType));
		
	}
	
	@Override
	public ImpSetupImgLoader getSetupImgLoader( int setupId )
	{
		return setupImgLoaders.get(setupId);
	}

	@Override
	public CacheControl getCacheControl()
	{
		return queue;
	}
	
	public class ImpSetupImgLoader extends AbstractViewerSetupImgLoader< T, V > 
	{		
		private final int setupId;
		
		protected ImpSetupImgLoader(final int setupId, final T type,
				 final V volatileType)
		{
			super(type, volatileType);
			this.setupId = setupId;
		}

		@Override
		public RandomAccessibleInterval< V > getVolatileImage( int timepointId, int level, ImgLoaderHint... hints )
		{
			return VolatileViews.wrapAsVolatile( prepareCachedImage(timepointId), queue);
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public RandomAccessibleInterval< T > getImage( int timepointId, int level, ImgLoaderHint... hints )
		{
			return ( RandomAccessibleInterval< T > )Views.hyperSlice(Views.hyperSlice( raiWrap, 4, setupId), 3, timepointId);
		}
		
		@SuppressWarnings( "unchecked" )
		protected CachedCellImg< T, ? >
		prepareCachedImage(final int timepointId)
		{
			
			final RandomAccessibleInterval< T > raiBlock = 
					( RandomAccessibleInterval< T > ) Views.hyperSlice(Views.hyperSlice( raiWrap, 4, setupId), 3, timepointId);
	        
		    final int[] cellDimensions = new int[raiBlock.numDimensions()];
		    for ( int d = 0; d < cellDimensions.length; d++ )
			{
		    	cellDimensions[ d ] = ( int ) Math.min( BVVSettings.cacheBlockSize, raiBlock.dimension( d ));
			}
			
			final CellGrid grid =
	                new CellGrid(raiBlock.dimensionsAsLongArray(), cellDimensions);

	        
	        final CellLoader<T> cellLoader =  BlockAlgoUtils.cellLoader(
	        						BlockSupplier.of(raiBlock, PrimitiveBlocks.OnFallback.ACCEPT ));
	        
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
	}
	
	public static RandomAccessibleInterval<?> wrapImagePlusToRAIXYZTC(final ImagePlus imp)
	{
		final Img< ? > raiIn = ImageJFunctions.wrap( imp );
		RandomAccessibleInterval<?> outRAI = raiIn;
		final int nDims = raiIn.numDimensions();
		
		for(int i = 0; i < 5 - nDims; i ++)
		{
			outRAI = Views.addDimension(outRAI, 0, 0);			
		}
		String sDims = getImageJAxesOrder(imp);
		if(sDims.indexOf( 'C' ) != 4)
		{
			outRAI = Views.permute( outRAI, sDims.indexOf( 'C' ) , 4);
			StringBuilder sb = new StringBuilder(sDims);

			sb.setCharAt(sDims.indexOf( 'C' ), sDims.charAt( 4 ));
			sb.setCharAt(4, 'C');
			sDims = sb.toString();
		}
		
		if(sDims.charAt(3) == 'Z')
		{
			outRAI = Views.permute( outRAI, 3 , 2);			
		}
		
		return outRAI;
	}
	
	public static String getImageJAxesOrder( final ImagePlus ip )
	{
		String sDims = "XY";
		boolean bC = false;
		boolean bT = false;
		boolean bZ = false;
		if ( ip.getNChannels() > 1 )
		{
			sDims = sDims + "C";
			bC = true;
		}

		if ( ip.getNSlices() > 1 )
		{
			sDims = sDims + "Z";
			bZ = true;
		}

		if ( ip.getNFrames() > 1 )
		{
			sDims = sDims + "T";
			bT = true;
		}
		if(!bC)
		{
			sDims = sDims + "C";
		}
		if(!bZ)
		{
			sDims = sDims + "Z";
		}
		if(!bT)
		{
			sDims = sDims + "T";
		}

		return sDims;
	}
	
}
