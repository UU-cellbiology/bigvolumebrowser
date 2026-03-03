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

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.SourceToConverterSetupBimap;
import bvb.shapes.BasicShape;
import bvb.utils.Bounds3D;
import bvb.utils.Misc;
import bvvpg.source.converters.Clippable3D;

public class ClipCenterBounds
{
	private final SourceToConverterSetupBimap bimap;

	private final Map< Clippable3D, Bounds3D > objToBounds = new HashMap<>();
	
	public ClipCenterBounds( final SourceToConverterSetupBimap bimap )
	{
		this.bimap = bimap;
	}
	
	public Bounds3D getBounds( final Clippable3D obj )
	{
		return objToBounds.compute( obj, this::getExtendedBounds );
	}
	
	public void setBounds( final Clippable3D obj, final Bounds3D bounds )
	{
		objToBounds.put( obj, bounds );
	}
	
	public Bounds3D getDefaultBounds( final Clippable3D obj )
	{
		Bounds3D bounds = null;

		if(obj instanceof ConverterSetup)
		{
			final SourceAndConverter< ? > sac = bimap.getSource( (ConverterSetup)obj );
			if ( sac != null )
			{
				//get the range over all timepoints
				bounds = new Bounds3D(Misc.getSourceBoundingBoxAllTP(sac.getSpimSource()));
			}
			else
			{
				System.out.println("error in estimation of center bounds, no source found");
			}
		}
		if(obj instanceof BasicShape)
		{
			bounds = new Bounds3D(((BasicShape)obj).boundingBox());
		}
		return bounds;
	}

	private Bounds3D getExtendedBounds( final Clippable3D obj, Bounds3D bounds )
	{
		if ( bounds == null )
			bounds = getDefaultBounds( obj );
		final RealInterval clipInterval = obj.getClipInterval();
		if(clipInterval == null)
			return bounds;

		return bounds.join( new Bounds3D( clipInterval) );
	}
		
}
