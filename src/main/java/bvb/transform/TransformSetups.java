package bvb.transform;

import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.viewer.ConverterSetups;
import bdv.viewer.Source;
import bvb.core.BigVolumeBrowser;
import bvb.gui.SelectedSources;

public class TransformSetups
{
	final BigVolumeBrowser bvb;
	
	public ConverterSetups converterSetups;
	
	public SelectedSources selectedSources;
	
	final public TransformScale transformScale;
	
	final public TransformTranslation transformTranslation;
	
	final public TransformTranslationBounds transformTranslationBounds;
	
	public TransformSetups (final BigVolumeBrowser bvb_)
	{
		this.bvb = bvb_;
		converterSetups = bvb.bvvViewer.getConverterSetups();
		selectedSources = bvb.selectedSources;
		
		transformScale = new TransformScale(converterSetups);
		transformTranslation = new TransformTranslation(converterSetups);
		transformTranslationBounds = new TransformTranslationBounds(converterSetups);
		
	}
	
	public void updateTranslation(final ConverterSetup cs)
	{
		Source< ? > src = converterSetups.getSource( cs ).getSpimSource();
		AffineTransform3D srcTrFixed = new AffineTransform3D();
		AffineTransform3D srcTrInc = new AffineTransform3D();

		(( TransformedSource< ? > )src).getFixedTransform( srcTrFixed );
		(( TransformedSource< ? > )src).getIncrementalTransform( srcTrInc );
		
		double [] tr = transformTranslation.getTranslation( cs );
		for (int d = 0; d < 3; d++ )
		{
			srcTrInc.set( tr[d]-srcTrFixed.get( d, 3 ), d, 3 );
		}
		(( TransformedSource< ? > )src).setIncrementalTransform( srcTrInc );
		bvb.updateSceneRender();
	}
	
	public void updateBVV()
	{
		bvb.updateSceneRender();
		
	}
}
