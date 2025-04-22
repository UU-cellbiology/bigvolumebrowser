package bvb.scene;

import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GL3;


import bvvpg.core.backend.jogl.JoglGpuContext;
import bvvpg.core.shadergen.DefaultShader;
import bvvpg.core.shadergen.Shader;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;

import net.imglib2.RealPoint;
import net.imglib2.mesh.impl.nio.BufferMesh;

public class VisWireMesh {
	
	public static final int OUTLINE=0, WIRE=1, SURFACE=2;
	
	private final Shader progLine;
	
	private final Shader progMesh;

	private int vao;
		
	private float vertices[]; 
	
	public int nPointsN = 0;

	public int renderType = 1;
	
	private Vector4f l_color;	
	
	public float fLineThickness;
	
	public final float fWireLineThickness = 1.0f;

	private boolean initialized;
	
	private BufferMesh mesh = null;
	
	public static final int SURFACE_PLAIN=0, SURFACE_SHADE=1, SURFACE_SHINY=2, SURFACE_SILHOUETTE=3; 
	
	int surfaceRender = SURFACE_PLAIN;
	
	public static final int silhouette_TRANSPARENT=0, silhouette_CULLED=1; 
	
	int silhouetteRender = silhouette_TRANSPARENT;
	
	float silhouetteDecay = 2.0f;
	
	boolean wireAntiAliasing = false;
	
	private long nMeshTrianglesSize = 0;
	
	volatile boolean bLocked = false;
	
	VisPolyLineAA centerLine = null;
	
	ArrayList<VisPolyLineAA> wireLine = null;
	

	public VisWireMesh()
	{
		final Segment lineVp = new SegmentTemplate( VisWireMesh.class, "/scene/simple_color_clip.vp" ).instantiate();
		final Segment lineFp = new SegmentTemplate( VisWireMesh.class, "/scene/simple_color_clip.fp" ).instantiate();		
		progLine = new DefaultShader( lineVp.getCode(), lineFp.getCode() );
				
		final Segment meshVp = new SegmentTemplate( VisWireMesh.class, "/scene/mesh.vp" ).instantiate();
		final Segment meshFp = new SegmentTemplate( VisWireMesh.class, "/scene/mesh.fp" ).instantiate();
		progMesh = new DefaultShader( meshVp.getCode(), meshFp.getCode() );
	}
	
	public VisWireMesh(final BufferMesh meshin, final float fLineThickness_, final Color color_in, final int nRenderType)
	{
		this();
		
		fLineThickness= fLineThickness_;	
		l_color = new Vector4f(color_in.getComponents(null));		
		renderType = nRenderType;
		this.mesh = meshin;
		
	}
	
	
	public void setThickness(float fLineThickness_)
	{
		fLineThickness = fLineThickness_;
		if(centerLine!=null)
			centerLine.setThickness( fLineThickness );
	}
	
	public void setColor(Color color_in)
	{
		l_color = new Vector4f(color_in.getComponents(null));
		if(centerLine != null)
			centerLine.setColor( l_color );
		if(wireLine!=null)
		{
			 for (VisPolyLineAA segment : wireLine)
				 segment.setColor( l_color ); 
			
		}
	}
	
	public void setRenderType(int nRenderType_)
	{
		renderType = nRenderType_;		
	}
	
	public int getRenderType()
	{
		return renderType;		
	}
	
	public void setMesh(BufferMesh mesh)
	{
		this.mesh = mesh;
	}
	
	
	private boolean init( final GL3 gl )
	{
		bLocked  = true;
		//if(renderType == SURFACE)
		//{
			return initGPUBufferMesh(gl);	
		//}
		
		
		//bLocked  = false;
		//return true;
	}
	
