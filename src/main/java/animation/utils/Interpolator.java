package animation.utils;

public interface Interpolator <T>
{
	T interpolate(T a, T b, float t);
}
