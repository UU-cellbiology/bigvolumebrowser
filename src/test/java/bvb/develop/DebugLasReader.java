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
package bvb.develop;

import com.github.mreutegg.laszip4j.LASPoint;
import com.github.mreutegg.laszip4j.LASReader;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import net.imglib2.RealPoint;

import bvb.core.BigVolumeBrowser;
import bvb.scene.VisSpots;
import bvb.shapes.Spots;
import ij.IJ;
import ij.ImageJ;

public class DebugLasReader
{
	public static void main( final String[] args )
	{
				
		new ImageJ();

		//start BVB
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 		
		
		testBVB.startBVB("");
			
				
		//define point size, color, shape and filling
		//spots with the same diameter
		Spots samePoints = new Spots(60, new Color(255,0,0,255), VisSpots.SHAPE_ROUND, VisSpots.RENDER_FILLED);
		//spots with different diameters

		final ArrayList<RealPoint> verticesSame = new ArrayList<>();

		LASReader reader = new LASReader(new File("/home/eugene/Desktop/projects/BVB/points/LAS/32CZ1_01.las"));
		
//		LASReader reader = new LASReader(new File("/home/eugene/Desktop/projects/BVB/points/LAS/Kruyt_AHN2_32CZ1_01.LAZ"));
		//LASReader reader = new LASReader(new File("/home/eugene/Desktop/projects/BVB/LAS/DoM_AHN2_31HZ2_02.LAZ"));
		//LASReader reader = new LASReader(new File("/home/eugene/Desktop/projects/BVB/LAS/our_house.LAZ"));

		int nCount = 0;
		long nRecords = reader.getHeader().getNumberOfPointRecords();
		
		int nMaxRecords = (int)nRecords;
		System.out.println(nRecords);
		float [] colors = new float[(nMaxRecords)*4];
		for (LASPoint p : reader.getPoints()) 
	    {
	    	if(nCount >= nMaxRecords)
	    		break;
	        // read something from point
			verticesSame.add( new RealPoint(new double[] {p.getX(), p.getY(), p.getZ()}));
			int r = p.getRed();
			int g = p.getGreen();
			int b = p.getBlue();
			int a = p.getIntensity();
			colors[nCount*4] = r/255f;
			colors[nCount*4+1] = g/255f;
			colors[nCount*4+2] = b/255f;
			colors[nCount*4+3] = a/255f;//1.0f;
			//verticesColor.add( Color.BLUE );
	    	nCount++;
	    	IJ.showProgress( (double)nCount/((double)nMaxRecords) );

	    }
	    System.out.println(nCount);

	    samePoints.setPoints( verticesSame, null, null );
	    samePoints.setColors( colors );
		testBVB.addShape( samePoints );



	}
}