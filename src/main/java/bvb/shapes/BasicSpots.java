package bvb.shapes;

import java.awt.Color;
import java.awt.image.IndexColorModel;

public interface BasicSpots
{
	public void setPointSize (final float pointSize_);
	public float getPointSize();
	
	public void setColor(Color pointColor_);
	public Color getColor();
	
	public void setLUT(String sLUTName);
	public void setLUT(final IndexColorModel icm_, String sLUTName); 
	public String getLUTName();
	public void setMapLUTMode(int nMapLUTMode);
	public int getMapLUTMode();
	
	public void setRenderType(int nRenderType);
	public int getRenderType();
	
	public void setPointShape(int nShape);
	public int getPointShape();
	
	
}
