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
