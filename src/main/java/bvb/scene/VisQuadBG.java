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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import net.imglib2.realtransform.AffineTransform3D;

import org.joml.Vector2f;

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
	
	private int nBGShader;
	
	private int vaoQuad;
	
	private boolean quadInitialized;
	
	final int nTotShadersN = 7;
	
	public VolumeViewerPanel bvvViewer; 
	
	long fTimeIni = 0;
	
	float fObjectSize = 20;
	float fImageAspect = 20;
	
	int textureId = 0;
	
	int nMethod = 0;

	public VisQuadBG()
	{
		nBGShader = (int) (Math.random() * nTotShadersN) + 1;
		//debug
		//nBGShader = 7;
	
		initShader();
	}
	
	public void bindBVV(final VolumeViewerPanel bvvViewer_)
	{
		this.bvvViewer = bvvViewer_;
		if(nBGShader == 3 || nBGShader == 4)
		{
		    final AffineTransform3D t = bvvViewer.state().getViewerTransform();
		    t.rotate( 0, -Math.PI/2.5 );
		    bvvViewer.state().setViewerTransform( t );
		}
		
		if(nBGShader == 5)
		{
		    final AffineTransform3D t = bvvViewer.state().getViewerTransform();
		    t.rotate( 1, -0.2 );
		    bvvViewer.state().setViewerTransform( t );
		}
	}
	
	private void initShader()
	{
		final Segment quadvp = new SegmentTemplate( VisQuadBG.class, BVBSettings.sShaderPath + "bg/bg.vp" ).instantiate();
		Segment quadfp = null;
		if(nBGShader <  1 || nBGShader > nTotShadersN)
			nBGShader = 2;

		quadfp = new SegmentTemplate( VisQuadBG.class, BVBSettings.sShaderPath + "bg/bg"+Integer.toString( nBGShader )+".fp" ).instantiate();
		
		progQuad = new DefaultShader( quadvp.getCode(), quadfp.getCode() );
		
		if(nBGShader == 5)
		{
			fObjectSize += Math.random() * 100;
			nMethod = ( int ) Math.round( Math.random() );
		}
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
				 1,  1, 0,    1.0f, 1.0f, // top right
				 1, -1, 0,    1.0f, 0.0f, // bottom right
				-1, -1, 0,    0.0f, 0.0f, // bottom left
				-1,  1, 0,    0.0f, 1.0f  // top left
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
		
		
		// Attribute 0: Position (3 floats, stride = 5 * Float.BYTES)
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
		gl.glEnableVertexAttribArray(0);
		
		// Attribute 1: UV / posW (2 floats, offset = 3 * Float.BYTES)
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
		gl.glEnableVertexAttribArray(1);
		
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, eboQuad );
		gl.glBindVertexArray( 0 );
		
		fTimeIni = System.currentTimeMillis();
		
		//ping pong logo
		if(nBGShader == 7)
		{
			try (InputStream stream = getClass().getResourceAsStream("/bvb/scene/bg/bvb_bounce.png")) {
			    if (stream == null) {
			        throw new RuntimeException("Resource not found: /bvb/scene/bg/bvb_bounce.png");
			    }
			    textureId = loadTexture(gl, stream);
			} catch (IOException e) {
			    e.printStackTrace();
			}
			//textureId = loadTexture(gl, this.getClass().getResource("/bvb/scene/bg/bvb_bounce.png").toString());
		}
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
		if(nBGShader >=2)
		{
			setGizmoAlignedMatrix();
		}
		if(nBGShader == 7)
		{
			gl.glActiveTexture( GL.GL_TEXTURE0 );
			gl.glBindTexture( GL.GL_TEXTURE_2D, textureId );
			progQuad.getUniform1i("logoImg").set(0);
			progQuad.getUniform1f("imgAspect").set(fImageAspect);
		}
		progQuad.getUniform2f( "u_renderSize" ).set( new Vector2f(BVVSettings.renderWidth, BVVSettings.renderHeight) );
		progQuad.getUniform2f( "u_canvasSize" ).set( new Vector2f(bvvViewer.getWidth(), bvvViewer.getHeight()) );
		progQuad.getUniform1f("fTime").set(fTime);
		
		progQuad.setUniforms( context );
		
		progQuad.use( context );

		gl.glBindVertexArray( vaoQuad );
		gl.glDrawElements( GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0 );
		gl.glBindVertexArray( 0 );
		if(nBGShader == 7)
		{
			gl.glBindTexture( GL.GL_TEXTURE_2D, 0 );
		}

	}
	
	void setGizmoAlignedMatrix()
	{
	    final AffineTransform3D t = bvvViewer.state().getViewerTransform();
	    
	    // Extract normalized rotation components directly from the transform matrix

	    double [][] matBVV = new double[3][4];
	    t.toMatrix( matBVV);

	    double scaleD = 0.0;
	    for(int d = 0; d < 3; d++)
	    {
	    	scaleD += matBVV[0][d] * matBVV[0][d];
	    	//reverse the angle of Y to match GLSL UV
	    	matBVV[1][d] *= -1;
	    }
	    
	    // Get the scale/zoom
	    float scale = ( float ) Math.sqrt( scaleD);
	    
	    String [] varNames = new String [] {"u_uu", "u_vv", "u_ww"};
	    // Upload basis camera vectors to GLSL	    
	    for(int d = 0; d < 3; d++)
	    {
	    	 progQuad.getUniform3f(varNames[d]).set((float) matBVV[d][0]/scale, (float)matBVV[d][1]/scale, (float)matBVV[d][2]/scale);
	    }

	    //BVB logo or FIJI logo
	    if(nBGShader == 2 || nBGShader == 6 )
	    {
	    	//lock the scale
	    	double scalefin;
	    	if(nBGShader == 2)
	    		scalefin = Math.min( Math.max( scale, 0.33f ), 2.37f);
	    	else
	    		scalefin = Math.min( Math.max( scale, 0.5*0.33f ), 0.5*2.37f);	    	
	    	t.scale( scalefin/scale );
	    	bvvViewer.state().setViewerTransform(t);
	    	progQuad.getUniform1f( "scale" ).set( (float)scalefin );
		    //System.out.println("scale " + Float.toString( scale )); 	
	    }
	    //infinite cubes
	    if(nBGShader == 5)
	    {
	    	progQuad.getUniform1f( "objSize").set( fObjectSize );
	    	progQuad.getUniform1i( "nMethod").set( nMethod );	
	    }
  
	}
	
	public int loadTexture(GL3 gl, InputStream inputStream) {
	    try {
	    	BufferedImage image = ImageIO.read(inputStream);
	        int width = image.getWidth();
	        int height = image.getHeight();
	        fImageAspect = (float)width / (float)height;
	        int[] pixels = new int[width * height];
	        image.getRGB(0, 0, width, height, pixels, 0, width);

	        // Convert ARGB to RGBA ByteBuffer
	        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4)
	        		.order(java.nio.ByteOrder.nativeOrder());
	        for (int y = 0; y < height; y++) {
	            for (int x = 0; x < width; x++) {
	                int pixel = pixels[y * width + x];
	                buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
	                buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
	                buffer.put((byte) (pixel & 0xFF));         // Blue
	                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
	            }
	        }
	        buffer.flip();

	        // Generate and bind OpenGL Texture
	        int[] texTmp = new int[1];
	        gl.glGenTextures(1, texTmp, 0);
	        int texId = texTmp[0];

	        gl.glBindTexture(GL.GL_TEXTURE_2D, texId);

	        // Set wrapping & filtering parameters
	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

	        // Upload pixel buffer
	        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
	        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

	        return texId;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;
	    }
	}
}
