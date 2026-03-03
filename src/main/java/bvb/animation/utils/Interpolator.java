/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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

	public static Interpolator<float [][]> floatIndexArrayLerp = (a, b, t) -> floatIndexArrayLerp( a, b, t );

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

	static float[][] floatIndexArrayLerp( float[][] a, float[][] b, float t )
	{
		final int indL = a.length;
		final float [][] out = new float[indL][a[0].length];
		for (int i = 0; i < indL; i++)
		{
			out[ i ] = floatArrayLerp(a[i], b[i], t);
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
	
	static float[] floatArrayLerp( float[] a, float[] b, float t )
	{
		final float [] out = new float[a.length];
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
