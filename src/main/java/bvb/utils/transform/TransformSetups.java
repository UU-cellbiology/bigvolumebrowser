package bvb.utils.transform;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.viewer.ConverterSetups;
import bdv.viewer.Source;
import bvb.core.BigVolumeBrowser;
import bvb.gui.SelectedSources;
import bvb.utils.Misc;

public class TransformSetups
{
	final public BigVolumeBrowser bvb;
	
	public ConverterSetups converterSetups;
	
	public SelectedSources selectedSources;
	
	final public TransformScale transformScale;
	
	final public TransformCenter transformCenters;
	
	final public TransformCenterBounds transformTranslationBounds;
	
	public TransformSetups (final BigVolumeBrowser bvb_)
	{
		this.bvb = bvb_;
		
		converterSetups = bvb.bvvViewer.getConverterSetups();
		
		selectedSources = bvb.selectedSources;
		
		transformScale = new TransformScale(converterSetups);
		transformCenters = new TransformCenter(converterSetups);
		transformTranslationBounds = new TransformCenterBounds(converterSetups);
		
	}
	
	public void updateTransform(final ConverterSetup cs)
	{
		Source< ? > src = converterSetups.getSource( cs ).getSpimSource();

		AffineTransform3D srcTrFixed = new AffineTransform3D();
		
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
		final double [] tr = transformCenters.getCenters( cs );

		//move things to the center
		final AffineTransform3D translTr = new AffineTransform3D();

		translTr.translate( tr );
		srcTrFixed = srcTrFixed.preConcatenate( translTr );
		
		
		
		(( TransformedSource< ? > )src).setFixedTransform( srcTrFixed );
		bvb.updateSceneRender();		
		
	}
	

	public void updateBVV()
	{
		bvb.updateSceneRender();
		
	}
}
