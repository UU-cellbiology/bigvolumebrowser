package bvb.utils.clip;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.FinalRealInterval;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.BoundedRange;

import bdv.viewer.SourceAndConverter;
import bdv.viewer.SourceToConverterSetupBimap;
import bvb.utils.Bounds3D;
import bvb.utils.Misc;
import bvvpg.source.converters.GammaConverterSetup;

/** a set of 3D bounds defining clipping volumes **/
public class ClipAxesBounds
{
	private final SourceToConverterSetupBimap bimap;

	private final Map< ConverterSetup, Bounds3D > setupToBounds = new HashMap<>();
	
	
	public ClipAxesBounds( final SourceToConverterSetupBimap bimap )
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

		if(setup instanceof GammaConverterSetup)
		{
			GammaConverterSetup gsetup = (GammaConverterSetup)setup;
			final FinalRealInterval clipInterval = gsetup.getClipInterval();
			final double [] min = clipInterval.minAsDoubleArray( );
			final double [] max = clipInterval.maxAsDoubleArray( );
			
			final BoundedRange [] range = new BoundedRange[3];
			for (int d=0; d<3; d++)
			{
					range[d] = new BoundedRange( min[d], max[d], min[d], max[d] ).withMinBound( bounds.getMinBound()[d] ).withMaxBound( bounds.getMaxBound()[d] );
			}
			boolean bUpdate = false;
			for(int d=0;d<3;d++)
			{
				if ( range[d].getMin() != min[d] || range[d].getMax() != max[d] )
				{
					min[d] = range[d].getMin();
					max[d] = range[d].getMax();
					bUpdate = true;
				}
			}
			if(bUpdate)
			{
				gsetup.setClipInterval( new FinalRealInterval(min,max) );
			}
		}
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
			System.out.println("error in estimation of clipping bounds, no source found");
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
