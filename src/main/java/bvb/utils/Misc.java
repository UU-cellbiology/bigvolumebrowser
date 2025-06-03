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
package bvb.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;
import net.imglib2.util.LinAlgHelpers;

import bdv.tools.transformation.TransformedSource;
import bdv.util.Affine3DHelpers;
import bdv.util.BoundedRange;
import bdv.viewer.Source;

public class Misc
{
	public static FinalRealInterval getSourceBoundingBox(final Source<?> source, int nTimePoint, int baseLevel)
	{
		final AffineTransform3D transformSource = new AffineTransform3D();
		(( TransformedSource< ? > ) source).getSourceTransform(nTimePoint, baseLevel, transformSource);
		final double [] min = source.getSource( nTimePoint, baseLevel ).minAsDoubleArray();
		final double [] max = source.getSource( nTimePoint, baseLevel ).maxAsDoubleArray();
		//extend to include all range
		for(int d=0; d<3; d++)
		{
			min[d] -= 0.5;
			max[d] += 0.5;
		}
		final FinalRealInterval interval = new FinalRealInterval(min, max);
		return transformSource.estimateBounds( interval );
	}
	
	public static FinalRealInterval getSourceBoundingBoxAllTP(final Source<?> source)
	{
		FinalRealInterval interval = null;
		if ( source != null )
		{
			//get the range over all timepoints
			int t = 0;
			while(source.isPresent( t ))
			{
				if(interval == null)
				{
					interval = Misc.getSourceBoundingBox(source,t,0);
				}
				else
				{
					interval = Intervals.union( interval, Misc.getSourceBoundingBox(source,t,0));
				}
					
				t++;
			}
		}
		return interval;
	}
	public static FinalRealInterval getSourceTranlsationRange(final Source<?> source, int nTimePoint, int baseLevel)
	{
		final AffineTransform3D transformSource = new AffineTransform3D();
		(( TransformedSource< ? > ) source).getSourceTransform(nTimePoint, baseLevel, transformSource);
		final double [] min = source.getSource( nTimePoint, baseLevel ).minAsDoubleArray();
		final double [] max = source.getSource( nTimePoint, baseLevel ).maxAsDoubleArray();
		//extend to include all range
		for(int d=0; d<3; d++)
		{
			min[d] -= 0.5;
			max[d] += 0.5;
		}
		for(int d=0; d<3; d++)
		{
			min[d] -= (max[d]-min[d]);
		}
		final FinalRealInterval interval = new FinalRealInterval(min, max);
		return transformSource.estimateBounds( interval );
	}
	
	public static FinalRealInterval getSourceTranlsationRangeAllTP(final Source<?> source)
	{
		FinalRealInterval interval = null;
		if ( source != null )
		{
			//get the range over all timepoints
			int t = 0;
			while(source.isPresent( t ))
			{
				if(interval == null)
				{
					interval = Misc.getSourceTranlsationRange(source,t,0);
				}
				else
				{
					interval = Intervals.union( interval, Misc.getSourceTranlsationRange(source,t,0));
				}
					
				t++;
			}
		}
		return interval;
	}
	
	public static double[] getSourceMin(final Source<?> source, int nTimePoint, int baseLevel)
	{
		final AffineTransform3D transformSource = new AffineTransform3D();
		(( TransformedSource< ? > ) source).getSourceTransform(nTimePoint, baseLevel, transformSource);
		final double [] min = source.getSource( nTimePoint, baseLevel ).minAsDoubleArray();
		final double [] max = source.getSource( nTimePoint, baseLevel ).maxAsDoubleArray();
		//extend to include all range
		for(int d=0; d<3; d++)
		{
			min[d] -= 0.5;
			max[d] += 0.5;
		}
		final FinalRealInterval interval = transformSource.estimateBounds( new FinalRealInterval(min, max) );
		
		return interval.minAsDoubleArray();
	}
	
