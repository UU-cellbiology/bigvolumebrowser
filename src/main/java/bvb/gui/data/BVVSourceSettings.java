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
