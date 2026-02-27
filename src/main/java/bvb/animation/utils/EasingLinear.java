package bvb.animation.utils;

public class EasingLinear implements Easing 
{
	    @Override
	    public String getId() {
	        return "linear";
	    }

	    @Override
	    public float apply(float t) 
	    {
	        return t;
	    }
}

