package bvb.shapes;

import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.util.ArrayList;

import net.imglib2.RealPoint;

import org.joml.Matrix4fc;

import bvb.scene.VisPointsScaled;


public class Points implements Shape
{
	
	public static final int RENDER_FILLED = 0, RENDER_OUTLINE = 1; 

	public static final int SHAPE_ROUND = 0, SHAPE_SQUARE = 1; 

	public VisPointsScaled vertexVis = null;
	
	float pointSize;
	Color pointColor;
	int renderType;
	int pointShape;
	
	public Points(final float pointSize_, final Color pointColor_, final int nRenderType_, final int nShape_)
	{
		pointSize = pointSize_;		
		renderType = nRenderType_;
		pointColor = new Color(pointColor_.getRed(),pointColor_.getGreen(),pointColor_.getBlue(),pointColor_.getAlpha());
		pointShape = nShape_;
	}
	@Override
	public void draw( GL3 gl, Matrix4fc pvm, Matrix4fc vm, int[] screen_size )
	{
		if(!(vertexVis == null))
		{
			vertexVis.draw( gl, pvm, screen_size);
		}
	}
	
	public void setPoints(final ArrayList<RealPoint> vertices)
	{
		if(vertexVis == null)
		{
			vertexVis = new VisPointsScaled(vertices, pointSize, pointColor,renderType, pointShape);
		}
		else
		{
			vertexVis.setVertices(vertices);
		}
	}
	
	
	public void setPointsColor(Color pointColor_) 
	{

		pointColor = new Color(pointColor_.getRed(),pointColor_.getGreen(),pointColor_.getBlue(),pointColor_.getAlpha());
		
		if(!(vertexVis == null))
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

}
