package bvb.utils.transform;

import java.util.HashMap;
import java.util.Map;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.SourceToConverterSetupBimap;
import bvb.utils.Bounds3D;
import bvb.utils.Misc;

public class TransformCenterBounds
{
	private final SourceToConverterSetupBimap bimap;

	private final Map< ConverterSetup, Bounds3D > setupToBounds = new HashMap<>();
	
	public TransformCenterBounds( final SourceToConverterSetupBimap bimap )
	{
		this.bimap = bimap;
	}
	
	public Bounds3D getBounds( final ConverterSetup setup )
	{
		return setupToBounds.compute( setup, this::getExtendedBounds );
	}
	
	public void setBounds( final ConverterSetup setup, final Bounds3D bounds )
	{
		setupToBounds.put( setup, bounds );

	}
	
	public Bounds3D getDefaultBounds( final ConverterSetup setup )
	{
		Bounds3D bounds = null;

		final SourceAndConverter< ? > sac = bimap.getSource( setup );
		if ( sac != null )
		{			
			//get the range over all timepoints
			bounds = new Bounds3D(Misc.getSourceBoundingBoxAllTP(sac.getSpimSource()));
		}
		else
		{
			System.out.println("error in estimation of center bounds, no source found");
		}
		return bounds;
	}

	private Bounds3D getExtendedBounds( final ConverterSetup setup, Bounds3D bounds )
	{
		if ( bounds == null )
			bounds = getDefaultBounds( setup );
		else
		{
			 bounds.join( getDefaultBounds( setup ) );
		}
//		if(setup instanceof GammaConverterSetup)
//		{
//			GammaConverterSetup gsetup = (GammaConverterSetup)setup;
//			final FinalRealInterval clipInterval = gsetup.getClipInterval();
//			if(clipInterval == null)
//				return bounds;
//			
//			return bounds.join( new Bounds3D( clipInterval) );
//		}
		return bounds;
	}
}
