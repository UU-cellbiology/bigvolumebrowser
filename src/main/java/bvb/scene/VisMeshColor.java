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
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Map;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector4f;

import bvb.core.BVVSettings;
import bvb.scene.WelshPowell.Vertex;

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

public class VisMeshColor 
{	
	private Shader progPoints;
	
	private Shader progMesh;

	private int vao;
	
	private Vector4f l_color =  new Vector4f(Color.WHITE.getComponents(null));	

	private boolean initialized;
	
	private BufferMesh mesh = null;

	public static final int MESH = 0, POINTS = 1;	
	
	public int renderType = MESH;
	
	public float fPointSize = 0.1f;
	
	public static final int SURFACE_PLAIN = 0, SURFACE_SHADE = 1, SURFACE_SHINY = 2, SURFACE_SILHOUETTE = 3; 
	
	int surfaceRender = SURFACE_SHADE;
	
	public static final int silhouette_TRANSPARENT = 0, silhouette_CULLED = 1; 
	
	int silhouetteRender = silhouette_TRANSPARENT;	

	float silhouetteDecay = 1.0f;
	
	public static final int GRID_FILLED = 0, GRID_WIRE = 1, GRID_CARTESIAN = 2;
	
	int gridType = GRID_FILLED;
	
	float cartesianGridStep = 2.0f;
	
	float cartesianFraction = 0.2f;
	
	volatile boolean bLocked = false;
	

	public VisMeshColor()
	{
		initShader();
	}
	
	void initShader()
	{
		final Segment pointVp = new SegmentTemplate( VisPointsScaled.class, "/scene/scaled_point.vp" ).instantiate();
		final Segment pointFp = new SegmentTemplate( VisPointsScaled.class, "/scene/scaled_point.fp" ).instantiate();		
		progPoints = new DefaultShader( pointVp.getCode(), pointFp.getCode() );
				
		final Segment meshVp = new SegmentTemplate( VisMeshColor.class, "/scene/mesh.vp" ).instantiate();
		final Segment meshFp = new SegmentTemplate( VisMeshColor.class, "/scene/mesh.fp" ).instantiate();
		progMesh = new DefaultShader( meshVp.getCode(), meshFp.getCode() );
	}
	
	public VisMeshColor(final Mesh meshin)
	{
		this();
		setMesh(meshin);
		
	}
	
	public void setColor(final Color color_in)
	{
		l_color = new Vector4f(color_in.getComponents(null));
	}
	
	public void setRenderType(final int nRenderType_)
	{
		renderType = nRenderType_;		
	}
	
	public void setSurfaceRenderType(final int surfaceRender_)
	{
		surfaceRender = surfaceRender_;		
	}
	
	public void setSurfaceGridType(final int gridType_)
	{
		gridType = gridType_;		
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
	
	public int getRenderType()
	{
		return renderType;		
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
		
		
//		final float [] barycenter = new float [mesh.vertices().size()*9];
//		
//		for(int i=0; i<mesh.triangles().size(); i++)
//		{
//			barycenter[i*9] = 1.0f;
//			barycenter[i*9+4] = 1.0f;
//			barycenter[i*9+8] = 1.0f;
//		}

//		final float [] barycenter = new float [9];
//		barycenter[0] = 1.0f;
//		barycenter[4] = 1.0f;
//		barycenter[8] = 1.0f;
				

		
		final float [] barycenter = new float [mesh.vertices().size()*3];
		
		final IntBuffer indicesT = mesh.triangles().indices().duplicate();
		indicesT.rewind();
		//boolean test = indices.hasArray();
		final int [] trindices = IntBuffertoArray(indicesT);
		//boolean bFirst = true;
		
		WelshPowell wp = new WelshPowell(mesh.vertices().size(),trindices);
		Map< Vertex, Integer > cIndex = wp.colourVertices();
		
		for (Map.Entry<Vertex, Integer > entry : cIndex.entrySet()) 
		{
			barycenter[entry.getKey().node.intValue()*3+entry.getValue().intValue()]= 1.0f;
//		    System.out.println(entry.getKey() + "/" + entry.getValue());
		}
//		
//		for(int i=0; i<trindices.length; i+=3)
//		{
//			
//			for(int j=0;j<3;j++)
//			{
//				for(int d=0;d<3;d++)
//				{
//					barycenter[trindices[i+j]*3+d] = 0.0f;
//				}
//			}
//			if(bFirst)
//			{
//				barycenter[trindices[i]*3+1] = 1.0f;
//				barycenter[trindices[i+1]*3+2] = 1.0f;
//				barycenter[trindices[i+2]*3] = 1.0f;
//			
//			}
//			else
//			{
//				barycenter[trindices[i]*3+2] = 1.0f;
//				barycenter[trindices[i+1]*3+2] = 1.0f;
//				barycenter[trindices[i+2]*3+1] = 1.0f;				
//			}
//			bFirst = !bFirst;
////			for(int j=0;j<3;j++)
////			{
////				barycenter[trindices[i+j]*3+j] = 1.0f;
////			}
//
//		}

		final int[] tmp = new int[ 4 ];
		gl.glGenBuffers( 4, tmp, 0 );
		final int meshPosVbo = tmp[ 0 ];
		final int meshNormalVbo = tmp[ 1 ];
		final int meshBaryVbo = tmp[ 2 ];
		final int meshEbo = tmp[ 3 ];
		
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
		
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshBaryVbo );
		gl.glBufferData( GL.GL_ARRAY_BUFFER, barycenter.length * Float.BYTES, FloatBuffer.wrap( barycenter ), GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );

		final IntBuffer indices = mesh.triangles().indices();
		indices.rewind();
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, meshEbo );
		gl.glBufferData( GL.GL_ELEMENT_ARRAY_BUFFER, indices.limit() * Integer.BYTES, indices, GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, 0 );

