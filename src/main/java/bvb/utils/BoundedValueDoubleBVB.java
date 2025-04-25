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

import bdv.util.BoundedValueDouble;

public class BoundedValueDoubleBVB extends BoundedValueDouble
{
	public BoundedValueDoubleBVB( final double rangeMin, final double rangeMax, final double currentValue )
	{
		super(rangeMin, rangeMax,  currentValue);
	}
	
	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( o == null || getClass() != o.getClass() )
			return false;

		final BoundedValueDoubleBVB that = ( BoundedValueDoubleBVB ) o;

		if ( Double.compare( that.getRangeMin(), getRangeMin() ) != 0 )
			return false;
		if ( Double.compare( that.getRangeMax(), getRangeMax() ) != 0 )
			return false;
		return Double.compare( that.getCurrentValue(), getCurrentValue()) == 0; 
				
	}
	
	public BoundedValueDoubleBVB join( final BoundedValueDoubleBVB other )
	{
		final double newMinRange = Math.min( getRangeMin(), other.getRangeMin() );
		final double newMaxRange = Math.max( getRangeMax(), other.getRangeMax() );
		final double newValue = 0.5*(getCurrentValue()+other.getCurrentValue());
		return new BoundedValueDoubleBVB( newMinRange, newMaxRange, newValue );
	}
	
	@Override
	public int hashCode()
	{
		int result;
		long temp;
		temp = Double.doubleToLongBits( getRangeMin() );
		result = ( int ) ( temp ^ ( temp >>> 32 ) );
		temp = Double.doubleToLongBits( getRangeMax()  );
		result = 31 * result + ( int ) ( temp ^ ( temp >>> 32 ) );
		temp = Double.doubleToLongBits( getCurrentValue() );
		result = 31 * result + ( int ) ( temp ^ ( temp >>> 32 ) );
		return result;
		
	}
	
	@Override
	public String toString()
	{
		return "BoundedValueDouble[ (" + getRangeMin() + ") " + getCurrentValue() + " (" + getRangeMax() + ") ]";
	}
}
