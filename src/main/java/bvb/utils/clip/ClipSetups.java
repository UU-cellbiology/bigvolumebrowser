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
import net.imglib2.util.LinAlgHelpers;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.viewer.ConverterSetups;
import bdv.viewer.Source;
import bvb.core.BigVolumeBrowser;
import bvb.gui.SelectedObjects;
import bvb.shapes.BasicShape;
import bvb.utils.Misc;
import bvvpg.source.converters.GammaConverterSetup;

public class ClipSetups
{
	final public ClipRotation clipRotation = new ClipRotation();
	
	final public ClipAxesBounds clipAxesBounds;
	
	final public ClipCenters clipCenters;
	
	final public ClipCenterBounds clipCenterBounds;
	
	public ConverterSetups converterSetups;
	
	public SelectedObjects selectedObjects;
	
	public final BigVolumeBrowser bvb;
	
	public boolean bLocalCoordinates = true;
	
	public ClipSetups (final BigVolumeBrowser bvb_)
	{
		bvb = bvb_;
		converterSetups = bvb.bvvViewer.getConverterSetups();
		selectedObjects = bvb.selectedObjects;
		clipAxesBounds = new ClipAxesBounds(converterSetups);
		clipCenters = new ClipCenters(converterSetups);
		clipCenterBounds = new ClipCenterBounds(converterSetups);
	}
	
	public synchronized void updateClipTransform( final GammaConverterSetup cs, final double [] previousAngles)
	{				
		//rotation
		final AffineTransform3D trRot = new AffineTransform3D();		
		final double [] qCurr = clipRotation.getQuaternion( cs );
		final double [] eAngles = clipRotation.getAngles( cs );
		//reset rotation
		if(LinAlgHelpers.length( eAngles )<0.0001)
		{
			qCurr[0] = 1.0;
			for(int d=1;d<4;d++)
				qCurr[d] = 0.0;
		}
		else
		{
			if(previousAngles != null )
			{

				//add quaternion rotation
				//calculate changes in angles
				final double [] dChangeAngle = new double [3];
				for (int d=0;d<3;d++)
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
		
		AffineTransform3D clipTr = new AffineTransform3D();
		
		FinalRealInterval clipInt = cs.getClipInterval();
		
		if (clipInt == null)
		{
			clipInt = Misc.getSourceBoundingBoxAllTP(converterSetups.getSource( cs ).getSpimSource() );
		}

		final double [] center =  Misc.getIntervalCenterNegative( clipInt );
		
		final double [] centerNew = clipCenters.getCenters( cs );

		clipTr.translate( center );
		
		clipTr = clipTr.preConcatenate( trRot );

		clipTr.translate( centerNew );	

		cs.setClipTransform(clipTr);
		
	}
	
	public synchronized void updateClipTransform( final BasicShape sh)
	{
		final double [] eAngles = clipRotation.getAngles( sh );
		
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
	
	public double [] getSourceMinWithScale(final ConverterSetup cs)
	{
		final double [] relShift = Misc.getSourceMinNoFixedTransformAllTP( converterSetups.getSource( cs ).getSpimSource() );
		final double [] scales = bvb.controlPanel.tabPanelView.transformPanel.transformSetups.transformScale.getScale( cs );
		for(int d=0; d<3; d++)
			relShift[d] *= scales[d];
		return relShift;
	}
	
	public double [] getSourceMinWithScaleTranslation(final ConverterSetup cs)
	{
		final Source< ? > src = converterSetups.getSource( cs ).getSpimSource();
		final double [] relShift = Misc.getSourceMinNoFixedTransformAllTP( src );
		final double [] scales = bvb.controlPanel.tabPanelView.transformPanel.transformSetups.transformScale.getScale( cs );
		
		//center in the absolute world
		final double [] centerAbs = Misc.getIntervalCenter( Misc.getSourceBoundingBoxAllTP( src ));
		AffineTransform3D fixedTr = new AffineTransform3D();
		(( TransformedSource< ? > ) src).getFixedTransform( fixedTr );
		final double [] centerRel = new double[3];

		fixedTr.inverse().apply( centerAbs, centerRel );
		for(int d=0; d<3; d++)
			relShift[d] = (scales[d]*relShift[d])-centerRel[d]+centerAbs[d];
		
		return relShift;
	}

	
}
