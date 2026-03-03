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

import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import java.awt.image.IndexColorModel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import ij.plugin.LutLoader;

public class LUTUploaderGPU
{
	private IndexColorModel icm = null;
	
	private String sLUTName = null;
	
	private int texColor = -1;
	
	private boolean bTextureInit = true;
	
	int nLUTSize = -1;
	
	public void setLUT(String sLUTName)
	{
		final IndexColorModel icm_lut = LutLoader.getLut(sLUTName);
		if(icm_lut == null)
		{
			System.err.println("Cannot load ImageJ LUT with the name \""+sLUTName+ "\". Wrong name/not installed?");
			return;
		}
		setLUT(icm_lut, sLUTName);
	}
	
	public void setLUT(final IndexColorModel icm_, String sLUTName) 
	{		
		this.sLUTName = sLUTName;
		icm = icm_;
		nLUTSize  = icm.getMapSize();
		bTextureInit = false;

	}
	
	public int getLUTSize()
	{
		return nLUTSize;
	}
	
	public String getLUTName()
	{
		return sLUTName;
	}
	
	public int getTextureID()
	{
		if(icm == null)
			return -1;
		return texColor;
	}
	
	public boolean initTexture( final GL3 gl )
	{		
		if(bTextureInit)
			return true;
		int size_ = icm.getMapSize();
		if (size_ < 65536)
		{
			int nHeight = (int)Math.ceil(size_/256.0);
			final int[] tmp = new int[ 1 ];
			gl.glGenTextures( 1, tmp, 0 );
			texColor = tmp[ 0 ];
			gl.glBindTexture( GL_TEXTURE_2D, texColor );
			//gl.glTexStorage2D( GL_TEXTURE_2D, 1, GL.GL_RGBA8, 256, 256 );
			
			gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );
			gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
			gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
			gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
			gl.glTexImage2D(GL_TEXTURE_2D, 0, GL.GL_RGBA, 256, nHeight, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, ICMToByteBuffer( icm ) );
			gl.glGenerateMipmap( GL_TEXTURE_2D );
			gl.glBindTexture( GL_TEXTURE_2D, 0 );
		}
		else
		{
			System.out.println("LUTs larger than 65535 elements are not supported, aborting. ");
			bTextureInit = false;
			return false;
		}
		bTextureInit = true;
		return true;
	}
	
	public static ByteBuffer ICMToByteBuffer(final IndexColorModel icm )
	{
		ByteBuffer data = null;
		if(icm != null)
		{
			int size_ = icm.getMapSize();
			if (size_ < 65536)
			{
				int nTextureSpan = 256*(int)Math.ceil(size_/256.0);
				final int numBytes = 4 * nTextureSpan;
				data = ByteBuffer.allocateDirect( numBytes ); // allocate a bit more than needed...
				data.order( ByteOrder.nativeOrder() );	
				final IntBuffer sdata = data.asIntBuffer();
				byte [][] colorsARGB = new byte[4][nTextureSpan];
				icm.getAlphas( colorsARGB[0] );
				icm.getReds( colorsARGB[1] );
				icm.getGreens( colorsARGB[2] );
				icm.getBlues( colorsARGB[3] );
				int all = 0;
				for (int i=0; i<size_;i++)
				{
					final int a = colorsARGB[0][i] & 0xff;
					final int r = colorsARGB[1][i] & 0xff;
					final int g = colorsARGB[2][i] & 0xff;
					final int b = colorsARGB[3][i] & 0xff;
					all = ( a << 24 ) | ( b << 16 ) | ( g << 8 ) | r;
					sdata.put( i, all );	
				}
				//fill the rest with the last color
				for (int i = size_; i < nTextureSpan; i++)
				{
					sdata.put( i, all );
				}
			}
		}
		return data;
	}
}
