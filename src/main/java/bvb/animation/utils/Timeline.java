/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bvb.animation.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bvb.animation.KeyFrameScene;
import bvb.core.BigVolumeBrowser;
import bvb.io.dto.TimeLineDTO;
import bvb.registry.PropertyRegistry;

public class Timeline 
{	
    private final List<Track<?>> tracks = new ArrayList<>();
    
    private final Map<String, Track<?>> trackIndex = new HashMap<>();
    
    private final Map <KeyFrameScene, Set<Track<?>>> keyFramesToTracks = new HashMap<>();
    
    final PropertyRegistry propertyRegistry;
    
    final public static EasingRegistry easingRegistry = new EasingRegistry();
       
    public Timeline (final BigVolumeBrowser bvb)
    {
    	propertyRegistry = bvb.propertyRegistry;
    }
    
    public <T> void addTrackAndBind(final Track<T> track)
    {   	
    	final PropertyBinding< T > binding = propertyRegistry.get( track.getObjectId(), track.getPropertyName());
        if(binding == null)
        {
        	//System.err.println("Cannot find binding between " + track.getObjectId() + "and " + track.getPropertyName());
        	return;
        }
        track.bind( binding.property, binding.interpolator );
    	tracks.add( track );
    	trackIndex.put(track.getTrackId(), track);
    	for (final Keyframe<T> kf : track.getKeyFrames())
    	{
    		final KeyFrameScene keyFrameScene = kf.parentKF;
    		Set< Track<?> > setT = keyFramesToTracks.get( keyFrameScene );
    		if(setT == null)
    		{
    			setT = new HashSet<>();
    			keyFramesToTracks.put( keyFrameScene, setT );
    		}
    		setT.add( track );
    	}

    }
    
    public void apply(float time) 
    {
        for (Track<?> track : tracks) {
            track.apply(time);
        }
    }
    
    public <T> void addCurrentKeyframe(
            String objectId,
            String propertyName,
            final Property<T> property,
            final Interpolator<T> interpolator,
            final KeyFrameScene keyFrameScene)
    {
        @SuppressWarnings("unchecked")
        Track<T> track = (Track<T>) trackIndex.get(objectId + propertyName);

        if (track == null) 
        {
            track = new Track<>(objectId, propertyName, property, interpolator); 
            tracks.add( track );
            trackIndex.put(objectId + propertyName, track);
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
    
    public <T> void addCurrentKeyframe(
            String objectId,
            String propertyName,
            final KeyFrameScene keyFrameScene)
    {
        final PropertyBinding< T > binding = propertyRegistry.get( objectId, propertyName );
        
        if(binding == null)
        {
        	System.err.println("Cannot find binding between " + objectId + "and " + propertyName);
        	return;
        }
        @SuppressWarnings("unchecked")
        Track<T> track = (Track<T>) trackIndex.get(objectId + propertyName);
    	

        if (track == null) 
        {
            track = new Track<>(objectId, propertyName);
            track.bind( binding.property, binding.interpolator );
            tracks.add( track );
            trackIndex.put(track.getTrackId(), track);
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
        track.addKeyframe(new Keyframe<>(binding.property.get(), keyFrameScene));
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
    
    public void addKeyframeBVB(final BigVolumeBrowser bvb, final KeyFrameScene keyFrameScene)
    {
    	
    	final List<String> objIDs = bvb.objectHashStorage.getAllObjectIDs();
    	for(String objID : objIDs)
    	{
    		final List< String > prList = PropertyRegistry.getPropertyNames( bvb.objectHashStorage.getObjectFromHash( objID ) );
    		for (int i = 0; i < prList.size(); i++ )
    		{
    			addCurrentKeyframe(objID, prList.get( i ), keyFrameScene);
    		}
    	}
    	
    }
    void addCurrentKeyframe(String objID, List<String> propertyNames, final KeyFrameScene keyFrameScene)
    {
    	for(int i = 0; i < propertyNames.size(); i++)
    	{
    		addCurrentKeyframe(objID, propertyNames.get( i ), keyFrameScene);
    	}
    }
    
    public TimeLineDTO toDTO()
    {
    	TimeLineDTO out = new TimeLineDTO();
    	for (int i = 0; i < tracks.size(); i++)
    	{
    		out.tracks.add( tracks.get( i ).toDTO() );
    	}
    	return out;
    }
    
    public void restoreFromDTO(final TimeLineDTO dto, final Map<String, KeyFrameScene > mapKF)
    {
    	tracks.clear();
        trackIndex.clear();        
        keyFramesToTracks.clear();
        
        for(int i = 0; i < dto.tracks.size(); i++)
        {
        	final Track< ? > track = Track.fromDTO( dto.tracks.get( i ), mapKF );
        	addTrackAndBind(track);
        }
    }
}
