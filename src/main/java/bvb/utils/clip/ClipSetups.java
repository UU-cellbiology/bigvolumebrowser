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
package bvb.utils.clip;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.viewer.ConverterSetups;
import bvb.core.BigVolumeBrowser;
import bvb.gui.SelectedObjects;
import bvb.shapes.BasicShape;
import bvb.utils.Misc;
import bvvpg.source.converters.GammaConverterSetup;

public class ClipSetups
{
	final public ClipRotationAngles clipRotationAngles = new ClipRotationAngles();
	
	final public ClipAxesBounds clipAxesBounds;
	
	final public ClipCenters clipCenters;
	
	final public ClipCenterBounds clipCenterBounds;
	
	public ConverterSetups converterSetups;
	
	public SelectedObjects selectedObjects;
	
	public final BigVolumeBrowser bvb;
	
	public ClipSetups (final BigVolumeBrowser bvb_)
	{
		bvb = bvb_;
		converterSetups = bvb.bvvViewer.getConverterSetups();
		selectedObjects = bvb.selectedObjects;
		clipAxesBounds = new ClipAxesBounds(converterSetups);
		clipCenters = new ClipCenters(converterSetups);
		clipCenterBounds = new ClipCenterBounds(converterSetups);
	}
	
	public synchronized void updateClipTransform( final GammaConverterSetup cs)
	{
		final double [] eAngles = clipRotationAngles.getAngles( cs );
		
		final AffineTransform3D clipRot = Misc.getRotationTransform( eAngles );
		
		AffineTransform3D clipTr = new AffineTransform3D();
		
		FinalRealInterval clipInt = cs.getClipInterval();
		
		if (clipInt == null)
		{
			clipInt = Misc.getSourceBoundingBoxAllTP(converterSetups.getSource( cs ).getSpimSource() );
		}

		final double [] center =  Misc.getIntervalCenterNegative( clipInt );
		
		final double [] centerNew = clipCenters.getCenters( cs );

		clipTr.translate( center );
		
		clipTr = clipTr.preConcatenate( clipRot );

		clipTr.translate( centerNew );	

		cs.setClipTransform(clipTr);
		
	}
	
	public synchronized void updateClipTransform( final BasicShape sh)
	{
		final double [] eAngles = clipRotationAngles.getAngles( sh );
		
		final AffineTransform3D clipRot = Misc.getRotationTransform( eAngles );
		
		AffineTransform3D clipTr = new AffineTransform3D();
		
		FinalRealInterval clipInt = sh.getClipInterval();
		if(clipInt == null)
		{
			clipInt = new FinalRealInterval (sh.boundingBox());
		}

		final double [] center =  Misc.getIntervalCenterNegative( clipInt );
		
		final double [] centerNew = clipCenters.getCenters( sh );

		clipTr.translate( center );
		
		clipTr = clipTr.preConcatenate( clipRot );

		clipTr.translate( centerNew );	

		sh.setClipTransform(clipTr);
		
	}
	

	
}