	private void initGPUBufferWire( final GL3 gl )
	{
		initialized = true;
		if(nPointsN>1)
		{

			// ..:: VERTEX BUFFER ::..
	
			final int[] tmp = new int[ 2 ];
			gl.glGenBuffers( 1, tmp, 0 );
			final int vbo = tmp[ 0 ];
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, vbo );
			gl.glBufferData( GL.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap( vertices ), GL.GL_STATIC_DRAW );
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
	
	
			// ..:: VERTEX ARRAY OBJECT ::..
	
			gl.glGenVertexArrays( 1, tmp, 0 );
			vao = tmp[ 0 ];
			gl.glBindVertexArray( vao );
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, vbo );
			gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 0 );
			gl.glBindVertexArray( 0 );
		}
	}
	

	
	/** upload MeshData to GPU **/
	private boolean initGPUBufferMesh( GL3 gl )
	{
		
		final int[] tmp = new int[ 3 ];
		
		gl.glGenBuffers( 3, tmp, 0 );
		final int meshPosVbo = tmp[ 0 ];
		final int meshNormalVbo = tmp[ 1 ];
		final int meshEbo = tmp[ 2 ];
		
		if(mesh==null)
			return false;
		else
			if (mesh.vertices()==null)
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

		gl.glGenVertexArrays( 1, tmp, 0 );
		vao = tmp[ 0 ];
		gl.glBindVertexArray( vao );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshPosVbo );
		gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 0 );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, meshNormalVbo );
		gl.glVertexAttribPointer( 1, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 1 );
		
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, meshEbo );
		
		gl.glBindVertexArray( 0 );
		
		nMeshTrianglesSize = mesh.triangles().size();	
		System.out.println(nMeshTrianglesSize*3);
		System.out.println(mesh.vertices().size());
		System.out.println(mesh.triangles().indices().capacity());

		initialized = true;

		return true; 
	}

	public void draw( final GL3 gl, final Matrix4fc pvm, Matrix4fc vm )
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
			if(!init(gl))
			{
				bLocked = false;
				return;
			}
		}
		bLocked = false;

//		if(nPointsN>1)
//		{
			
			JoglGpuContext context = JoglGpuContext.get( gl );
			
			gl.glDepthFunc( GL.GL_LESS);
			
			if(renderType == SURFACE)
			{
				final Matrix4f itvm = vm.invert( new Matrix4f() ).transpose();

				progMesh.getUniformMatrix4f( "pvm" ).set( pvm );
				progMesh.getUniformMatrix4f( "vm" ).set( vm );
				progMesh.getUniformMatrix3f( "itvm" ).set( itvm.get3x3( new Matrix3f() ) );
				progMesh.getUniform4f("colorin").set(l_color);
				progMesh.getUniform1i("surfaceRender").set(surfaceRender);
//				progMesh.getUniform1i("clipactive").set(BigTraceData.nClipROI);
//				progMesh.getUniform3f("clipmin").set(new Vector3f(BigTraceData.nDimCurr[0][0],BigTraceData.nDimCurr[0][1],BigTraceData.nDimCurr[0][2]));
//				progMesh.getUniform3f("clipmax").set(new Vector3f(BigTraceData.nDimCurr[1][0],BigTraceData.nDimCurr[1][1],BigTraceData.nDimCurr[1][2]));
				progMesh.getUniform1i("silType").set(silhouetteRender);
				progMesh.getUniform1f("silDecay").set(silhouetteDecay);
				progMesh.setUniforms( context );
				progMesh.use( context );
				if(surfaceRender == SURFACE_SILHOUETTE && silhouetteRender == silhouette_TRANSPARENT)
				{
					gl.glDepthFunc( GL.GL_ALWAYS);
				}

				//gl.glEnable(GL.GL_BLEND);
				//gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
				gl.glBindVertexArray( vao );			
				gl.glDrawElements( GL_TRIANGLES, ( int ) nMeshTrianglesSize * 3, GL_UNSIGNED_INT, 0 );
				gl.glBindVertexArray( 0 );
			}
			else
			{
		
				progLine.getUniformMatrix4f( "pvm" ).set( pvm );
				progLine.getUniform4f("colorin").set(l_color);
//				progMesh.getUniform1i("clipactive").set(BigTraceData.nClipROI);
//				progMesh.getUniform3f("clipmin").set(new Vector3f(BigTraceData.nDimCurr[0][0],BigTraceData.nDimCurr[0][1],BigTraceData.nDimCurr[0][2]));
//				progMesh.getUniform3f("clipmax").set(new Vector3f(BigTraceData.nDimCurr[1][0],BigTraceData.nDimCurr[1][1],BigTraceData.nDimCurr[1][2]));
				progLine.setUniforms( context );
				progLine.use( context );			
	
				gl.glBindVertexArray( vao );
				//gl.glDrawArrays( GL.GL_TRIANGLE_STRIP, 0, nTotVert);
				gl.glLineWidth(1.0f);
				gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL.GL_LINE_STRIP );//oly
