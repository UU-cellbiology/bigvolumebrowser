package bvb.shapes;

import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.util.ArrayList;

import net.imglib2.RealPoint;

import org.joml.Matrix4fc;

import bvb.scene.VisPointsScaled;

/** Example class that uses points shader **/

public class PointsSame implements Shape
{
	

	public VisPointsScaled vertexVis = null;
	
	float pointSize;
	Color pointColor;
	int renderType;
	int pointShape;
	
	public PointsSame(final float pointSize_, final Color pointColor_, final int nShape_, final int nRenderType_)
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
			vertexVis = new VisPointsScaled(vertices, pointSize, pointColor, pointShape, renderType);
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
