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

import java.util.Random;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedShortConverter;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;

import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;
import bdv.ViewerSetupImgLoader;
import bdv.cache.CacheControl;
import bdv.img.cache.CacheArrayLoader;
import bdv.img.cache.VolatileCachedCellImg;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.viewer.Source;
import bvb.utils.Misc;
import ij.IJ;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;

public class SourceImgLoaderBvv extends AbstractViewerSetupImgLoader< UnsignedShortType, VolatileUnsignedShortType > implements ViewerImgLoader
{
	final Source<?> src;
	
	final int numScales;
	
	final AffineTransform3D [] mipmapTransforms;
	
	final double [][] mipmapResolutions; 
	
	private VolatileGlobalCellCache cache;
	
	final Object typeIn;
	
	boolean bFloat = false;
	
	double dMin,dMax;
		
	private final CacheArrayLoader<VolatileShortArray> loader;
	
	public SourceImgLoaderBvv(final Source<?> source_)
	{
		super( new UnsignedShortType(), new VolatileUnsignedShortType() );
		
		src = source_;
		numScales = src.getNumMipmapLevels();
		typeIn =  src.getSource( 0, 0 ).getType();
		cache = new VolatileGlobalCellCache( numScales+1, 1 );

		mipmapTransforms = new AffineTransform3D[numScales];

		mipmapResolutions = new double[ numScales ][];
		
		AffineTransform3D transformSource = new AffineTransform3D();
		src.getSourceTransform( 0, 0, transformSource );
		
		final double [] zeroScale = Misc.getScale( transformSource);

		for(int i=0;i<numScales;i++)
		{
			AffineTransform3D transform = new AffineTransform3D();
			src.getSourceTransform( 0, i, transform );			
			mipmapTransforms[i] = transform;
			
			double [] currScale = Misc.getScale( transform );
			mipmapResolutions[i] = new double [3];
			for(int d=0;d<3;d++)
			{
				mipmapResolutions[i][d] = currScale[d]/zeroScale[d];
			}		
		}

		if(!(typeIn instanceof IntegerType) )
		{
			bFloat = true;
			IJ.log( "Estimating Float Source range..." );
			ValuePair< Double, Double > dPair = getMinMax(src); 
			dMin = dPair.getA().doubleValue();
			dMax = dPair.getB().doubleValue();
			IJ.log( "found ["+Double.toString( dMin )+"," +Double.toString( dMax )+"]");
		}
		
		loader = new SourceArrayLoader(src, bFloat, dMin, dMax);
	}

