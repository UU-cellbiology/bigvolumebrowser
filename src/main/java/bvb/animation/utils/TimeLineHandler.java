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
	
    public void addKeyframeTransform(final Object obj, final KeyFrameScene keyFrameScene)
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
			timeline.addCurrentKeyframe(clName, "transform_center", keyFrameScene );	    	
	    	// transform rotation
			timeline.addCurrentKeyframe(clName, "transform_rotation", keyFrameScene );  	
	    	// transform scale
			timeline.addCurrentKeyframe(clName, "transform_scale", keyFrameScene );
	    	// transform deskew
			timeline.addCurrentKeyframe(clName, "transform_deskew", keyFrameScene );

    	}
    }
    
    public void addKeyframeClippable3D(final Clippable3D clippable, final KeyFrameScene keyFrameScene)
    {
    	String clName = getObjectName (clippable);
    	if(clName == null)
    	{
    		return;
    	}
	
		timeline.addCurrentKeyframe(clName, "clip_type", keyFrameScene );
    	// clip range
		timeline.addCurrentKeyframe(clName, "clip_range", keyFrameScene );   	
    	// clip centers
		timeline.addCurrentKeyframe(clName, "clip_center", keyFrameScene );   	
    	// clip rotation
		timeline.addCurrentKeyframe(clName, "clip_rotation", keyFrameScene );

    }
    
    public void addKeyframeConverterSetup(final GammaConverterSetup cs, final KeyFrameScene keyFrameScene)
    {
    	
    	String csName = getObjectName(cs);
    	if(csName == null)
    	{
    		return;
    	}

		timeline.addCurrentKeyframe(csName, "cs_visibility", keyFrameScene );
    	
		timeline.addCurrentKeyframe(csName, "cs_displayRange", keyFrameScene );
    	
		timeline.addCurrentKeyframe(csName, "cs_gamma", keyFrameScene );

		timeline.addCurrentKeyframe(csName, "cs_alphaRange", keyFrameScene ); 	
    	
		timeline.addCurrentKeyframe(csName, "cs_alphaGamma",  keyFrameScene );

		timeline.addCurrentKeyframe(csName, "cs_renderType", keyFrameScene );

		timeline.addCurrentKeyframe(csName, "cs_lightingType", keyFrameScene );

		timeline.addCurrentKeyframe(csName, "cs_voxelInterpolation", keyFrameScene );
		
		timeline.addCurrentKeyframe(csName, "cs_color", keyFrameScene );

		timeline.addCurrentKeyframe(csName, "cs_LUT", keyFrameScene );

    }
    
    public void addKeyframeBasicShape(final BasicShape shape, final KeyFrameScene keyFrameScene)
    {
    	String shapeName = getObjectName(shape);
    	if(shapeName == null)
    	{
    		return;
    	}

		timeline.addCurrentKeyframe(shapeName, "bs_visible",  keyFrameScene );
		
		if(shape instanceof BasicMeshShape)
		{
			addKeyframeBasicMeshShape((BasicMeshShape)shape, keyFrameScene);
		}
		if(shape instanceof BasicSpots)
		{
			addKeyframeBasicSpots((BasicSpots)shape, keyFrameScene);
		}
    }
    
    public void addKeyframeBasicMeshShape(final BasicMeshShape shape, final KeyFrameScene keyFrameScene)
    {
    	String shapeName = getObjectName (shape);

		timeline.addCurrentKeyframe(shapeName, "mesh_color", keyFrameScene );
		
		//use of texture
		if(shape.hasTexture())
		{
			timeline.addCurrentKeyframe(shapeName, "mesh_useTexture", keyFrameScene );			
		}
		
		timeline.addCurrentKeyframe(shapeName, "mesh_renderType", keyFrameScene );			
    	
		timeline.addCurrentKeyframe(shapeName, "mesh_pointSize", keyFrameScene );			

		timeline.addCurrentKeyframe(shapeName, "mesh_surfaceRenderType", keyFrameScene );			

		timeline.addCurrentKeyframe(shapeName, "mesh_surfaceGrid", keyFrameScene );			

		timeline.addCurrentKeyframe(shapeName, "mesh_wireWidth", keyFrameScene );			

		timeline.addCurrentKeyframe(shapeName, "mesh_silDecay", keyFrameScene );				
    }
    
    public void addKeyframeBasicSpots(final BasicSpots shape, final KeyFrameScene keyFrameScene)
    {
    	String shapeName = getObjectName (shape);
    	
    	//if point size is the same
    	if(shape.getPointSize() >= 0.0)
    	{
    		// point size
    		timeline.addCurrentKeyframe(shapeName, "spots_pointSize", keyFrameScene );				
    	}
    	//each point has its own size
    	else
    	{
    		timeline.addCurrentKeyframe(shapeName, "spots_pointSizeScale", keyFrameScene );				    		
    	}
    	
    	if(!shape.isMultiColor())
    	{
    		//spots color 
    		timeline.addCurrentKeyframe(shapeName, "spots_color", keyFrameScene );
    	}
    	//extra transparency
    	timeline.addCurrentKeyframe(shapeName, "spots_extraAlpha", keyFrameScene );				    		

    	//renderType
		timeline.addCurrentKeyframe(shapeName, "spots_renderType", keyFrameScene );

    	//spots shape
		timeline.addCurrentKeyframe(shapeName, "spots_shape", keyFrameScene );
		
    	//spots shade
		timeline.addCurrentKeyframe(shapeName, "spots_shade", keyFrameScene );
		
		// LUT mapping mode
		timeline.addCurrentKeyframe(shapeName, "spots_mapLUTMode", keyFrameScene );
		
		//LUT
		timeline.addCurrentKeyframe(shapeName, "spots_LUT", keyFrameScene );

		// LUT inversion
		timeline.addCurrentKeyframe(shapeName, "spots_LUTInverse", keyFrameScene );

		//LUT Range
		timeline.addCurrentKeyframe(shapeName, "spots_LUTRange", keyFrameScene );

		// alpha mapping mode
		timeline.addCurrentKeyframe(shapeName, "spots_alphaMapMode", keyFrameScene );
		
		// alpha map inversion
		timeline.addCurrentKeyframe(shapeName, "spots_alphaInverse", keyFrameScene );

		//LUT Range
		timeline.addCurrentKeyframe(shapeName, "spots_alphaRange", keyFrameScene );
		
    }
}
