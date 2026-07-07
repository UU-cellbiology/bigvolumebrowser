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

import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL2GL3.GL_LINE;
import static com.jogamp.opengl.GL2GL3.GL_FILL;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;


import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector4f;

import bvb.core.BVBSettings;
import bvb.core.BVVSettings;
import bvb.shapes.MeshProcessing;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;


import bvvpg.core.backend.jogl.JoglGpuContext;
import bvvpg.core.shadergen.DefaultShader;
import bvvpg.core.shadergen.Shader;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;
import bvvpg.core.util.MatrixMath;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.impl.nio.BufferMesh;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

/** example class showing different ways to render a mesh**/

public class VisMesh extends AbstractClipTransformVis
{	
	private Shader progPoints;
	
	private Shader progMesh;

	private int vao;
	
	private BufferedImage imageTexture = null;
	
	private int texId;
	
	private Vector4f l_color =  new Vector4f(Color.WHITE.getComponents(null));	

	private boolean initialized;
	
	private BufferMesh mesh = null;

	public static final int MESH = 0, POINTS = 1;	
	
	int renderType = MESH;
	
	float fPointSize = 0.1f;
	
	public static final int SURFACE_PLAIN = 0, SURFACE_SHADE = 1, SURFACE_SHINY = 2, SURFACE_SILHOUETTE = 3; 
	
	int surfaceRender = SURFACE_SHADE;
	
	static final int silhouette_TRANSPARENT = 0, silhouette_CULLED = 1; 
	
	int silhouetteRender = silhouette_TRANSPARENT;	

	float silhouetteDecay = 1.0f;
	
	public static final int GRID_FILLED = 0, GRID_WIRE = 1, GRID_CARTESIAN = 2;
	
	int gridType = GRID_FILLED;
	
	float cartesianGridStep = 2.0f;
	
	float cartesianFraction = 0.2f;
	
	float fWireLineWidth = 1.0f;
	
	volatile boolean bLocked = false;
	
	boolean bHasTexture = false;
	
	boolean bUseTexture = false;  
	
	
	public VisMesh()
	{
		initShader();
	}
	
	void initShader()
	{
		final Segment pointVp = new SegmentTemplate( VisMesh.class, BVBSettings.sShaderPath + "scaled_point.vp" ).instantiate();
		final Segment pointFp = new SegmentTemplate( VisMesh.class, BVBSettings.sShaderPath + "scaled_point.fp" ).instantiate();		
		progPoints = new DefaultShader( pointVp.getCode(), pointFp.getCode() );
			
		final Segment meshVp = new SegmentTemplate( VisMesh.class, BVBSettings.sShaderPath + "mesh.vp" ).instantiate();
		final Segment meshFp = new SegmentTemplate( VisMesh.class, BVBSettings.sShaderPath + "mesh.fp" ).instantiate();
		progMesh = new DefaultShader( meshVp.getCode(), meshFp.getCode() );
	}
	
	public VisMesh(final Mesh meshin)
	{
		this();
		setMesh(meshin);
		
	}
	
	public VisMesh(final Mesh meshin, final BufferedImage imageTexture )
	{
		this(imageTexture);
		setMesh(meshin);
		bHasTexture  = true;
		bUseTexture = true;
	}	
	
	public VisMesh(final BufferedImage imageTexture)
	{
		this.imageTexture = imageTexture;
		initShader();
	}
	
	public Color getColor()
	{
		return new Color(l_color.x,l_color.y,l_color.z,l_color.w);
	}
	
	public void setColor(final Color color_in)
	{
		l_color = new Vector4f(color_in.getComponents(null));
	}
	
	public boolean hasTexture()
	{
		return bHasTexture;
	}
	
	public synchronized void useTexture(boolean bUseTexture_)
	{
		if(bHasTexture)
		{
			bUseTexture = bUseTexture_;
		}
	}
	
	public boolean isTextureUsed()
	{
		return bUseTexture;
	}
	
	public void setRenderType(final int nRenderType_)
	{
		renderType = nRenderType_;		
	}
	
	public int getRenderType()
	{
		return renderType;		
	}
	
	public void setSurfaceRenderType(final int surfaceRender_)
	{
		surfaceRender = surfaceRender_;		
	}
	
	public int getSurfaceRenderType()
	{
		return surfaceRender;		
	}
	
	public void setSurfaceGridType(final int gridType_)
	{
		gridType = gridType_;		
	}
	
	public int getSurfaceGridType()
	{
		return gridType;		
	}
	
	public void setWireLineWidth(final float fIn)
	{
		fWireLineWidth = fIn;
	}
	
	public float getWireLineWidth()
	{
		return fWireLineWidth;
	}
	
	public void setCartesianGrid(final float cartesianGridStep_, final float cartesianFraction_)
	{
		cartesianGridStep = cartesianGridStep_;		
		cartesianFraction = cartesianFraction_;
	}
	
	public void setPointsSize(final float fPointSize_)
	{
		fPointSize = fPointSize_;
	}
	
	public float getPointsSize()
	{
		return fPointSize;
	}
	
