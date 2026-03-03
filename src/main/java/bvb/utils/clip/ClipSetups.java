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

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.ConverterSetups;
import bdv.viewer.SourceAndConverter;
import bvb.core.BigVolumeBrowser;
import bvb.gui.SelectedObjects;
import bvb.shapes.BasicShape;
import bvb.utils.Misc;
import bvvpg.source.converters.Clippable3D;
import ij.Prefs;

public class ClipSetups
{
	final public ClipRotation clipRotation = new ClipRotation();
	
	final public ClipRangeBounds clipRangeBounds;
	
	final public ClipCenters clipCenters;
	
	final public ClipCenterBounds clipCenterBounds;
	
	public ConverterSetups converterSetups;
	
	public SelectedObjects selectedObjects;
	
	public final BigVolumeBrowser bvb;
	
	public boolean bLocalCoordinates = Prefs.get( "BVB.bClipLocalCoordinates", true );
	
	public ClipSetups (final BigVolumeBrowser bvb_)
	{
		bvb = bvb_;
		converterSetups = bvb.bvvViewer.getConverterSetups();
		selectedObjects = bvb.selectedObjects;
		clipRangeBounds = new ClipRangeBounds(converterSetups);
		clipCenters = new ClipCenters(converterSetups);
		clipCenterBounds = new ClipCenterBounds(converterSetups);
	}
	
	
	public synchronized void updateClipTransform( final Clippable3D obj, final double [] previousAngles)
	{				
		//rotation
		final AffineTransform3D trRot = new AffineTransform3D();
		
		getRotation( trRot, obj, previousAngles );				
		
		// find current center
		RealInterval clipInt = obj.getClipInterval();
		
		if (clipInt == null)
		{
			if(obj instanceof ConverterSetup)
			{
				clipInt = Misc.getSourceBoundingBoxAllTP(converterSetups.getSource( (ConverterSetup)obj ).getSpimSource() );
			}
			if(obj instanceof BasicShape)
			{
				clipInt = new FinalRealInterval (((BasicShape)obj).boundingBox());
			}

		}

		final double [] center =  Misc.getIntervalCenterNegative( clipInt );
		
		final double [] centerNew = clipCenters.getCenters( obj );

		final AffineTransform3D clipTr = new AffineTransform3D();
		
		clipTr.translate( center );
		
		clipTr.preConcatenate( trRot );

		clipTr.translate( centerNew );	

		obj.setClipTransform(clipTr);
		
	}
	
	void getRotation(final AffineTransform3D trRot, final Clippable3D obj, final double [] previousAngles)
	{		
		final double [] qCurr = clipRotation.getQuaternion( obj );
		final double [] eAngles = clipRotation.getAngles( obj );
		
		//reset rotation
		if(LinAlgHelpers.length( eAngles ) < 0.0001)
		{
			qCurr[0] = 1.0;
			for(int d = 1; d < 4; d++)
				qCurr[d] = 0.0;
		}
		else
		{
			if(previousAngles != null )
			{
				//add quaternion rotation
				//calculate changes in angles
				final double [] dChangeAngle = new double [3];
				for (int d = 0; d < 3; d++)
				{
					dChangeAngle[d] = eAngles[d] - previousAngles[d];
				}
				//construct quaternion
				final double [] qAdd = Misc.getRotationQuaternion( dChangeAngle );
				LinAlgHelpers.quaternionMultiply(qAdd,qCurr,qCurr);
				LinAlgHelpers.normalize( qCurr );

			}
		}
		
		final double [][] rotMatrix = new double [3][4];  
		LinAlgHelpers.quaternionToR( qCurr, rotMatrix );		
		trRot.set( rotMatrix );	
	}
	
	public double [] getCurrentObjectCenter(final Object obj)
	{
		RealInterval interval = null;
		if(obj instanceof ConverterSetup)
		{
			final SourceAndConverter< ? > source = converterSetups.getSource( (ConverterSetup)obj );
			interval = Misc.getSourceBoundingBoxAllTP(source.getSpimSource());
		}
		if(obj instanceof BasicShape)
		{
			interval = ((BasicShape)obj).boundingBox();
		}	
		if(interval!= null)
		{
			return Misc.getIntervalCenter( interval );
		}
		return null;
	}
	
	
}
