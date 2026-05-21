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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

public class SourcesToSpimDataBvv
{
	/** wraps UnsignedByte, UnsignedShort, UnsignedLong or Float type list of source to a cached spimdata 
	 * (of UnsignedShort type) to display in BVV, otherwise returns null **/
	@SuppressWarnings( { "rawtypes", "unchecked", "null" } )
	public static < T extends RealType< T > & NativeType< T >, V extends Volatile< T > & NativeType < V >> AbstractSpimData< ? > 
		spimDataSourcesListWrap(final List< Source<?>> srcs, final List< String > srcNames)
	{		
		if(srcs.size() == 0)
			return null;
		
		final int numSetups = srcs.size();
		
		final Object type = srcs.get( 0 ).getType() ;

		for(final Source<?> src:srcs)
		{
			final Object typeS = src.getType();
			if(!(typeS instanceof RealType  && typeS instanceof NativeType))
			{
				System.err.println( "Sources with pixel type " + typeS.getClass().getName() + " currently not supported in BigVolumeBrowser.");
				return null;
			}
			//check if all sources are the same type
			if(!typeS.getClass().equals( type.getClass()))
			{
				System.err.println( "Error during sources list import:\n"
						+ "list should contain sources with the same type, but got " + typeS.getClass().getName() + " and " + type.getClass().getName() +".");
				return null;	
			}
		}
		Volatile volatileType = ( Volatile )VolatileTypeMatcher.getVolatileTypeForType(( T ) type);
		
		final SourcesImgLoaderBvv imgLoader;
		
		if( !needsConvertion(type) )
		{
			imgLoader = new SourcesImgLoaderBvv(srcs, (T) type,  volatileType );
		}
		else
		{
			imgLoader = new SourcesImgLoaderBvv(srcs, new UnsignedShortType(), new VolatileUnsignedShortType());			
		}

		final FinalDimensions size = new FinalDimensions( srcs.get( 0 ).getSource( 0, 0 ));

		int numTimepoints = 0;
		
		for(final Source<?> src:srcs)
		{
			int numTP = 0;
			while(src.isPresent( numTP ))
				numTP++;
			numTimepoints = Math.max( numTP, numTimepoints);
		}
		
		boolean bUseInternalSourceNames = false;
		
		if(srcNames != null)
		{
			if( srcNames.size() == numSetups )
				bUseInternalSourceNames = true;
		}
		final List<String> srcNamesFin = new ArrayList<>();

		for(int setupId = 0; setupId < numSetups; setupId++)
		{
			if (!bUseInternalSourceNames)
				srcNamesFin.add(srcs.get( setupId ).getName() );
			else
				srcNamesFin.add( srcNames.get( setupId ) );
		}			
		
		final HashMap< Integer, BasicViewSetup > setups = new HashMap<>( srcs.size() );
		
		for(int setupId = 0; setupId < numSetups; setupId++)
		{
			setups.put( setupId, new BasicViewSetup( setupId, srcNamesFin.get( setupId ), size, srcs.get( setupId ).getVoxelDimensions() ));
		}
		
		final ArrayList< TimePoint > timepoints = new ArrayList<>( numTimepoints );
		for ( int t = 0; t < numTimepoints; ++t )
			timepoints.add( new TimePoint( t ) );
		
		final SequenceDescriptionMinimal seq = new SequenceDescriptionMinimal( new TimePoints( timepoints ), setups, imgLoader, null );
		
		final ArrayList< ViewRegistration > registrations = new ArrayList<>();
		for (int s = 0; s < numSetups; s++)
		{
			for ( int t = 0; t < numTimepoints; ++t )
			{
				final AffineTransform3D transform = new AffineTransform3D();
				registrations.add( new ViewRegistration( t, s, transform ) );
			}
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
