package bvb.develop;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.impl.naive.NaiveFloatMesh;

import bvb.core.BigVolumeBrowser;
import bvb.examples.RAIdummy;
import bvb.scene.VisMeshColor;
import bvb.shapes.MeshColor;
import ij.ImageJ;

public class WRL_Parser
{
	public static void main( final String[] args )
	{
		final Mesh mesh = new NaiveFloatMesh();
		TriangleMaker tr = null;
		String sFilename = "/home/eugene/Desktop/projects/BVB/wrl_example/Image_6.wrl";
		try ( BufferedReader br = new BufferedReader(new FileReader(sFilename));) 
		{
			
			String line = "";
			String[] la;
			boolean bContinue = true;
			int nMeshesN = 0;
			while(bContinue)
			{
				line = br.readLine();
				if(line==null)
					break;
				la = line.split("\\s+|,");
				if(la.length>1)
				{
					if(la[1].equals( "Coordinate" ) )
					{
						line = br.readLine();
						la = line.split("\\s+|,");

						
						int nVert = 0;
						//System.out.println("SUCCESS");
						//System.out.println(line);
						
						mesh.vertices().addf( Float.parseFloat( la[3] ), Float.parseFloat( la[4] ), Float.parseFloat( la[5] ) );
						boolean bRead = true;
						while(bRead)
						{
							line = br.readLine();
							la = line.split("\\s+|,");
							mesh.vertices().addf( Float.parseFloat( la[1] ), Float.parseFloat( la[2] ), Float.parseFloat( la[3] ) );				
							nVert++;
							if(la.length>4)
								bRead = false;
							if(nVert==388)
							{
								bRead = false;
							}
						}
						System.out.println("Loaded " +Integer.toString( nVert )+ " vertices");
						//bContinue = false;
					}
						
					
				}
				if(la.length>1)
				{
					if(la[1].equals( "coordIndex" ) )
					{
						nMeshesN ++;
						System.out.println("SUCCESS2");
						la = line.split("\\s+|,");
						tr = new TriangleMaker(mesh);
						for(int i = 3; i<la.length;i++)
						{
							if(la[i].length()>0)
								tr.addIndex( la[i] );
						}
						bContinue = false;
						boolean bRead = true;
						while(bRead)
						{
							line = br.readLine();
							la = line.split("\\s+|,");
							if(la[la.length-1].equals( "]" ))
							{
								bRead = false;
							}
							else
							{
								for(int i = 1; i<la.length;i++)
									if(la[i].length()>0)
										tr.addIndex( la[i] );
							}
						}
					}
				}
				if(nMeshesN>0)
					bContinue = false;
			}
			System.out.println("Found " +Integer.toString( nMeshesN )+ " meshes");
			
		}
		catch ( FileNotFoundException exc )
		{
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}
		catch ( IOException exc )
		{
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}
		new ImageJ();

		//start BVB
		BigVolumeBrowser bvbTest = new BigVolumeBrowser(); 		
		bvbTest.startBVB("");
		System.out.println("vert_" + Integer.toString(mesh.vertices().size()) );

		System.out.println("tr_"+Integer.toString(  mesh.triangles().size()) );

		System.out.println("maxInd_"+Integer.toString(  tr.nMax) );

		
		final Color meshColor = Color.CYAN;
		
		//let's add an empty volume around it
		RealInterval bunnyInt = Meshes.boundingBox( mesh );
		bvbTest.addRAI(RAIdummy.dummyRAI(bunnyInt));
		MeshColor meshBVB = new MeshColor(mesh);
		meshBVB.setColor(meshColor );
		meshBVB.setPointsRender( 0.3f );
		//meshBVB.setSurfaceRender( VisMeshColor.SURFACE_SHADE );
		meshBVB.setSurfaceRender( VisMeshColor.SURFACE_PLAIN);
		meshBVB.setSurfaceGrid( VisMeshColor.GRID_WIRE);
		meshBVB.setCartesianGrid( 2.0f, 0.1f );
		//and finally add mesh to BVB
		bvbTest.addShape( meshBVB );	
	}
}
