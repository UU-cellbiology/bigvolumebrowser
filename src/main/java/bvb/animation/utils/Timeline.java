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
import bvvpg.source.converters.RealARGBColorGammaConverterSetup;

public class Timeline 
{
    private final List<Track<?>> tracks = new ArrayList<>();
    
    private final Map<String, Track<?>> trackIndex = new HashMap<>();
    
    private final Map <KeyFrameScene, Set<Track<?>>> keyFramesToTracks = new HashMap<>();
        
    String tempName (final Object obj)
    {
    	return obj.toString();
    }
    
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
    	final ArrayList<RealARGBColorGammaConverterSetup> converterSetups = new ArrayList<>();
    	// add all converter setups
    	final Set< SourceAndConverter< ? > > allSources = bvb.bvvViewer.state().getVisibleAndPresentSources();
    	
    	for(final SourceAndConverter< ? > sac :allSources)
		{
    		converterSetups.add( ( RealARGBColorGammaConverterSetup ) bvb.bvvHandle.getConverterSetups().getConverterSetup( sac ) );
    		allObjects.add( bvb.bvvHandle.getConverterSetups().getConverterSetup( sac ) );
		}
    	
    	//add all the shapes
    	final ArrayList<BasicShape> shapes = new ArrayList<>();
    	
    	for (final BasicShape shape : bvb.shapes)
    	{
    		allObjects.add( shape );
    		shapes.add( shape );
    	}
    	//cs specific routine
    	for(final RealARGBColorGammaConverterSetup cs :converterSetups)
		{
    		addKeyframeConverterSetup(cs, keyFrameScene, easing);
		}
    	//shape specific routine
    	for (final BasicShape shape : shapes)
    	{
    		addKeyframeBasicShape( shape, keyFrameScene, easing);
    	}
    	
    }
    
    public void addKeyframeConverterSetup(final RealARGBColorGammaConverterSetup cs, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	
    	String csName = tempName(cs);
    	
    	//display range
    	final Property<double[]> pDisplayRange = new Property<double[]>() {
		    @Override
			public double[] get() { return new double[] {cs.getDisplayRangeMin(), cs.getDisplayRangeMax()}; }
		    @Override
			public void set(double[] v) { cs.setDisplayRange( v[0], v[1] ); }
		};   	
		addKeyframe(csName + "_displayRange", pDisplayRange, Interpolator.doubleArrayLerp, easing, keyFrameScene );
    	
		// alpha    	
    	final Property<Double> pAlpha = new Property<Double>() {
		    @Override
			public Double get() { return cs.getDisplayGamma(); }
		    @Override
			public void set(Double v) { cs.setDisplayGamma( v ); }
		};
    	addKeyframe(csName + "_alpha", pAlpha, Interpolator.doubleLerp, easing, keyFrameScene );

    	//alpha range
    	final Property<double[]> pAlphaRange = new Property<double[]>() {
		    @Override
			public double[] get() { return new double[] {cs.getAlphaRangeMin(), cs.getAlphaRangeMax()}; }
		    @Override
			public void set(double[] v) { cs.setAlphaRange( v[0], v[1] ); }
		};   	
		addKeyframe(csName + "_alphaRange", pAlphaRange, Interpolator.doubleArrayLerp, easing, keyFrameScene ); 	
    	
		// alpha gamma    	
    	final Property<Double> pAlphaGamma = new Property<Double>() {
		    @Override
			public Double get() { return cs.getAlphaGamma(); }
		    @Override
			public void set(Double v) { cs.setAlphaGamma( v );}
		};
    	addKeyframe(csName + "_alpha_gamma", pAlphaGamma, Interpolator.doubleLerp, easing, keyFrameScene );
    	
    }
    public void addKeyframeBasicShape(final BasicShape shape, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String shapeName = tempName (shape);
    	// visibility
    	final Property<Boolean> pVisible = new Property<Boolean>() {
		    @Override
			public Boolean get() { return shape.isVisible(); }
		    @Override
			public void set(Boolean v) { shape.setVisible( v ); }
		};

    	addKeyframe(shapeName + "_visible", pVisible, Interpolator.booleanStep, easing, keyFrameScene );
    }
}