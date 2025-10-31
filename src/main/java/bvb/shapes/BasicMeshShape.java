package bvb.shapes;

import java.awt.Color;

public interface BasicMeshShape
{
	
	public void setColor(final Color colorin);
	
	public Color getColor();
	
	public boolean hasTexture();
	
	public void useTexture(boolean bUseTexture);
	
	public boolean isTextureUsed();

	public void setRenderType(final int nRenderType_);
	
	public int getRenderType();
	
	public void setPointSize (final float fPointSize);
	
	public float getPointSize();
		
	public void setSurfaceRender(final int nSurfaceRenderType);

	public int getSurfaceRender();
	
	public void setSurfaceGrid(final int nSurfaceGridType);
	
	public int getSurfaceGrid();

	public void setWireLineWidth(final float fThickness);
	
	public void setCartesianGrid(final float cartesianGridStep, final float cartesianFraction);
	
	public void setSilhouetteDecay(final float silhouetteDecay);
	
	
	
}
