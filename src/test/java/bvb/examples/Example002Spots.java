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

import java.awt.Color;
import java.util.ArrayList;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.type.numeric.integer.UnsignedByteType;


import bvb.core.BigVolumeBrowser;
import bvb.scene.VisPointsScaled;
import bvb.shapes.SpotsSame;
import ij.ImageJ;

public class Example002Spots
{
	public static void main( final String[] args )
	{
				
		new ImageJ();

		//start BVB
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 		
		testBVB.startBVB("");
		
		//load some data
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube.xml" );		
		
		//add sphere with random values as background		
		int nRadius = 35;
		int maxInt = 200;
		final RandomAccessibleInterval< UnsignedByteType > sphereRai = RandomHyperSphere.generateRandomSphere(nRadius, maxInt);
		
		testBVB.addRAI( sphereRai );

		
		//define point size, color, shape and filling
		SpotsSame testPoints = new SpotsSame(nRadius*0.08f, Color.RED, VisPointsScaled.SHAPE_ROUND, VisPointsScaled.RENDER_OUTLINE);
		
		final ArrayList<RealPoint> vertices = new ArrayList<>();
		
		int nTotNumber = 100;
		
		double nScale = nRadius*2.0;
		
		for(int i=0;i<nTotNumber; i++)
		{
			vertices.add( new RealPoint(new double[] {Math.random()*nScale, Math.random()*nScale, Math.random()*nScale}));
		}
		
		testPoints.setPoints( vertices );
		testBVB.addShape( testPoints );
	}
}
