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
package bvb.gui.shapes;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.RealInterval;

import bdv.util.BoundedRange;
import bvb.shapes.BasicSpots;
import bvb.utils.BoundedValueDoubleBVB;
import bvb.shapes.BasicShape;

public class SpotsAlphaMapSetups
{
	private final Map< BasicSpots, float[][]> spotsToMaps = new HashMap<>();
	
	public float[][] getLUTMapAllFloat( final BasicSpots obj )
	{
		float [][] out = spotsToMaps.get( obj );
		if(out == null)
		{
			out = getDefaultRanges(obj);
			setRanges( obj, out );
		}		
		return out;
	}
	public float [] getLUTMapRangeFloat( final BasicSpots obj, final int nPropertyInd )
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

	
	public BoundedValueDoubleBVB getLUTMapGamma( final BasicSpots obj, final int nPropertyInd )
	{
		//int nPropertyInd = nPropertyInd_ - 1;
		float [] out = getLUTMapRangeFloat(obj, nPropertyInd );
		if(out != null)
		{
			return new BoundedValueDoubleBVB(out[5], out[6], out[4]);
		}
		
		return null;
		
	}
	
	public BoundedRange getLUTMapRange( final BasicSpots obj, final int nPropertyInd )
	{
		//int nPropertyInd = nPropertyInd_ - 1;
		float [] out = getLUTMapRangeFloat(obj, nPropertyInd );
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