	public void setSilhouetteDecay(final float silhouetteDecay_)
	{
		silhouetteDecay = silhouetteDecay_;
	}
	
	public float getSilhouetteDecay()
	{
		return silhouetteDecay;
	}
	
	public void setMesh(final Mesh mesh)
	{
		
		if( mesh.vertices().size() == 0)
		{
			System.err.println("Error loading mesh, zero vertices!");
			return;
		}
		//see if normals are already present in the provided mesh
		final double [] test_norm = new double[] {mesh.vertices().nx( 0 ),mesh.vertices().ny( 0 ), mesh.vertices().nz( 0 )};
		//yes, they are, let's just load
		if(Double.compare(  LinAlgHelpers.length( test_norm ), 0.0) != 0)
		{
			this.mesh = new BufferMesh( mesh.vertices().size(), mesh.triangles().size(), true );
			Meshes.copy( mesh, this.mesh );
		}
		//no, let's calculate normals
		else
		{
			this.mesh = new BufferMesh( mesh.vertices().size(), mesh.triangles().size(), true );
			MeshProcessing.calculateNormals( mesh, this.mesh );
		}
	}	
	
	/** upload MeshData to GPU **/
	private boolean initMeshGPU( final GL3 gl )
	{
		final IntBuffer indicesT = mesh.triangles().indices().duplicate();
		indicesT.rewind();

		final int[] tmp = new int[ 4 ];
		gl.glGenBuffers( 4, tmp, 0 );
		final int meshPosVbo = tmp[ 0 ];
		final int meshNormalVbo = tmp[ 1 ];
		final int meshEbo = tmp[ 2 ];
		final int meshUVVbo = tmp[ 3 ];
		
		if(mesh == null)
			return false;
		else
			if (mesh.vertices() == null)
				return false;

		final FloatBuffer vertBuff = mesh.vertices().verts();
		vertBuff.rewind();
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshPosVbo );
		gl.glBufferData( GL.GL_ARRAY_BUFFER, vertBuff.limit() * Float.BYTES, vertBuff, GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );

