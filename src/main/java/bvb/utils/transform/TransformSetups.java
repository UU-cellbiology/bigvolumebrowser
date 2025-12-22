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
package bvb.utils.transform;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.viewer.ConverterSetups;
import bdv.viewer.Source;
import bvb.core.BigVolumeBrowser;
import bvb.gui.SelectedObjects;
import bvb.shapes.BasicShape;
import bvb.utils.Bounds3D;
import bvb.utils.Misc;
import bvvpg.source.converters.Clippable3D;
import ij.Prefs;

public class TransformSetups
{
	final public BigVolumeBrowser bvb;
	
	public ConverterSetups converterSetups;
	
	public SelectedObjects selectedObjects;
	
	final public TransformScale transformScale;
	
	final public TransformCenter transformCenters;
	
	final public TransformCenterBounds transformCenterBounds;
	
	final public TransformRotation transformRotation;
	
	final public TransformDeskew transformDeskew;

	public boolean bTransformClip = Prefs.get( "BVB.bTransformClip", true );
	
	public boolean bLocalCoordinates = Prefs.get( "BVB.bCenterPanel", true );
	
	public TransformSetups (final BigVolumeBrowser bvb_)
	{
		this.bvb = bvb_;
		
		converterSetups = bvb.bvvViewer.getConverterSetups();
		
		selectedObjects = bvb.selectedObjects;
		
		transformScale = new TransformScale(converterSetups);
		transformCenters = new TransformCenter(converterSetups);
		transformCenterBounds = new TransformCenterBounds(converterSetups);
		transformRotation = new TransformRotation(converterSetups);
		transformDeskew = new TransformDeskew();
		
	}
	
