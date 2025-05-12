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

import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.util.ArrayList;

import net.imglib2.RealPoint;

import org.joml.Matrix4fc;

import bvb.scene.VisSpotsSame;

/** Example class that uses points shader **/

public class SpotsSame implements BasicShape
{
	

	public VisSpotsSame vertexVis = null;
	
	float pointSize;
	Color pointColor;
	int renderType;
	int pointShape;
	
	public SpotsSame(final float pointSize_, final Color pointColor_, final int nShape_, final int nRenderType_)
	{
		pointSize = pointSize_;		
		renderType = nRenderType_;
		pointColor = new Color(pointColor_.getRed(),pointColor_.getGreen(),pointColor_.getBlue(),pointColor_.getAlpha());
		pointShape = nShape_;
	}
	
	@Override
	public void draw( GL3 gl, Matrix4fc pvm, Matrix4fc vm, int[] screen_size )
	{
		if(vertexVis != null)
		{
			vertexVis.draw( gl, pvm, screen_size);
		}
	}
	
	public void setPoints(final ArrayList<RealPoint> vertices)
	{
		if(vertexVis == null)
		{
			vertexVis = new VisSpotsSame(vertices, pointSize, pointColor, pointShape, renderType);
		}
		else
		{
			vertexVis.setVertices(vertices);
		}
	}
	
	
	public void setPointsColor(Color pointColor_) 
	{

		pointColor = new Color(pointColor_.getRed(),pointColor_.getGreen(),pointColor_.getBlue(),pointColor_.getAlpha());
		
		if(vertexVis != null)
		{
			vertexVis.setColor(pointColor);			
		}
	}
	
	public void setRenderType(int nRenderType)
	{
		pointShape = nRenderType;
		vertexVis.setShape( pointShape );
		
		return;
	}	
	
	public void setPointShape(int nShape)
	{
		renderType = nShape;
		vertexVis.setRenderType(renderType);
		
		return;
	}	
	
	@Override
	public String toString()
	{
		return "point"+Integer.toString(this.hashCode());

	}

	@Override
	public void reload()
	{
		if(vertexVis != null)
			vertexVis.reload();
		
	}

}
