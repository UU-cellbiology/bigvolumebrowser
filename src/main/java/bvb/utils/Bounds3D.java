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
package bvb.utils;

import net.imglib2.FinalRealInterval;

public class Bounds3D
{
	private final double [] minBound;
	private final double [] maxBound;

	public Bounds3D( final double []  minBound, final double [] maxBound )
	{
		if(minBound.length != 3 || maxBound.length != 3)
			throw new IllegalArgumentException();
		for(int d=0; d<3; d++ )
		{
			if ( minBound[d] > maxBound[d] )
				throw new IllegalArgumentException();
		}
		this.minBound = minBound;
		this.maxBound = maxBound;
	}
	public Bounds3D( FinalRealInterval bounds3D)
	{
		if(bounds3D.numDimensions() != 3)
			throw new IllegalArgumentException();
		this.minBound = bounds3D.minAsDoubleArray();
		this.maxBound = bounds3D.maxAsDoubleArray();

	}
	public double [] getMinBound()
	{
		return minBound;
	}

	public double [] getMaxBound()
	{
		return maxBound;
	}
	
	public Bounds3D join( final Bounds3D other )
	{
		final double [] newMinBound = new double [3]; 
		final double [] newMaxBound = new double [3];

		for (int d=0; d<3; d++)
		{
			newMinBound[d] = Math.min( minBound[d], other.minBound[d] );
			newMaxBound[d] = Math.max( maxBound[d], other.maxBound[d] );
		}
		
		return new Bounds3D( newMinBound, newMaxBound );
	}
	
	@Override
	public String toString()
	{
		String output = "Bounds3D ";
		for (int d=0; d<3; d++)
		{
			output = output + "[ "+ minBound[d] + ", " + maxBound[d] + " ]";
		}
		return output;
	}
	
	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( o == null || getClass() != o.getClass() )
			return false;

		final Bounds3D that = ( Bounds3D ) o;
		for (int d=0; d<3; d++)
		{
			if ( Double.compare( that.minBound[d], minBound[d] ) != 0 )
				return false;
			if ( Double.compare( that.maxBound[d], maxBound[d] ) != 0 )
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode()
	{
		int hash = 17;
		for(int d=0; d<3; d++)
		{
			hash = hash * 23 + Double.hashCode(minBound[d]);
			hash = hash * 23 + Double.hashCode(maxBound[d]);
		}
		return hash;
	}
}
