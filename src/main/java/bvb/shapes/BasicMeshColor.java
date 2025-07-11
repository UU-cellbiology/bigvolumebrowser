package bvb.shapes;

import java.awt.Color;

public interface BasicMeshColor
{
	
	public void setColor(final Color colorin);
	
	public Color getColor();

	public void setRenderType(final int nRenderType_);
	
	public int getRenderType();
	
	public void setPointSize (final float fPointSize);
	
	public float getPointSize ();
	
	public int getSurfaceRender();
	
	public void setSurfaceRender(final int nSurfaceRenderType);
	
	public void setSurfaceGrid(final int nSurfaceGridType);

	public void setCartesianGrid(final float cartesianGridStep, final float cartesianFraction);
	
	public void setSilhouetteDecay(final float silhouetteDecay);
	
}
