package bvb.animation.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bdv.viewer.SourceAndConverter;
import bvb.animation.KeyFrameScene;
import bvb.core.BigVolumeBrowser;
import bvb.shapes.BasicShape;
import bvvpg.source.converters.Clippable3D;
import bvvpg.source.converters.GammaConverterSetup;

public class Timeline 
{
	
    private final List<Track<?>> tracks = new ArrayList<>();
    
    private final Map<String, Track<?>> trackIndex = new HashMap<>();
    
    private final Map <KeyFrameScene, Set<Track<?>>> keyFramesToTracks = new HashMap<>();
    final TimeLineHandler timeLineHandler;
       
    public Timeline (final BigVolumeBrowser bvb)
    {
    	timeLineHandler = new TimeLineHandler(bvb, this);
    }
    
    public void addTrack(final Track<?> track)
    {
    	tracks.add( track );
    	trackIndex.put(track.getId(), track);
    }
    
    public Track<?> getTrack(String id) 
    {
        return trackIndex.get(id);
    }
    
    public void apply(float time) 
    {
        for (Track<?> track : tracks) {
            track.apply(time);
        }
    }
    
    public <T> void addKeyframe(
            String trackId,
            final Property<T> property,
            final Interpolator<T> interpolator,
            final Easing easing,
            final KeyFrameScene keyFrameScene)
    {
        @SuppressWarnings("unchecked")
        Track<T> track = (Track<T>) trackIndex.get(trackId);

        if (track == null) 
        {
            track = new Track<>(trackId, property, interpolator, easing); 
            tracks.add( track );
            trackIndex.put(trackId, track);
        }
        //store the parent scene key frame
        Set< Track<?> > setT = keyFramesToTracks.get( keyFrameScene );
        if(setT == null)
        {
        	setT = new HashSet<>();
        	keyFramesToTracks.put( keyFrameScene, setT );
        }
        setT.add( track );
        
        //add keyframe to the track
        track.addKeyframe(new Keyframe<>(property.get(), keyFrameScene));
    }
    
    public void deleteKeyframe(final KeyFrameScene keyFrameScene)
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
    
    public void updateKeyframe(final KeyFrameScene keyFrameScene)
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
    
    public void addKeyframeBVB(final BigVolumeBrowser bvb, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	final ArrayList<Object> allObjects = new ArrayList<>();
    	final ArrayList<GammaConverterSetup> converterSetups = new ArrayList<>();
    	// add all converter setups
    	final List< SourceAndConverter< ? > > allSources = bvb.bvvViewer.state().getSources();
    	
    	for(final SourceAndConverter< ? > sac :allSources)
		{
    		converterSetups.add( ( GammaConverterSetup ) bvb.bvvHandle.getConverterSetups().getConverterSetup( sac ) );
    		allObjects.add( bvb.bvvHandle.getConverterSetups().getConverterSetup( sac ) );
		}
    	
    	//add all the shapes
    	final ArrayList<BasicShape> shapes = new ArrayList<>();
    	
    	for (final BasicShape shape : bvb.shapes)
    	{
    		allObjects.add( shape );
    		shapes.add( shape );
    	}
    	
    	//clipping
    	for(final Object obj : allObjects)
		{
    		if(obj instanceof Clippable3D)
    		{
    			timeLineHandler.addKeyframeTransform(obj, keyFrameScene, easing);
    			timeLineHandler.addKeyframeClippable3D((Clippable3D)obj, keyFrameScene, easing);    		
    		}
		}    	
    	
    	//cs specific routine
    	for(final GammaConverterSetup cs :converterSetups)
		{
    		timeLineHandler.addKeyframeConverterSetup(cs, keyFrameScene, easing);
		}
    	
    	//shape specific routine
    	for (final BasicShape shape : shapes)
    	{
    		timeLineHandler.addKeyframeBasicShape( shape, keyFrameScene, easing);
    	}
    	
    }
}