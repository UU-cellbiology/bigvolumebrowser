package bvb.animation.utils;

public class EasingOutQuad implements Easing
{
    @Override
    public String getId() {
        return "easeOutQuad";
    }

    @Override
    public float apply(float t) 
    {
        return t * (2 - t);
    }

}
