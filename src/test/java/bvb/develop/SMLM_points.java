package bvb.develop;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import net.imglib2.RealPoint;

import bvb.core.BigVolumeBrowser;
import bvb.scene.VisSpotsSame;
import bvb.shapes.SpotsSame;
import ij.ImageJ;

public class SMLM_points
{
	public static void main( final String[] args )
	{
		new ImageJ();
		BigVolumeBrowser bvbTest = new BigVolumeBrowser(); 		
		bvbTest.startBVB("");
//		String sFilename = "/home/eugene/Desktop/projects/BVB/SMLM/Z_Tr_Results_2022-09-27_LLSmotorPAINT_Jurkat7min_SNAP1.5_s2_astig_LP100_ex60ms_23.csv";
		String sFilename = "/home/eugene/Desktop/projects/BVB/SMLM/2025-04-10-09-54LUNAR_LL_predictions_assembly_smap_filtered.csv";

		final ArrayList<RealPoint> vertices = new ArrayList<>();
		
		try ( BufferedReader br = new BufferedReader(new FileReader(sFilename));
				) 
		{
			String line = "";
			String [] la;
			//header
			line = br.readLine();
			boolean bContinue = true;
			
			while(bContinue)
			{
				line = br.readLine();
				if(line == null)
					break;
				la = line.split(",");	
//				la = la[0].split(",");	

				//System.out.println(la[0]);
				//vertices.add(DoMToRP(la) );
				vertices.add(somethingToRP(la) );
			}
			
			
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
		
		SpotsSame testPoints = new SpotsSame(20.0f, Color.WHITE, VisSpotsSame.SHAPE_ROUND, VisSpotsSame.RENDER_GAUSS);
		testPoints.setPoints( vertices );
		System.out.println( vertices.size() );
		bvbTest.addShape( testPoints );
		bvbTest.focusOnRealInterval( testPoints.boundingBox() );
		
		
	}
	
	public static RealPoint DoMToRP(final String [] la)
	{
		return new RealPoint(Float.parseFloat( la[4] )-5000.0f,
							 Float.parseFloat( la[6] )-35000.0f,
							 Float.parseFloat( la[8] ));
	}
	public static RealPoint somethingToRP(final String [] la)
	{
		return new RealPoint(Float.parseFloat( la[0] ),
							 Float.parseFloat( la[1] ),
							 Float.parseFloat( la[2] ));
	}
}
