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
package bvb.geometry;

import org.joml.Vector3f;

import net.imglib2.RealPoint;
import net.imglib2.util.LinAlgHelpers;

/**
 *  vector notation for a line in 3D:
 *  linev[0]+ linev[1]*d
 *  
 *  linev[0] - some vector on the line
 *  linev[1] - vector along the line (normalized)
 * **/
public class Line3D {
	
	/**
	 *  linev[0] - some vector on the line
	 *  linev[1] - vector along the line (normalized)
	 * **/
	public double [][] linev;
	
    /** empty constructor **/
	public Line3D()
	{
		linev = new double [2][3]; 
	}
	/**
	 *  @param v0_ - some vector on the line
	 *  @param v1_ - vector along the line (does not have to be normalized)
	 * **/
	public void setVectors(final double [] v0_, final double [] v1_)
	{
		for (int i =0;i<3;i++)
		{
			linev[0][i]=v0_[i];
			linev[1][i]=v1_[i];
		}
		LinAlgHelpers.normalize(linev[1]);
		
	}

	
	/** from two vectors on the line (from RealPoint) **/
	public Line3D(final RealPoint v1, final RealPoint v2)
	{
		initFromTwoPoints(v1,v2);
	}	
	
	public void initFromTwoPoints(final RealPoint v1, final RealPoint v2)
	{
		linev= new double [2][3];
		v2.localize(linev[1]);
		v1.localize(linev[0]);
		LinAlgHelpers.subtract(linev[1], linev[0], linev[1]);
		LinAlgHelpers.normalize(linev[1]);
	}
	/** from two vectors on the line (for Vector3f) **/
	public Line3D(final Vector3f v1, final Vector3f v2)
	{
		linev= new double [2][3];
		for (int i=0;i<3;i++)
		{
			linev[0][i]=v1.get(i);
			linev[1][i]=v2.get(i)-linev[0][i];
		}
		LinAlgHelpers.normalize(linev[1]);
	}
	
	/** from two vectors on the line (for double) **/
	public Line3D(final double [] v1, final double [] v2)
	{
		linev= new double [2][3];
		for (int i=0;i<3;i++)
		{
			linev[0][i]=v1[i];
			linev[1][i]=v2[i]-linev[0][i];
		}
		LinAlgHelpers.normalize(linev[1]);
	}
	//return a vector on the line at d
	public void value(final double d, final Vector3f out)
	{
		
		out.x = (float) (linev[0][0]+linev[1][0]*d);
		out.y = (float) (linev[0][1]+linev[1][1]*d);
		out.z = (float) (linev[0][2]+linev[1][2]*d);
	}
	//return a vector on the line at d
	public void value(final double d, final RealPoint out)
	{
		out.setPosition(linev[0][0]+linev[1][0]*d, 0);
		out.setPosition(linev[0][1]+linev[1][1]*d, 1);
		out.setPosition(linev[0][2]+linev[1][2]*d, 2);			
	}
	//return a vector on the line at d
	public void value(final double d, final double [] out)
	{
		out[0]=linev[0][0]+linev[1][0]*d;
		out[1]=linev[0][1]+linev[1][1]*d;
		out[2]=linev[0][2]+linev[1][2]*d;
	}
		
	/** distance between line and point in 3D **/
	public static double distancePointLine(RealPoint point_, Line3D line)
	{
		double [] point = new double [3];
		double [] dist = new double [3];
		point_.localize(point);
		LinAlgHelpers.subtract(point, line.linev[0], point);
		LinAlgHelpers.cross(point, line.linev[1], dist);
		return LinAlgHelpers.length(dist);
	}
	
	/** returns line parameter values for the shortest segment
	 * between the lines in 3D.
	 * taken from https://paulbourke.net/geometry/pointlineplane/	 * **/
	public static double [] linesIntersect(final Line3D l1, final Line3D l2)
	{
		double [] out = new double [2];
		double [] l13 = new double [3];
		LinAlgHelpers.subtract( l1.linev[0], l2.linev[0], l13 );
		final double d1343 = dcoef(l13, l2.linev[1]);
		final double d4321 = dcoef(l2.linev[1], l1.linev[1]);
		final double d1321 = dcoef(l13, l1.linev[1]);
		final double d4343 = dcoef(l2.linev[1], l2.linev[1]);
		final double d2121 = dcoef(l1.linev[1], l1.linev[1]);
		out[0] =  (d1343*d4321 - d1321*d4343 ) / ( d2121*d4343 - d4321*d4321);
		out[1] =  ( d1343 + out[0]* d4321 ) / d4343;
		return out;
	}
	
	public static double dcoef(double [] v1, double v2[])
	{
		return LinAlgHelpers.dot( v1, v2 );
		
	}
}
