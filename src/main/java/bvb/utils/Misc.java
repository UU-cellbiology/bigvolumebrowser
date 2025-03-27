package bvb.utils;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;

import bdv.tools.transformation.TransformedSource;
import bdv.viewer.Source;

public class Misc
{
	public static FinalRealInterval getSourceBoundingBox(final Source<?> source, int nTimePoint, int baseLevel)
	{
		AffineTransform3D transformSource = new AffineTransform3D();
		(( TransformedSource< ? > ) source).getSourceTransform(nTimePoint, baseLevel, transformSource);
		double [] min = source.getSource( nTimePoint, baseLevel ).minAsDoubleArray();
		double [] max = source.getSource( nTimePoint, baseLevel ).maxAsDoubleArray();
		//extend to include all range
		for(int d=0; d<3; d++)
		{
			min[d] -= 0.5;
			max[d] += 0.5;
		}
		FinalRealInterval interval = new FinalRealInterval(min, max);
		return transformSource.estimateBounds( interval );
	}
	
	public static double quaternionToAngle(int nAxis, double [] q)
	{
		double sin;
		double cos;
		switch (nAxis)
		{
		case 0:
			sin = 2 * (q[0] * q[1] + q[2] * q[3]);
			cos = 1 - 2 * (q[1] * q[1] + q[2] * q[2]);
			return Math.atan2( sin, cos);
		case 1:
			sin = Math.sqrt(1 + 2 * (q[0] * q[2] - q[1] * q[3]));
			cos = Math.sqrt(1 - 2 * (q[0] * q[2] - q[1] * q[3]));
			return 2.0*Math.atan2(sin, cos) - Math.PI*0.5;
		case 2:
			sin = 2 * (q[0] * q[3] + q[1] * q[2]);
			cos = 1 - 2 * (q[2] * q[2] + q[3] * q[3]);
			return Math.atan2(sin, cos);
		default:
			return 0.0;
		}
	}
	
	public static double [] getIntervalCenterShift(RealInterval interval)
	{
		if(interval == null)
			return new double [3];
		final double [] min = interval.minAsDoubleArray();
		final double [] max = interval.maxAsDoubleArray();
		
		
		for(int d=0;d<3;d++)
		{
			min[d] = -0.5*(max[d]+min[d]);
		}
		
		return min;
		
	}
}
