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
import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.realtransform.AffineTransform3D;

import bvb.core.BigVolumeBrowser;
import bvb.scene.VisMesh;
import bvb.shapes.MeshShape;
import bvb.shapes.MultiMeshShape;
import ij.ImageJ;

public class Example003Mesh
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
		
		//make a BVB mesh object
		int nMeshCount = 1;
		MeshShape meshBunny = new MeshShape(fMeshFilename);
		
		//render with points
		meshBunny.setRenderType( VisMesh.POINTS);
		meshBunny.setPointSize( 0.4f );
		meshBunny.setColor( meshColor );
		meshBunny.setName( "bunny" + nMeshCount );
		nMeshCount++;
		//and finally add mesh to BVB
		bvbTest.addShape( meshBunny );	
		
		//now let's load imglib2 mesh separately
		Mesh bunny = MeshShape.loadMeshFromFile( fMeshFilename );
		
		
		//its bounding box
		RealInterval bunnyInt = Meshes.boundingBox( bunny );				
	
		//let's modify original mesh 
		final double displacementX = 1.1 * (bunnyInt.realMax( 0 ) - bunnyInt.realMin( 0 ));
		//translate along X 
		Meshes.translate( bunny, new double[] {displacementX,0,0} );
		
		// render as wireframe
		MeshShape meshBunnyNext = new MeshShape(bunny);
		meshBunnyNext.setSurfaceRender( VisMesh.SURFACE_SHADE);
		meshBunnyNext.setSurfaceGrid( VisMesh.GRID_WIRE );
		meshBunnyNext.setColor( meshColor );
			
		meshBunnyNext.setName( "bunny" + nMeshCount );
		nMeshCount++;
		bvbTest.addShape( meshBunnyNext  );		

		//now let's modify meshShape transform
		meshBunnyNext = new MeshShape(bunny);
		final AffineTransform3D meshTranslate = new AffineTransform3D();
		
		meshTranslate.translate( displacementX, 0.0, 0.0 );
		meshBunnyNext.setTransform( meshTranslate );
		
		// render as silhouette
		meshBunnyNext.setSurfaceRender( VisMesh.SURFACE_SHINY);
		meshBunnyNext.setSurfaceGrid( VisMesh.GRID_FILLED );
		meshBunnyNext.setColor( meshColor );
			
		meshBunnyNext.setName( "bunny" + nMeshCount );
		nMeshCount++;
		bvbTest.addShape( meshBunnyNext  );		
		
		//now let's load three meshes together as multi-mesh object
		MultiMeshShape multiMeshShape = new MultiMeshShape();
		
		final double displacementY = -1.3 * (bunnyInt.realMax( 1 ) - bunnyInt.realMin( 1 ));

		
		for(int i = 0; i < 3; i++)
		{	
			meshTranslate.identity();
			meshTranslate.translate( displacementX*(i-1), displacementY, 0.0 );
			
			meshBunnyNext = new MeshShape(bunny);	
			meshBunnyNext.setTransform( meshTranslate );
			meshBunnyNext.setSurfaceRender( VisMesh.SURFACE_SILHOUETTE);
			meshBunnyNext.setSurfaceGrid( VisMesh.GRID_FILLED);
			meshBunnyNext.setColor( meshColor );
			
			meshBunnyNext.setName( "bunny" + nMeshCount );
			nMeshCount++;
			multiMeshShape.addMeshShape( meshBunnyNext );
			
		}
		
		multiMeshShape.setName( "three bunnies" );
		bvbTest.addShape( multiMeshShape );		
		
		//focus on everything
		bvbTest.bvbActions.actionCenterView();

	}
}
