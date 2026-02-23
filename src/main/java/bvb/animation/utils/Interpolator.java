package bvb.animation.utils;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.util.LinAlgHelpers;

public interface Interpolator <T>
{
	T interpolate(T a, T b, float t);
	public static Interpolator<Float> floatLerp = (a, b, t) -> a + (b - a) * t;
	public static Interpolator<Double> doubleLerp = (a, b, t) -> a + (b - a) * t;
	public static Interpolator<float []> floatArrayLerp = (a, b, t) -> floatArrayLerp( a, b, t );
	public static Interpolator<double []> doubleArrayLerp = (a, b, t) -> doubleArrayLerp( a, b, t );
	public static final Interpolator<Boolean> booleanStep =  (a, b, t) -> t < 1.0f ? a : b;
	public static final Interpolator<Integer> integerStep = (a, b, t) -> t < 1.0f ? a : b;
	public static final Interpolator<Integer> integerRound  =  (a, b, t) -> Math.round(a + (b - a) * t);
	public static final Interpolator<double []> quatSLerp  =  (a, b, t) -> slerp( a, b, t);
	public static final Interpolator<String> stringStep = (a, b, t) -> t < 1.0f ? a : b;

	public static final Interpolator<RealInterval> realInterval  =  (a, b, t) -> 
	{
		if(a == null || b == null)
			return null;
		final double [] mina = a.minAsDoubleArray();
		final double [] minb = b.minAsDoubleArray();

		final double [] maxa = a.maxAsDoubleArray();
		final double [] maxb = b.maxAsDoubleArray();

		final double [] min = doubleArrayLerp(mina,minb,t);
		final double [] max = doubleArrayLerp(maxa,maxb,t);
		return new FinalRealInterval(min, max, false);

	};

	static float[] floatArrayLerp( float[] a, float[] b, float t )
	{
		final float [] out = new float[a.length];
		for (int i = 0; i < a.length; i++)
		{
			out[ i ] = a[i] + (b[i] - a[i]) * t;
		}
		return out;
	}
	
	static double[] doubleArrayLerp( double[] a, double[] b, float t )
	{
		final double [] out = new double[a.length];
		for (int i = 0; i < a.length; i++)
		{
			out[ i ] = a[i] + (b[i] - a[i]) * t;
		}
		return out;
	}

	
	static double [] slerp (final double [] a, final double [] b, final double t)
	{
		final double [] qout = new double[4];
		final double [] q1 = new double[4];

		double dot = 0.0;
		// Compute cosine of angle between 
		for(int d = 0; d < 4; d++)
		{
			q1[d] = b[d];
			dot += a[d] * b[d];
		}
		// Use shortest path
		if (dot < 0.0) 
		{
			for(int d = 0; d < 4; d++)
			{
				q1[d] = - b[d];
			}
			dot = -dot;
		}
		// If angle is very small, use LERP
		if (dot > 0.9995) 
		{
			for(int d = 0; d < 4; d++)
			{
				qout[d] =  a[d] + t * (q1[d] - a[d]);
			}
			LinAlgHelpers.normalize( qout );
			return qout;
		}
		final double theta0 = Math.acos(dot);
		final double theta  = theta0 * t;

		final double sinTheta  = Math.sin(theta);
		final double sinTheta0 = Math.sin(theta0);

		final double s0 = Math.cos(theta) - dot * sinTheta / sinTheta0;
		final double s1 = sinTheta / sinTheta0;

		for(int d = 0; d < 4; d++)
		{
			qout[d] = s0 * a[d] + s1 * q1[d];
		}        

		return qout;
	}
}
