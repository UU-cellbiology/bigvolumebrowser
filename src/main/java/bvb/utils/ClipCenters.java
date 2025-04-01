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
		double [] out =  setupToCenters.get( setup );
		String s = "";
		if(out!=null)
		{
			for (int d=0;d<3;d++)
				s = s + Double.toString( out[d] )+" ";
			System.out.println("Old: " +s);
		}
		out = getCurrentOrDefaultCenters(setup);
//		double [] corr = Misc.getIntervalCenter( ((GammaConverterSetup)setup).getClipInterval() ); 
//		for (int d=0;d<3;d++)
//			out[d]+=corr[d];

		s = "";
		for (int d=0;d<3;d++)
			s = s + Double.toString( out[d] )+" ";

		System.out.println("New: " +s);

		setCenters( setup, out);
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
		
		clipTr = clipTr.inverse();
		
		final double [] center = Misc.getIntervalCenter(interval);
		clipTr.apply( center, center );
		//clipTr.apply( center, center );
		String s = "";
		for (int d=0;d<3;d++)
			s = s + Double.toString( center[d] )+" ";

		System.out.println("center: " +s);
		
//		double [] min = interval.minAsDoubleArray();
//		double [] max = interval.maxAsDoubleArray();
//
//		double [] out = new double[3];
//		clipTr.apply( min, min );
//		clipTr.apply( max, max );
		
//		s = "";
//		for (int d=0;d<3;d++)
//			s = s + Double.toString( min[d] )+" ";
//		System.out.println("min: " +s);
//
//		s = "";
//		for (int d=0;d<3;d++)
//			s = s + Double.toString( max[d] )+" ";
//		System.out.println("max: " +s);
//
//		
//		for(int d=0;d<3;d++)
//		{
//			out[d] = 0.5*(max[d]+min[d]);
//		}
//		s = "";
//		for (int d=0;d<3;d++)
//			s = s + Double.toString( out[d] )+" ";
//		System.out.println("min max: " +s);
//		
//		out = Misc.getIntervalCenter(clipTr.estimateBounds( interval ));
//		s = "";
//		for (int d=0;d<3;d++)
//			s = s + Double.toString( out[d] )+" ";
//		System.out.println("bounding box: " +s);
		return center;
		
	}
}
