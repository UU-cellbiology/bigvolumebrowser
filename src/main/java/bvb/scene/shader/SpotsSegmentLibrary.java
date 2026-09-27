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
		segments.put( SpotsSegmentType.SpotsRoundMSAA, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/roundMSAA.fp", 
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
		segments.put( SpotsSegmentType.SpotsRoundOutlineMSAA, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/round_outlineMSAA.fp" ).instantiate());

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
