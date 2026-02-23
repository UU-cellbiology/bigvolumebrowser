package bvb.animation.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import bvb.animation.KeyFrameScene;

public class Track< T >
{	
	private final String objectId;
	
	private final String propertyName;
	
    private transient Property<T> property;
    
    private transient Interpolator<T> interpolator;
    
    private final List<Keyframe<T>> keyframes = new ArrayList<>();
    
    private final Easing easing;
    
    public Track(String objectId,
    		String propertyName,
            Property<T> property,
            Interpolator<T> interpolator,
            Easing easing)
    {
    	this.objectId = objectId;
    	this.propertyName = propertyName;
        this.property = property;
        this.interpolator = interpolator;
        this.easing = easing != null ? easing : Easing.LINEAR;
    }
    
    public Track(String objectId,
    		String propertyName,
            Easing easing)
    {
    	this.objectId = objectId;
    	this.propertyName = propertyName;
        this.easing = easing != null ? easing : Easing.LINEAR;
    }
    

    public void bind(Property<T> property_,
                     Interpolator<T> interpolator_) {
        this.property = property_;
        this.interpolator = interpolator_;
    }
    
    public void addKeyframe(Keyframe<T> key) 
    {
        keyframes.add(key);
        keyframes.sort(Comparator.comparingDouble(k -> k.getTime()));
    }
    
    @SuppressWarnings( "null" )
	public void apply(float time) 
    {
    	if (keyframes.size() < 2)
              return;
    	// Before first key
        if (time <= keyframes.get(0).getTime()) 
        {
        	property.set(keyframes.get(0).value);
            return;
        }
        // After last key
        final int last = keyframes.size() - 1;
        if (time >= keyframes.get(last).getTime()) 
        {
       		property.set(keyframes.get(last).value);
            return;
        }
    	
        // Find surrounding keys
        Keyframe<T> a = null, b = null;
        
        for (int i = 0; i < last; i++) 
        {
            a = keyframes.get(i);
            b = keyframes.get(i + 1);
            if (time >= a.getTime() && time <= b.getTime())
                break;
        }

        float localT =
            (time - a.getTime()) / (b.getTime() - a.getTime());

        localT = easing.apply(localT);

        T value = interpolator.interpolate(a.value, b.value, localT);

        property.set(value);
    }
    
    public String getObjectId() {
        return objectId;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public String getTrackId() {
        return objectId + propertyName;
    }
    
    public void deleteKeyFrameScene(final KeyFrameScene keyFrameScene)
    {
    	for(final Keyframe<T> keyframe : keyframes)
    	{
    		if(keyframe.parentKF == keyFrameScene)
    		{
    			keyframes.remove(keyframe);
    			return;
    		}
    	}
    	System.err.println("Cannot find keyframe in the track");
    }
    
    public void sortKeyFrames()
    {
    	  keyframes.sort(Comparator.comparingDouble(k -> k.getTime()));
    }
}
