package bvb.animation.utils;

import bvb.animation.KeyFrameScene;
import bvb.core.BigVolumeBrowser;
import bvb.io.ObjectHashStorage;
import bvb.shapes.BasicMeshShape;
import bvb.shapes.BasicShape;
import bvb.shapes.BasicSpots;
import bvvpg.source.converters.Clippable3D;
import bvvpg.source.converters.GammaConverterSetup;

public class TimeLineHandler
{
	
	final ObjectHashStorage objectHashStorage;
	
	final Timeline timeline;
	
	public TimeLineHandler (final BigVolumeBrowser bvb, final Timeline timeline)
    {
    	objectHashStorage = bvb.objectHashStorage;
    	this.timeline = timeline;
    }
	
    String getObjectName(final Object obj)
    {
    	return objectHashStorage.getBVBHashString( obj );
    }
	
    public void addKeyframeTransform(final Object obj, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String clName = getObjectName(obj);
    	if(clName == null)
    	{
    		return;
    	}
		//it is both BasicShape and GammaConverterSetup
    	if(obj instanceof Clippable3D )
    	{	      	
	    	// transform centers
			timeline.addCurrentKeyframe(clName, "transform_center", easing, keyFrameScene );	    	
	    	// transform rotation
			timeline.addCurrentKeyframe(clName, "transform_rotation", easing, keyFrameScene );  	
	    	// transform scale
			timeline.addCurrentKeyframe(clName, "transform_scale", easing, keyFrameScene );
	    	// transform deskew
			timeline.addCurrentKeyframe(clName, "transform_deskew", easing, keyFrameScene );

    	}
    }
    
    public void addKeyframeClippable3D(final Clippable3D clippable, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String clName = getObjectName (clippable);
    	if(clName == null)
    	{
    		return;
    	}
	
		timeline.addCurrentKeyframe(clName, "clip_type", easing, keyFrameScene );
    	// clip range
		timeline.addCurrentKeyframe(clName, "clip_range", easing, keyFrameScene );   	
    	// clip centers
		timeline.addCurrentKeyframe(clName, "clip_center", easing, keyFrameScene );   	
    	// clip rotation
		timeline.addCurrentKeyframe(clName, "clip_rotation", easing, keyFrameScene );

    }
    
    public void addKeyframeConverterSetup(final GammaConverterSetup cs, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	
    	String csName = getObjectName(cs);
    	if(csName == null)
    	{
    		return;
    	}

		timeline.addCurrentKeyframe(csName, "cs_visibility", easing, keyFrameScene );
    	
		timeline.addCurrentKeyframe(csName, "cs_displayRange", easing, keyFrameScene );
    	
		timeline.addCurrentKeyframe(csName, "cs_gamma", easing, keyFrameScene );

		timeline.addCurrentKeyframe(csName, "cs_alphaRange", easing, keyFrameScene ); 	
    	
		timeline.addCurrentKeyframe(csName, "cs_alphaGamma", easing, keyFrameScene );

		timeline.addCurrentKeyframe(csName, "cs_renderType", easing, keyFrameScene );

		timeline.addCurrentKeyframe(csName, "cs_lightingType", easing, keyFrameScene );

		timeline.addCurrentKeyframe(csName, "cs_voxelInterpolation", easing, keyFrameScene );

    }
    
    public void addKeyframeBasicShape(final BasicShape shape, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String shapeName = getObjectName(shape);
    	if(shapeName == null)
    	{
    		return;
    	}

		timeline.addCurrentKeyframe(shapeName, "bs_visible", easing, keyFrameScene );
		
		if(shape instanceof BasicMeshShape)
		{
			addKeyframeBasicMeshShape((BasicMeshShape)shape, keyFrameScene, easing);
		}
		if(shape instanceof BasicSpots)
		{
			addKeyframeBasicSpots((BasicSpots)shape, keyFrameScene, easing);
		}
    }
    
    public void addKeyframeBasicMeshShape(final BasicMeshShape shape, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String shapeName = getObjectName (shape);

		timeline.addCurrentKeyframe(shapeName, "mesh_color", easing, keyFrameScene );
		
		//use of texture
		if(shape.hasTexture())
		{
			timeline.addCurrentKeyframe(shapeName, "mesh_useTexture", easing, keyFrameScene );			
		}
		
		timeline.addCurrentKeyframe(shapeName, "mesh_renderType", easing, keyFrameScene );			
    	
		timeline.addCurrentKeyframe(shapeName, "mesh_pointSize", easing, keyFrameScene );			

		timeline.addCurrentKeyframe(shapeName, "mesh_surfaceRenderType", easing, keyFrameScene );			

		timeline.addCurrentKeyframe(shapeName, "mesh_surfaceGrid", easing, keyFrameScene );			

		timeline.addCurrentKeyframe(shapeName, "mesh_wireWidth", easing, keyFrameScene );			

		timeline.addCurrentKeyframe(shapeName, "mesh_silDecay", easing, keyFrameScene );				
    }
    
    public void addKeyframeBasicSpots(final BasicSpots shape, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String shapeName = getObjectName (shape);
    	
    	//if point size is the same
    	if(shape.getPointSize() >= 0.0)
    	{
    		// point size
    		timeline.addCurrentKeyframe(shapeName, "spots_pointSize", easing, keyFrameScene );				
    	}
    	//each point has its own size
    	else
    	{
    		timeline.addCurrentKeyframe(shapeName, "spots_pointSizeScale", easing, keyFrameScene );				    		
    	}
    	
    	if(!shape.isMultiColor())
    	{
    		//spots color 
    		timeline.addCurrentKeyframe(shapeName, "spots_color", easing, keyFrameScene );
    	}
    	//extra transparency
    	timeline.addCurrentKeyframe(shapeName, "spots_extraAlpha", easing, keyFrameScene );				    		

    	//renderType
		timeline.addCurrentKeyframe(shapeName, "spots_renderType", easing, keyFrameScene );

    	//spots shape
		timeline.addCurrentKeyframe(shapeName, "spots_shape", easing, keyFrameScene );
		
    	//spots shade
		timeline.addCurrentKeyframe(shapeName, "spots_shade", easing, keyFrameScene );
		
		// LUT mapping mode
		timeline.addCurrentKeyframe(shapeName, "spots_mapLUTMode", easing, keyFrameScene );
		
		//LUT
		timeline.addCurrentKeyframe(shapeName, "spots_LUT", easing, keyFrameScene );

		// LUT inversion
		timeline.addCurrentKeyframe(shapeName, "spots_LUTInverse", easing, keyFrameScene );

		//LUT Range
		timeline.addCurrentKeyframe(shapeName, "spots_LUTRange", easing, keyFrameScene );

		// alpha mapping mode
		timeline.addCurrentKeyframe(shapeName, "spots_alphaMapMode", easing, keyFrameScene );
		
		// alpha map inversion
		timeline.addCurrentKeyframe(shapeName, "spots_alphaInverse", easing, keyFrameScene );

		//LUT Range
		timeline.addCurrentKeyframe(shapeName, "spots_alphaRange", easing, keyFrameScene );
		
    }
}
