package bvb.utils.transform;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.viewer.Source;

import bdv.viewer.SourceToConverterSetupBimap;
import bvb.shapes.BasicShape;
import bvb.utils.Misc;


public class TransformCenter
{
	private final SourceToConverterSetupBimap bimap;
	
	private final Map< Object, double[]> objToCenters = new HashMap<>();
	
	/** a map to store object's center coordinates,
	 *  to reduce calculations **/
	private final Map< Object, double[] > objToDefCenters = new HashMap<>();

	public TransformCenter( final SourceToConverterSetupBimap bimap )
	{
		this.bimap = bimap;
	}
	
	public double[] getCenters( final Object obj )
	{
		double [] out =  objToCenters.get( obj );
		if(out == null)
		{
			out = getDefaultCenters(obj);
			setCenters( obj, out );
		}		
		return out;
	}
	
	public void updateCenters(final Object obj)
	{
		setCenters( obj, getDefaultCenters(obj));
	}

	public void setCenters( final Object obj, final double[] centers)
	{
		objToCenters.put( obj, centers );
	}	
	
	
	public double [] getDefaultCenters(final Object obj)
	{
		RealInterval interval = null;
		if(obj instanceof ConverterSetup)
		{
			final Source< ? > src = bimap.getSource( (ConverterSetup)obj ).getSpimSource();
			interval = Misc.getSourceBoundingBoxAllTP(src);
		}	
		if(obj instanceof BasicShape)
		{
			interval = ((BasicShape)obj).boundingBox();
		}

		return Misc.getIntervalCenter(interval);			
	}
	
	public double [] getNonTransformedCenters(final Object obj)
	{
		double [] center = objToDefCenters.get(obj);
		if(center != null)
			return center;
		
		if(obj instanceof ConverterSetup)
		{
			final Source< ? > src = bimap.getSource( (ConverterSetup)obj ).getSpimSource();
			RealInterval interval = Misc.getSourceBoundingBoxAllTP(src);
			center = Misc.getIntervalCenter( interval );
			AffineTransform3D srcTrFixed = new AffineTransform3D();
			(( TransformedSource< ? > )src).getFixedTransform( srcTrFixed );
			srcTrFixed.inverse().apply( center, center );
		}

		if(obj instanceof BasicShape)
		{
			center = Misc.getIntervalCenter( ((BasicShape)obj).boundingBoxNotTransformed() );
		}
		objToDefCenters.put( obj, center );
		
		return center;		
	}
}
