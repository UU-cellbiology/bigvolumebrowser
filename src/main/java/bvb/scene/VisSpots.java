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

import bvb.core.BVBSettings;
import bvb.core.BVVSettings;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector4f;

import bvvpg.core.backend.jogl.JoglGpuContext;
import bvvpg.core.shadergen.DefaultShader;
import bvvpg.core.shadergen.Shader;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;
import bvvpg.core.util.MatrixMath;

import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;


/** example class that draws point of specific shape and filling type **/

public class VisSpots extends AbstractClipTransformVis
{
	public static final int RENDER_FILLED = 0, RENDER_OUTLINE = 1, RENDER_GAUSS = 2; 

	public static final int SHAPE_ROUND = 0, SHAPE_SQUARE = 1; 
	
	private Shader prog;

	private int vao;
	
	private Vector4f l_color;
	
	public float fSpotSize;
	
	public float fSizeScale = 1.0f;
	
	private int renderType = 0;
	
	private int spotShape = 0;
	
	private int spotShade = 0;
	
	float vertices[] = null; 
	
	float spotSizes[] = null;
	
	float property[] = null; 
	
	float colors[] = null; 
	
	private int nSpotsN;
	
	private boolean initialized;
	
	volatile boolean bLocked = false;
	
	LUTUploaderGPU lutGPU = null;
	
	boolean bInvertLUT = false;
	 
	private int nMapLUTMode = 0;
	
	final float [] fMapLUTMinRange = new float[2];
	
	float fMapLUTGamma = 1.0f;
	
	boolean bInvertAlpha = false;
	 
	private int nMapAlphaMode = 0;
	
	final float [] fMapAlphaMinRange = new float[2];
	
	float fMapAlphaGamma = 1.0f;
	
	float fExtraAlpha = 1.0f;
	
	int colorsVbo;
	
	boolean reInitColors = false;
	
	public VisSpots()
	{
		initShader();
	}
	
	void initShader()
	{
		final Segment pointVp = new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots.vp" ).instantiate();
		final Segment pointFp = new SegmentTemplate( VisSpots.class, BVBSettings.sShaderPath + "spots.fp" ).instantiate();		
		prog = new DefaultShader( pointVp.getCode(), pointFp.getCode() );
	}
	
	/** constructor with multiple vertices **/
	public VisSpots(final float fSpotSize_, final Color color_in, final int nShape_, final int nRenderType_)
	{
		this();
		
		fSpotSize = Math.abs(fSpotSize_);
		
		l_color = new Vector4f(color_in.getComponents(null));		
		
		renderType = nRenderType_;
		
		spotShape = nShape_;
		
		vertices = new float [nSpotsN*3];//assume 3D
	}
	
	void setVertices( ArrayList< RealPoint > points)
	{
		int i,j;	
		
		nSpotsN = points.size();
		
		vertices = new float [nSpotsN * 3]; //assume 3D
	
		for (i = 0; i < nSpotsN; i++)
		{
			for (j = 0; j < 3; j++)
			{
				vertices[i*3+j] = points.get(i).getFloatPosition(j);
			}			
		}		
		initialized = false;
	}

	
	/** any of the last two arguments can be null **/
	public void setVertices( final ArrayList< RealPoint > points, final float [] spotSizes_, final float [] property_)
	{	
		setVertices(points);

		if(spotSizes_ != null)
		{
			if(points.size() != spotSizes_.length)
			{
				System.err.println( "Number of spots is not equal to number of sizes records!");
				return;
			}	
			setSizes(spotSizes_);
		}
		
		if(property_ != null)
		{
			if(points.size() != property_.length)
			{
				System.err.println( "Number of spots is not equal to number of spot property records!");
				return;
			}	
			setProperty(property_);
		}
		initialized = false;
	}
	
	void setSizes(final float [] spotSizes_)
	{
		if(vertices == null)
		{
			System.err.println( "Error setting spot sizes, first coordinates need to be initialized!");
			return;
		}
		
		if(vertices.length/3 != spotSizes_.length)
		{
			System.err.println( "Number of spots is not equal to the provided number of sizes.");
			return;
		}
		spotSizes = new float[spotSizes_.length];
		
		for (int i = 0; i < spotSizes_.length; i++)
		{
			spotSizes[i] = spotSizes_[i];
		}
		
		fSpotSize = -1.0f;
		
	}
	
	void setProperty(final float [] property_)
	{
		if(vertices == null)
		{
			System.err.println( "Error setting up spots properties, first coordinates need to be initialized!");
			return;
		}
		
		if(vertices.length/3 != property_.length)
		{
			System.err.println( "Number of spots is not equal to number of provided property items");
			return;
		}
		property = new float[property_.length];
		
		for (int i = 0; i < property_.length; i++)
		{
			property[i] = property_[i];
		}
				
	}
	
