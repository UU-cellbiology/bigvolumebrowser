package bvb.scene.shader;

import java.util.HashMap;
import java.util.Map;

import bvb.core.BVBSettings;
import bvb.scene.VisSpots;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;

public class SegmentsLibrary
{
	public static final Map< SegmentTypeComposite, SegmentTemplate > compositeSTemplate = getDefaultCompositeSTemplates();
	public static final Map< SegmentTypeStatic, Segment> staticSegments = getDefaultStaticSegments();

	public static final Segment emptySeg = new SegmentTemplate( BVBSettings.sShaderPath + "empty.txt" ).instantiate();
	
	public static Map< SegmentTypeComposite, SegmentTemplate > getDefaultCompositeSTemplates()
	{
		final HashMap< SegmentTypeComposite, SegmentTemplate > segments = new HashMap<>();
		
		segments.put( SegmentTypeComposite.FragmentSpots, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/spots.fp", 
						"preClip", "mClip", "spotsShape", "preOIT","wOIT" ));

		return segments;
	}
	
	public static Map< SegmentTypeStatic, Segment> getDefaultStaticSegments()
	{
		final HashMap< SegmentTypeStatic, Segment > segments = new HashMap<>();
		segments.put( SegmentTypeStatic.VertexSpots, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/spots.vp" ).instantiate());
		segments.put( SegmentTypeStatic.preClip, new SegmentTemplate( BVBSettings.sShaderPath + "preClip.fp" ).instantiate());
		segments.put( SegmentTypeStatic.mClip, new SegmentTemplate( BVBSettings.sShaderPath + "mClip.fp" ).instantiate());
		segments.put( SegmentTypeStatic.preOIT, new SegmentTemplate( BVBSettings.sShaderPath + "preOIT.fp" ).instantiate());
		segments.put( SegmentTypeStatic.wOIT, new SegmentTemplate( BVBSettings.sShaderPath + "wOIT.fp" ).instantiate());
		
		return segments;
	}
}
