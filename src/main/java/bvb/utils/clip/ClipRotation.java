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
package bvb.utils.clip;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.util.Affine3DHelpers;
import bvb.utils.Misc;
import bvvpg.source.converters.Clippable3D;

public class ClipRotation
{	
	private final Map< Clippable3D, double[]> objToAngles = new HashMap<>();
	
	private final Map< Clippable3D, double[]> objToQuaternion = new HashMap<>();

	public ClipRotation( )
	{

	}
	
	public double[] getAngles( final Clippable3D obj )
	{
		double [] out =  objToAngles.get( obj );
		if(out == null)
		{
			out = getCurrentEulerAngles(obj);
			setAngles(obj, out);
		}
		
		return out;
	}

	public double[] getQuaternion( final Clippable3D obj )
	{
		double [] out =  objToQuaternion.get( obj );
		if(out == null)
		{
			out = getCurrentEulerAngles(obj);
			setAngles(obj, out);
		}
		out = objToQuaternion.get( obj );
		return out;
	}
	
	public void setAngles( final Clippable3D obj, final double[] eAngles)
	{
		objToAngles.put( obj, eAngles );
		//updates quaternion
		getCurrentEulerAngles(obj);
	}
	
	public void setQuaternion( final Clippable3D obj, final double[] quat)
	{
		objToQuaternion.put( obj, quat );
		objToAngles.put( obj, Misc.quaternionToEulerAngles(quat) );
	}
	
	
	public double [] getCurrentEulerAngles(final Clippable3D obj)
	{
		final AffineTransform3D clipTr = new AffineTransform3D();
		
		obj.getClipTransform(clipTr);
		
		final RealInterval interval = obj.getClipInterval(); 
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
		objToQuaternion.put( obj, qRotation );
		return Misc.quaternionToEulerAngles(qRotation);
		
	}
	
}
