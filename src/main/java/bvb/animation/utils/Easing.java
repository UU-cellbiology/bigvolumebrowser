package bvb.animation.utils;

@FunctionalInterface
public interface Easing 
{
    float apply(float t);

    Easing LINEAR = t -> t;

    Easing EASE_IN = t -> t * t;

    Easing EASE_OUT = t -> t * (2 - t);

    Easing EASE_IN_OUT = t -> {
        if (t < 0.5f)
            return 2f * t * t;
        return 1f - (float)Math.pow(-2f * t + 2f, 2f) / 2f;
    };
}
