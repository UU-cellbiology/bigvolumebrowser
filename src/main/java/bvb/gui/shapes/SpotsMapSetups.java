package bvb.gui.shapes;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.RealInterval;

import bdv.util.BoundedRange;
import bvb.shapes.BasicSpots;
import bvb.utils.BoundedValueDoubleBVB;
import bvb.shapes.BasicShape;

/** class used to store LUT and Alpha mapping of Spots object
 * to different axis or properties (for GUI) **/
public class SpotsMapSetups
{
	private final Map< BasicSpots, float[][]> spotsToMaps = new HashMap<>();
	
	public float[][] getMapAllFloat( final BasicSpots obj )
	{
		float [][] out = spotsToMaps.get( obj );
		if(out == null)
		{
			out = getDefaultRanges(obj);
			setRanges( obj, out );
		}		
		return out;
	}
	public float [] getMapRangeFloat( final BasicSpots obj, final int nPropertyInd )
	{
		//int nPropertyInd = nPropertyInd_ - 1;
		float [][] out = spotsToMaps.get( obj );
		if(out == null)
		{
			out = getDefaultRanges(obj);
			setRanges( obj, out );
		}		
		if(nPropertyInd < 0 || nPropertyInd > 4)
			return null;
		
		return out[nPropertyInd];
	}

	
	public BoundedValueDoubleBVB getMapGamma( final BasicSpots obj, final int nPropertyInd )
	{
		//int nPropertyInd = nPropertyInd_ - 1;
		float [] out = getMapRangeFloat(obj, nPropertyInd );
		if(out != null)
		{
			return new BoundedValueDoubleBVB(out[5], out[6], out[4]);
		}
		
		return null;
		
	}
	
	public BoundedRange getMapRange( final BasicSpots obj, final int nPropertyInd )
	{
		//int nPropertyInd = nPropertyInd_ - 1;
		float [] out = getMapRangeFloat(obj, nPropertyInd );
		if(out != null)
		{
			return new BoundedRange(out[2],out[3],out[0],out[1]);
		}
		return null;	
	}
	
	public float[][] getDefaultRanges( BasicSpots spots )
	{
		final float [][] ranges = new float [5][7];
		RealInterval bbox = ((BasicShape)spots).boundingBoxNotTransformed();
		//RealInterval bbox = ((BasicShape)spots).boundingBox();

		for(int d = 0; d < 3; d++)
		{
			for(int i = 0; i < 2; i++)
			{
				ranges[d][i*2] = ( float ) bbox.realMin( d );
				ranges[d][i*2+1] = ( float ) bbox.realMax( d );

			}
		}
		if(spots.getPointSize() < 0.0)
		{
			final float [] sizeRange = spots.getSizeRange();
			for(int i = 0; i < 2; i++)
			{
				ranges[3][i*2] = sizeRange[0];
				ranges[3][i*2+1] = sizeRange[1];
			}
		}
		if(spots.hasProperty())
		{
			final float [] propRange = spots.getPropertyRange();
			for(int i = 0; i < 2; i++)
			{
				ranges[4][i*2] = propRange[0];
				ranges[4][i*2+1] = propRange[1];
			}
		}
		//gamma and its ranges
		for(int i = 0; i < 5; i++)
		{
			ranges[i][4] = 1.0f;
			ranges[i][5] = 0.01f;
			ranges[i][6] = 5.0f;
		}
		return ranges;
	}

	public void setRanges( final BasicSpots obj, final float[][] ranges)
	{		
		spotsToMaps.put( obj, ranges );
	}
}
