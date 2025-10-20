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
		
	}
	
	void getRotation(final AffineTransform3D trRot, final Object obj, final double [] previousAngles)
	{
		final double [] qCurr = transformRotation.getQuaternion( obj );
		final double [] eAngles = transformRotation.getAngles( obj );
		
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
	}

	public void updateTransform(final Object obj, final double [] previousAngles)
	{
		
		//rotation
		final AffineTransform3D trRot = new AffineTransform3D();		
		getRotation( trRot, obj, previousAngles );
	
		final AffineTransform3D newTransform = new AffineTransform3D();
		
		RealInterval interval = null;
		
		final AffineTransform3D oldTr = new AffineTransform3D();
		if(obj instanceof ConverterSetup)
		{		
			final Source< ? > src = converterSetups.getSource((ConverterSetup)obj ).getSpimSource();
			(( TransformedSource< ? > )src).getFixedTransform( oldTr );
			//reset both transforms just in case
			(( TransformedSource< ? > )src).setFixedTransform( newTransform );
			(( TransformedSource< ? > )src).setIncrementalTransform( newTransform );
			
			interval = Misc.getSourceBoundingBoxAllTP(src);
		}
		if(obj instanceof BasicShape)
		{
			((BasicShape)obj).getTransform( oldTr );
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
		scaleTr.scale( dCurrScale[0],dCurrScale [1],dCurrScale[2] );
		newTransform.preConcatenate( scaleTr );
		//rotate
		newTransform.preConcatenate( trRot );
		
		
		//move things to the current volume's center
		final double [] tr = transformCenters.getCenters( obj );		
		final AffineTransform3D translTr = new AffineTransform3D();
		translTr.translate( tr );
		newTransform.preConcatenate( translTr );
		
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
				AffineTransform3D clipBake = new AffineTransform3D ();
				
				//go to absolute coordinates
				clipBake.set( oldTr.inverse() );
				//account for the new transform
				clipBake.preConcatenate( newTransform );
				AffineTransform3D clipUpdate = new AffineTransform3D ();
				clipUpdate.set( clipBake );
		
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
				for(int d=0;d<3;d++)
				{
					shift[d] = clipCent[d]-clipCentOld[d];
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
					for(int d=0;d<3;d++)
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
}
