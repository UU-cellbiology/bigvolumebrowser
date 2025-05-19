package bvb.transform;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.transformation.TransformedSource;
import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.Source;

import bdv.viewer.SourceToConverterSetupBimap;


public class TransformTranslation
{
	private final SourceToConverterSetupBimap bimap;
	
	private final Map< ConverterSetup, double[]> setupToTranslation = new HashMap<>();

	public TransformTranslation( final SourceToConverterSetupBimap bimap)
	{
		this.bimap = bimap;
	}
	
	public double[] getTranslation( final ConverterSetup setup )
	{
		double [] out =  setupToTranslation.get( setup );
		if(out == null)
		{
			out = getCurrentOrDefaultTranslation(setup);
			setTranslation( setup, out );
		}		
		return out;
	}
	
	public void updateTransation(final ConverterSetup setup)
	{
		setTranslation( setup, getCurrentOrDefaultTranslation(setup));
	}
	
	public void setTranslation( final ConverterSetup setup, final double[] centers)
	{
		setupToTranslation.put( setup, centers );
	}
	
	public double [] getCurrentOrDefaultTranslation(final ConverterSetup setup)
	{
		Source< ? > src = bimap.getSource( setup ).getSpimSource();
		
		AffineTransform3D srcTr = new AffineTransform3D();
		(( TransformedSource< ? > )src).getFixedTransform( srcTr );
		final double out [] = new double [3];
		
		for (int d = 0; d < 3; d++ )
		{
			out[d] = srcTr.get( d, 3 );
		}
		
		return out;		
		//setup.g
//		AffineTransform3D clipTr = new AffineTransform3D();
//		((GammaConverterSetup)setup).getClipTransform(clipTr);
//		FinalRealInterval interval = ((GammaConverterSetup)setup).getClipInterval(); 
//
//		if(interval == null)
//		{
//			final SourceAndConverter< ? > source = bimap.getSource( setup );
//			interval = Misc.getSourceBoundingBoxAllTP(source.getSpimSource());
//
//		}
//		if(interval == null)
//			return null;
//			
//		final double [] center = Misc.getIntervalCenter(interval);
//		
//		clipTr.apply( center, center );
//
//		return center;
		
	}
}
