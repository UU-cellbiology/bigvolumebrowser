package bvb.shapes;

import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.util.ArrayList;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;

import org.joml.Matrix4fc;

import bvb.scene.VisPolyLineAA;
import bvb.utils.Misc;


public class VolumeBox implements Shape
{
	public ArrayList<RealPoint> vertices;
	public ArrayList<ArrayList<RealPoint>> edges;
	public ArrayList<VisPolyLineAA> edgesVis;
	public float lineThickness;
	public Color lineColor;
	public AffineTransform3D transform = null;
	
	public RealInterval interval;
	
	public VolumeBox(RealInterval nIntervalBox, AffineTransform3D transform_, final float lineThickness_, final Color lineColor_)
	{
		interval = nIntervalBox;

		lineThickness = lineThickness_;
		
		lineColor = new Color(lineColor_.getRed(),lineColor_.getGreen(),lineColor_.getBlue(),lineColor_.getAlpha());

		if(transform_ == null)
		{
			transform = null;
		}
		else
		{
			transform = new AffineTransform3D();
			transform.set( transform_ );
		}
		
		setInterval(nIntervalBox);

	}
	
	public void setTransform (final AffineTransform3D transformIn, boolean bUpdateInterval)
	{
		transform = new AffineTransform3D();
		transform.set( transformIn );
		if(bUpdateInterval)
		{
			setInterval(interval);
		}
	}
	
	public void setInterval(RealInterval nIntervalBox)
	{
		float [][] nDimBox = new float [2][3];
		
		final double [] minI = nIntervalBox.minAsDoubleArray();
		
		final double [] maxI = nIntervalBox.maxAsDoubleArray();
		
		interval = new FinalRealInterval(minI, maxI);
		
		for(int d=0;d<3;d++)
		{
			nDimBox[0][d] = (float)minI[d];
			nDimBox[1][d] = (float)maxI[d];

		}
		final ArrayList<ArrayList< RealPoint >> edgesPairPoints = getEdgesPairPoints(nDimBox);
		if(transform != null)
		{
			for(int i=0; i<edgesPairPoints.size(); i++)
			{
				for(RealPoint pt : edgesPairPoints.get( i ))
				{
					transform.apply( pt, pt);
				}
			}
		}
		edgesVis = new ArrayList<>();
		for(int i=0; i<edgesPairPoints.size(); i++)
		{
			edgesVis.add(new VisPolyLineAA(edgesPairPoints.get(i), lineThickness, lineColor));
		}
	}
	
	@Override
	public void draw(final GL3 gl, final Matrix4fc pvm, final Matrix4fc vm, final int[] screen_size) {
	
		if(edgesVis != null)
		{
			for (VisPolyLineAA edge : edgesVis)
			{
				edge.draw(gl, pvm);
			}
		}
	}
	
	public void setLineColor(Color lineColor_) 
	{
		
		lineColor = new Color(lineColor_.getRed(),lineColor_.getGreen(),lineColor_.getBlue(),lineColor_.getAlpha());
		for(int i =0; i<edgesVis.size();i++)
		{
			edgesVis.get(i).setColor(lineColor);
		}
	}
	
