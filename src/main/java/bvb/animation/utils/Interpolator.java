package bvb.animation.utils;

public interface Interpolator <T>
{
	T interpolate(T a, T b, float t);
	public static Interpolator<Float> floatLerp = (a, b, t) -> a + (b - a) * t;
}
