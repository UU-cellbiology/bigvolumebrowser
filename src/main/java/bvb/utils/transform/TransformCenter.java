package bvb.utils.transform;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.FinalRealInterval;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.Source;

import bdv.viewer.SourceToConverterSetupBimap;
import bvb.shapes.BasicShape;
import bvb.utils.Misc;


public class TransformCenter
{
	private final SourceToConverterSetupBimap bimap;
	
	private final Map< ConverterSetup, double[]> setupToCenters = new HashMap<>();
	private final Map< BasicShape, double[]> shapeToCenters = new HashMap<>();

	public TransformCenter( final SourceToConverterSetupBimap bimap )
	{
		this.bimap = bimap;
	}
	
	public double[] getCenters( final ConverterSetup setup )
	{
		double [] out =  setupToCenters.get( setup );
		if(out == null)
		{
			out = getDefaultCenters(setup);
			setCenters( setup, out );
		}		
		return out;
	}
	
	public double[] getCenters( final BasicShape shape )
	{
		double [] out =  shapeToCenters.get( shape );
		if(out == null)
		{
			out = getDefaultCenters(shape);
			setCenters( shape, out );
		}		
		return out;
	}
	
	public void updateCenters(final ConverterSetup setup)
	{
		setCenters( setup, getDefaultCenters(setup));
	}

	public void updateCenters(final BasicShape shape)
	{
		setCenters( shape, getDefaultCenters(shape));
	}

	public void setCenters( final ConverterSetup setup, final double[] centers)
	{
		setupToCenters.put( setup, centers );
	}	
	
	public void setCenters( final BasicShape shape, final double[] centers)
	{
		shapeToCenters.put( shape, centers );
	}
	
	public double [] getDefaultCenters(final ConverterSetup setup)
	{
		final Source< ? > src = bimap.getSource( setup ).getSpimSource();
				
		FinalRealInterval interval = Misc.getSourceBoundingBoxAllTP(src);

		return Misc.getIntervalCenter(interval);		
		
	}
	public double [] getDefaultCenters(final BasicShape shape)
	{				
		return Misc.getIntervalCenter(shape.boundingBox());		
		
	}
}
