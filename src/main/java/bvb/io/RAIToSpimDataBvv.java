package bvb.io;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import bdv.spimdata.SequenceDescriptionMinimal;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.Channel;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;

public class RAIToSpimDataBvv
{
	/** very limited and simple loader for now.
	 * Expects RAI dimensions >= 3 in the format of XYZTC **/
	public static AbstractSpimData<?> getSpimData(final RandomAccessibleInterval<?> rai)
	{
		
		Object type = rai.getType() ;
		
		if(!(type instanceof UnsignedShortType  || type instanceof UnsignedByteType))
		{
			System.err.println( "Error: RAI import of type " + type + " is currently not supported.");
			return null;
		}
		int raiNdim = rai.numDimensions(); 
		if(rai.numDimensions() < 3)
		{
			System.err.println( "Error: RAI dimensions < 3, only volumetric RAI are currently supported.");
			return null;			
		}
		long[] dims = rai.dimensionsAsLongArray();
		int numTimepoints = 1;
		int numSetups = 1;
		FinalDimensions size; 
		switch(raiNdim)
		{
			case 3:
				//numSetups = 1;
				size = new FinalDimensions( rai);
				break;
			case 4:
				//numSetups = 1;
				numTimepoints = ( int ) dims[3];
				size = new FinalDimensions( Views.hyperSlice( rai, 3, 0 ));
				break;
			case 5:
				numSetups = ( int ) dims[4];
				numTimepoints = ( int ) dims[3];
				size = new FinalDimensions( Views.hyperSlice(Views.hyperSlice( rai, 4, 0 ),3,0));
				break;
			default:
				return null;
		}
		final HashMap< Integer, BasicViewSetup > setups = new HashMap<>( 1 );
		
		final FinalVoxelDimensions voxelSize = new FinalVoxelDimensions("pixels", 1.0,
				1.0, 1.0);
		
		for(int i=0;i<numSetups;i++)
		{
			final BasicViewSetup setup = new BasicViewSetup( i, "channel_"+Integer.toString( i+1 ), size, voxelSize);			
			setup.setAttribute(new Channel(i + 1));
			setups.put( i, setup );
			
		}
		
		final ArrayList< TimePoint > timepoints = new ArrayList<>( numTimepoints );
		for ( int t = 0; t < numTimepoints; ++t )
			timepoints.add( new TimePoint( t ) );
		
		final RAIImgLoaderBvv imgLoader = new RAIImgLoaderBvv(rai, size.dimensionsAsLongArray(), numSetups);
		
		final SequenceDescriptionMinimal seq = new SequenceDescriptionMinimal( new TimePoints( timepoints ), setups, imgLoader, null );

		final ArrayList< ViewRegistration > registrations = new ArrayList<>();
		for ( int t = 0; t < numTimepoints; ++t )
		{
			for(int i=0;i<numSetups;i++)
			{
				AffineTransform3D transform = new AffineTransform3D();
				//scale transform already in the multires, no need
				//src_.getSourceTransform( t,0, transform );
				registrations.add( new ViewRegistration( t, i, transform ) );
			}
		}
		File dummy = null;
		return new AbstractSpimData<>( dummy, seq, new ViewRegistrations( registrations) );
	}
}
