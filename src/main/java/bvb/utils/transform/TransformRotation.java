package bvb.utils.transform;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.RealInterval;
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
	
	private final Map< Object, double[]> objToAngles = new HashMap<>();
	
	private final Map< Object, double[]> objToQuaternion = new HashMap<>();
		
	public TransformRotation( final SourceToConverterSetupBimap bimap )
	{
		this.bimap = bimap;
	}
	
	public double[] getAngles( final Object obj )
	{
		double [] out =  objToAngles.get( obj );
		if(out == null)
		{
			out = getCurrentEulerAngles(obj);
			setAngles(obj, out);
		}
		
		return out;
	}
	public double[] getQuaternion( final Object obj )
	{
		double [] out =  objToQuaternion.get( obj );
		if(out == null)
		{
			out = getCurrentEulerAngles(obj);
			setAngles(obj, out);
		}
		out =  objToQuaternion.get( obj );
		return out;
	}
	
	public void setAngles( final Object obj, final double[] eAngles)
	{
		objToAngles.put( obj, eAngles );
	}
	

	public void setQuaternion( final Object obj, final double[] quat)
	{
		objToQuaternion.put( obj, quat );
	}
		
	public double [] getCurrentEulerAngles(final Object obj)
	{
		
		final AffineTransform3D trRotation = new AffineTransform3D();
		RealInterval interval = null;
		if(obj instanceof ConverterSetup)
		{
			final ConverterSetup setup = (ConverterSetup)obj;
			final Source< ? > src = bimap.getSource( setup ).getSpimSource();
			final AffineTransform3D srcTrIc = new AffineTransform3D();
			
			//reset both transforms just in case
			(( TransformedSource< ? > )src).getFixedTransform( trRotation );
			(( TransformedSource< ? > )src).getIncrementalTransform( srcTrIc );
			
			trRotation.preConcatenate( srcTrIc );
			interval = Misc.getSourceBoundingBoxAllTP(src);

		}
		else if(obj instanceof BasicShape)
		{
			((BasicShape)obj).getTransform( trRotation );
			interval = ((BasicShape)obj).boundingBox();
		}
		
		final double [] center = Misc.getIntervalCenterNegative( interval );

		trRotation.translate( center );
		
		final double[] qRotation = new double[4];

		Affine3DHelpers.extractRotationAnisotropic( trRotation, qRotation );
		
		objToQuaternion.put( obj, qRotation );
		
		return Misc.quaternionToEulerAngles(qRotation);
		
	}

}
