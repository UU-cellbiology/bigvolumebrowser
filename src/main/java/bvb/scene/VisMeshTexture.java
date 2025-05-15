/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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

import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_RGB;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector4f;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;


import bvvpg.core.backend.jogl.JoglGpuContext;
import bvvpg.core.shadergen.DefaultShader;
import bvvpg.core.shadergen.Shader;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.impl.nio.BufferMesh;
import net.imglib2.util.LinAlgHelpers;

/** example class showing different ways to render a mesh**/

public class VisMeshTexture 
{	
	private final BufferedImage imageTexture;
	
	private Shader prog;

	private int vao;
	
	private int texId;

	private boolean initialized;
	
	private BufferMesh mesh = null;
	
	volatile boolean bLocked = false;
	

	public VisMeshTexture(final BufferedImage imageTexture)
	{
		this.imageTexture = imageTexture;
		initShader();
	}
	
	void initShader()
	{
		final Segment meshtxVP = new SegmentTemplate( VisMeshTexture.class, "/scene/mesh_texture.vp" ).instantiate();
		final Segment meshtxFp = new SegmentTemplate( VisMeshTexture.class, "/scene/mesh_texture.fp" ).instantiate();
		prog = new DefaultShader( meshtxVP.getCode(), meshtxFp.getCode() );
	}
	
	public VisMeshTexture(final Mesh meshin, final BufferedImage imageTexture )
	{
		this(imageTexture);
		setMesh(meshin);
		
	}

	
	public void setMesh(final Mesh mesh)
	{
		
		this.mesh = new BufferMesh( mesh.vertices().size(), mesh.triangles().size(), true );
		
		//see if normals were setup already
		final double [] test_norm = new double[] {mesh.vertices().nx( 0 ),mesh.vertices().ny( 0 ), mesh.vertices().nz( 0 )};
		
		if(Double.compare(  LinAlgHelpers.length( test_norm ),0.0) != 0)
		{
			Meshes.copy( mesh, this.mesh );
		}
		else
		{
			Meshes.calculateNormals( mesh, this.mesh );
		}
	}	
	
	
	private boolean initMesh( final GL3 gl )
	{
		return initGPUBufferMesh(gl);	
	}
	
	public void reload()
	{
		initShader();
		
		initialized = false;
	}
	
	/** upload MeshData to GPU **/
	private boolean initGPUBufferMesh( GL3 gl )
	{
		
		final int[] tmp = new int[ 3 ];
		gl.glGenBuffers( 3, tmp, 0 );
		final int meshPosVbo = tmp[ 0 ];
		final int meshUVVbo = tmp[ 1 ];
		final int meshEbo = tmp[ 2 ];
		
		
		final FloatBuffer vertBuff = mesh.vertices().verts();
		vertBuff.rewind();
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshPosVbo );
		gl.glBufferData( GL.GL_ARRAY_BUFFER, vertBuff.limit() * Float.BYTES, vertBuff, GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );	

		final FloatBuffer uvs = mesh.vertices().texCoords();
		uvs.rewind();
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshUVVbo );
		gl.glBufferData( GL.GL_ARRAY_BUFFER, uvs.limit() * Float.BYTES, uvs, GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
		
		final IntBuffer indices = mesh.triangles().indices();
		indices.rewind();
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, meshEbo );
		gl.glBufferData( GL.GL_ELEMENT_ARRAY_BUFFER, indices.limit() * Integer.BYTES, indices, GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, 0 );
		
		// ..:: TEXTURES ::..

		gl.glGenTextures( 1, tmp, 0 );
		texId = tmp[ 0 ];
		final ByteBuffer pixelBuffer = convertImageToByteBuffer(imageTexture, false);
		int width = imageTexture.getWidth();
	    int height = imageTexture.getHeight();
		gl.glActiveTexture( GL_TEXTURE0 );
		gl.glBindTexture( GL_TEXTURE_2D, texId );
		gl.glTexImage2D( GL_TEXTURE_2D, 
				0, 
				GL_RGBA, 
				width, 
				height, 
				0, 
				GL_RGBA, 
				GL_UNSIGNED_BYTE, 
				pixelBuffer);
		gl.glGenerateMipmap( GL_TEXTURE_2D );
		gl.glBindTexture( GL_TEXTURE_2D, 0 );
		
		// ..:: VERTEX ARRAY OBJECT ::..

		gl.glGenVertexArrays( 1, tmp, 0 );
		vao = tmp[ 0 ];
		gl.glBindVertexArray( vao );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshPosVbo );
		
		gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 0 );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshUVVbo );
		gl.glVertexAttribPointer( 1, 2, GL_FLOAT, false, 2 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 1 );		

		
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, meshEbo );
		gl.glBindVertexArray( 0 );


