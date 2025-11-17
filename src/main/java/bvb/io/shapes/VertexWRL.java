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
package bvb.io.shapes;

public class VertexWRL
{
	public final float [] xyz = new float[3];
	public final float [] uv = new float [2];
	public final float [] nxyz = new float[3];

	public void setXYZ(final float x,final  float y,final  float z)
	{
		this.xyz[0] = x;
		this.xyz[1] = y;
		this.xyz[2] = z;
	}
	
	public void setXYZ(String x,String y,String z)
	{
		this.xyz[0] = Float.parseFloat( x );
		this.xyz[1] = Float.parseFloat( y );
		this.xyz[2] = Float.parseFloat( z );
	}
	
	public void setUV(final float u, final float v)
	{
		this.uv[0] = u;
		this.uv[1] = v;
	}

	
	public void setUV(String u, String v)
	{
		this.uv[0] = Float.parseFloat( u );
		this.uv[1] = Float.parseFloat( v );
	}

	
	public void setNXYZ(final float x, final float y, final float z)
	{
		this.nxyz[0] = x;
		this.nxyz[1] = y;
		this.nxyz[2] = z;
		normalize( this.nxyz );
	}
	
	public void setNXYZ(String x,String y,String z)
	{
		this.nxyz[0] = Float.parseFloat( x );
		this.nxyz[1] = Float.parseFloat( y );
		this.nxyz[2] = Float.parseFloat( z );
		normalize( this.nxyz );
	}
	
	public static void normalize( final float[] a )
	{
		final double len = Math.sqrt( a[0]*a[0] + a[1]*a[1] + a[2]*a[2]);
		for ( int i = 0; i < 3; ++i )
			a[ i ] /= len;
	}
	
}
