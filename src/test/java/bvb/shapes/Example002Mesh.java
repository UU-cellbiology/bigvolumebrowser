package bvb.shapes;


import java.awt.Color;

import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;

import bvb.core.BigVolumeBrowser;
import bvb.scene.VisMesh;
import ij.ImageJ;

public class Example002Mesh
{
	public static void main( final String[] args )
	{
				
		new ImageJ();

		//start BVB
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 		
		testBVB.startBVB();
	
		
		//load and show bunny mesh
		String fMeshFilename  = "src/test/resources/mesh/bunny.stl";
		
		MeshExample meshBunny = new MeshExample(fMeshFilename);
		
		//render with points
		meshBunny.setPointsRender( 0.3f );
		meshBunny.setColor( Color.CYAN );
		
		//now load mesh separately
		Mesh bunny = MeshExample.loadMeshFromFile( fMeshFilename );
		
		RealInterval bunnyInt = Meshes.boundingBox( bunny );
		testBVB.addRAI(RAIdummy.dummyRAI(bunnyInt));
		meshBunny.setColor( Color.CYAN );
		testBVB.addShape( meshBunny );	
		


		final double displacementX = 1.1*(bunnyInt.realMax( 0 )-bunnyInt.realMin( 0 ));
		final double displacementY = -1.3*(bunnyInt.realMax( 1 )-bunnyInt.realMin( 1 ));
		
		//show different grid surface renders
		int [] arrSurfaceGrid = new int [] {VisMesh.GRID_WIRE,  
				VisMesh.GRID_CARTESIAN};
		
		for(int i=0;i<2;i++)
		{		
			//translate along X and add a copy
			Meshes.translate( bunny, new double[] {displacementX,0,0} );
			
			meshBunny = new MeshExample(bunny);
			testBVB.addRAI(RAIdummy.dummyRAI(Meshes.boundingBox( bunny )));
			meshBunny.setSurfaceRender( VisMesh.SURFACE_SHADE);
			meshBunny.setSurfaceGrid( arrSurfaceGrid[i] );
			if(i==1)
			{
				meshBunny.setCartesianGrid( 2.0f, 0.1f );
			}
			meshBunny.setColor( Color.CYAN );
			testBVB.addShape( meshBunny );		
		}
		
		
		
		//translate along X and add a copy
		Meshes.translate( bunny, new double[] {-displacementX*3.0, displacementY,0} );
		
		//show different surface renders
		int [] arrSurfaceRender = new int [] {VisMesh.SURFACE_SHADE,  
				VisMesh.SURFACE_SHINY, VisMesh.SURFACE_SILHOUETTE};
		
		for(int i=0;i<3;i++)
		{		
			//translate along X and add a copy
			Meshes.translate( bunny, new double[] {displacementX,0,0} );
			
			meshBunny = new MeshExample(bunny);
			testBVB.addRAI(RAIdummy.dummyRAI(Meshes.boundingBox( bunny )));
			
			meshBunny.setSurfaceRender( arrSurfaceRender[i]);
			meshBunny.setSurfaceGrid( VisMesh.GRID_FILLED);
			meshBunny.setColor( Color.CYAN );
			testBVB.addShape( meshBunny );		
		}
		

	}
}
