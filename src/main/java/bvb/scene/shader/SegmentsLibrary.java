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
import bvb.scene.VisMesh;
import bvb.scene.VisSpots;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;

public class SegmentsLibrary
{
	public static final Map< SegmentTypeComposite, SegmentTemplate > compositeSegments = getDefaultCompositeSTemplates();
	public static final Map< SegmentTypeStatic, Segment> staticSegments = getDefaultStaticSegments();

	public static final Segment emptySeg = SegmentTemplate.fromCode("").instantiate();
	
	public static Map< SegmentTypeComposite, SegmentTemplate > getDefaultCompositeSTemplates()
	{
		final HashMap< SegmentTypeComposite, SegmentTemplate > segments = new HashMap<>();
		
		segments.put( SegmentTypeComposite.VertexSpots, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/spots.vp", 
						"spotsScaling" ));
		
		segments.put( SegmentTypeComposite.FragmentSpots, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots/spots.fp", 
						"preColorLUT", "preAlphaMap", "preOIT", "preClip", "mClip", 
						"spotsColor", "spotsAlpha", "spotsShape", "wOIT" ));
		
		
		segments.put( SegmentTypeComposite.VertexMesh, 
				new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "mesh/mesh.vp", 
						"useTexture" ));
		
		segments.put( SegmentTypeComposite.FragmentMesh, 
				new SegmentTemplate( VisMesh.class, BVBSettings.sShaderPath + "mesh/mesh.fp", 
						 "preOIT", "preClip", "meshSurfaceRender", "mClip", "useTexture", "wOIT" ));

		return segments;
	}
	
	public static Map< SegmentTypeStatic, Segment> getDefaultStaticSegments()
	{
		final HashMap< SegmentTypeStatic, Segment > segments = new HashMap<>();
		segments.put( SegmentTypeStatic.mClip, new SegmentTemplate( BVBSettings.sShaderPath + "mClip.fp" ).instantiate());
		segments.put( SegmentTypeStatic.preClip, new SegmentTemplate( BVBSettings.sShaderPath + "preClip.fp" ).instantiate());
		segments.put( SegmentTypeStatic.mClip, new SegmentTemplate( BVBSettings.sShaderPath + "mClip.fp" ).instantiate());
		segments.put( SegmentTypeStatic.preOIT, new SegmentTemplate( BVBSettings.sShaderPath + "preOIT.fp" ).instantiate());
		segments.put( SegmentTypeStatic.wOIT, new SegmentTemplate( BVBSettings.sShaderPath + "wOIT.fp" ).instantiate());
		
		return segments;
	}
}
