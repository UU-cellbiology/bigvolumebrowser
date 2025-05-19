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
package bvb.clip;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;

import bdv.viewer.SourceAndConverter;
import bdv.viewer.SourceToConverterSetupBimap;
import bvb.utils.Misc;
import bvvpg.source.converters.GammaConverterSetup;

public class ClipCenters
{
	private final SourceToConverterSetupBimap bimap;
	
	private final Map< ConverterSetup, double[]> setupToCenters = new HashMap<>();
	
	public ClipCenters( final SourceToConverterSetupBimap bimap)
	{
		this.bimap = bimap;
	}
	
	public double[] getCenters( final ConverterSetup setup )
	{
		double [] out =  setupToCenters.get( setup );
		if(out == null)
		{
			out = getCurrentOrDefaultCenters(setup);
			setCenters( setup, out );
		}		
		return out;
	}
	
	public void updateCenters(final ConverterSetup setup)
	{
		setCenters( setup, getCurrentOrDefaultCenters(setup));
	}
	
	public void setCenters( final ConverterSetup setup, final double[] centers)
	{
		setupToCenters.put( setup, centers );
	}
	
	public double [] getCurrentOrDefaultCenters(final ConverterSetup setup)
	{
		AffineTransform3D clipTr = new AffineTransform3D();
		((GammaConverterSetup)setup).getClipTransform(clipTr);
		FinalRealInterval interval = ((GammaConverterSetup)setup).getClipInterval(); 

		if(interval == null)
		{
			final SourceAndConverter< ? > source = bimap.getSource( setup );
			interval = Misc.getSourceBoundingBoxAllTP(source.getSpimSource());

		}
		if(interval == null)
			return null;
			
		final double [] center = Misc.getIntervalCenter(interval);
		
		clipTr.apply( center, center );

		return center;
		
	}
}