	public void setLineThickness(float line_thickness) 
	{

		lineThickness = line_thickness;
		for(int i =0; i<edgesVis.size();i++)
		{
			edgesVis.get(i).setThickness(lineThickness);
		}
	}

	
	/** returns array of paired coordinates for each edge of the box,
	 * specified by nDimBox[0] - one corner, nDimBox[1] - opposite corner.
	 * no checks on provided coordinates performed  **/
	public static ArrayList<ArrayList< RealPoint >> getEdgesPairPoints(final float [][] nDimBox)
	{
		int i,j,z;
		
		ArrayList<ArrayList< RealPoint >> out = new ArrayList<>();
		
		int [][] edgesxy = new int [5][2];
		
		edgesxy[0] = new int[]{0,0};
		edgesxy[1] = new int[]{1,0};
		edgesxy[2] = new int[]{1,1};
		edgesxy[3] = new int[]{0,1};
		edgesxy[4] = new int[]{0,0};
		
		//draw front and back
		RealPoint vertex1 = new RealPoint(0,0,0);
		RealPoint vertex2 = new RealPoint(0,0,0);
		
		for (z=0;z<2;z++)
		{
			for (i=0;i<4;i++)
			{
				for (j=0;j<2;j++)
				{
					vertex1.setPosition(nDimBox[edgesxy[i][j]][j], j);
					vertex2.setPosition(nDimBox[edgesxy[i+1][j]][j], j);
				}
				//z coord
				vertex1.setPosition(nDimBox[z][2], 2);
				vertex2.setPosition(nDimBox[z][2], 2);
				
				ArrayList< RealPoint > point_coords = new ArrayList<  >();
				point_coords.add(new RealPoint(vertex1));
				point_coords.add(new RealPoint(vertex2));

				out.add(point_coords);

			}
		}
		
		//draw the rest 4 edges
		for (i=0;i<4;i++)
		{
			for (j=0;j<2;j++)
			{
				vertex1.setPosition(nDimBox[edgesxy[i][j]][j], j);
				vertex2.setPosition(nDimBox[edgesxy[i][j]][j], j);
			}
			//z coord
			vertex1.setPosition(nDimBox[0][2], 2);
			vertex2.setPosition(nDimBox[1][2], 2);
			ArrayList< RealPoint > point_coords = new ArrayList<  >();

			point_coords.add(new RealPoint(vertex1));
			point_coords.add(new RealPoint(vertex2));
			out.add(point_coords);
	
		}	
		return out;
	}
	
	/** returns vertices of box specified by provided interval in no particular order **/
	public static ArrayList<RealPoint > getBoxVertices(final RealInterval interval)
	{	
		ArrayList<RealPoint> out = new ArrayList<>();
		
		RealPoint [] rpBounds = new RealPoint [2];
		
		rpBounds[0]= interval.minAsRealPoint();
		rpBounds[1]= interval.maxAsRealPoint();
		
		for (int i=0; i<8; i++)
		{
			
		  String indexes = String.format("%3s", Integer.toBinaryString(i)).replaceAll(" ", "0");

		  //System.out.println(indexes);
		  
		  RealPoint vert = new RealPoint(3);
		  
		  for(int d=0; d<3; d++)
		  {
			  vert.setPosition(rpBounds[Character.getNumericValue(indexes.charAt(d))].getDoublePosition(d), d);
		  }
		  
		  out.add(vert);
		}
		
		return out;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof VolumeBox &&
				compareVBoxes(  ( VolumeBox ) obj );
	}
	
	boolean compareVBoxes(VolumeBox v2)
	{
		if (v2==null)
			return false;
		boolean bFinal = true;
		//compare intervals
		bFinal  &= interval.equals( v2.interval );
		//compare transforms
		bFinal  &= Misc.compareAffineTransforms( this.transform, v2.transform );
		return bFinal;
		
	}
	public boolean compareIntervalTransformm(RealInterval int2, AffineTransform3D tr2)
	{

		boolean bFinal = true;
		//compare intervals
		bFinal  &= interval.equals( int2 );
		//compare transforms
		bFinal  &= Misc.compareAffineTransforms( this.transform, tr2 );
		return bFinal;
		
	}
	@Override
	public int hashCode()
	{
		int hash = 17;
		if(interval != null)
		{
			final double [] min = interval.minAsDoubleArray();
			final double [] max = interval.minAsDoubleArray();
		
			for(int d=0; d<3; d++)
			{
				hash = hash * 23 + Double.hashCode(min[d]);
				hash = hash * 23 + Double.hashCode(max[d]);
			}
		}
		if(transform != null)
		{
	
			for(int i=0; i<3; i++)
				for(int j=0; j<3; j++)
				{
					hash = hash * 23 + Double.hashCode(transform.get( i,j ));

				}
		}
		return hash;
	}

	@Override
	public void reload()
	{
		if(edgesVis != null)
		{
			for (VisPolyLineAA edge : edgesVis)
			{
				edge.reload();
			}
		}
		
	}
}
