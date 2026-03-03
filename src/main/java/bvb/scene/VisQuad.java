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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import bvb.core.BVBSettings;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;

import bvvpg.core.backend.jogl.JoglGpuContext;
import bvvpg.core.shadergen.DefaultShader;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;

public class VisQuad
{
	private DefaultShader progQuad = null;
	
	private final int nBGShader;
	
	private int vaoQuad;
	
	private boolean quadInitialized;
	
	long fTimeIni  = 0;

	public VisQuad(final int nShaderN )
	{
		nBGShader = nShaderN;
	
		initShader();
	}
	private void initShader()
	{
		final Segment quadvp = new SegmentTemplate( VisQuad.class, BVBSettings.sShaderPath + "bg/bg.vp" ).instantiate();
		Segment quadfp = null;
		switch(nBGShader)
		{
		case 2:
			quadfp = new SegmentTemplate( VisQuad.class, BVBSettings.sShaderPath + "bg/bg2.fp" ).instantiate();
			break;
		case 3:
			quadfp = new SegmentTemplate( VisQuad.class, BVBSettings.sShaderPath + "bg/bg3.fp" ).instantiate();
			break;
		case 4:
			quadfp = new SegmentTemplate( VisQuad.class, BVBSettings.sShaderPath + "bg/bg4.fp" ).instantiate();
			break;
		default:
			quadfp = new SegmentTemplate( VisQuad.class, BVBSettings.sShaderPath + "bg/bg1.fp" ).instantiate();
		}
		progQuad = new DefaultShader( quadvp.getCode(), quadfp.getCode() );
	}
	
	public void reload()
	{
		initShader();
		quadInitialized = false;
	}
	
	private void initQuad( GL3 gl )
	{
		if ( quadInitialized )
			return;
		quadInitialized = true;

		final float verticesQuad[] = {
				//    pos      
				 1,  1, 0,     // top right
				 1, -1, 0,     // bottom right
				-1, -1, 0,     // bottom left
				-1,  1, 0,     // top left
		};

		final int[] tmp = new int[ 1 ];
		gl.glGenBuffers( 1, tmp, 0 );
		final int vboQuad = tmp[ 0 ];
		gl.glBindBuffer( GL_ARRAY_BUFFER, vboQuad );
		gl.glBufferData( GL_ARRAY_BUFFER, verticesQuad.length * Float.BYTES, FloatBuffer.wrap( verticesQuad ), GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );

		final int indices[] = {
				0, 3, 1,   // first triangle
				1, 3, 2    // second triangle
		};
		gl.glGenBuffers( 1, tmp, 0 );
		final int eboQuad = tmp[ 0 ];
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, eboQuad );
		gl.glBufferData( GL_ELEMENT_ARRAY_BUFFER, indices.length * Integer.BYTES, IntBuffer.wrap( indices ), GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );

		gl.glGenVertexArrays( 1, tmp, 0 );
		vaoQuad = tmp[ 0 ];
		gl.glBindVertexArray( vaoQuad );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, vboQuad );
		gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 0 );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, eboQuad );
		gl.glBindVertexArray( 0 );
		
		fTimeIni = System.currentTimeMillis();
	}
	
	public void drawQuad( GL3 gl )
	{
		initQuad( gl );
		
		JoglGpuContext context = JoglGpuContext.get( gl );

		//float fTime =  ( System.currentTimeMillis()%200)+1;
		float fTime =  ( System.currentTimeMillis()- fTimeIni);
		if(nBGShader == 1 && fTime > 2000)
		{
			fTimeIni = System.currentTimeMillis();
		}

		gl.glDepthFunc( GL.GL_ALWAYS);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); 
		

		progQuad.getUniform1f("fTime").set(fTime);
		
		progQuad.setUniforms( context );
		
		progQuad.use( context );

		gl.glBindVertexArray( vaoQuad );
		gl.glDrawElements( GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0 );
		gl.glBindVertexArray( 0 );
		//gl.glDepthFunc( GL.GL_LESS);
	}
}
