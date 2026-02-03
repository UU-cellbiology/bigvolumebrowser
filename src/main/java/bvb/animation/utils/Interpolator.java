package bvb.animation.utils;

public interface Interpolator <T>
{
	T interpolate(T a, T b, float t);
	public static Interpolator<Float> floatLerp = (a, b, t) -> a + (b - a) * t;
	public static Interpolator<Double> doubleLerp = (a, b, t) -> a + (b - a) * t;
	public static Interpolator<double []> doubleArrayLerp = (a, b, t) -> 
	{
		final double [] out = new double[a.length];
		for (int i = 0; i < a.length; i++)
		{
			out[ i ] = a[i] + (b[i] - a[i]) * t;
		}
		return out;};
	public static final Interpolator<Boolean> booleanStep =  (a, b, t) -> t < 1.0f ? a : b;
	public static final Interpolator<Integer> integerStep = (a, b, t) -> t < 1.0f ? a : b;
	public static final Interpolator<Integer> integerRound  =  (a, b, t) -> Math.round(a + (b - a) * t);
}
