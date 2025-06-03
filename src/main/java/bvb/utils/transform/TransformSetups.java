package bvb.utils.transform;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.viewer.ConverterSetups;
import bdv.viewer.Source;
import bvb.core.BigVolumeBrowser;
import bvb.gui.SelectedObjects;
import bvb.shapes.BasicShape;
import bvb.utils.Bounds3D;
import bvb.utils.Misc;
import bvvpg.source.converters.GammaConverterSetup;

public class TransformSetups
{
	final public BigVolumeBrowser bvb;
	
	public ConverterSetups converterSetups;
	
	public SelectedObjects selectedObjects;
	
	final public TransformScale transformScale;
	
	final public TransformCenter transformCenters;
	
	final public TransformCenterBounds transformTranslationBounds;
	
	final public TransformRotation transformRotation;
	
	public TransformSetups (final BigVolumeBrowser bvb_)
	{
		this.bvb = bvb_;
		
		converterSetups = bvb.bvvViewer.getConverterSetups();
		
		selectedObjects = bvb.selectedObjects;
		
		transformScale = new TransformScale(converterSetups);
		transformCenters = new TransformCenter(converterSetups);
		transformTranslationBounds = new TransformCenterBounds(converterSetups);
		transformRotation = new TransformRotation(converterSetups);
		
	}

	public void updateTransform(final BasicShape sh)
	{
		final double [] eAngles = transformRotation.getAngles( sh );
		
		final AffineTransform3D trRot = Misc.getRotationTransform( eAngles );		
		
		AffineTransform3D srcTrFixed = new AffineTransform3D();
		
		//reset both transforms just in case
		sh.setTransform( srcTrFixed );

		final double [] center =  Misc.getIntervalCenterNegative( sh.boundingBox() );
		final double [] dCurrScale = transformScale.getScale( sh );		

		//move to the origin
		srcTrFixed.translate( center );

		//scale
		final AffineTransform3D scaleTr = new AffineTransform3D();
		scaleTr.scale( dCurrScale[0],dCurrScale [1],dCurrScale[2] );
		srcTrFixed = srcTrFixed.preConcatenate( scaleTr );
		
		//rotate
		srcTrFixed = srcTrFixed.preConcatenate( trRot );		
		
		//move things to the current volume's center
		final double [] tr = transformCenters.getCenters( sh );		
		final AffineTransform3D translTr = new AffineTransform3D();
		translTr.translate( tr );
		srcTrFixed = srcTrFixed.preConcatenate( translTr );		
		
		sh.setTransform( srcTrFixed );

		bvb.updateSceneRender();	
	}
	
	public void updateTransform(final ConverterSetup cs)
	{
		Source< ? > src = converterSetups.getSource( cs ).getSpimSource();

		final double [] eAngles = transformRotation.getAngles( cs );
		
		final AffineTransform3D trRot = Misc.getRotationTransform( eAngles );
		
		AffineTransform3D srcTrFixed = new AffineTransform3D();
		
		
		AffineTransform3D oldTr = new AffineTransform3D();
		(( TransformedSource< ? > )src).getFixedTransform( oldTr );
		//reset both transforms just in case
		(( TransformedSource< ? > )src).setFixedTransform( srcTrFixed );
		(( TransformedSource< ? > )src).setIncrementalTransform( srcTrFixed );
		
		FinalRealInterval interval = Misc.getSourceBoundingBoxAllTP(src);
		final double [] center =  Misc.getIntervalCenterNegative( interval );
		final double [] dCurrScale = transformScale.getScale( cs );		

		//move to the origin
		srcTrFixed.translate( center );

		//scale
		final AffineTransform3D scaleTr = new AffineTransform3D();
		scaleTr.scale( dCurrScale[0],dCurrScale [1],dCurrScale[2] );
		srcTrFixed = srcTrFixed.preConcatenate( scaleTr );
		//rotate
		srcTrFixed = srcTrFixed.preConcatenate( trRot );
		
		
		//move things to the current volume's center
		final double [] tr = transformCenters.getCenters( cs );		
		final AffineTransform3D translTr = new AffineTransform3D();
		translTr.translate( tr );
		srcTrFixed = srcTrFixed.preConcatenate( translTr );
		(( TransformedSource< ? > )src).setFixedTransform( srcTrFixed );
		AffineTransform3D clipUpdate = new AffineTransform3D ();
		
		clipUpdate.set( oldTr.inverse() );
		clipUpdate.preConcatenate( srcTrFixed );
		
		
		//update centers
		double [] clipCent = bvb.controlPanel.tabPanelView.clipPanel.clipSetups.clipCenters.getCenters( cs );
		
		clipUpdate.apply( clipCent, clipCent );
		//go to absolute coordinates
		//oldTr.applyInverse( clipCent, clipCent );
		//account for the new transform
		//srcTrFixed.apply( clipCent, clipCent );
		bvb.controlPanel.tabPanelView.clipPanel.clipSetups.clipCenters.setCenters( cs, clipCent );
//		double [] dAnglesOld = bvb.controlPanel.tabPanelView.clipPanel.clipSetups.clipRotationAngles.getAngles( cs );
//		
//		double [] dAngUpdated = new double[3];
//		for(int d=0;d<3;d++)
//		{
//			dAngUpdated [d] = dAnglesOld[d] + eAngles[d]; 
//		}
//		 bvb.controlPanel.tabPanelView.clipPanel.clipSetups.clipRotationAngles.setAngles( cs, dAngUpdated );

		//update rangebounds
		Bounds3D bounds = bvb.controlPanel.tabPanelView.clipPanel.clipSetups.clipAxesBounds.getBounds( cs );
		bounds.applyTransform( clipUpdate );

		//update clip interval
		
		FinalRealInterval intOld = ((GammaConverterSetup)cs).getClipInterval();
		if(intOld != null)
		{
			final double [][] minmax = new double [2][3];
			minmax[0] = intOld.minAsDoubleArray();
			minmax[1] = intOld.maxAsDoubleArray();
			
			for(int i=0;i<2;i++)
			{
				clipUpdate.apply( minmax[i],minmax[i] );
				//oldTr.applyInverse(minmax[i],minmax[i]);
				//srcTrFixed.apply(minmax[i],minmax[i]);
			}
			((GammaConverterSetup)cs).setClipInterval( new FinalRealInterval(minmax[0],minmax[1]) );

		}
		bvb.controlPanel.tabPanelView.clipPanel.clipSetups.updateClipTransform( (GammaConverterSetup) cs);
		
		bvb.updateSceneRender();		
		
	}
	

	public void updateBVV()
	{
		bvb.updateSceneRender();
		
	}
}