	public void setColors(final float [] colors_)
	{
		if(vertices == null)
		{
			System.err.println( "Error setting up spots colors, first coordinates need to be initialized!");
			return;
		}
		if(vertices.length/3 != colors_.length/4)
		{
			System.err.println( "Number of spots is not equal to number of provided colors");
			return;
		}
		
		colors = new float[colors_.length];
		
		for (int i = 0; i < colors_.length; i++)
		{
			colors[i] = colors_[i];
		}
		if(initialized)
		{
			reInitColors = true;
		}
	}
	
	public void setMapAlphaMode(int nMapAlphaMode_)
	{
		nMapAlphaMode = nMapAlphaMode_;
	}
	
	public int getMapAlphaMode()
	{
		return nMapAlphaMode;
	}

	public void setMapLUTMode(final int nMapLUTMode_)
	{
		nMapLUTMode = nMapLUTMode_;
	}
	
	public void setInvertedLUT(boolean bInv)
	{
		bInvertLUT = bInv;
	}
	public boolean isInvertedLUT()
	{
		return bInvertLUT;
	}
	
	public void setMapLUTRange(final float fMin, final float fMax)
	{
		fMapLUTMinRange[0] = fMin;
		fMapLUTMinRange[1] = fMax - fMin;
	}
	public void setInvertedAlpha(boolean bInv)
	{
		bInvertAlpha = bInv;
	}
	public boolean isInvertedAlpha()
	{
		return bInvertAlpha;
	}
	
	public void setMapAlphaRange(final float fMin, final float fMax)
	{
		fMapAlphaMinRange[0] = fMin;
		fMapAlphaMinRange[1] = fMax - fMin;
	}
	
	public void setExtraAlphaCoefficient(final float dCoeff)
	{
		fExtraAlpha = dCoeff;
	}
	
	public float getExtraAlphaCoefficient()
	{
		return fExtraAlpha;
	}
	
	public void setSizeScale(final float fSizeScale_)
	{
		fSizeScale = fSizeScale_;
	}
	
	public float getSizeScale()
	{
		return fSizeScale;
	}
	
	public int getMapLUTMode()
	{
		return nMapLUTMode;
	}
	
	public void setMapLUTGamma(final float fGamma)
	{
		fMapLUTGamma = fGamma;
	}
	
	public void setMapAlphaGamma(final float fGamma)
	{
		fMapAlphaGamma = fGamma;
	}
	
	public void setLUTUploaderGPU (final LUTUploaderGPU lutGPU)
	{
		this.lutGPU = lutGPU;
	}
	
	public void setColor(Color pointColor) 
	{		
		l_color = new Vector4f(pointColor.getComponents(null));		
	}
	
	public Color getColor() 
	{
		return new Color(l_color.x,l_color.y,l_color.z,l_color.w);
	}
	
	public void setSize(float fSpotSize_)
	{
		fSpotSize = fSpotSize_;
		initialized = false;
	}
	
	/** 0 - filled, 1 - outline **/
	public void setRenderType(int nRenderType_)
	{
		renderType = nRenderType_;
		
	}
	
	public int getRenderType()
	{
		return renderType;
	}
	
	/** 0 - round, 1 - square **/
	public void setShape(int nShape_)
	{
		spotShape = nShape_;		
	}
	
	public int getShape()
	{
		return spotShape;
	}
	
	/** only for round filled spots 
	 * 0 - plain, 1 - shaded **/
	public void setShade(int nShade_)
	{
		spotShade = nShade_;		
	}
	
	public int getShade()
	{
		return spotShade;
	}	
	
	private void init( final GL3 gl )
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

		//quad per point 
		float[] quadVertices = {
			    -0.5f, -0.5f,
			     0.5f, -0.5f,
			    -0.5f,  0.5f,
			     0.5f,  0.5f
			};

		// reserve buffers

