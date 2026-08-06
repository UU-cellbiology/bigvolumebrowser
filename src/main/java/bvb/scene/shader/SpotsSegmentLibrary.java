package bvb.scene.shader;

import java.util.HashMap;
import java.util.Map;

import bvb.core.BVBSettings;
import bvb.scene.VisSpots;
import bvvpg.core.shadergen.generate.SegmentTemplate;


public class SpotsSegmentLibrary
{
	public static final Map< SpotsSegmentType, Object > spotSegments = getDefaultSpotsSegments(); 
	
	public static Map<SpotsSegmentType, Object > getDefaultSpotsSegments()
	{
		final HashMap< SpotsSegmentType, Object > segments = new HashMap<>();
		
		//composite
		segments.put( SpotsSegmentType.SpotsRound, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/round.fp", 
						"roundRenderType" ));
		segments.put( SpotsSegmentType.SpotsRoundDepth, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/round_depth.fp", 
						"spotsRoundShade" ));
		segments.put( SpotsSegmentType.SpotsColorLUTMode,
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/colors/lutmode.fp", 
						"shaderMapLUTMode", "invertColorLUT" ));
		segments.put( SpotsSegmentType.SpotsAlphaMapMode,
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/alpha/alphamode.fp", 
						"alphaMapMode", "invertAlphaMap" ));
		
		//static
		segments.put( SpotsSegmentType.SpotsRoundGauss, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/round_gauss.fp" ).instantiate());
		segments.put( SpotsSegmentType.SpotsRoundOutline, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/round_outline.fp" ).instantiate());

		segments.put( SpotsSegmentType.SpotsRoundShaded, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/round_shade.fp" ).instantiate());

		segments.put( SpotsSegmentType.SpotsSquareGauss, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/square_gauss.fp" ).instantiate());
		segments.put( SpotsSegmentType.SpotsSquareOutline, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/square_outline.fp" ).instantiate());

		segments.put( SpotsSegmentType.preColorLUT, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/colors/preColorLUT.fp" ).instantiate());
		segments.put( SpotsSegmentType.LutAxis, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/colors/lutAxis.fp" ).instantiate());
		
		segments.put( SpotsSegmentType.preAlphaMap, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/alpha/preAlphaMap.fp" ).instantiate());
		segments.put( SpotsSegmentType.alphaAxis, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/alpha/alphaAxis.fp" ).instantiate());		
		return segments;
	}
}
