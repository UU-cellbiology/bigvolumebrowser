package bvb.shapes;

import java.awt.Color;
import java.awt.image.IndexColorModel;

public interface BasicSpots
{
	public void setPointSize (final float pointSize_);
	/** if points have provided size, returns a negative number **/
	public float getPointSize();
	
	public void setColor(Color pointColor_);
	public Color getColor();
	
	
	public void setLUT(String sLUTName);
	public void setLUT(final IndexColorModel icm_, String sLUTName); 
	public String getLUTName();
	/** LUT Mapping: 0 - no mapping, use color,
	 * 1 - map LUT to X coord
	 * 2 - map LUT to Y coord
	 * 3 - map LUT to Z coord
	 * 4 - map LUT to spot size
	 * 5 - map LUT to parameter **/
	public void setMapLUTMode(final int nMapLUTMode);
	
	public int getMapLUTMode();
	
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
	
	public void setRenderType(int nRenderType);
	public int getRenderType();
	
	public void setPointShape(int nShape);
	public int getPointShape();
	
	/** sets extra scale factor for various size spots **/
	public void setSizeScale(final float fSizeScale);
	/** returns scale factor for various size spots **/
	public float getSizeScale();

	/** returns null if spots have the same size**/
	public float [] getSizeRange();

	public boolean hasProperty();
	
	/** returns null if spots do not have set property**/	
	public float [] getPropertyRange();
	
	public void setExtraAlphaCoefficient(float dCoeff);
	public float getExtraAlphaCoefficient();
	
}
