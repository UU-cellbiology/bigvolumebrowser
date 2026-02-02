package animation.utils;

public class Keyframe< T >
{
    public final float time;
    public final T value;

    public Keyframe(float time, T value) {
        this.time = time;
        this.value = value;
    }
}
