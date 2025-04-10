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
		final AffineTransform3D transformSource = new AffineTransform3D();
		(( TransformedSource< ? > ) source).getSourceTransform(nTimePoint, baseLevel, transformSource);
		final double [] min = source.getSource( nTimePoint, baseLevel ).minAsDoubleArray();
		final double [] max = source.getSource( nTimePoint, baseLevel ).maxAsDoubleArray();
		//extend to include all range
		for(int d=0; d<3; d++)
		{
			min[d] -= 0.5;
			max[d] += 0.5;
		}
		final FinalRealInterval interval = new FinalRealInterval(min, max);
		return transformSource.estimateBounds( interval );
	}
	
	public static FinalRealInterval getSourceBoundingBoxAllTP(final Source<?> source)
	{
		FinalRealInterval interval = null;
		if ( source != null )
		{
			//get the range over all timepoints
			int t = 0;
			while(source.isPresent( t ))
			{
				if(interval == null)
				{
					interval = Misc.getSourceBoundingBox(source,t,0);
				}
				else
				{
					interval = Intervals.union( interval, Misc.getSourceBoundingBox(source,t,0));
				}
					
				t++;
			}
		}
		return interval;
	}

	
	public static double[] getSourceMin(final Source<?> source, int nTimePoint, int baseLevel)
	{
		final AffineTransform3D transformSource = new AffineTransform3D();
		(( TransformedSource< ? > ) source).getSourceTransform(nTimePoint, baseLevel, transformSource);
		final double [] min = source.getSource( nTimePoint, baseLevel ).minAsDoubleArray();
		final double [] max = source.getSource( nTimePoint, baseLevel ).maxAsDoubleArray();
		//extend to include all range
		for(int d=0; d<3; d++)
		{
			min[d] -= 0.5;
			max[d] += 0.5;
		}
		final FinalRealInterval interval =transformSource.estimateBounds( new FinalRealInterval(min, max) );
		
		return interval.minAsDoubleArray();
	}
	
	public static double[] getSourceMinAllTP(final Source<?> source)
	{
		double [] min = null;
		if ( source != null )
		{
			//get the range over all timepoints
			int t = 0;
			while(source.isPresent( t ))
			{
				if(min == null)
				{
					min = Misc.getSourceMin(source,t,0);
				}
				else
				{
					final double [] minCurr = Misc.getSourceMin(source,t,0);
					for(int d=0; d<3; d++)
						min[d] = Math.min( min[d], minCurr[d] );
				}					
				t++;
			}
		}
		return min;
	}
	
	public static FinalRealInterval getSourceSize(final Source<?> source, int nTimePoint, int baseLevel)
	{
		final AffineTransform3D transformSource = new AffineTransform3D();
		(( TransformedSource< ? > ) source).getSourceTransform(nTimePoint, baseLevel, transformSource);
		final double [] min = source.getSource( nTimePoint, baseLevel ).minAsDoubleArray();
		final double [] max = source.getSource( nTimePoint, baseLevel ).maxAsDoubleArray();
		//extend to include all range
		for(int d=0; d<3; d++)
		{
			min[d] -= 0.5;
			max[d] += 0.5;
		}
		final FinalRealInterval interval = transformSource.estimateBounds( new FinalRealInterval(min, max) ) ;
		interval.realMin( min );
		interval.realMax( max );
		for(int d=0; d<3; d++)
		{
			max[d] -= min[d];
			min[d] = 0.0;
		}		
		return new FinalRealInterval( min, max );
	}
	
	
	/** depending on nAxis value, extracts Euler angle (rotation around nAxis)
	 * value (in radians) from the quaternion q.
	 * Follows the formula/code from wiki 
	 * https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles **/
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
			sin = Math.sqrt(1.0 + 2.0 * (q[0] * q[2] - q[1] * q[3]));
			cos = Math.sqrt(1.0 - 2.0 * (q[0] * q[2] - q[1] * q[3]));
			return 2.0*Math.atan2(sin, cos) - Math.PI*0.5;
		case 2:
			sin = 2 * (q[0] * q[3] + q[1] * q[2]);
			cos = 1 - 2 * (q[2] * q[2] + q[3] * q[3]);
			return Math.atan2(sin, cos);
		default:
			return 0.0;
		}
	}
	
	/** https://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/ **/
	public static double quaternionToAngleSecond(int nAxis, double [] q)
	{
	    		//heading Y axis 
				//attitude Z axis
				//bank X asis
			double test = q[1]*q[2] + q[3]*q[0];
			// singularity at north pole
			if (test > 0.4999) 
			{ 
				switch (nAxis)
				{
				case 0:
					return 0.0;
				case 1:
					return 2.0 * Math.atan2(q[1],q[0]);
				case 2:
					return Math.PI/2;
				default:
					return 0.0;
				}
			}
			// singularity at south pole
			if (test < -0.4999) 
			{ 
				switch (nAxis)
				{
				case 0:
					return 0.0;
				case 1:
					return -2.0 * Math.atan2(q[1],q[0]);
				case 2:
					return - Math.PI/2;
				default:
					return 0.0;
				}

			}
			double [] sq = new double[3];
			for(int d=0;d<3;d++)
			{
				sq[d] = q[d+1]*q[d+1];
			}
			switch (nAxis)
			{
			case 0:
				return Math.atan2(2.0*q[1]*q[0]-2.0*q[2]*q[3] , 1.0 - 2.0*sq[1] - 2.0*sq[3]);
			case 1:
				return Math.atan2(2.0*q[2]*q[0]-2.0*q[1]*q[3] , 1.0 - 2.0*sq[2] - 2.0*sq[3]);
			case 2:
				return Math.asin(2.0*test);
			default:
				return 0.0;
			}	

	}
	
	/** converts the quaternion rotation to a Euler angles (with ambiguity!) **/
	public static double[]  quaternionToEulerAngles(double [] q)
	{
		final double [] eAngles = new double[3];
		
		for (int d=0;d<3;d++)
		{
			eAngles[d] = quaternionToAngle(d,q);
		}
		return eAngles;
	}
	
	/** returns the center of an interval where all coordinates
	 * were multiplied by -1.
	 * Return zero vector if the interval is null **/	
	public static double [] getIntervalCenterNegative(RealInterval interval)
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
	
	public static double [] getIntervalCenter(RealInterval interval)
	{
		if(interval == null)
			return new double [3];
		final double [] min = interval.minAsDoubleArray();
		final double [] max = interval.maxAsDoubleArray();
		
		for(int d=0;d<3;d++)
		{
			min[d] = 0.5*(max[d]+min[d]);
		}
		
		return min;
		
	}
	
	public static boolean compareAffineTransforms(AffineTransform3D af1,AffineTransform3D af2 )
	{
		boolean bOut = true;
		if(af1 == null && af2 == null)
			return true;
		if(af1 == null || af2 == null)
			return false;
		for(int i=0; i<3; i++)
		{
			for(int j=0; j<4; j++)
			{
				bOut &= Double.compare( af1.get( i, j ), af2.get( i, j ) ) == 0 ;
			}
			if(!bOut)
				return false;
		}
		
		return bOut;
	}
}
