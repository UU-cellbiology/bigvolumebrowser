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

import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.ValuePair;

import bvb.core.BigVolumeBrowser;
import bvvpg.vistools.BvvStackSource;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import mpicbg.spim.data.generic.AbstractSpimData;

public class Example001AddingData
{
	public static void main( final String[] args )
	{
				
		new ImageJ();

		//start BVB
		BigVolumeBrowser bvbTest = new BigVolumeBrowser(); 		
		bvbTest.startBVB("");
		
		
		//make and add ImagePlus
		ImagePlus imp = IJ.createHyperStack( "test ImageJ", 100, 100, 2, 100, 3, 8 );
		ij.plugin.HyperStackMaker.labelHyperstack( imp );
		bvbTest.addImagePlus( imp );
		
		//add BDV xml
		final ValuePair< AbstractSpimData< ? >, List< BvvStackSource< ? > > > spimOut = bvbTest.loadBDVHDF5( "src/test/resources/images/t1-head.xml" );
		//adjust brightness
		final BvvStackSource< ? > bvvSource = spimOut.getB().get( 0 );
		bvvSource.setDisplayRange( 0, 500 );
		//set LUT
		bvvSource.setLUT( "Fire" );
		
		//make and add RAI (sphere with random values)
		int nRadius = 35;
		int maxInt = 200;
		final RandomAccessibleInterval< UnsignedByteType > sphereRai = RandomHyperSphere.generateRandomSphere(nRadius, maxInt);		
		bvbTest.addRAI( sphereRai );
		
		//focus on everything
		bvbTest.bvbActions.actionCenterView();

	}
}
