package bvb.gui.shapes;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.RealInterval;

import bvb.shapes.BasicSpots;
import bvb.shapes.BasicShape;

public class SpotsLUTMapSetups
{
	private final Map< BasicSpots, float[][]> spotsToMaps = new HashMap<>();
	
	public float[][] getLUTMapRanges( final BasicSpots obj )
	{
		float [][] out = spotsToMaps.get( obj );
		if(out == null)
		{
			out = getCurrentOrDefaultRanges(obj);
			setRanges( obj, out );
		}		
		return out;
	}
	
	private float[][] getCurrentOrDefaultRanges( BasicSpots spots )
	{
		final float [][] ranges = new float [5][4];
		RealInterval bbox = ((BasicShape)spots).boundingBoxNotTransformed();
		for(int d = 0; d<3; d++)
		{
			for(int i = 0; i < 2; i++)
			{
				ranges[d][i*2] = ( float ) bbox.realMin( d );
				ranges[d][i*2+1] = ( float ) bbox.realMax( d );

			}
		}
		if(spots.getPointSize()<0.0)
		{
			final float [] sizeRange = spots.getSizeRange();
			for(int i = 0; i < 2; i++)
			{
				ranges[3][i*2] = sizeRange[0];
				ranges[3][i*2+1] = sizeRange[1];
			}
		}
		return ranges;
	}

	public void setRanges( final BasicSpots obj, final float[][] ranges)
	{		
		spotsToMaps.put( obj, ranges );
	}
}
