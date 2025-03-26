package bvb.utils;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;


import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.util.BoundedRange;

import bdv.viewer.SourceAndConverter;
import bdv.viewer.SourceToConverterSetupBimap;
import bvvpg.source.converters.GammaConverterSetup;

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

	private Bounds3D getDefaultBounds( final ConverterSetup setup )
	{
		Bounds3D bounds;

		final SourceAndConverter< ? > source = bimap.getSource( setup );
		if ( source != null )
		{
			//get transform
			AffineTransform3D transformSource = new AffineTransform3D();
			(( TransformedSource< ? > ) source.getSpimSource() ).getSourceTransform(0, 0, transformSource);
			double [] min = source.getSpimSource().getSource( 0, 0 ).minAsDoubleArray();
			double [] max = source.getSpimSource().getSource( 0, 0 ).maxAsDoubleArray();
			//extend to include all range
			for(int d=0; d<3; d++)
			{
				min[d] -= 0.5;
				max[d] += 0.5;
			}
			FinalRealInterval interval = new FinalRealInterval(min, max);
			interval = transformSource.estimateBounds( interval );
			bounds = new Bounds3D(interval);
		}
		else
		{
			System.out.println("error in estimation of cliping bounds, no source found");
			bounds = null;
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
