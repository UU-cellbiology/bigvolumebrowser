package bvb.animation.utils;

public class EasingInOutQuad implements Easing
{
    @Override
    public String getId() {
        return "easeInOutQuad";
    }

    @Override
    public float apply(float t) 
    {
    	if (t < 0.5f)
          return 2f * t * t;
    	return 1f - (float)Math.pow(-2f * t + 2f, 2f) / 2f;
    }

}
