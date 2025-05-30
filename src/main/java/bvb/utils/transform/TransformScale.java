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
	
	private final Map< ConverterSetup, double[]> setupToScale = new HashMap<>();
	private final Map< BasicShape, double[]> shapeToScale = new HashMap<>();
	
	public TransformScale( final SourceToConverterSetupBimap bimap)
	{
		this.bimap = bimap;
	}
	
	public double[] getScale( final ConverterSetup setup )
	{
		double [] out = setupToScale.get( setup );
		if(out == null)
		{
			out = getCurrentOrDefaultScale(setup);
			setScale( setup, out );
		}		
		return out;
	}
	
	public double[] getScale( final BasicShape shape )
	{
		double [] out = shapeToScale.get( shape );
		if(out == null)
		{
			out = getCurrentOrDefaultScale(shape);
			setScale( shape, out );
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
	
	public void setScale( final ConverterSetup setup, final double[] scales)
	{
		setupToScale.put( setup, scales );
	}
	
	public void setScale( final BasicShape shape, final double[] scales)
	{
		shapeToScale.put( shape, scales );
	}
	
	public double [] getCurrentOrDefaultScale(final ConverterSetup setup)
	{
		Source< ? > src = bimap.getSource( setup ).getSpimSource();
		
		AffineTransform3D srcTr = new AffineTransform3D();
		AffineTransform3D srcInc = new AffineTransform3D();
		(( TransformedSource< ? > )src).getFixedTransform( srcTr );
		(( TransformedSource< ? > )src).getIncrementalTransform( srcInc );
		
		srcTr = srcTr.preConcatenate( srcInc );
		
		final double out [] = new double [3];
		
		for (int d = 0; d < 3; d++ )
		{
			out[d] = Affine3DHelpers.extractScale( srcTr, d );
		}
		
		return out;				
	}
	public double [] getCurrentOrDefaultScale(final BasicShape shape)
	{
		
		AffineTransform3D shapeTr = new AffineTransform3D();

		shape.getTransform( shapeTr );
		
		final double out [] = new double [3];
		
		for (int d = 0; d < 3; d++ )
		{
			out[d] = Affine3DHelpers.extractScale( shapeTr, d );
		}
		
		return out;				
	}
}
