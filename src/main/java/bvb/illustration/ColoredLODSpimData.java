package bvb.illustration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


import net.imglib2.FinalDimensions;

import net.imglib2.realtransform.AffineTransform3D;


import bdv.spimdata.SequenceDescriptionMinimal;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;

public class ColoredLODSpimData
{
	public static  AbstractSpimData< ? > 
	getLODSpimData(final long [] dims, final int numScales)
	{	
		final FinalDimensions size = new FinalDimensions(dims);

		int numTimepoints = 0;
		final HashMap< Integer, BasicViewSetup > setups = new HashMap<>(1);
		final FinalVoxelDimensions voxelSize = new FinalVoxelDimensions("pixels", 1.0,
				1.0, 1.0);
		setups.put( 0, new BasicViewSetup( 0, "LOD", size, voxelSize ));
		final ArrayList< TimePoint > timepoints = new ArrayList<>( numTimepoints );
		timepoints.add( new TimePoint( 0 ) );
		
		final ColoredLODLoader imgLoader = new ColoredLODLoader(dims, numScales );
		final SequenceDescriptionMinimal seq = new SequenceDescriptionMinimal( new TimePoints( timepoints ), setups, imgLoader, null );
		final ArrayList< ViewRegistration > registrations = new ArrayList<>();
		AffineTransform3D transform = new AffineTransform3D();
		registrations.add( new ViewRegistration( 0, 0, transform ) );
		File dummy = null;
		return new AbstractSpimData<>( dummy, seq, new ViewRegistrations( registrations ) );
	}
}