	@Override
	public int numMipmapLevels()
	{
		return numScales;
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
	
	
	protected <T extends NativeType<T>> VolatileCachedCellImg<T, VolatileShortArray>
	prepareCachedImage(final int timepointId, final int level, final int setupId,
					   final LoadingStrategy loadingStrategy, final T typeCache)
	{
		final long[] dimensions = src.getSource( timepointId, level ).dimensionsAsLongArray();
		final int priority = numScales - 1 - level;
		
		final CacheHints cacheHints = new CacheHints( loadingStrategy, priority, false );
		
		//final int[] cellDimensions = new int [] {(int)imageDimensions[level][0],(int)imageDimensions[level][1],1};
		final int[] cellDimensions = new int [] {32,32,32};
		
		final CellGrid grid = new CellGrid(dimensions, cellDimensions);
		return cache.createImg(grid, timepointId, setupId, level, cacheHints,
				loader, typeCache);
	}
	
	@SuppressWarnings( "unchecked" )
	@Override
	public RandomAccessibleInterval< UnsignedShortType > getImage( int timepointId, int level, ImgLoaderHint... hints )
	{
		final RandomAccessibleInterval< ? > raiXYZ = src.getSource( timepointId, level );
		
		if(!bFloat)
		{
			return convertIntegerRAIToShort(raiXYZ);
		}
		return convertRealRAIToShort(( RandomAccessibleInterval< FloatType > ) raiXYZ, dMin, dMax);
			
	}
	
	@Override
	public RandomAccessibleInterval< VolatileUnsignedShortType > getVolatileImage( int timepointId, int level, ImgLoaderHint... hints )
	{		
		return prepareCachedImage(timepointId, level, 0, LoadingStrategy.VOLATILE, volatileType);
	}
	

	@Override
	public CacheControl getCacheControl()
	{		
		return cache;
	}
	
	@Override
	public ViewerSetupImgLoader< ?, ? > getSetupImgLoader( int setupId )
	{
		return this;
	}
	
	public void setCache( final VolatileGlobalCellCache cache )
	{
		this.cache = cache;
	}
		
	
	static class SourceArrayLoader implements CacheArrayLoader<VolatileShortArray> 
	{
		
		final Source<?> src;
		final boolean bFloatType;
		final double dMin, dMax;
		
		public SourceArrayLoader (final Source<?> source_, final boolean bFloat_, final double dMin_, final double dMax_)
		{
			src = source_;
			bFloatType = bFloat_;
			dMin = dMin_;
			dMax = dMax_;
		}
		
		@Override
		public int getBytesPerElement() {
			return 2;
		}

		@Override
		public  VolatileShortArray loadArray( int timepoint, int setup, int level, int[] dimensions, long[] min ) throws InterruptedException
		{
			final RandomAccessibleInterval< ? > raiXYZ = src.getSource( timepoint, level );
			
			final short[] data = new short[dimensions[0]*dimensions[1]*dimensions[2]];
			
			final long[][] intRange = new long [2][3];
			for(int d=0;d<3;d++)
			{
				intRange[0][d]= min[d];
				intRange[1][d]= min[d]+dimensions[d]-1;
			}

			IterableInterval< UnsignedShortType > iterRAI;
			if(!bFloatType)
			{
				iterRAI = Views.flatIterable( convertIntegerRAIToShort(Views.interval( raiXYZ, new FinalInterval(intRange[0],intRange[1]))));				
			}
			else
			{
				iterRAI = Views.flatIterable( convertRealRAIToShort( ( RandomAccessibleInterval< ? > ) Views.interval( raiXYZ, new FinalInterval(intRange[0],intRange[1])), dMin,dMax));
			}
			int nCount = 0;

			Cursor< UnsignedShortType > cur = iterRAI.cursor();
			while (cur.hasNext())
			{
				cur.fwd();
				data[nCount] = cur.get().getShort();
				nCount++;
			}
			return new VolatileShortArray(data,true);
		}


	}
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public static RandomAccessibleInterval< UnsignedShortType > convertIntegerRAIToShort(RandomAccessibleInterval< ? > raiXYZ)
	{
	
		Object typein = raiXYZ.getType();
		if ( typein instanceof UnsignedShortType )
		{
			return (RandomAccessibleInterval <UnsignedShortType >) raiXYZ;
		}
		else if ( typein instanceof UnsignedByteType )
		{
			return Converters.convert(
					raiXYZ,
					( i, o ) -> o.setInteger( ((UnsignedByteType) i).get() ),
					new UnsignedShortType( ) );
		}
		else
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
	
	@SuppressWarnings( "unchecked" )
	public static  <R extends RealType< R > > RandomAccessibleInterval< UnsignedShortType > convertRealRAIToShort(RandomAccessibleInterval< ? > raiXYZ, double minVal, double maxVal)
	{	
		
		return Converters.convert( (RandomAccessibleInterval< R >)raiXYZ, new RealUnsignedShortConverter<>(minVal,maxVal), new UnsignedShortType() );

	}
	
	//taken from LabKit
	//https://github.com/juglab/labkit-ui/blob/01a5c8058459a0d1a2eedc10f7212f64e021f893/src/main/java/sc/fiji/labkit/ui/bdv/BdvAutoContrast.java#L51
	private static ValuePair<Double, Double> getMinMax(final Source<?> src) 
	{
		int level = src.getNumMipmapLevels() - 1;
		RandomAccessibleInterval<?> source = src.getSource(0, level);
		if (source.getType() instanceof RealType)
			return getMinMaxForRealType(Cast.unchecked(source));
		return new ValuePair<>(0.0, 255.0);
	}
	
	private static ValuePair<Double, Double> getMinMaxForRealType(
			RandomAccessibleInterval<? extends RealType<?>> source)
		{
			Cursor<? extends RealType<?>> cursor = source.cursor();
			if (!cursor.hasNext()) return new ValuePair<>(0.0, 255.0);
			long stepSize = Intervals.numElements(source) / 10000 + 1;
			int randomLimit = (int) Math.min(Integer.MAX_VALUE, stepSize);
			Random random = new Random(42);
			double min = cursor.next().getRealDouble();
			double max = min;
			while (cursor.hasNext()) {
				double value = cursor.get().getRealDouble();
				cursor.jumpFwd(stepSize + random.nextInt(randomLimit));
				min = Math.min(min, value);
				max = Math.max(max, value);
			}
			return new ValuePair<>(min, max);
		}

}
