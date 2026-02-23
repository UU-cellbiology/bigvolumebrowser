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
	
	public void setPointSize(final float fPointSize);
	
	public float getPointSize();
		
	public void setSurfaceRender(final int nSurfaceRenderType);

	public int getSurfaceRender();
	
	public void setSurfaceGrid(final int nSurfaceGridType);
	
	public int getSurfaceGrid();

	public void setWireLineWidth(final float fThickness);
	
	public float getWireLineWidth();
	
	public void setCartesianGrid(final float cartesianGridStep, final float cartesianFraction);
	
	public void setSilhouetteDecay(final float silhouetteDecay);
	
	public float getSilhouetteDecay();
	
}