//		gl.glGenVertexArrays( 1, tmp, 0 );
//		vao = tmp[ 0 ];
//		gl.glBindVertexArray( vao );
//		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshPosVbo );
//		gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
//		gl.glEnableVertexAttribArray( 0 );
//		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshNormalVbo );
//		gl.glVertexAttribPointer( 1, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
//		gl.glEnableVertexAttribArray( 1 );
//
//		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshBaryVbo );
//		gl.glVertexAttribPointer( 2, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
//		gl.glEnableVertexAttribArray( 2 );
//		
//		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, meshEbo );
//		gl.glBindVertexArray( 0 );		
		
		
//		//build baricentric coordinates for each triangle
//		final float [] barycenter = new float [mesh.vertices().size()*3];
//		
//		final IntBuffer indicesT = mesh.triangles().indices().duplicate();
//		indicesT.rewind();
//
//		final int [] trindices = IntBuffertoArray(indicesT);
//
//		for(int i=0; i<trindices.length; i+=3)
//		{			
//			for(int j=0;j<3;j++)
//			{
//				for(int d=0;d<3;d++)
//				{
//					barycenter[trindices[i+j]*3+d] = 0.0f;
//				}
//				
//			}
//			for(int j=0;j<3;j++)
//			{
//				barycenter[trindices[i+j]*3+j] = 1.0f;
//			}
//
//		}
//
//		final int[] tmp = new int[ 4 ];
//		gl.glGenBuffers( 4, tmp, 0 );
//		final int meshPosVbo = tmp[ 0 ];
//		final int meshNormalVbo = tmp[ 1 ];
//		final int meshBaryVbo = tmp[ 2 ];
//		final int meshEbo = tmp[ 3 ];
//		
//		if(mesh == null)
//			return false;
//		else
//			if (mesh.vertices() == null)
//				return false;
//
//		final FloatBuffer vertBuff = mesh.vertices().verts();
//		vertBuff.rewind();
//		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshPosVbo );
//		gl.glBufferData( GL.GL_ARRAY_BUFFER, vertBuff.limit() * Float.BYTES, vertBuff, GL.GL_STATIC_DRAW );
//		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
//
//		final FloatBuffer normals = mesh.vertices().normals();
//		normals.rewind();
//		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshNormalVbo );
//		gl.glBufferData( GL.GL_ARRAY_BUFFER, normals.limit() * Float.BYTES, normals, GL.GL_STATIC_DRAW );
//		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
//		
//		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshBaryVbo );
//		gl.glBufferData( GL.GL_ARRAY_BUFFER, barycenter.length * Float.BYTES, FloatBuffer.wrap( barycenter ), GL.GL_STATIC_DRAW );
//		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
//
//		final IntBuffer indices = mesh.triangles().indices();
//		indices.rewind();
//		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, meshEbo );
//		gl.glBufferData( GL.GL_ELEMENT_ARRAY_BUFFER, indices.limit() * Integer.BYTES, indices, GL.GL_STATIC_DRAW );
//		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, 0 );
//
//		gl.glGenVertexArrays( 1, tmp, 0 );
//		vao = tmp[ 0 ];
//		gl.glBindVertexArray( vao );
//		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshPosVbo );
//		gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
//		gl.glEnableVertexAttribArray( 0 );
//		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshNormalVbo );
//		gl.glVertexAttribPointer( 1, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
//		gl.glEnableVertexAttribArray( 1 );
//
//		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshBaryVbo );
//		gl.glVertexAttribPointer( 2, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
//		gl.glEnableVertexAttribArray( 2 );
//		
//		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, meshEbo );
//		gl.glBindVertexArray( 0 );

		initialized = true;

		return true; 
	}

	public void draw( final GL3 gl, final Matrix4fc pvm, Matrix4fc vm,  final int [] screen_size)
	{
		
		while (bLocked)
		{
			try
			{
				Thread.sleep( 10 );
			}
			catch ( InterruptedException exc )
			{
				exc.printStackTrace();
			}
		}
		
		bLocked = true;
		
		if ( !initialized )
		{
			if(!initMesh(gl))
			{
				bLocked = false;
				return;
			}
		}
		
		bLocked = false;

		JoglGpuContext context = JoglGpuContext.get( gl );

		prog.getUniformMatrix4f( "pvm" ).set( pvm );
		prog.setUniforms( context );
		prog.use( context );

		gl.glActiveTexture( GL_TEXTURE0 );
		gl.glBindTexture( GL_TEXTURE_2D, texId );
		gl.glBindVertexArray( vao );
		gl.glDrawElements( GL_TRIANGLES, mesh.triangles().size() * 3, GL_UNSIGNED_INT, 0 );
		gl.glBindVertexArray( 0 );
//		gl.glDrawArrays( GL_TRIANGLES, 0, 36 );
		gl.glBindTexture( GL_TEXTURE_2D, 0 );
		gl.glBindVertexArray( 0 );
	

	}
	
	public ByteBuffer convertImageToByteBuffer(BufferedImage image, boolean flipVertically) 
	{
	    int width = image.getWidth();
	    int height = image.getHeight();

	    // Use TYPE_4BYTE_ABGR so we know the byte order
	    BufferedImage convertedImg = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
	    convertedImg.getGraphics().drawImage(image, 0, 0, null);

	    byte[] abgr = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();
	    ByteBuffer rgbaBuffer = ByteBuffer.allocateDirect(width * height * 4);

	    int stride = width * 4;

	    for (int y = 0; y < height; y++) {
	        int row = flipVertically ? (height - 1 - y) : y;
	        int rowStart = row * stride;

	        for (int x = 0; x < width; x++) {
	            int i = rowStart + x * 4;
	            byte a = abgr[i + 0];
	            byte b = abgr[i + 1];
	            byte g = abgr[i + 2];
	            byte r = abgr[i + 3];

	            rgbaBuffer.put(r).put(g).put(b).put(a); // RGBA order
	        }
	    }

	    rgbaBuffer.flip();
	    return rgbaBuffer;
	}

}
