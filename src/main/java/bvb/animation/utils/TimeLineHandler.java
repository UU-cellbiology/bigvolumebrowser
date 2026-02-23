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
			timeline.addKeyframe(clName, "transform_center", easing, keyFrameScene );	    	
	    	// transform rotation
			timeline.addKeyframe(clName, "transform_rotation", easing, keyFrameScene );  	
	    	// transform scale
			timeline.addKeyframe(clName, "transform_scale", easing, keyFrameScene );
	    	// transform deskew
			timeline.addKeyframe(clName, "transform_deskew", easing, keyFrameScene );

    	}
    }
    
    public void addKeyframeClippable3D(final Clippable3D clippable, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String clName = getObjectName (clippable);
    	if(clName == null)
    	{
    		return;
    	}
	
		timeline.addKeyframe(clName, "clip_type", easing, keyFrameScene );
    	// clip range
		timeline.addKeyframe(clName, "clip_range", easing, keyFrameScene );   	
    	// clip centers
		timeline.addKeyframe(clName, "clip_center", easing, keyFrameScene );   	
    	// clip rotation
		timeline.addKeyframe(clName, "clip_rotation", easing, keyFrameScene );

    }
    
    public void addKeyframeConverterSetup(final GammaConverterSetup cs, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	
    	String csName = getObjectName(cs);
    	if(csName == null)
    	{
    		return;
    	}

		timeline.addKeyframe(csName, "cs_visibility", easing, keyFrameScene );
    	
		timeline.addKeyframe(csName, "cs_displayRange", easing, keyFrameScene );
    	
		timeline.addKeyframe(csName, "cs_gamma", easing, keyFrameScene );

		timeline.addKeyframe(csName, "cs_alphaRange", easing, keyFrameScene ); 	
    	
		timeline.addKeyframe(csName, "cs_alphaGamma", easing, keyFrameScene );

		timeline.addKeyframe(csName, "cs_renderType", easing, keyFrameScene );

		timeline.addKeyframe(csName, "cs_lightingType", easing, keyFrameScene );

		timeline.addKeyframe(csName, "cs_voxelInterpolation", easing, keyFrameScene );

    }
    
    public void addKeyframeBasicShape(final BasicShape shape, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String shapeName = getObjectName(shape);
    	if(shapeName == null)
    	{
    		return;
    	}

		timeline.addKeyframe(shapeName, "bs_visible", easing, keyFrameScene );
		
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

		timeline.addKeyframe(shapeName, "mesh_color", easing, keyFrameScene );
		
		//use of texture
		if(shape.hasTexture())
		{
			timeline.addKeyframe(shapeName, "mesh_useTexture", easing, keyFrameScene );			
		}
		
		timeline.addKeyframe(shapeName, "mesh_renderType", easing, keyFrameScene );			
    	
		timeline.addKeyframe(shapeName, "mesh_pointSize", easing, keyFrameScene );			

		timeline.addKeyframe(shapeName, "mesh_surfaceRenderType", easing, keyFrameScene );			

		timeline.addKeyframe(shapeName, "mesh_surfaceGrid", easing, keyFrameScene );			

		timeline.addKeyframe(shapeName, "mesh_wireWidth", easing, keyFrameScene );			

		timeline.addKeyframe(shapeName, "mesh_silDecay", easing, keyFrameScene );				
    }
    
    public void addKeyframeBasicSpots(final BasicSpots shape, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String shapeName = getObjectName (shape);
    	
    	//if point size is the same
    	if(shape.getPointSize() >= 0.0)
    	{
    		// point size
    		timeline.addKeyframe(shapeName, "spots_pointSize", easing, keyFrameScene );				
    	}
    	//each point has its own size
    	else
    	{
    		timeline.addKeyframe(shapeName, "spots_pointSizeScale", easing, keyFrameScene );				    		
    	}
    	
    	if(!shape.isMultiColor())
    	{
    		//spots color 
    		timeline.addKeyframe(shapeName, "spots_color", easing, keyFrameScene );
    	}
    	//extra transparency
    	timeline.addKeyframe(shapeName, "spots_extraAlpha", easing, keyFrameScene );				    		

    	//renderType
		timeline.addKeyframe(shapeName, "spots_renderType", easing, keyFrameScene );

    	//spots shape
		timeline.addKeyframe(shapeName, "spots_shape", easing, keyFrameScene );
		
    	//spots shade
		timeline.addKeyframe(shapeName, "spots_shade", easing, keyFrameScene );
		
		//LUT
		if(!shape.getLUTName().equals( "" ))
		{
			timeline.addKeyframe(shapeName, "spots_LUT", easing, keyFrameScene );
		}
    }
}
