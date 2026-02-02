package animation.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Track< T >
{
	
	private final String id;
	
    private final Property<T> property;
    
    private final List<Keyframe<T>> keyframes = new ArrayList<>();
    
    private final Interpolator<T> interpolator;
    private final Easing easing;
    
    public Track(String id,
            Property<T> property,
            Interpolator<T> interpolator,
            Easing easing)
    {
    	this.id = id;
        this.property = property;
        this.interpolator = interpolator;
        this.easing = easing != null ? easing : Easing.LINEAR;
    }
    
    public void addKeyframe(Keyframe<T> key) {
        keyframes.add(key);
        keyframes.sort(Comparator.comparingDouble(k -> k.time));
    }
    
    public void apply(float time) 
    {
    	if (keyframes.isEmpty())
              return;
    	// Before first key
        if (time <= keyframes.get(0).time) 
        {
            //property.set(keyframes.get(0).value);
            return;
        }
        // After last key
        int last = keyframes.size() - 1;
        if (time >= keyframes.get(last).time) 
        {
            //property.set(keyframes.get(last).value);
            return;
        }
    	
        // Find surrounding keys
        Keyframe<T> a = null, b = null;

        for (int i = 0; i < keyframes.size() - 1; i++) 
        {
            a = keyframes.get(i);
            b = keyframes.get(i + 1);
            if (time >= a.time && time <= b.time)
                break;
        }

        float localT =
            (time - a.time) / (b.time - a.time);

        localT = easing.apply(localT);

        T value = interpolator.interpolate(a.value, b.value, localT);

        property.set(value);
    }
    
    public String getId() {
        return id;
    }
}