		final int[] tmp = new int[ 5 ];
		gl.glGenBuffers( 5, tmp, 0 );
		final int quadVBO = tmp [ 0 ];
		final int posVbo = tmp[ 1 ];
		final int sizeVbo = tmp[ 2 ];
		final int propertyVbo = tmp[ 3 ];
		colorsVbo = tmp[ 4 ];
		
		
		// ..:: QUAD BUFFER ::..
		
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, quadVBO);
		gl.glBufferData(GL.GL_ARRAY_BUFFER, quadVertices.length * Float.BYTES, FloatBuffer.wrap(quadVertices), GL.GL_STATIC_DRAW);
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
		
		// ..:: VERTEX BUFFER ::..

		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, posVbo );
		gl.glBufferData( GL.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap( vertices ), GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );

		// ..:: RADIUS BUFFER ::..

		if( fSpotSize < 0.0f )
		{
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, sizeVbo );
			gl.glBufferData( GL.GL_ARRAY_BUFFER, spotSizes.length * Float.BYTES, FloatBuffer.wrap( spotSizes ), GL.GL_STATIC_DRAW );
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
		}
		
		// ..:: PROPERTY BUFFER ::..

		if( property != null )
		{
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, propertyVbo );
			gl.glBufferData( GL.GL_ARRAY_BUFFER, property.length * Float.BYTES, FloatBuffer.wrap( property ), GL.GL_STATIC_DRAW );
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
		}
		
		// ..:: COLORS BUFFER ::..

		if( colors != null )
		{
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, colorsVbo );
			gl.glBufferData( GL.GL_ARRAY_BUFFER, colors.length * Float.BYTES, FloatBuffer.wrap( colors ), GL.GL_STATIC_DRAW );
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
		}
		
		
		// ..:: VERTEX ARRAY OBJECT ::..

		gl.glGenVertexArrays( 1, tmp, 0 );
		vao = tmp[ 0 ];
		gl.glBindVertexArray( vao );
		
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, quadVBO );
		gl.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, 2 * Float.BYTES, 0);
		gl.glEnableVertexAttribArray(0);

		
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, posVbo );
		gl.glVertexAttribPointer( 1, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 1 );
		gl.glVertexAttribDivisor(1, 1);
		
		if( fSpotSize < 0.0f )
		{
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, sizeVbo );
			gl.glVertexAttribPointer( 2, 1, GL_FLOAT, false, Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 2 );
			gl.glVertexAttribDivisor(2, 1);
		}
		if( property != null )
		{		
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, propertyVbo );
			gl.glVertexAttribPointer( 3, 1, GL_FLOAT, false, Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 3 );
			gl.glVertexAttribDivisor(3, 1);
		}
		
		if( colors != null )
		{		
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, colorsVbo );
			gl.glVertexAttribPointer( 4, 4, GL_FLOAT, false, 4 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 4 );
			gl.glVertexAttribDivisor(4, 1);
		}
		
		gl.glBindVertexArray( 0 );
		
		initialized = true;
		bLocked  = false;

	}
	
	private void reInitColors( GL3 gl )
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
		gl.glBindVertexArray( vao );
		if( colors != null )
		{
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, colorsVbo );
			gl.glBufferData( GL.GL_ARRAY_BUFFER, colors.length * Float.BYTES, FloatBuffer.wrap( colors ), GL.GL_STATIC_DRAW );
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, colorsVbo );
			gl.glVertexAttribPointer( 3, 4, GL_FLOAT, false, 4*Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 3 );

		}
		gl.glBindVertexArray( 0 );
		reInitColors = false;
		bLocked  = false;
	}
	
	@Override
	public void reload()
	{
		initShader();
		initialized = false;
	}

	@Override
	public void draw(final GL3 gl, final Matrix4fc pvm, final Matrix4fc vm, final int [] screen_size , final int nTimePoint, final boolean bWeightedOIT)
	{
	
		if ( !initialized )
			init( gl );
		
		if(reInitColors)
			reInitColors( gl );
		
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
		if(nMapLUTMode > 0 && lutGPU != null)
		{
			if(!lutGPU.initTexture(gl))
			{
				nMapLUTMode = 0;
			}
			
		}
		
		//let's extract pure projection matrix
		final Matrix4f pureProj = new Matrix4f(pvm);
		final Matrix4f invView = new Matrix4f(vm);
		invView.invert();
		pureProj.mul(invView);
		
		
		//full view transform + object transform
		final Matrix4f vtm = new Matrix4f();
		//add transform
		final Matrix4f trM = MatrixMath.affine( transform, new Matrix4f() );
		vm.mul( trM, vtm );
		
		// point scale factor in the view space
		// taking into account possible shear
		final Vector2f pScale = getScaleFactorNoShear(vtm);
		
		JoglGpuContext context = JoglGpuContext.get( gl );
	
		prog.getUniformMatrix4f( "vm" ).set( vtm );
		prog.getUniformMatrix4f( "pm" ).set( pureProj );
		
		prog.getUniform1f( "fnratio" ).set( BVVSettings.fnratio );
		
		prog.getUniform1f( "pointSizeReal" ).set( fSpotSize );
		prog.getUniform2f( "pScale" ).set( pScale );
		
		if(fSpotSize < 0.0)
		{
			prog.getUniform1f( "fSizeScale" ).set( fSizeScale );			
		}
		else
		{
			prog.getUniform1f( "fSizeScale" ).set( 1.0f );			
		}

		prog.getUniform4f( "colorin" ).set( l_color );
		prog.getUniform1i( "nHasColors" ).set( colors == null ? 0:1 );
		prog.getUniform1i( "renderType" ).set( renderType );
		prog.getUniform1i( "pointShape" ).set( spotShape );
		prog.getUniform1i( "pointShade" ).set( spotShade );
		prog.getUniform1i( "clipactive" ).set(0);
		
		if(clipState != 0 && clipInt != null)
		{
			prog.getUniform1i("clipactive").set(clipState);
			prog.getUniform3f("clipmin").set(clipInt,bvvpg.core.shadergen.MinMax.MIN);
			prog.getUniform3f("clipmax").set(clipInt,bvvpg.core.shadergen.MinMax.MAX);
			final AffineTransform3D t = new AffineTransform3D();
			t.set( transform );
			t.preConcatenate( clipTransform.inverse() );
			prog.getUniformMatrix4f( "cliptransform" ).set( MatrixMath.affine(t, new Matrix4f()) );
		}	
		
		prog.getUniform1i("wOIT").set(bWeightedOIT?1:0);
		prog.getUniform1i("nMapLUTMode").set(nMapLUTMode);
		prog.getUniform1f("mapGamma").set(fMapLUTGamma);
		prog.getUniform1i("bInvLUT").set( bInvertLUT?1:0 );
		
		prog.getUniform1i("nMapAlphaMode").set(nMapAlphaMode);
		prog.getUniform1f("alphaGamma").set(fMapAlphaGamma);
		prog.getUniform1i("bInvAlpha").set( bInvertAlpha?1:0 );
		prog.getUniform1f("extraAlpha").set(fExtraAlpha);
		

		if(nMapLUTMode > 0 && lutGPU != null)
		{
			prog.getUniform1i("sizeLUT").set(lutGPU.getLUTSize());
			prog.getUniform1f("mapMin").set(fMapLUTMinRange[0]);
			prog.getUniform1f("mapRange").set(fMapLUTMinRange[1]);	
		}
		
		if(nMapAlphaMode > 0 )
		{
			prog.getUniform1f("alphaMin").set(fMapAlphaMinRange[0]);
			prog.getUniform1f("alphaRange").set(fMapAlphaMinRange[1]);		
		}
		prog.setUniforms( context );		
		prog.use( context );
		
		if(nMapLUTMode > 0)
		{
			gl.glActiveTexture( GL_TEXTURE0 );
			if(lutGPU.getTextureID() > 0)
			{
				gl.glBindTexture( GL_TEXTURE_2D, lutGPU.getTextureID() );
			}
		}
		
		gl.glBindVertexArray( vao );
		gl.glDrawArraysInstanced( GL.GL_TRIANGLE_STRIP, 0, 4, nSpotsN );
		gl.glBindVertexArray( 0 );		
		if(nMapLUTMode > 0)
		{
			if(lutGPU.getTextureID() > 0)
				gl.glBindTexture( GL_TEXTURE_2D, 0 );
		}
	}
	
	Vector2f getScaleFactorNoShear(final Matrix4f vtm)
	{
		final float[] m = new float[16];
		vtm.get( m ); // raw, sheared view matrix
		
		//Extract the basis vectors
		// Column 0: Basis X (Right vector)
		float rx = m[0];
		float ry = m[1];
		float rz = m[2];

		// Column 1: Basis Y (Up vector)
		float ux = m[4];
		float uy = m[5];
		float uz = m[6];
		
		// Extract the clean X-scale factor (represents true zoom)
		float trueZoomScale = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);

		// Normalize the Right vector to strip its scale, leaving pure direction
		float invScaleX = (trueZoomScale > 0.0f) ? 1.0f / trueZoomScale : 1.0f;
		float normRx = rx * invScaleX;
		float normRy = ry * invScaleX;
		float normRz = rz * invScaleX;

		//Remove any shear component (projection of Right onto Up) from the Up vector
		float dotRU = normRx * ux + normRy * uy + normRz * uz;
		float cleanUx = ux - dotRU * normRx;
		float cleanUy = uy - dotRU * normRy;
		float cleanUz = uz - dotRU * normRz;

		//Calculate the clean Y-scale factor just in case zoom/scale is non-uniform
		float trueScaleY = (float) Math.sqrt(cleanUx * cleanUx + cleanUy * cleanUy + cleanUz * cleanUz);
		
		return new Vector2f(trueZoomScale, trueScaleY);
	}
}
