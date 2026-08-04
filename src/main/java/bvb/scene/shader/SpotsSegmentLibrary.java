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
		
		segments.put( SpotsSegmentType.NoLutColors, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/colors/nolutcolors.fp" ).instantiate());
		segments.put( SpotsSegmentType.NoLutNoColors, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/colors/nolutnocolors.fp" ).instantiate());
		segments.put( SpotsSegmentType.LutAxis, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/colors/lutAxis.fp" ).instantiate());
		segments.put( SpotsSegmentType.LutSize, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/colors/lutSize.fp" ).instantiate());
		segments.put( SpotsSegmentType.LutProperty, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/colors/lutProperty.fp" ).instantiate());
		segments.put( SpotsSegmentType.LutInvert, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/colors/lutInvert.fp" ).instantiate());

		
		return segments;
	}
}
