package bvb.animation.utils;

import bvb.animation.KeyFrameScene;

public class Keyframe< T >
{
    public final T value;
    public final KeyFrameScene parentKF;

    public Keyframe(final T value, final KeyFrameScene parentKF) 
    {
    	this.parentKF = parentKF;
        this.value = value;
    }
    
    public float getTime()
    {
    	return parentKF.getMovieTimePoint();
    }
}
