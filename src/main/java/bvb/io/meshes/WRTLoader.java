package bvb.io.meshes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.impl.naive.NaiveFloatMesh;

public class WRTLoader
{
	ArrayList<Mesh> meshes = new ArrayList<>();
	ArrayList<VertexWRT> vertices = new ArrayList<>();
	int nMeshesN = 0;
	
	long nLineN = 0;
	public ArrayList<Mesh> readWRT(String sFilename)
	{
			
		try ( BufferedReader br = new BufferedReader(new FileReader(sFilename));) 
		{
			
			String line = "";
			//String[] la;
			boolean bContinue = true;

			while(bContinue)
			{
				line = br.readLine();
				nLineN ++;
				if(line==null)
					break;

				if(line.contains( " Coordinate {" ) )
				{
					loadVertices(br);
				}

				if(line.contains( " TextureCoordinate" ) )
				{
					loadUV(br);
				}


				if(line.contains( " Normal" ) )
				{
					loadNormals(br);
				}

				if(line.contains( " coordIndex" ) )
				{
					bContinue = loadIndices(br, line);

				}
				
			
			}
			System.out.println("Found " +Integer.toString( nMeshesN )+ " meshes");
			
		}
		catch ( FileNotFoundException exc )
		{
			exc.printStackTrace();
		}
		catch ( IOException exc )
		{
			exc.printStackTrace();
		}
		return meshes;

	}
	
	boolean loadVertices(final BufferedReader br) throws IOException
	{		
		String line = "";
		String[] la;
		nMeshesN ++;
		meshes.add( new NaiveFloatMesh() );
		System.out.println("Mesh "+Integer.toString(nMeshesN));
		vertices.clear();
		line = br.readLine();
		nLineN++;
		la = line.split("\\s+|,");		
		int nVert = 1;
		VertexWRT currV = new VertexWRT ();
		currV.setXYZ( la[3], la[4], la[5]);
		vertices.add( currV );
		boolean bRead = true;
		while(bRead)
		{
			line = br.readLine();
			nLineN++;
			la = line.split("\\s+|,");
			currV = new VertexWRT ();
			currV.setXYZ( la[1], la[2], la[3]);
			vertices.add( currV );

			nVert++;
			if(la.length>4)
				bRead = false;
		}
		System.out.println("Loaded " +Integer.toString( nVert )+ " vertices");
		//bContinue = false;
		return true;
	}
	
	boolean loadUV(final BufferedReader br) throws IOException
	{
		String line = "";
		String[] la;
		line = br.readLine();
		nLineN++;
		la = line.split("\\s+|,");						
		int nVert = 0;

		vertices.get( nVert ).setUV( la[3], la[4]);
		nVert++;
		boolean bRead = true;
		while(bRead)
		{
			line = br.readLine();
			nLineN++;
			la = line.split("\\s+|,");
			vertices.get( nVert ).setUV( la[1], la[2]);
			nVert++;

			if(la.length>3)
				bRead = false;

		}
		//System.out.println("Loaded " +Integer.toString( nVert )+ " texture coordinates");
		return true;

	}
	
	boolean loadNormals(final BufferedReader br) throws IOException
	{		
		String line = "";
		String[] la;
		line = br.readLine();
		nLineN++;
		la = line.split("\\s+|,");

		
		int nVert = 0;

		vertices.get( nVert ).setNXYZ( la[3], la[4], la[5]);
		nVert++;
		boolean bRead = true;
		while(bRead)
		{
			line = br.readLine();
			nLineN++;
			la = line.split("\\s+|,");
			vertices.get( nVert ).setNXYZ( la[1], la[2], la[3]);
			//mesh.vertices().addf( Float.parseFloat( la[1] ), Float.parseFloat( la[2] ), Float.parseFloat( la[3] ) );				
			nVert++;
			if(la.length>4)
				bRead = false;
		}
		//System.out.println("Loaded " +Integer.toString( nVert )+ " normals");

		return true;
	}
	
	boolean loadIndices(final BufferedReader br, String linein) throws IOException
	{
		
		//calculate vertices per primitive (triangles or "squares")
		int nVertPerPrim = TriangleMaker.getVerticesNPerPrimitive(linein);
		String line = "";
		String[] la;
		Mesh currMesh = meshes.get( nMeshesN-1 );
		VertexWRT v;
		for(int i=0;i<vertices.size();i++)
		{
			v = vertices.get( i );
			if(nVertPerPrim ==4)
			{
				currMesh.vertices().addf( v.xyz[0], v.xyz[1], v.xyz[2], 
						-v.nxyz[0], -v.nxyz[1], -v.nxyz[2], v.uv[0], v.uv[1] );
			}
			else
			{
				currMesh.vertices().addf( v.xyz[0], v.xyz[1], v.xyz[2], 
						v.nxyz[0], v.nxyz[1], v.nxyz[2], v.uv[0], v.uv[1] );				
			}
		}

		//System.out.println("SUCCESS2");
		la = linein.split("\\s+|,");
		final TriangleMaker tr = new TriangleMaker(currMesh);
		for(int i = 3; i<la.length;i++)
		{
			if(la[i].length()>0)
			{
				if(!tr.addIndex( la[i] ))
				{
					//int j=10;
				}
			}
		}
		//bContinue = false;
		boolean bRead = true;
		while(bRead)
		{
			line = br.readLine();
			nLineN++;
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
		if(nMeshesN>=1)
			return false;
		return true;
	}
	
}
