package bvb.transform;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.util.Affine3DHelpers;
import bdv.viewer.Source;
import bdv.viewer.SourceToConverterSetupBimap;

public class TransformScale
{
	private final SourceToConverterSetupBimap bimap;
	
	private final Map< ConverterSetup, double[]> setupToScale = new HashMap<>();
	
	public TransformScale( final SourceToConverterSetupBimap bimap)
	{
		this.bimap = bimap;
	}
	
	public double[] getScale( final ConverterSetup setup )
	{
		double [] out =  setupToScale.get( setup );
		if(out == null)
		{
			out = getCurrentOrDefaultScale(setup);
			setScale( setup, out );
		}		
		return out;
	}
	
	public void updateScale(final ConverterSetup setup)
	{
		setScale( setup, getCurrentOrDefaultScale(setup));
	}
	
	public void setScale( final ConverterSetup setup, final double[] centers)
	{
		setupToScale.put( setup, centers );
	}
	
	public double [] getCurrentOrDefaultScale(final ConverterSetup setup)
	{
		Source< ? > src = bimap.getSource( setup ).getSpimSource();
		
		AffineTransform3D srcTr = new AffineTransform3D();
		(( TransformedSource< ? > )src).getFixedTransform( srcTr );
		
		final double out [] = new double [3];
		
		for (int d = 0; d < 3; d++ )
		{
			out[d] = Affine3DHelpers.extractScale( srcTr, d );
		}
		
		return out;				
	}
}
