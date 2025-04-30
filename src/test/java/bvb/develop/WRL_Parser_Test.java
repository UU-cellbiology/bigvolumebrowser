package bvb.develop;

import java.awt.Color;

import java.util.ArrayList;

import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.util.Intervals;

import bvb.core.BigVolumeBrowser;
import bvb.examples.RAIdummy;
import bvb.io.meshes.WRLParser;
import bvb.scene.VisMeshColor;
import bvb.shapes.MeshColor;
import ij.ImageJ;

public class WRL_Parser_Test
{
	

	public static void main( final String[] args )
	{
		new ImageJ();
		
		String sFilename = "/home/eugene/Desktop/projects/BVB/wrl_example/Image_6.wrl";
		//String sFilename = "/home/eugene/Desktop/projects/BVB/wrl_example/240822_Droplet_LAIR_TOPRO00_Timelapse_B7_Merged_xyCorrected_[ims1_2024-09-26T13-56-16.610]_MSC_RPMI.wrl";

		
		WRLParser loaderWRT = new WRLParser ();
		//loaderWRT.nMaxMeshes = 1000;
		//loaderWRT.nMaxTimePoints = 20;
		loaderWRT.bEnableWireGrid = true;
		ArrayList< Mesh > loadedMeshes = loaderWRT.readWRL(sFilename);
		//start BVB
		BigVolumeBrowser bvbTest = new BigVolumeBrowser(); 		
		bvbTest.startBVB("");
//		System.out.println("vert_" + Integer.toString(mesh.vertices().size()) );
//
//		System.out.println("tr_"+Integer.toString(  mesh.triangles().size()) );
//
//		System.out.println("maxInd_"+Integer.toString(  tr.nMax) );

		//Mesh mesh = loadedMeshes.get( 0 );
		
		final Color meshColor = Color.CYAN;


		//let's add an empty volume around it
		RealInterval totInt = Meshes.boundingBox( loadedMeshes.get(0) );
		
		for(int i=1;i<loadedMeshes.size();i++)
		{
			totInt = Intervals.union( totInt,  Meshes.boundingBox( loadedMeshes.get(i) )); 
		}
		
		if(!loaderWRT.isTimeData())
		{
			//let's add an empty volume around it
			bvbTest.addRAI(RAIdummy.dummyRAI(totInt));
		}
		else
		{
			int nMaxTP = 0;
			for(int i=0;i<loaderWRT.timePoints.size();i++)
			{
				if(loaderWRT.timePoints.get( i )>nMaxTP)
					nMaxTP = loaderWRT.timePoints.get( i );
			}
			bvbTest.addRAI(RAIdummy.dummyRAI(totInt, nMaxTP+1));
		}
		

		for(int i=0;i<loadedMeshes.size();i++)
		{
		
			MeshColor meshBVB = new MeshColor(loadedMeshes.get( i ), bvbTest);

			meshBVB.setColor(meshColor );
		
			//meshBVB.setPointsRender( 0.3f );
			//meshBVB.setSurfaceRender( VisMeshColor.SURFACE_SILHOUETTE );

			meshBVB.setSurfaceRender( VisMeshColor.SURFACE_SHINY );
			//meshBVB.setSurfaceGrid( VisMeshColor.GRID_WIRE );
			//cartesian
//			meshBVB.setSurfaceGrid( VisMeshColor.GRID_CARTESIAN);
//			meshBVB.setCartesianGrid( 1.0f, 0.1f );
			//and finally add mesh to BVB
			
			if(loaderWRT.isTimeData())
			{
				meshBVB.setTimePoint( loaderWRT.timePoints.get( i ) );
			}
			
			bvbTest.addShape( meshBVB );	
		}
		
	}
}
