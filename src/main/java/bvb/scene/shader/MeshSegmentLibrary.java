package bvb.scene.shader;

import java.util.HashMap;
import java.util.Map;

import bvb.core.BVBSettings;
import bvb.scene.VisMesh;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;

public class MeshSegmentLibrary
{
	public static final Map< String, Segment> meshSegments = getDefaultMeshSegments();
	
	public static Map< String, Segment> getDefaultMeshSegments()
	{
		final HashMap< String, Segment > segments = new HashMap<>();
		segments.put( "shaded", 
				new SegmentTemplate( VisMesh.class, BVBSettings.sShaderPath + "mesh/shaded.fp" ).instantiate());	
		segments.put( "shiny", 
				new SegmentTemplate( VisMesh.class, BVBSettings.sShaderPath + "mesh/shiny.fp" ).instantiate());	
		segments.put( "silh", 
				new SegmentTemplate( VisMesh.class, BVBSettings.sShaderPath + "mesh/silh.fp" ).instantiate());	
		
		return segments;		
	}
}
