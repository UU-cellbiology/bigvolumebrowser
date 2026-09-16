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

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import org.joml.Vector2f;

import bdv.util.Affine3DHelpers;
import bvb.core.BVBSettings;
import bvb.core.BVVSettings;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;

import bvvpg.core.VolumeViewerPanel;
import bvvpg.core.backend.jogl.JoglGpuContext;
import bvvpg.core.shadergen.DefaultShader;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;

public class VisQuadBG
{
	private DefaultShader progQuad = null;
	
	private final int nBGShader;
	
	private int vaoQuad;
	
	private boolean quadInitialized;
	
	public VolumeViewerPanel bvvViewer; 
	
	long fTimeIni = 0;

	public VisQuadBG(final int nShaderN )
	{
		nBGShader = nShaderN;
	
		initShader();
	}
	
	public void bindBVV(final VolumeViewerPanel bvvViewer_)
	{
		this.bvvViewer = bvvViewer_;
	}
	
	
	private void initShader()
	{
		final Segment quadvp = new SegmentTemplate( VisQuadBG.class, BVBSettings.sShaderPath + "bg/bg.vp" ).instantiate();
		Segment quadfp = null;
		switch(nBGShader)
		{
		case 2:
			quadfp = new SegmentTemplate( VisQuadBG.class, BVBSettings.sShaderPath + "bg/bg2.fp" ).instantiate();
			break;
		case 3:
			quadfp = new SegmentTemplate( VisQuadBG.class, BVBSettings.sShaderPath + "bg/bg3.fp" ).instantiate();
			break;
		case 4:
			quadfp = new SegmentTemplate( VisQuadBG.class, BVBSettings.sShaderPath + "bg/bg4.fp" ).instantiate();
			break;
		case 5:
			quadfp = new SegmentTemplate( VisQuadBG.class, BVBSettings.sShaderPath + "bg/bg5.fp" ).instantiate();
			break;
		default:
			quadfp = new SegmentTemplate( VisQuadBG.class, BVBSettings.sShaderPath + "bg/bg1.fp" ).instantiate();
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

		gl.glDepthFunc( GL.GL_ALWAYS);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); 
		
		//float fTime =  ( System.currentTimeMillis()%200)+1;
		float fTime =  ( System.currentTimeMillis()- fTimeIni);
		if(nBGShader == 1 && fTime > 2000)
		{
			fTimeIni = System.currentTimeMillis();
		}
		if(nBGShader == 5)
		{
			setGizmoAlignedMatrix();
		}
		bvvViewer.state().getViewerTransform();
		progQuad.getUniform2f( "u_renderSize" ).set( new Vector2f(BVVSettings.renderWidth, BVVSettings.renderHeight) );
		progQuad.getUniform2f( "u_canvasSize" ).set( new Vector2f(bvvViewer.getWidth(), bvvViewer.getHeight()) );
		progQuad.getUniform1f("fTime").set(fTime);
		
		progQuad.setUniforms( context );
		
		progQuad.use( context );

		gl.glBindVertexArray( vaoQuad );
		gl.glDrawElements( GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0 );
		gl.glBindVertexArray( 0 );
		//gl.glDepthFunc( GL.GL_LESS);
	}
	
	void setGizmoAlignedMatrix()
	{
	    AffineTransform3D t = bvvViewer.state().getViewerTransform();
	    
	    // Extract normalized rotation components directly from the transform matrix
	    // Row 0 = Right Vector (X)
	    // Row 1 = Up Vector (Y)
	    // Row 2 = Forward/Look Vector (Z)
	    
	    float m00 = (float) t.get(0, 0), m01 = (float) t.get(0, 1), m02 = (float) t.get(0, 2);
	    float m10 = (float) t.get(1, 0), m11 = (float) t.get(1, 1), m12 = (float) t.get(1, 2);
	    float m20 = (float) t.get(2, 0), m21 = (float) t.get(2, 1), m22 = (float) t.get(2, 2);

	    // Normalize to strip out scale/zoom
	    float lenX = (float) Math.sqrt(m00*m00 + m01*m01 + m02*m02);
	    float lenY = (float) Math.sqrt(m10*m10 + m11*m11 + m12*m12);
	    float lenZ = (float) Math.sqrt(m20*m20 + m21*m21 + m22*m22);

	    // Upload basis vectors to GLSL
	    progQuad.getUniform3f("u_uu").set(m00 / lenX, m01 / lenX, m02 / lenX); // Camera Right (X)
	    progQuad.getUniform3f("u_vv").set(-m10 / lenY, -m11 / lenY, -m12 / lenY); // Camera Up (-Y to match GLSL UV)
	    progQuad.getUniform3f("u_ww").set(m20 / lenZ, m21 / lenZ, m22 / lenZ); // Camera Look (Z)

	}
}
