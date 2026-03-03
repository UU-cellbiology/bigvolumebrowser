/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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


import java.awt.Color;

import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;

import bvb.core.BigVolumeBrowser;
import bvb.scene.VisMesh;
import bvb.shapes.MeshShape;
import ij.ImageJ;

public class Navigationexample
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
		
		MeshShape meshBunny = new MeshShape(fMeshFilename);
		
		//render with points
		meshBunny.setRenderType( VisMesh.POINTS);
		meshBunny.setPointSize( 0.4f );
		meshBunny.setColor( meshColor );
		
		//now let's load mesh separately
		Mesh bunny = MeshShape.loadMeshFromFile( fMeshFilename );
		
		//let's add an empty volume around it
		RealInterval bunnyInt = Meshes.boundingBox( bunny );
		meshBunny.setColor(meshColor );
		
		//and finally add mesh to BVB
		//bvbTest.addShape( meshBunny );	
		

		//now let's show other ways to render meshed

		//we going to shift them next to each other
		final double displacementX = 1.1*(bunnyInt.realMax( 0 )-bunnyInt.realMin( 0 ));
		final double displacementY = -1.3*(bunnyInt.realMax( 1 )-bunnyInt.realMin( 1 ));
		
//		//show different grid surface renders
//		int [] arrSurfaceGrid = new int [] {VisMesh.GRID_WIRE,  
//				VisMesh.GRID_CARTESIAN};
		int nBunnyCount = 0;
		for(int i = 0; i < 2; i++)
		{		
			//translate along X and add a copy
			Meshes.translate( bunny, new double[] {displacementX,-displacementX,0} );
			
			meshBunny = new MeshShape(bunny);
			meshBunny.setSurfaceRender( VisMesh.SURFACE_SHADE);
//			meshBunny.setSurfaceGrid( arrSurfaceGrid[i] );
	//		if( i == 1)
		//	{
			//	meshBunny.setCartesianGrid( 2.0f, 0.1f );
			//}
			meshBunny.setColor( meshColor );
			nBunnyCount++;
			meshBunny.setName( "bunny" + nBunnyCount ); 
			bvbTest.addShape( meshBunny );		
		}
		
		
		//let's start next row
		Meshes.translate( bunny, new double[] {-displacementX*3.0, displacementY,0} );
		
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
//			
//			meshBunny.setSurfaceRender( arrSurfaceRender[i]);
//			meshBunny.setSurfaceGrid( VisMeshColor.GRID_FILLED);
//			meshBunny.setColor( meshColor );
//			nBunnyCount++;
//			meshBunny.setName( "bunny" + nBunnyCount ); 
//			bvbTest.addShape( meshBunny );		
//		}
		
		//focus on everything
		bvbTest.bvbActions.actionCenterView();

	}
}
