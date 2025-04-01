package bvb.utils;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;

import bdv.tools.brightness.ConverterSetup;

import bdv.viewer.SourceAndConverter;
import bdv.viewer.SourceToConverterSetupBimap;
import bvvpg.source.converters.GammaConverterSetup;

public class ClipCenters
{
	private final SourceToConverterSetupBimap bimap;
	
	private final Map< ConverterSetup, double[]> setupToCenters = new HashMap<>();
	
	public ClipCenters( final SourceToConverterSetupBimap bimap)
	{
		this.bimap = bimap;
	}
	
	public double[] getCenters( final ConverterSetup setup )
	{
		double [] out =  setupToCenters.get( setup );
		if(out == null)
		{
			out = getCurrentOrDefaultCenters(setup);
			setCenters( setup, out );
		}		
		return out;
	}
	
	public void updateCenters(final ConverterSetup setup)
	{
		setCenters( setup, getCurrentOrDefaultCenters(setup));
	}
	
	public void setCenters( final ConverterSetup setup, final double[] centers)
	{
		setupToCenters.put( setup, centers );
	}
	
	public double [] getCurrentOrDefaultCenters(final ConverterSetup setup)
	{
		AffineTransform3D clipTr = new AffineTransform3D();
		((GammaConverterSetup)setup).getClipTransform(clipTr);
		FinalRealInterval interval = ((GammaConverterSetup)setup).getClipInterval(); 

		if(interval == null)
		{
			final SourceAndConverter< ? > source = bimap.getSource( setup );
			if ( source != null )
			{
				//get the range over all timepoints
				int t = 0;
				while(source.getSpimSource().isPresent( t ))
				{
					if(interval == null)
					{
						interval = Misc.getSourceBoundingBox(source.getSpimSource(),t,0);
					}
					else
					{
						interval = Intervals.union( interval, Misc.getSourceBoundingBox(source.getSpimSource(),t,0));
					}
						
					t++;
				}
			}
		}
		if(interval == null)
			return null;
			
		final double [] center = Misc.getIntervalCenter(interval);
		
		clipTr.apply( center, center );

		return center;
		
	}
}