	public static double[] getSourceMinNoFixedTransform(final Source<?> source, int nTimePoint, int baseLevel)
	{
		final AffineTransform3D transformFullSource = new AffineTransform3D();
		(( TransformedSource< ? > ) source).getSourceTransform(nTimePoint, baseLevel, transformFullSource);
		final AffineTransform3D transformFixed = new AffineTransform3D();
		(( TransformedSource< ? > ) source).getFixedTransform( transformFixed );
		
		//remove fixed transform
		final AffineTransform3D transformSource = new AffineTransform3D ();
		transformSource.set( transformFullSource );
		transformSource.preConcatenate( transformFixed.inverse() );
		
		final double [] min = source.getSource( nTimePoint, baseLevel ).minAsDoubleArray();
		final double [] max = source.getSource( nTimePoint, baseLevel ).maxAsDoubleArray();
		//extend to include all range
		for(int d=0; d<3; d++)
		{
			min[d] -= 0.5;
			max[d] += 0.5;
		}
		final FinalRealInterval interval = transformSource.estimateBounds( new FinalRealInterval(min, max) );
		
		return interval.minAsDoubleArray();
	}
	
	public static double[] getSourceMinAllTP(final Source<?> source)
	{
		double [] min = null;
		if ( source != null )
		{
			//get the range over all timepoints
			int t = 0;
			while(source.isPresent( t ))
			{
				if(min == null)
				{
					min = Misc.getSourceMin(source,t,0);
				}
				else
				{
					final double [] minCurr = Misc.getSourceMin(source,t,0);
					for(int d=0; d<3; d++)
						min[d] = Math.min( min[d], minCurr[d] );
				}					
				t++;
			}
		}
		return min;
	}
	
	public static double[] getSourceMinNoFixedTransformAllTP(final Source<?> source)
	{
		double [] min = null;
		if ( source != null )
		{
			//get the range over all timepoints
			int t = 0;
			while(source.isPresent( t ))
			{
				if(min == null)
				{
					min = Misc.getSourceMinNoFixedTransform(source,t,0);
				}
				else
				{
					final double [] minCurr = Misc.getSourceMinNoFixedTransform(source,t,0);
					for(int d=0; d<3; d++)
						min[d] = Math.min( min[d], minCurr[d] );
				}					
				t++;
			}
		}
		return min;
	}
	
	public static FinalRealInterval getSourceSize(final Source<?> source, int nTimePoint, int baseLevel)
	{
		final AffineTransform3D transformSource = new AffineTransform3D();
		(( TransformedSource< ? > ) source).getSourceTransform(nTimePoint, baseLevel, transformSource);
		final double [] min = source.getSource( nTimePoint, baseLevel ).minAsDoubleArray();
		final double [] max = source.getSource( nTimePoint, baseLevel ).maxAsDoubleArray();
		//extend to include all range
		for(int d=0; d<3; d++)
		{
			min[d] -= 0.5;
			max[d] += 0.5;
		}
		final FinalRealInterval interval = transformSource.estimateBounds( new FinalRealInterval(min, max) ) ;
		interval.realMin( min );
		interval.realMax( max );
		for(int d=0; d<3; d++)
		{
			max[d] -= min[d];
			min[d] = 0.0;
		}		
		return new FinalRealInterval( min, max );
	}
	
	
	/** depending on nAxis value, extracts Euler angle (rotation around nAxis)
	 * value (in radians) from the quaternion q.
	 * Follows the formula/code from wiki 
	 * https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles **/
	public static double quaternionToAngle(int nAxis, double [] q)
	{
		double sin;
		double cos;
		switch (nAxis)
		{
		case 0:
			sin = 2 * (q[0] * q[1] + q[2] * q[3]);
			cos = 1 - 2 * (q[1] * q[1] + q[2] * q[2]);
			return Math.atan2( sin, cos);
		case 1:
			sin = Math.sqrt(1.0 + 2.0 * (q[0] * q[2] - q[1] * q[3]));
			cos = Math.sqrt(1.0 - 2.0 * (q[0] * q[2] - q[1] * q[3]));
			return 2.0*Math.atan2(sin, cos) - Math.PI*0.5;
		case 2:
			sin = 2 * (q[0] * q[3] + q[1] * q[2]);
			cos = 1 - 2 * (q[2] * q[2] + q[3] * q[3]);
			return Math.atan2(sin, cos);
		default:
			return 0.0;
		}
	}
	