		final FloatBuffer normals = mesh.vertices().normals();
		normals.rewind();
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshNormalVbo );
		gl.glBufferData( GL.GL_ARRAY_BUFFER, normals.limit() * Float.BYTES, normals, GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );

		final IntBuffer indices = mesh.triangles().indices();
		indices.rewind();
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, meshEbo );
		gl.glBufferData( GL.GL_ELEMENT_ARRAY_BUFFER, indices.limit() * Integer.BYTES, indices, GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, 0 );
		
		if(bHasTexture)
		{
			final FloatBuffer uvs = mesh.vertices().texCoords();
			uvs.rewind();
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshUVVbo );
			gl.glBufferData( GL.GL_ARRAY_BUFFER, uvs.limit() * Float.BYTES, uvs, GL.GL_STATIC_DRAW );
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
			
			// ..:: TEXTURE ::..

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
			
		}

		gl.glGenVertexArrays( 1, tmp, 0 );
		vao = tmp[ 0 ];
		gl.glBindVertexArray( vao );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshPosVbo );
		gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 0 );

		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshNormalVbo );
		gl.glVertexAttribPointer( 1, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 1 );
		
		if(bHasTexture)
		{
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshUVVbo );
			gl.glVertexAttribPointer( 2, 2, GL_FLOAT, false, 2 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 2 );
		}
		
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, meshEbo );
		gl.glBindVertexArray( 0 );

		initialized = true;

		return true; 
	}
	
	@Override
	public void reload()
	{
		initShader();		
		initialized = false;
	}

	@Override
	public void draw( final GL3 gl, final Matrix4fc pvm, final Matrix4fc vm, final int [] screen_size, final int nTimePoint, final boolean bWeightedOIT)
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
			if(!initMeshGPU(gl))
			{
				bLocked = false;
				return;
			}
		}
		
		bLocked = false;

		JoglGpuContext context = JoglGpuContext.get( gl );

		//gl.glDepthFunc( GL.GL_LESS);

		//add transform
		final Matrix4f trM = MatrixMath.affine( transform, new Matrix4f() );
		final Matrix4f pvtm = new Matrix4f();
		final Matrix4f vtm = new Matrix4f();

		pvm.mul( trM, pvtm );
		vm.mul( trM, vtm );
		

		if(renderType == MESH)
		{
			final Matrix4f itvm = vtm.invert( new Matrix4f() ).transpose();
			
			progMesh.getUniformMatrix4f( "pvm" ).set( pvtm );
			progMesh.getUniformMatrix4f( "vm" ).set( vtm );
			progMesh.getUniformMatrix3f( "itvm" ).set( itvm.get3x3( new Matrix3f() ) );
			progMesh.getUniform1f( "fnratio" ).set( BVVSettings.fnratio );
			
			progMesh.getUniform4f("colorMesh").set(l_color);
			progMesh.getUniform1i("surfaceRender").set(surfaceRender);
			progMesh.getUniform1i("gridType").set(gridType);
			progMesh.getUniform1f("cartesianGridStep").set(cartesianGridStep);
			progMesh.getUniform1f("cartesianFraction").set(cartesianFraction);
			
			progMesh.getUniform1i("silType").set(silhouetteRender);
			progMesh.getUniform1f("silDecay").set(silhouetteDecay);
			progMesh.getUniform1i("clipactive").set(0);
			if(clipState !=0 && clipInt != null)
			{
				progMesh.getUniform1i("clipactive").set(clipState);
				progMesh.getUniform3f("clipmin").set(clipInt,bvvpg.core.shadergen.MinMax.MIN);
				progMesh.getUniform3f("clipmax").set(clipInt,bvvpg.core.shadergen.MinMax.MAX);
				final AffineTransform3D t = new AffineTransform3D();
				t.set( transform );
				t.preConcatenate( clipTransform.inverse() );
				//t.set( clipTransform.inverse() );
		
				progMesh.getUniformMatrix4f( "cliptransform" ).set( MatrixMath.affine(t, new Matrix4f()) );
			}
			progMesh.getUniform1i("wOIT").set(bWeightedOIT?1:0);
			progMesh.getUniform1i("bUseTexture").set(bUseTexture?1:0);
			progMesh.setUniforms( context );
			progMesh.use( context );

//			gl.glEnable(GL.GL_BLEND);
//			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			
			if(bHasTexture && bUseTexture)
			{
				gl.glActiveTexture( GL_TEXTURE0 );
				gl.glBindTexture( GL_TEXTURE_2D, texId );
			}
			
			if(gridType == GRID_WIRE)
			{
				gl.glLineWidth( fWireLineWidth );
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL_LINE);
			}
			gl.glBindVertexArray( vao );			
			gl.glDrawElements( GL_TRIANGLES, mesh.triangles().size() * 3, GL_UNSIGNED_INT, 0 );
			if(gridType == GRID_WIRE)
			{
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL_FILL);
			}
			gl.glBindVertexArray( 0 );
			if(bHasTexture && bUseTexture)
			{
				gl.glBindTexture( GL_TEXTURE_2D, 0 );
				gl.glBindVertexArray( 0 );
			}

		}
		else
		{

			Vector2f window_sizef =  new Vector2f (screen_size[0], screen_size[1]);
			
			Vector2f ellipse_axes = new Vector2f((float)screen_size[0]/(float)BVVSettings.renderWidth, (float)screen_size[1]/(float)BVVSettings.renderHeight);
			
			float fPointScale = Math.min(ellipse_axes.x,ellipse_axes.y);
			ellipse_axes.mul(1.0f/fPointScale);
			ellipse_axes.x = ellipse_axes.x * ellipse_axes.x;
			ellipse_axes.y = ellipse_axes.y * ellipse_axes.y;
	
			progPoints.getUniformMatrix4f( "pvm" ).set( pvtm );
			progPoints.getUniform1f( "pointSizeReal" ).set( fPointSize );
			progPoints.getUniform1f( "pointScale" ).set( fPointScale );
			progPoints.getUniform4f( "colorin" ).set( l_color );
			progPoints.getUniform2f( "windowSize" ).set( window_sizef );
			progPoints.getUniform2f( "ellipseAxes" ).set( ellipse_axes );
			progPoints.getUniform1i( "renderType" ).set( VisSpots.RENDER_FILLED );
			progPoints.getUniform1i( "pointShape" ).set( VisSpots.SHAPE_ROUND );
			progPoints.getUniform1i("clipactive").set(0);
			
			if(clipState != 0 && clipInt != null)
			{
				progPoints.getUniform1i("clipactive").set(clipState);
				progPoints.getUniform3f("clipmin").set(clipInt,bvvpg.core.shadergen.MinMax.MIN);
				progPoints.getUniform3f("clipmax").set(clipInt,bvvpg.core.shadergen.MinMax.MAX);
				AffineTransform3D t = new AffineTransform3D();
				t.set( transform );
				t.preConcatenate( clipTransform.inverse() );
				progPoints.getUniformMatrix4f( "cliptransform" ).set( MatrixMath.affine(t, new Matrix4f()) );
			}
			
			progPoints.getUniform1i("wOIT").set(bWeightedOIT?1:0);
			progPoints.setUniforms( context );			
			progPoints.use( context );
			
			gl.glBindVertexArray( vao );
			gl.glDrawArrays( GL.GL_POINTS, 0, mesh.vertices().size());
			gl.glBindVertexArray( 0 );

		}

	}
	
	public static int[] IntBuffertoArray(IntBuffer b) {
	    if(b.hasArray()) 
	    {
	        if(b.arrayOffset() == 0)
	            return b.array();

	        return Arrays.copyOfRange(b.array(), b.arrayOffset(), b.array().length);
	    }

	    b.rewind();
	    int[] foo = new int[b.remaining()];
	    b.get(foo);

	    return foo;
	}
	
	
	public static ByteBuffer convertImageToByteBuffer(BufferedImage image, boolean flipVertically) 
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