	void getRotation(final AffineTransform3D trRot, final Object obj, final double [] previousAngles)
	{
		final double [] qCurr = transformRotation.getQuaternion( obj );
		final double [] eAngles = transformRotation.getAngles( obj );
		
		//reset rotation
		if(LinAlgHelpers.length( eAngles )<0.0001)
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

	public void updateTransform(final Object obj, final double [] previousAngles)
	{
		
		//rotation
		final AffineTransform3D trRot = new AffineTransform3D();		
		getRotation( trRot, obj, previousAngles );
	
		final AffineTransform3D newTransform = new AffineTransform3D();
		
		RealInterval interval = null;
		
		final AffineTransform3D oldTransform = new AffineTransform3D();
		if(obj instanceof ConverterSetup)
		{		
			final Source< ? > src = converterSetups.getSource((ConverterSetup)obj ).getSpimSource();
			(( TransformedSource< ? > )src).getFixedTransform( oldTransform );
			// reset both transforms just in case
			(( TransformedSource< ? > )src).setFixedTransform( newTransform );
			(( TransformedSource< ? > )src).setIncrementalTransform( newTransform );
			
			interval = Misc.getSourceBoundingBoxAllTP(src);
		}
		if(obj instanceof BasicShape)
		{
			((BasicShape)obj).getTransform( oldTransform );
			//reset transform just in case
			((BasicShape)obj).setTransform( newTransform );
			interval = ((BasicShape)obj).boundingBox();
		}
		final double [] center =  Misc.getIntervalCenterNegative( interval );
		final double [] dCurrScale = transformScale.getScale(obj );		

		//move to the origin
		newTransform.translate( center );

		//scale
		final AffineTransform3D scaleTr = new AffineTransform3D();
		scaleTr.scale( dCurrScale[0], dCurrScale [1], dCurrScale[2] );
		newTransform.preConcatenate( scaleTr );

		//deskew rotate
		final double angleDeskew = transformDeskew.getAngle( obj );
		final  AffineTransform3D deskewRot = new AffineTransform3D();
		deskewRot.rotate( 0, 0.5 * Math.PI -  angleDeskew);
		newTransform.preConcatenate( deskewRot );
		
		//rotate
		newTransform.preConcatenate( trRot );
		
		//move things to the current volume's center
		final double [] tr = transformCenters.getCenters( obj );	
		final AffineTransform3D translTr = new AffineTransform3D();
		translTr.translate( tr );
		newTransform.preConcatenate( translTr );
			
		//deskew shear/scale/translate
		final AffineTransform3D deskewTr = makeDeskewTransform(angleDeskew, tr) ;
		newTransform.concatenate( deskewTr );
		
		if(obj instanceof ConverterSetup)
		{
			final Source< ? > src = converterSetups.getSource((ConverterSetup)obj ).getSpimSource();
			(( TransformedSource< ? > )src).setFixedTransform( newTransform );
		}
		if(obj instanceof BasicShape)
		{
			((BasicShape)obj).setTransform( newTransform );
		}

		/////   update clipping, if needed
		if(bTransformClip)
		{
			
			final Clippable3D objCl = (Clippable3D)obj;
			if(objCl.getClipState() != 0)
			{				
				// get change in the transform
				final AffineTransform3D clipUpdate = new AffineTransform3D ();
				
				//go to absolute coordinates
				clipUpdate.set( oldTransform.inverse() );
				
				//account for the new transform				
				clipUpdate.preConcatenate( newTransform );				
		
				//update centers
				double [] clipCentOld = bvb.bvbCards.clipPanel.clipSetups.clipCenters.getCenters( objCl );
				
				double [] clipCent = new double[3];
				clipUpdate.apply( clipCentOld, clipCent );
				bvb.bvbCards.clipPanel.clipSetups.clipCenters.setCenters( objCl, clipCent );		
						
				//update clipping interval
				final double [] shift = new double [3];
				final RealInterval clipInt = objCl.getClipInterval();
				double [] min = clipInt.minAsDoubleArray();
				double [] max = clipInt.maxAsDoubleArray();
				for(int d = 0; d < 3; d++)
				{
					shift[d] = clipCent[d] - clipCentOld[d];
					min[d] += shift[d];
					max[d] += shift[d];
				}
				objCl.setClipInterval( FinalRealInterval.wrap( min, max));
				final Bounds3D bounds = bvb.bvbCards.clipPanel.clipSetups.clipAxesBounds.getBounds( objCl );
				
				bounds.translate( shift );
				
				//update rotation				
				double [] dAnglesOld = bvb.bvbCards.clipPanel.clipSetups.clipRotation.getAngles( objCl );
								
				final double[] prevClipRotAngles = bvb.bvbCards.clipPanel.clipSetups.clipRotation.getAngles( objCl );		
				if(previousAngles != null)
				{
					final double [] eAngles = transformRotation.getAngles( obj );
					double [] dAngUpdated  = new double [3]; 
					for(int d = 0; d < 3; d++)
					{
						dAngUpdated [d] = dAnglesOld[d] - previousAngles[d] + eAngles[d]; 
					}
					bvb.bvbCards.clipPanel.clipSetups.clipRotation.setAngles( objCl, dAngUpdated );
				}					
				bvb.bvbCards.clipPanel.clipSetups.updateClipTransform( objCl, prevClipRotAngles);
			}
			bvb.bvbCards.clipPanel.updateGUI();
		}
		bvb.updateSceneRender();		
				
	}	

	public void updateBVV()
	{
		bvb.updateSceneRender();
		
	}
	
	/** function generates new deskew transform (shear/z-scaling + translation) 
	 * in the "common" configuration, i.e. XY plane tilted with respect to Z.
	 * It adds translation, so the provided centers remain the same.
	 * Does not perform rotation.
	 * @param angle deskew angle in radians
	 * @param centers center coordinates of deskewed volume 
	 * @return deskew transform **/
	public static AffineTransform3D makeDeskewTransform(final double angle, final double [] centers)
	{
		AffineTransform3D afDataTransform = new AffineTransform3D();
		AffineTransform3D tShear = new AffineTransform3D();

		//shearing transform
		tShear.set(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0/Math.tan( angle ), 0.0, 0.0, 0.0, 1.0, 0.0);
		//Z-step adjustment transform
		afDataTransform.set(1.0, 0.0, 0.0, 0.0, 
								0.0, 1.0, 0.0, 0.0, 
								0.0, 0.0, Math.sin( angle  ), 0.0);
		
		afDataTransform = tShear.concatenate( afDataTransform );
		
		//check how the centers will change
		final double [] centerDeskewNew = new double[3];
		
		afDataTransform.apply( centers, centerDeskewNew );
		
		for (int d = 0; d < 3; d++)
		{
			centerDeskewNew[d] = centers[d] - centerDeskewNew[d];
		}
		
		afDataTransform.translate( centerDeskewNew );
		
		return afDataTransform;
	}
}
