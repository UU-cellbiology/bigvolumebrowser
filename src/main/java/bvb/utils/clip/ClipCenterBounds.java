package bvb.utils.clip;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.FinalRealInterval;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.SourceToConverterSetupBimap;
import bvb.utils.Bounds3D;
import bvb.utils.Misc;
import bvvpg.source.converters.GammaConverterSetup;

public class ClipCenterBounds
{
	private final SourceToConverterSetupBimap bimap;

	private final Map< ConverterSetup, Bounds3D > setupToBounds = new HashMap<>();
	
	public ClipCenterBounds( final SourceToConverterSetupBimap bimap )
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

		final SourceAndConverter< ? > source = bimap.getSource( setup );
		if ( source != null )
		{
			//get the range over all timepoints
			int t = 0;
			while(source.getSpimSource().isPresent( t ))
			{
				if(bounds == null)
				{
					bounds = new Bounds3D(Misc.getSourceBoundingBox(source.getSpimSource(),t,0));
				}
				else
				{
					bounds = bounds.join( new Bounds3D(Misc.getSourceBoundingBox(source.getSpimSource(),t,0)) );
				}
					
				t++;
			}
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
		if(setup instanceof GammaConverterSetup)
		{
			GammaConverterSetup gsetup = (GammaConverterSetup)setup;
			final FinalRealInterval clipInterval = gsetup.getClipInterval();
			if(clipInterval == null)
				return bounds;
			
			return bounds.join( new Bounds3D( clipInterval) );
		}
		return bounds;
	}
	
}
