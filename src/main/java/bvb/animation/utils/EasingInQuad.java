package bvb.animation.utils;

public class EasingInQuad implements Easing
{
    @Override
    public String getId() {
        return "easeInQuad";
    }

    @Override
    public float apply(float t) 
    {
        return t * t;
    }
}
