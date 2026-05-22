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
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;

import net.imglib2.converter.Converters;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;

import bdv.cache.CacheControl;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.Source;
import bvb.core.BVVSettings;
import bvb.utils.Misc;

import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.generic.sequence.TypedBasicImgLoader;


/** **/
public class SourcesImgLoaderBvv < T extends RealType< T > & NativeType< T >, 
								  V extends Volatile< T > & NativeType < V >> 
								  implements ViewerImgLoader, TypedBasicImgLoader<T>
{
	final List<Source<?>> srcs;
	
	private final HashMap<Integer, SourceSetupImgLoader> setupImgLoaders;	
	
	public SourcesImgLoaderBvv(final  List<Source< ? >> srcs, final T type, final V volatileType)
	{
		this.srcs = srcs;	
		
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
		return new CacheControl.Dummy();
	}
	
	@Override
	public SourceSetupImgLoader getSetupImgLoader( int setupId )
	{
		return setupImgLoaders.get(setupId);
	}
	
	
	public class SourceSetupImgLoader extends AbstractViewerSetupImgLoader<T, V> {

		final Source<?> src;
		
		final int numScales;
		
		final AffineTransform3D [] mipmapTransforms;
		
		final double [][] mipmapResolutions; 
		
		final private boolean bConvertSrc;

		protected SourceSetupImgLoader(final Source<?> src_, final T type,
								 final V volatileType)
		{
			super(type, volatileType);
			
			this.src = src_; 
			
			//loader = new SourceArrayLoader<>(this.src);
			
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
			return VolatileViews.wrapAsVolatile( prepareCachedImage(timepointId, level));
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
		protected CachedCellImg<T, ?>
		prepareCachedImage(final int timepointId, final int level)
		{
			final RandomAccessibleInterval< T > rai;
			if(!bConvertSrc)
				rai = ( RandomAccessibleInterval< T > ) src.getSource( timepointId, level );
			else
				rai = ( RandomAccessibleInterval< T > ) convertIntegerRAIToShort(src.getSource( timepointId, level ));
		    final long[] dimensions =
		            rai.dimensionsAsLongArray();

		    final int[] cellDimensions = new int[] {BVVSettings.cacheBlockSize};
		    BlockSupplier< T > blocks = BlockSupplier.of( rai );

		    return new ReadOnlyCachedCellImgFactory().create(
					dimensions,
					blocks.getType(),
					cellLoader( blocks ),
					ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions ) );
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
	
	public static < T extends NativeType< T > >
	CellLoader< T > cellLoader( final BlockSupplier< T > blocks )
	{
		final BlockSupplier< T > ts = blocks.threadSafe();
		return cell -> ts.copy( cell, cell.getStorageArray() );
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
