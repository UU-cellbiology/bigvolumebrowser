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
import bdv.util.Affine3DHelpers;
import bvb.utils.Misc;
import bvvpg.source.converters.GammaConverterSetup;

public class ClipRotationAngles
{
	

	private final Map< ConverterSetup, double[]> setupToAngles = new HashMap<>();
	public ClipRotationAngles( )
	{


	}
	public double[] getAngles( final ConverterSetup setup )
	{
		double [] out =  setupToAngles.get( setup );
		if(out == null)
		{
			out = getCurrentEulerAngles(setup);
			setAngles(setup, out);
		}
		
		return out;
	}
	
	public void setAngles( final ConverterSetup setup, final double[] eAngles)
	{
		setupToAngles.put( setup, eAngles );
	}
	
	public double [] getCurrentEulerAngles(final ConverterSetup setup)
	{
		final AffineTransform3D clipTr = new AffineTransform3D();
		((GammaConverterSetup)setup).getClipTransform(clipTr);
		final FinalRealInterval interval = ((GammaConverterSetup)setup).getClipInterval(); 
		final double [] center;
		if(interval == null)
		{
			center = new double[3];
		}
		else
		{
			center = Misc.getIntervalCenterNegative( interval);
		}
		clipTr.translate( center );
		final double[] qRotation = new double[4];

		Affine3DHelpers.extractRotationAnisotropic( clipTr, qRotation );
		return Misc.quaternionToEulerAngles(qRotation);
		
	}
}
