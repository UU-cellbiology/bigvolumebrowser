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