	/** https://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/ **/
	public static double quaternionToAngleSecond(int nAxis, double [] q)
	{
	    		//heading Y axis 
				//attitude Z axis
				//bank X asis
			double test = q[1]*q[2] + q[3]*q[0];
			// singularity at north pole
			if (test > 0.4999) 
			{ 
				switch (nAxis)
				{
				case 0:
					return 0.0;
				case 1:
					return 2.0 * Math.atan2(q[1],q[0]);
				case 2:
					return Math.PI/2;
				default:
					return 0.0;
				}
			}
			// singularity at south pole
			if (test < -0.4999) 
			{ 
				switch (nAxis)
				{
				case 0:
					return 0.0;
				case 1:
					return -2.0 * Math.atan2(q[1],q[0]);
				case 2:
					return - Math.PI/2;
				default:
					return 0.0;
				}

			}
			double [] sq = new double[3];
			for(int d=0;d<3;d++)
			{
				sq[d] = q[d+1]*q[d+1];
			}
			switch (nAxis)
			{
			case 0:
				return Math.atan2(2.0*q[1]*q[0]-2.0*q[2]*q[3] , 1.0 - 2.0*sq[1] - 2.0*sq[3]);
			case 1:
				return Math.atan2(2.0*q[2]*q[0]-2.0*q[1]*q[3] , 1.0 - 2.0*sq[2] - 2.0*sq[3]);
			case 2:
				return Math.asin(2.0*test);
			default:
				return 0.0;
			}	

	}
	
	/** converts the quaternion rotation to a Euler angles (with ambiguity!) **/
	public static double[]  quaternionToEulerAngles(double [] q)
	{
		final double [] eAngles = new double[3];
		
		for (int d=0;d<3;d++)
		{
			eAngles[d] = quaternionToAngle(d,q);
		}
		return eAngles;
	}
	
	/** returns the center of an interval where all coordinates
	 * were multiplied by -1.
	 * Return zero vector if the interval is null **/	
	public static double [] getIntervalCenterNegative(RealInterval interval)
	{
		if(interval == null)
			return new double [3];
		final double [] min = interval.minAsDoubleArray();
		final double [] max = interval.maxAsDoubleArray();
		
		
		for(int d=0;d<3;d++)
		{
			min[d] = -0.5*(max[d]+min[d]);
		}
		
		return min;
		
	}
	
	public static double [] getIntervalCenter(RealInterval interval)
	{
		if(interval == null)
			return new double [3];
		final double [] min = interval.minAsDoubleArray();
		final double [] max = interval.maxAsDoubleArray();
		
		for(int d=0;d<3;d++)
		{
			min[d] = 0.5*(max[d]+min[d]);
		}
		
		return min;
		
	}
	
	public static boolean compareAffineTransforms(AffineTransform3D af1,AffineTransform3D af2 )
	{
		boolean bOut = true;
		if(af1 == null && af2 == null)
			return true;
		if(af1 == null || af2 == null)
			return false;
		for(int i=0; i<3; i++)
		{
			for(int j=0; j<4; j++)
			{
				bOut &= Double.compare( af1.get( i, j ), af2.get( i, j ) ) == 0 ;
			}
			if(!bOut)
				return false;
		}
		
		return bOut;
	}
	
	public static double[] getScale( AffineTransform3D affineTransform3D ) {

		double[] scales = new double[3];
		for(int d = 0; d < 3; ++d)
			scales[d] = Affine3DHelpers.extractScale( affineTransform3D, d );

		return scales;
	}
	
	public static String getSourceStyleName(String sFilename)
	{
		final File f = new File(sFilename);
		return getSourceStyleName(f);
	}
	
	public static String getSourceStyleName(final File f)
	{
		String out = f.getName();
		out = out +"  ("+f.getParent()+")";
		return out;
	}
	
	public static AffineTransform3D getRotationTransform(final double [] eAngles)
	{
		final double[] qRotation = new double[4];
		final double[] q = new double[4];

		final double[] dAxis = new double[3];
		dAxis[0] = 1.0;
		LinAlgHelpers.quaternionFromAngleAxis( dAxis, eAngles[0], qRotation );
		for (int d=1;d<3;d++)
		{
			dAxis[d-1] = 0.0;
			dAxis[d] = 1.0;
			LinAlgHelpers.quaternionFromAngleAxis( dAxis, eAngles[d], q);
			LinAlgHelpers.quaternionMultiply( q, qRotation, qRotation );
		}
		
		final double [][] rotMatrix = new double [3][4];  
		LinAlgHelpers.quaternionToR( qRotation, rotMatrix );
		final AffineTransform3D clipRot = new AffineTransform3D();
		
		clipRot.set( rotMatrix );
		return clipRot;
	}
	
