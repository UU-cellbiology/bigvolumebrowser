package bvb.shapes;

import java.awt.Color;

public interface BasicSpots
{
	public void setPointSize (final float pointSize_);
	public float getPointSize();
	
	public void setColor(Color pointColor_);
	public Color getColor();
	
	public void setRenderType(int nRenderType);
	public int getRenderType();
	
	public void setPointShape(int nShape);
	public int getPointShape();
	
	
}
