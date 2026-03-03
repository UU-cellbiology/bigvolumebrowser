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
package bvb.utils.transform;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.viewer.Source;

import bdv.viewer.SourceToConverterSetupBimap;
import bvb.shapes.BasicShape;
import bvb.utils.Misc;


public class TransformCenter
{
	private final SourceToConverterSetupBimap bimap;
	
	private final Map< Object, double[]> objToCenters = new HashMap<>();
	
	/** a map to store object's center coordinates,
	 *  to reduce calculations **/
	private final Map< Object, double[] > objToDefCenters = new HashMap<>();

	public TransformCenter( final SourceToConverterSetupBimap bimap )
	{
		this.bimap = bimap;
	}
	
	public double[] getCenters( final Object obj )
	{
		double [] out =  objToCenters.get( obj );
		if(out == null)
		{
			out = getDefaultCenters(obj);
			setCenters( obj, out );
		}		
		return out;
	}
	
	public void updateCenters(final Object obj)
	{
		setCenters( obj, getDefaultCenters(obj));
	}

	public void setCenters( final Object obj, final double[] centers)
	{
		objToCenters.put( obj, centers );
	}	
	
	
	public double [] getDefaultCenters(final Object obj)
	{
		RealInterval interval = null;
		if(obj instanceof ConverterSetup)
		{
			final Source< ? > src = bimap.getSource( (ConverterSetup)obj ).getSpimSource();
			interval = Misc.getSourceBoundingBoxAllTP(src);
		}	
		if(obj instanceof BasicShape)
		{
			interval = ((BasicShape)obj).boundingBox();
		}

		return Misc.getIntervalCenter(interval);			
	}
	
	public double [] getNonTransformedCenters(final Object obj)
	{
		double [] center = objToDefCenters.get(obj);
		if(center != null)
			return center;
		
		if(obj instanceof ConverterSetup)
		{
			final Source< ? > src = bimap.getSource( (ConverterSetup)obj ).getSpimSource();
			RealInterval interval = Misc.getSourceBoundingBoxAllTP(src);
			center = Misc.getIntervalCenter( interval );
			AffineTransform3D srcTrFixed = new AffineTransform3D();
			(( TransformedSource< ? > )src).getFixedTransform( srcTrFixed );
			srcTrFixed.inverse().apply( center, center );
		}

		if(obj instanceof BasicShape)
		{
			center = Misc.getIntervalCenter( ((BasicShape)obj).boundingBoxNotTransformed() );
		}
		objToDefCenters.put( obj, center );
		
		return center;		
	}
}
