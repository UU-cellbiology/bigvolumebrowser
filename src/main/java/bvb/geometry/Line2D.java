package bvb.geometry;

import org.joml.Vector2f;

import net.imglib2.RealPoint;
import net.imglib2.util.LinAlgHelpers;

/**
 *  vector notation for a line in 3D:
 *  linev[0]+ linev[1]*d
 *  
 *  linev[0] - some vector on the line
 *  linev[1] - vector along the line (normalized)
 * **/
public class Line2D {
	
	/**
	 *  linev[0] - some vector on the line
	 *  linev[1] - vector along the line (normalized)
	 * **/
	public double [][] linev;
	
    /** empty constructor **/
	public Line2D()
	{
		linev = new double [2][2]; 
	}
	/**
	 *  @param v0_ - some vector on the line
	 *  @param v1_ - vector along the line (does not have to be normalized)
	 * **/
	public void setVectors(final double [] v0_, final double [] v1_)
	{
		for (int i =0;i<2;i++)
		{
			linev[0][i]=v0_[i];
			linev[1][i]=v1_[i];
		}
		LinAlgHelpers.normalize(linev[1]);
		
	}

	
	/** from two vectors on the line (from RealPoint) **/
	public Line2D(final RealPoint v1, final RealPoint v2)
	{
		initFromTwoPoints(v1,v2);
	}	
	
	
	public void initFromTwoPoints(final RealPoint v1, final RealPoint v2)
	{
		linev = new double [2][2];
		v2.localize(linev[1]);
		v1.localize(linev[0]);
		LinAlgHelpers.subtract(linev[1], linev[0], linev[1]);
		LinAlgHelpers.normalize(linev[1]);
	}
	
	public void initFromTwoPoints(final double [] v1, final double [] v2)
	{
		linev = new double [2][2];
		for(int i=0;i<2;i++)
		{
			linev[0][i] = v1[i];
			linev[1][i] = v2[i];
		}
		LinAlgHelpers.subtract(linev[1], linev[0], linev[1]);
		LinAlgHelpers.normalize(linev[1]);
	}
	
	/** from two vectors on the line (for Vector3f) **/
	public Line2D(final Vector2f v1, final Vector2f v2)
	{
		linev = new double [2][2];
		for (int i=0;i<2;i++)
		{
			linev[0][i]=v1.get(i);
			linev[1][i]=v2.get(i)-linev[0][i];
		}
		LinAlgHelpers.normalize(linev[1]);
	}
	
	/** from two vectors on the line (for double) **/
	public Line2D(final double [] v1, final double [] v2)
	{
		linev= new double [2][2];
		for (int i=0;i<2;i++)
		{
			linev[0][i]=v1[i];
			linev[1][i]=v2[i]-linev[0][i];
		}
		LinAlgHelpers.normalize(linev[1]);
	}
	//return a vector on the line at d
	public void value(final double d, final Vector2f out)
	{
		
		out.x = (float) (linev[0][0]+linev[1][0]*d);
		out.y = (float) (linev[0][1]+linev[1][1]*d);
	}
	//return a vector on the line at d
	public void value(final double d, final RealPoint out)
	{
		out.setPosition(linev[0][0]+linev[1][0]*d, 0);
		out.setPosition(linev[0][1]+linev[1][1]*d, 1);		
	}
	//return a vector on the line at d
	public void value(final double d, final double [] out)
	{
		out[0]=linev[0][0]+linev[1][0]*d;
		out[1]=linev[0][1]+linev[1][1]*d;
	}
		
	/** distance between line and point in 3D **/
	public static double distancePointLine(RealPoint point_, Line2D line)
	{
		double [] point = new double [2];
		double [] dist = new double [2];
		point_.localize(point);
		LinAlgHelpers.subtract(point, line.linev[0], point);
		LinAlgHelpers.cross(point, line.linev[1], dist);
		return LinAlgHelpers.length(dist);
	}
	
	public static double [] intersectionLines2D(Line2D l1, Line2D l2)
	{
		double [] d = new double[2];
		
		
		d[1] = l1.linev[0][1]-l2.linev[0][1] + (l2.linev[0][0]-l1.linev[0][0])*(l1.linev[1][1]/l1.linev[1][0]);
		d[1] = d[1]/(l2.linev[1][1]-(l1.linev[1][1]*l2.linev[1][0]/l1.linev[1][0]));
		
		d[0] = (l2.linev[0][0]+l2.linev[1][0]*d[1]-l1.linev[0][0])/l1.linev[1][0];
		
		//verify
//		double [] test1 = new double[2];
//		double [] test2 = new double[2];
//		l1.value( d[0], test1 );
//		l2.value( d[1], test2 );
		return d;
		
	}
}
