package bvb.shapes;


import java.awt.Color;

import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;

import bvb.core.BigVolumeBrowser;
import bvb.scene.VisMeshColor;
import ij.ImageJ;

public class Example003Mesh
{
	public static void main( final String[] args )
	{
				
		new ImageJ();

		//start BVB
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 		
		testBVB.startBVB();
	
		final Color meshColor = Color.MAGENTA;
		//load and show bunny mesh from file
		String fMeshFilename  = "src/test/resources/mesh/bunny.stl";
		
		MeshColorExample meshBunny = new MeshColorExample(fMeshFilename);
		
		//render with points
		meshBunny.setPointsRender( 0.3f );
		meshBunny.setColor( meshColor );
		
		//now let's load mesh separately
		Mesh bunny = MeshColorExample.loadMeshFromFile( fMeshFilename );
		
		//let's add an empty volume around it
		RealInterval bunnyInt = Meshes.boundingBox( bunny );
		testBVB.addRAI(RAIdummy.dummyRAI(bunnyInt));
		meshBunny.setColor(meshColor );
		
		//and finally add mesh to BVB
		testBVB.addShape( meshBunny );	
		

		//now let's show other ways to render meshed

		//we going to shift them next to each other
		final double displacementX = 1.1*(bunnyInt.realMax( 0 )-bunnyInt.realMin( 0 ));
		final double displacementY = -1.3*(bunnyInt.realMax( 1 )-bunnyInt.realMin( 1 ));
		
		//show different grid surface renders
		int [] arrSurfaceGrid = new int [] {VisMeshColor.GRID_WIRE,  
				VisMeshColor.GRID_CARTESIAN};
		
		for(int i=0;i<2;i++)
		{		
			//translate along X and add a copy
			Meshes.translate( bunny, new double[] {displacementX,0,0} );
			
			meshBunny = new MeshColorExample(bunny);
			testBVB.addRAI(RAIdummy.dummyRAI(Meshes.boundingBox( bunny )));
			meshBunny.setSurfaceRender( VisMeshColor.SURFACE_SHADE);
			meshBunny.setSurfaceGrid( arrSurfaceGrid[i] );
			if(i==1)
			{
				meshBunny.setCartesianGrid( 2.0f, 0.1f );
			}
			meshBunny.setColor( meshColor );
			testBVB.addShape( meshBunny );		
		}
		
		
		//let's start next row
		Meshes.translate( bunny, new double[] {-displacementX*3.0, displacementY,0} );
		
		//show different surface renders
		int [] arrSurfaceRender = new int [] {VisMeshColor.SURFACE_SHADE,  
				VisMeshColor.SURFACE_SHINY, VisMeshColor.SURFACE_SILHOUETTE};
		
		for(int i=0;i<3;i++)
		{		
			//translate along X and add a copy
			Meshes.translate( bunny, new double[] {displacementX,0,0} );
			
			meshBunny = new MeshColorExample(bunny);
			testBVB.addRAI(RAIdummy.dummyRAI(Meshes.boundingBox( bunny )));
			
			meshBunny.setSurfaceRender( arrSurfaceRender[i]);
			meshBunny.setSurfaceGrid( VisMeshColor.GRID_FILLED);
			meshBunny.setColor( meshColor );
			testBVB.addShape( meshBunny );		
		}
		

	}
}