	public static boolean checkInterval(RealInterval interval)
	{
		//sanity check
		final double [][] minmax = new double [2][]; 
		minmax[0] = interval.minAsDoubleArray();
		minmax[1] = interval.maxAsDoubleArray();
		for(int d=0; d<3;d++ )
		{
			for(int i=0; i<2; i++)
			{
				if(Double.isInfinite( minmax[i][d] ))
				{
					System.err.println("Error: interval bounds are infinite.");
					return false;
				}
				if(Double.isNaN( minmax[i][d] ))
				{
					System.err.println("Error: interval bounds are NaN.");
					return false;
				}

			}
			if(minmax[0][d]>=minmax[1][d])
			{
				System.err.println("Error: interval min bound >= max bound.");				
				return false;
			}
		}
		return true;
	}
	/** function analyzes a text file with at least 2 strings
	 * and returns the number of new line characters used in the file **/
	public static int getBytesPerNewLine(final File inFile) throws FileNotFoundException, IOException
	{
		String line1 = "";
		String line2 = "";
		int nCount = 0;
		//read two lines
		try ( BufferedReader br = new BufferedReader(new FileReader(inFile))) 
		{
			line1 = br.readLine();
			line2 = br.readLine();
		}

		char [] cFirst = new char[1];
		line2.getChars( 0, 1, cFirst, 0 );
		try ( BufferedReader br = new BufferedReader(new FileReader(inFile))) 
		{
			char [] cbuf = new char [line1.length()];
			
			br.read( cbuf, 0, line1.length());
			cbuf = new char[1];
			char val = (char)br.read();
			while(val != cFirst[0])
			{
				val = (char)br.read();
				nCount++;
			}
		}
		return nCount;
	}
	
	/** a bit more permissive comparison of bounded ranges **/
	public static boolean compareBoundedRanges(final BoundedRange r1, final BoundedRange r2)
	{
		if ( r1 == r2 )
			return true;
		if(r1 == null || r2 == null)
			return false;		
		if (compareRelativeDouble(r1.getMinBound(),r2.getMinBound()))
			return false;
		if (compareRelativeDouble(r1.getMaxBound(),r2.getMaxBound()))
			return false;
		if (compareRelativeDouble(r1.getMin(),r2.getMin()))
			return false;
		if (compareRelativeDouble(r1.getMax(),r2.getMax()))
			return false;
		
		return true;
	}

	/** a bit more permissive comparison of bounded values **/
	public static boolean compareBoundedValues(final BoundedValueDoubleBVB r1, final BoundedValueDoubleBVB r2)
	{
		if ( r1 == r2 )
			return true;
		if(r1 == null || r2 == null)
			return false;		
		if (compareRelativeDouble(r1.getRangeMin(),r2.getRangeMin()))
			return false;
		if (compareRelativeDouble(r1.getRangeMax(),r2.getRangeMax()))
			return false;
		if (compareRelativeDouble(r1.getCurrentValue(),r2.getCurrentValue()))
			return false;
		
		return true;
	}

	
	public static boolean compareRelativeDouble(final double v1, final double v2)
	{
		return (Math.abs( v1- v2)/Math.max( Math.abs( v1 ), Math.abs( v2 ) )) >0.1 ;
	}
	
	public static BoundedRange translateBoundedRange(final BoundedRange rangeOld, final double  translation)
	{
		final double [] rangeX = new double [4];
		rangeX[0] = rangeOld.getMin();
		rangeX[1] = rangeOld.getMax();
		rangeX[2] = rangeOld.getMinBound();
		rangeX[3] = rangeOld.getMaxBound();
		for(int i=0;i<4;i++)
		{
			rangeX[i] += translation;
		}
		return new BoundedRange (rangeX[2], rangeX[3], rangeX[0], rangeX[1]);
		
	}
}
