package bvb.utils.transform;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.util.Affine3DHelpers;
import bdv.viewer.Source;
import bdv.viewer.SourceToConverterSetupBimap;
import bvb.shapes.BasicShape;
import bvb.utils.Misc;

public class TransformRotation
{
	private final SourceToConverterSetupBimap bimap;
	
	private final Map< ConverterSetup, double[]> setupToAngles = new HashMap<>();
	
	private final Map< BasicShape, double[]> shapeToAngles = new HashMap<>();
	
	public TransformRotation( final SourceToConverterSetupBimap bimap )
	{
		this.bimap = bimap;
	}
	
	public double[] getAngles( final ConverterSetup setup )
	{
		double [] out =  setupToAngles.get( setup );
		if(out == null)
		{
			out = getCurrentEulerAngles(setup);
			setAngles(setup, out);
		}
		
		return out;
	}
	
	public double[] getAngles( final BasicShape shape )
	{
		double [] out =  shapeToAngles.get( shape  );
		if(out == null)
		{
			out = getCurrentEulerAngles(shape);
			setAngles(shape, out);
		}
		
		return out;
	}
	
	public void setAngles( final ConverterSetup setup, final double[] eAngles)
	{
		setupToAngles.put( setup, eAngles );
	}
	
	public void setAngles( final BasicShape shape, final double[] eAngles)
	{
		shapeToAngles.put( shape, eAngles );
	}
	
	public double [] getCurrentEulerAngles(final ConverterSetup setup)
	{
		
		final Source< ? > src = bimap.getSource( setup ).getSpimSource();
		
		AffineTransform3D srcTrFixed = new AffineTransform3D();
		final AffineTransform3D srcTrIc = new AffineTransform3D();
		
		//reset both transforms just in case
		(( TransformedSource< ? > )src).getFixedTransform( srcTrFixed );
		(( TransformedSource< ? > )src).getIncrementalTransform( srcTrIc );
		
		srcTrFixed = srcTrFixed.preConcatenate( srcTrIc );
		final FinalRealInterval interval = Misc.getSourceBoundingBoxAllTP(src);
		
		final double [] center = Misc.getIntervalCenterNegative( interval );

		srcTrFixed.translate( center );
		final double[] qRotation = new double[4];

		Affine3DHelpers.extractRotationAnisotropic( srcTrFixed, qRotation );
		
		return Misc.quaternionToEulerAngles(qRotation);
		
	}
	
	public double [] getCurrentEulerAngles(final BasicShape shape)
	{
		
		AffineTransform3D srcTrFixed = new AffineTransform3D();
		shape.getTransform( srcTrFixed );
		
		final double [] center = Misc.getIntervalCenterNegative( shape.boundingBox());

		srcTrFixed.translate( center );
		
		final double[] qRotation = new double[4];

		Affine3DHelpers.extractRotationAnisotropic( srcTrFixed, qRotation );
		
		return Misc.quaternionToEulerAngles(qRotation);
		
	}

}
