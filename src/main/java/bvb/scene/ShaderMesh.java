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
package bvb.scene;

import bvb.scene.shader.MeshSegmentLibrary;
import bvb.scene.shader.SegmentTypeComposite;
import bvb.scene.shader.SegmentTypeStatic;
import bvb.scene.shader.SegmentsLibrary;
import bvvpg.core.shadergen.Shader;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;
import bvvpg.core.shadergen.generate.SegmentedShaderBuilder;

public class ShaderMesh
{
	public static Shader buildMeshShader(final VisMesh visMesh, final boolean bCurrentwOIT)
	{
		final SegmentedShaderBuilder builder = new SegmentedShaderBuilder();
		//vertex
		final Segment meshVertex = SegmentsLibrary.compositeSegments.get( SegmentTypeComposite.VertexMesh ).instantiate();

		if(visMesh.isTextureUsed())
		{
			meshVertex.insert( "useTexture", SegmentTemplate.fromCode("    texCoord = vec2( aTexCoord.x, aTexCoord.y );").instantiate() );
		}
		else
		{
			meshVertex.insert( "useTexture", SegmentTemplate.fromCode("    texCoord = vec2( 0, 0 );").instantiate() );			
		}
		
		builder.vertex( meshVertex );
		
		//fragment
		final Segment meshFp = SegmentsLibrary.compositeSegments
										.get( SegmentTypeComposite.FragmentMesh ).instantiate();
		
		//clipping
		if(visMesh.clipState != 0 && visMesh.clipInt != null)
		{
			meshFp.insert( "preClip", SegmentsLibrary.staticSegments.get( SegmentTypeStatic.preClip) );
			meshFp.insert( "mClip", SegmentsLibrary.staticSegments.get( SegmentTypeStatic.mClip ) );			
		}
		else
		{
			meshFp.insert( "preClip", SegmentsLibrary.emptySeg );
			meshFp.insert( "mClip", SegmentsLibrary.emptySeg );
		}
		
		//usage of texture
		if(visMesh.isTextureUsed())
		{
			meshFp.insert( "useTexture", SegmentTemplate.fromCode("    vec4 colorout = texture( texture1, texCoord );").instantiate());
		}
		else
		{
			meshFp.insert( "useTexture", SegmentTemplate.fromCode("    vec4 colorout = colorMesh;").instantiate());
		}
		
		//surface render
		switch(visMesh.getSurfaceRenderType())
		{
		case VisMesh.SURFACE_PLAIN:
			meshFp.insert("meshSurfaceRender", SegmentsLibrary.emptySeg); 
			break;
		case VisMesh.SURFACE_SHADE:
			meshFp.insert("meshSurfaceRender", MeshSegmentLibrary.meshSegments.get( "shaded" )); 
			break;
		case VisMesh.SURFACE_SHINY:
			meshFp.insert("meshSurfaceRender", MeshSegmentLibrary.meshSegments.get( "shiny" )); 
			break;
		case VisMesh.SURFACE_SILHOUETTE:
			meshFp.insert("meshSurfaceRender", MeshSegmentLibrary.meshSegments.get( "silh" )); 
			break;

		}
		
		//weighted OIT
		if(bCurrentwOIT)
		{
			meshFp.insert( "preOIT", SegmentsLibrary.staticSegments.get( SegmentTypeStatic.preOIT ) );
			meshFp.insert( "wOIT", SegmentsLibrary.staticSegments.get( SegmentTypeStatic.wOIT ) );
		}
		else
		{
			meshFp.insert( "preOIT", SegmentsLibrary.emptySeg );
			meshFp.insert( "wOIT", SegmentsLibrary.emptySeg );
		}
		builder.fragment( meshFp );
//		final StringBuilder fragmentShaderCode = progMesh.getFragmentShaderCode();
//		System.out.println( "fragmentShaderCode MESH = " + fragmentShaderCode );
//		System.out.println( "\n\n--------------------------------\n\n" );
		return builder.build();
	}
}
