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
package bvb.shapes;

import java.awt.Color;
import java.awt.image.IndexColorModel;

public interface BasicSpots
{
	public void setPointSize (final float pointSize_);	
	/** if points have provided size, returns a negative number **/
	public float getPointSize();
	
	/** sets extra scale factor for various size spots **/
	public void setSizeScale(final float fSizeScale);
	/** returns scale factor for various size spots **/
	public float getSizeScale();
	
	public boolean isMultiColor();
	public void setColor(Color pointColor_);	
	public Color getColor();		
	
	public void setExtraAlphaCoefficient(float dCoeff);
	public float getExtraAlphaCoefficient();
	
	/** 0 - filled, 1 - outline, 2 - gaussian **/
	public void setRenderType(int nRenderType);
	/** 0 - filled, 1 - outline, 2 - gaussian **/
	public int getRenderType();
	
	/** 0 - round, 1 - square **/
	public void setPointShape(int nShape);
	/** 0 - round, 1 - square **/
	public int getPointShape();
	
	/** only for round filled spots 
	 * 0 - plain, 1 - shaded, 2 - Eye Dome Lighting (EDL)**/
	public void setPointShade(int nShade);
	/** only for round filled spots 
	 * 0 - plain, 1 - shaded, 2 - Eye Dome Lighting (EDL) **/
	public int getPointShade();
	
	/** LUT Mapping: 0 - no mapping, use color,
	 * 1 - map LUT to X coord
	 * 2 - map LUT to Y coord
	 * 3 - map LUT to Z coord
	 * 4 - map LUT to spot size
	 * 5 - map LUT to parameter **/
	public void setMapLUTMode(final int nMapLUTMode);
	
	/** LUT Mapping: 0 - no mapping, use color,
	 * 1 - map LUT to X coord
	 * 2 - map LUT to Y coord
	 * 3 - map LUT to Z coord
	 * 4 - map LUT to spot size
	 * 5 - map LUT to parameter **/	
	public int getMapLUTMode();
	
	public void setLUT(String sLUTName);
	public void setLUT(final IndexColorModel icm_, String sLUTName); 
	public String getLUTName();
	
	public void setInvertedLUT(boolean bInv);
	public boolean isInvertedLUT();
	
	public void setMapLUTRange(final float fMin, final float fMax);
	public void setMapLUTGamma(final float fGamma);
	
	public void setMapAlphaMode(final int nMapLUTMode);
	public int getMapAlphaMode();
	public void setMapAlphaRange(final float fMin, final float fMax);
	public void setMapAlphaGamma(final float fGamma);
	
	public void setInvertedAlpha(boolean bInv);
	public boolean isInvertedAlpha();
	
	
	/** returns null if spots have the same size**/
	public float [] getSizeRange();

	public boolean hasProperty();
	
	/** returns null if spots do not have set property**/	
	public float [] getPropertyRange();
	
}
