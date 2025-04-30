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
package bvb.examples;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import net.imglib2.view.Views;

public class RAIdummy
{

	/** make a dummy empty interval **/
	public static RandomAccessibleInterval<UnsignedShortType> dummyRAI(final RealInterval interval)
	{
		double [] min = interval.minAsDoubleArray();
		double [] max = interval.maxAsDoubleArray();
		long [][] minmax = new long[2][3];
		for(int d=0; d<3;d++)
		{
			minmax[0][d] = Math.round( Math.floor( min[d] ) );
			minmax[1][d] = Math.round( Math.ceil( max[d] ) );
		}
		
	    ArrayImg< UnsignedShortType, ShortArray > center = ArrayImgs.unsignedShorts(new long [] {1,1,1 });
	    return Views.interval( Views.extendZero( center ), minmax[0], minmax[1] );

	}
	/** make a dummy empty interval **/
	public static RandomAccessibleInterval<UnsignedShortType> dummyRAI(final RealInterval interval, final int nTimePoints)
	{
		double [] min = interval.minAsDoubleArray();
		double [] max = interval.maxAsDoubleArray();
		long [][] minmax = new long[2][4];
		for(int d=0; d<3;d++)
		{
			minmax[0][d] = Math.round( Math.floor( min[d] ) );
			minmax[1][d] = Math.round( Math.ceil( max[d] ) );
		}
		minmax[0][3] = 0;
		minmax[1][3] = nTimePoints-1;
	    ArrayImg< UnsignedShortType, ShortArray > center = ArrayImgs.unsignedShorts(new long [] {1,1,1, nTimePoints });
	    return Views.interval( Views.extendZero( center ), minmax[0], minmax[1] );

	}
}
