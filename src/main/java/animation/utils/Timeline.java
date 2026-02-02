package animation.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Timeline 
{
    private final List<Track<?>> tracks = new ArrayList<>();
    
    private final Map<String, Track<?>> trackIndex = new HashMap<>();
    
    public void addTrack(final Track<?> track)
    {
    	tracks.add( track );
    	trackIndex.put(track.getId(), track);
    }
    
    public Track<?> getTrack(String id) {
        return trackIndex.get(id);
    }
    
    public void apply(float time) {
        for (Track<?> track : tracks) {
            track.apply(time);
        }
    }
    
    public <T> void addKeyframe(
            String trackId,
            Property<T> property,
            Interpolator<T> interpolator,
            Easing easing,
            float time,
            T value)
    {
        @SuppressWarnings("unchecked")
        Track<T> track = (Track<T>) trackIndex.get(trackId);

        if (track == null) 
        {
            track = new Track<>(trackId, property, interpolator, easing); 
            tracks.add( track );
            trackIndex.put(trackId, track);
        }

        track.addKeyframe(new Keyframe<>(time, value));
    }
}