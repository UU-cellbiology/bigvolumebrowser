package bvb.utils.transform;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.util.Affine3DHelpers;
import bdv.viewer.Source;
import bdv.viewer.SourceToConverterSetupBimap;
import bvb.shapes.BasicShape;

public class TransformScale
{
	private final SourceToConverterSetupBimap bimap;

	
	private final Map< Object, double[]> objToScale = new HashMap<>();

	
	public TransformScale( final SourceToConverterSetupBimap bimap)
	{
		this.bimap = bimap;
	}
	
	public double[] getScale( final Object obj )
	{
		double [] out = objToScale.get( obj );
		if(out == null)
		{
			out = getCurrentOrDefaultScale(obj);
			setScale( obj, out );
		}		
		return out;
	}
	
	
	public void updateScale(final ConverterSetup setup)
	{
		setScale( setup, getCurrentOrDefaultScale(setup));
	}
	
	public void updateScale(final BasicShape shape)
	{
		setScale( shape, getCurrentOrDefaultScale(shape));
	}
	
	public void setScale( final Object obj, final double[] scales)
	{
		objToScale.put( obj, scales );
	}
	
	public double [] getCurrentOrDefaultScale(final Object obj)
	{

		final AffineTransform3D trScale = new AffineTransform3D();

		if(obj instanceof ConverterSetup)
		{
			Source< ? > src = bimap.getSource( (ConverterSetup)obj ).getSpimSource();
		
			final AffineTransform3D srcInc = new AffineTransform3D();
			(( TransformedSource< ? > )src).getFixedTransform( trScale );
			(( TransformedSource< ? > )src).getIncrementalTransform( srcInc );
		
			trScale.preConcatenate( srcInc );
		}
		
		if(obj instanceof BasicShape)
		{
			((BasicShape)obj).getTransform( trScale );
		}
		
		final double out [] = new double [3];
		
		for (int d = 0; d < 3; d++ )
		{
			out[d] = Affine3DHelpers.extractScale( trScale, d );
		}
		
		return out;				
	}
}
