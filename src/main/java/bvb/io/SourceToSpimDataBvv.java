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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import net.imglib2.FinalDimensions;
import net.imglib2.Volatile;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;

import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.util.volatiles.VolatileTypeMatcher;
import bdv.viewer.Source;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;

public class SourceToSpimDataBvv
{
	/** wraps UnsignedByte, UnsignedShort, UnsignedLong or Float type source to a cached spimdata 
	 * (of UnsignedShort type) to display in BVV, otherwise returns null **/
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static < T extends RealType< T > & NativeType< T >, V extends Volatile< T > & NativeType < V >> AbstractSpimData< ? > spimDataSourceWrap(final Source<?> src_)
	{		
		final Object type = src_.getType() ;
		
		if(!(type instanceof RealType  && type instanceof NativeType))
		{
			System.err.println( "Sources with pixel type " + type.getClass().getName() + " currently not supported in BigVolumeBrowser.");
			return null;
		}
		Volatile volatileType = ( Volatile )VolatileTypeMatcher.getVolatileTypeForType(( T ) type);
		final SourceImgLoaderBvv imgLoader;
		if( !needsConvertion(type) )
		{
			imgLoader = new SourceImgLoaderBvv(src_, (T) type,  volatileType );
		}
		else
		{
			imgLoader = new SourceImgLoaderBvv(src_, new UnsignedShortType(), new VolatileUnsignedShortType());			
		}
		
		int numTimepoints = 0;
		
		final FinalDimensions size = new FinalDimensions( src_.getSource( 0, 0 ));
		
		while(src_.isPresent( numTimepoints ))
			numTimepoints++;

		final HashMap< Integer, BasicViewSetup > setups = new HashMap<>( 1 );
		
		final BasicViewSetup setup = new BasicViewSetup( 0, src_.getName(), size, src_.getVoxelDimensions() );
		setups.put( 0, setup );
		final ArrayList< TimePoint > timepoints = new ArrayList<>( numTimepoints );
		for ( int t = 0; t < numTimepoints; ++t )
			timepoints.add( new TimePoint( t ) );
		final SequenceDescriptionMinimal seq = new SequenceDescriptionMinimal( new TimePoints( timepoints ), setups, imgLoader, null );
		final ArrayList< ViewRegistration > registrations = new ArrayList<>();
		for ( int t = 0; t < numTimepoints; ++t )
		{
			AffineTransform3D transform = new AffineTransform3D();
			registrations.add( new ViewRegistration( t, 0, transform ) );
		}
		File dummy = null;
		return new AbstractSpimData<>( dummy, seq, new ViewRegistrations( registrations) );
	}
	
	public static boolean needsConvertion (Object type)
	{
		if(type instanceof UnsignedByteType || 
				type instanceof UnsignedShortType ||
				type instanceof FloatType )
			{return false;}
		  return true;
	}
}