//				for(int i =0; i<mesh.triangles().size();i++)
//				{
//					gl.glDrawArrays(  GL.GL_LINE_STRIP, i*3,i*3+1 );
//				}
				//gl.glDrawArrays(  GL.GL_LINE_STRIP, 0, 200);
				//gl.glDrawArrays(  GL.GL_POINTS, 0, mesh.vertices().size());
				gl.glDrawElements( GL.GL_LINE_LOOP, ( int ) 2 * 3, GL_UNSIGNED_INT, 0 );
				gl.glBindVertexArray( 0 );	
				
				
			}
			gl.glDepthFunc( GL.GL_LESS);
		//}
	}
	
	public static float[] getNormal(float [][] triangle)
	{
        final float v10x = triangle[1][0] - triangle[0][0];
        final float v10y = triangle[1][1] - triangle[0][1];
        final float v10z = triangle[1][2] - triangle[0][2];

        final float v20x = triangle[2][0] - triangle[0][0];
        final float v20y = triangle[2][1] - triangle[0][1];
        final float v20z = triangle[2][2] - triangle[0][2];

        final float nx = v10y * v20z - v10z * v20y;
        final float ny = v10z * v20x - v10x * v20z;
        final float nz = v10x * v20y - v10y * v20x;
        final float nmag = (float) Math.sqrt(Math.pow(nx, 2) + Math.pow(ny, 2) + Math.pow(nz, 2));

        return new float[]{nx / nmag, ny / nmag, nz / nmag};
	}
	
	public static float[][] getCumNormal(float [] normale)
	{
		float [][] out = new float [3][3];
		for (int i=0;i<3;i++)
		{
			for(int d = 0; d<3;d++)
			{
				out[i][d] = (i+1)*normale[d]; 
			}
		}
		return out;
	}
	
	public static void addTriangle(final BufferMesh mesh_in, final float[][] triangle)
	{
		final float [] normale = getNormal(triangle);
		final float [][] cumNormal = getCumNormal(normale);
		long [] index = new long[3];
		double vNormalMag ;
		for (int i=0;i<3;i++)
		{
			vNormalMag =  Math.sqrt(Math.pow(cumNormal[i][0], 2) + Math.pow(cumNormal[i][1], 2) + Math.pow(cumNormal[i][2], 2));
			index[i] = mesh_in.vertices().add(triangle[i][0],triangle[i][1],triangle[i][2],
					cumNormal[i][0] / vNormalMag, cumNormal[i][1] / vNormalMag, cumNormal[i][2] / vNormalMag,0.0,0.0);
		}
		mesh_in.triangles().add(index[0], index[1], index[2], normale[0], normale[1], normale[2]);
	}
	
	public static void addTriangleWithoutNormale(final BufferMesh mesh_in, final float[][] triangle)
	{
		long [] index = new long[3];
		for (int i=0;i<3;i++)
		{			
			index[i] = mesh_in.vertices().add(triangle[i][0],triangle[i][1],triangle[i][2]);
		}
		mesh_in.triangles().add(index[0], index[1], index[2]);

	}
}
