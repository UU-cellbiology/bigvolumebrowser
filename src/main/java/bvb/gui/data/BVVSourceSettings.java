package bvb.gui.data;

import bvvpg.vistools.BvvStackSource;

import java.awt.image.IndexColorModel;

import net.imglib2.type.numeric.ARGBType;

import bvvpg.source.converters.GammaConverterSetup;

public class BVVSourceSettings
{
	
	boolean bLUT = false;
	
	ARGBType color = null;
	
	IndexColorModel lutICM = null;
	
	String sLUTName = "";
	
	double dDispMin = 0;
	
	double dDispMax = 65535;
	
	int nRenderType = 0;
	
	int nInterpType = 0;
	
	boolean bVisible = true;
	

	public BVVSourceSettings(BvvStackSource<?> bvvS)
	{
		GammaConverterSetup convS = ( GammaConverterSetup ) bvvS.getConverterSetups().get( 0 );
		if(convS.getLUTSize()>0)
		{
			bLUT = true;
			lutICM = convS.getLutICM();
			sLUTName = convS.getLUTName();
		}
		else
		{
			bLUT = false;
			color = convS.getColor();
		}
		//bVisible = bvvS..getSources().
		dDispMin =  convS.getDisplayRangeMin();
		dDispMax = convS.getDisplayRangeMax();
		nRenderType = convS.getRenderType();
		nInterpType = convS.getVoxelRenderInterpolation();
		
		bVisible = bvvS.getBvvHandle().getViewerPanel().state().isSourceVisible( 
				bvvS.getSources().get( 0 ) );
	}
	
	public void applyStoredSettings(BvvStackSource<?> bvvS)
	{
		if(color == null && lutICM == null)
			return;
		
		if(!bLUT)
		{
			bvvS.setColor( color );
		}
		else
		{
			bvvS.setLUT( lutICM, sLUTName );
		}
		bvvS.setDisplayRange( dDispMin, dDispMax );
		bvvS.setRenderType( nRenderType );
		bvvS.setVoxelRenderInterpolation( nInterpType );
		bvvS.setActive( bVisible );
	}
	
}
