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

import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;

import bvb.core.BigVolumeBrowser;
import bvb.io.meshes.WRLParser;
import bvb.scene.VisMeshColor;
import bvb.scene.VisPointsScaled;
import bvb.shapes.MeshColor;
import bvb.shapes.SpotsSame;
import ij.ImageJ;

public class Example003Mesh2
{
	public static void main( final String[] args )
	{
				
		new ImageJ();

		//start BVB
		BigVolumeBrowser bvbTest = new BigVolumeBrowser(); 		
		bvbTest.startBVB("");
	
		final Color meshColor = Color.CYAN;
		//load and show bunny mesh from file
		String fMeshFilename  = "src/test/resources/mesh/bunny.stl";
		String sFilename = "/home/eugene/Desktop/projects/BVB/wrl_example/Image_6.wrl";
		//String sFilename = "/home/eugene/Desktop/projects/BVB/wrl_example/240822_Droplet_LAIR_TOPRO00_Timelapse_B7_Merged_xyCorrected_[ims1_2024-09-26T13-56-16.610]_MSC_RPMI.wrl";

		
		WRLParser loaderWRT = new WRLParser ();
		loaderWRT.nMaxMeshes = 1;
		ArrayList< Mesh > loadedMeshes = loaderWRT.readWRL(sFilename);
		MeshColor meshBunny =  new MeshColor(loadedMeshes.get( 0 ));
//		MeshColor meshBunny = new MeshColor(fMeshFilename);
		
		//render with points
		//meshBunny.setSurfaceRender( VisMeshColor.SURFACE_SHADE);
		meshBunny.setPointsRender( 0.1f );
		meshBunny.setColor( meshColor );
		
		//now let's load mesh separately
		//Mesh bunny = MeshColor.loadMeshFromFile( fMeshFilename );

		Mesh bunny = loadedMeshes.get( 0 );
		
		//let's add an empty volume around it
		RealInterval bunnyInt = Meshes.boundingBox( bunny );
		bvbTest.addRAI(RAIdummy.dummyRAI(bunnyInt));
		meshBunny.setColor(meshColor );
		
		//and finally add mesh to BVB
		bvbTest.addShape( meshBunny );	
		
		//add spots
		
		SpotsSame testPoints = new SpotsSame(0.8f, Color.RED, VisPointsScaled.SHAPE_ROUND, VisPointsScaled.RENDER_OUTLINE);
		
		final ArrayList<RealPoint> vertices = new ArrayList<>();
		double [] ph = new double [3];
		for(int d=0;d<3;d++)
		{
			ph[d] = 0.5*(bunnyInt.realMax(d )+bunnyInt.realMin(d ));
		}
		int nTotNumber = 100;
		for(int i=0;i<nTotNumber; i++)
		{
			double [] p = new double [3];
			for(int d=0;d<3;d++)
			{
				p[d] = bunnyInt.realMin( d )+ Math.random()*(bunnyInt.realMax(d )-bunnyInt.realMin(d ));
			}
	
			vertices.add( new RealPoint(new double[] {p[0], p[1], p[2]}));
		}
		testPoints.setPoints( vertices );
		//bvbTest.addShape( testPoints );
		
		SpotsSame testPoint = new SpotsSame(0.8f, Color.RED, VisPointsScaled.SHAPE_ROUND, VisPointsScaled.RENDER_OUTLINE);
		
		vertices.clear();
		vertices.add( new RealPoint(ph) );
		testPoint.setPoints( vertices );
		bvbTest.addShape( testPoint );
		
		//now let's show other ways to render meshed

		//we going to shift them next to each other
		final double displacementX = 1.1*(bunnyInt.realMax( 0 )-bunnyInt.realMin( 0 ));
		final double displacementY = -1.3*(bunnyInt.realMax( 1 )-bunnyInt.realMin( 1 ));
		
		//show different grid surface renders
		int [] arrSurfaceGrid = new int [] {VisMeshColor.GRID_WIRE,  
				VisMeshColor.GRID_CARTESIAN};
		
		for(int i=0;i<1;i++)
		{		
			//translate along X and add a copy
			Meshes.translate( bunny, new double[] {displacementX,0,0} );
			
			meshBunny = new MeshColor(bunny);
			bvbTest.addRAI(RAIdummy.dummyRAI(Meshes.boundingBox( bunny )));
			meshBunny.setSurfaceRender( VisMeshColor.SURFACE_SHADE);
			meshBunny.setSurfaceGrid( arrSurfaceGrid[i] );
			if(i==1)
			{
				meshBunny.setCartesianGrid( 2.0f, 0.1f );
			}
			meshBunny.setColor( meshColor );
			bvbTest.addShape( meshBunny );		
		}
		
		
//		//let's start next row
//		Meshes.translate( bunny, new double[] {-displacementX*3.0, displacementY,0} );
//		
//		//show different surface renders
//		int [] arrSurfaceRender = new int [] {VisMeshColor.SURFACE_SHADE,  
//				VisMeshColor.SURFACE_SHINY, VisMeshColor.SURFACE_SILHOUETTE};
//		
//		for(int i=0;i<3;i++)
//		{		
//			//translate along X and add a copy
//			Meshes.translate( bunny, new double[] {displacementX,0,0} );
//			
//			meshBunny = new MeshColor(bunny);
//			bvbTest.addRAI(RAIdummy.dummyRAI(Meshes.boundingBox( bunny )));
//			
//			meshBunny.setSurfaceRender( arrSurfaceRender[i]);
//			meshBunny.setSurfaceGrid( VisMeshColor.GRID_FILLED);
//			meshBunny.setColor( meshColor );
//			bvbTest.addShape( meshBunny );		
//		}
//		
		//focus on everything
		bvbTest.bvbActions.actionCenterView();

	}
}