		gl.glGenVertexArrays( 1, tmp, 0 );
		vao = tmp[ 0 ];
		gl.glBindVertexArray( vao );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshPosVbo );
		gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 0 );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshNormalVbo );
		gl.glVertexAttribPointer( 1, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 1 );

		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshBaryVbo );
		gl.glVertexAttribPointer( 2, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 2 );
		
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, meshEbo );
		gl.glBindVertexArray( 0 );

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

		gl.glDepthFunc( GL.GL_LESS);

		if(renderType == MESH)
		{
			final Matrix4f itvm = vm.invert( new Matrix4f() ).transpose();

			progMesh.getUniformMatrix4f( "pvm" ).set( pvm );
			progMesh.getUniformMatrix4f( "vm" ).set( vm );
			progMesh.getUniformMatrix3f( "itvm" ).set( itvm.get3x3( new Matrix3f() ) );
			progMesh.getUniform4f("colorin").set(l_color);
			progMesh.getUniform1i("surfaceRender").set(surfaceRender);
			progMesh.getUniform1i("gridType").set(gridType);
			progMesh.getUniform1f("cartesianGridStep").set(cartesianGridStep);
			progMesh.getUniform1f("cartesianFraction").set(cartesianFraction);
			
			progMesh.getUniform1i("silType").set(silhouetteRender);
			progMesh.getUniform1f("silDecay").set(silhouetteDecay);
//			progMesh.getUniform1i("clipactive").set(BigTraceData.nClipROI);
//			progMesh.getUniform3f("clipmin").set(new Vector3f(BigTraceData.nDimCurr[0][0],BigTraceData.nDimCurr[0][1],BigTraceData.nDimCurr[0][2]));
//			progMesh.getUniform3f("clipmax").set(new Vector3f(BigTraceData.nDimCurr[1][0],BigTraceData.nDimCurr[1][1],BigTraceData.nDimCurr[1][2]));

			progMesh.setUniforms( context );
			progMesh.use( context );
			if(surfaceRender == SURFACE_SILHOUETTE && silhouetteRender == silhouette_TRANSPARENT)
			{
				gl.glDepthFunc( GL.GL_ALWAYS);
			}

			//gl.glEnable(GL.GL_BLEND);
			//gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
			gl.glBindVertexArray( vao );			
			gl.glDrawElements( GL_TRIANGLES, mesh.triangles().size() * 3, GL_UNSIGNED_INT, 0 );
			gl.glBindVertexArray( 0 );
		}
		else
		{

			Vector2f window_sizef =  new Vector2f (screen_size[0], screen_size[1]);
			
			Vector2f ellipse_axes = new Vector2f((float)screen_size[0]/(float)BVVSettings.renderWidth, (float)screen_size[1]/(float)BVVSettings.renderHeight);
			
			float fPointScale = Math.min(ellipse_axes.x,ellipse_axes.y);
			ellipse_axes.mul(1.0f/fPointScale);
			ellipse_axes.x = ellipse_axes.x * ellipse_axes.x;
			ellipse_axes.y = ellipse_axes.y * ellipse_axes.y;
					
			progPoints.getUniformMatrix4f( "pvm" ).set( pvm );
			progPoints.getUniform1f( "pointSizeReal" ).set( fPointSize );
			progPoints.getUniform1f( "pointScale" ).set( fPointScale );
			progPoints.getUniform4f( "colorin" ).set( l_color );
			progPoints.getUniform2f( "windowSize" ).set( window_sizef );
			progPoints.getUniform2f( "ellipseAxes" ).set( ellipse_axes );
			progPoints.getUniform1i( "renderType" ).set( VisPointsScaled.RENDER_FILLED );
			progPoints.getUniform1i( "pointShape" ).set( VisPointsScaled.SHAPE_ROUND );
			progPoints.setUniforms( context );			
			progPoints.use( context );
			
			gl.glBindVertexArray( vao );
			gl.glDrawArrays( GL.GL_POINTS, 0, mesh.vertices().size());
			gl.glBindVertexArray( 0 );

		}
		gl.glDepthFunc( GL.GL_LESS);

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

}
