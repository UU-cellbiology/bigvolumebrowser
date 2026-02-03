package bvb.animation.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bvb.animation.KeyFrameScene;

public class Timeline 
{
    private final List<Track<?>> tracks = new ArrayList<>();
    
    private final Map<String, Track<?>> trackIndex = new HashMap<>();
    
    private final Map <KeyFrameScene, Set<Track<?>>> keyFramesToTracks = new HashMap<>();
     
    
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
            final Property<T> property,
            final Interpolator<T> interpolator,
            final Easing easing,
            final KeyFrameScene keyFrameScene,
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
        //store the parent keyframescene
        Set< Track<?> > setT = keyFramesToTracks.get( keyFrameScene );
        if(setT == null)
        {
        	setT = new HashSet<>();
        	keyFramesToTracks.put( keyFrameScene, setT );
        }
        setT.add( track );
        
        //add keyframe to the track
        track.addKeyframe(new Keyframe<>(value, keyFrameScene));
    }
    
    public <T> void deleteKeyframe(final KeyFrameScene keyFrameScene)
    {
    	 Set< Track<?> > setT = keyFramesToTracks.get( keyFrameScene );
    	 if(setT == null)
    	 {
    		 System.err.println("Deleting keyframe without associated tracks!");
    	 }
    	 else
    	 {
	    	 for(final Track<?> track : setT)
	    	 {
	    		 track.deleteKeyFrameScene( keyFrameScene );
	    	 }
    	 }
    }
    
    public <T> void updateKeyframe(final KeyFrameScene keyFrameScene)
    {
    	 Set< Track<?> > setT = keyFramesToTracks.get( keyFrameScene );
    	 if(setT == null)
    	 {
    		 System.err.println("updating keyframe without associated tracks!");
    	 }
    	 else
    	 {
	    	 for(final Track<?> track : setT)
	    	 {
	    		 track.sortKeyFrames();
	    	 }
    	 }
    }
}