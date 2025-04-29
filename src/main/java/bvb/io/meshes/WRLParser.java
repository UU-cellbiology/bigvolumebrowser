package bvb.io.meshes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.impl.naive.NaiveFloatMesh;

public class WRLParser
{
	final ArrayList<Mesh> meshes = new ArrayList<>();
	final ArrayList<VertexWRL> vertices = new ArrayList<>();
	int nMeshesN = 0;
	int newVindex;
	int nVertPerPrim;
	long nLineN = 0;
	int nTimePoint = -1;
	boolean bTimeData = false;
	
	public boolean bEnableWireGrid = true;	
	public int nMaxMeshes = Integer.MAX_VALUE;
	public int nMaxTimePoints = Integer.MAX_VALUE;	
	
	Map<Integer,Integer> addedInd = new ConcurrentHashMap<>();
	
	public ArrayList<Mesh> readWRL(String sFilename)
	{
		

		nMeshesN = 0;

		nLineN = 0;
		nTimePoint = -1;
		bTimeData = false;
		try ( BufferedReader br = new BufferedReader(new FileReader(sFilename));) 
		{
			
			String line = "";
			boolean bContinue = true;

			while(bContinue)
			{
				line = br.readLine();
				nLineN ++;
				if(line==null)
					break;
				if(line.contains( " TimeSwitch" ) )
				{
					bTimeData = true;
				}
				
				
				if(line.contains( " Coordinate {" ) )
				{
					System.out.println( nLineN );
					loadVertices(br);
					if(bTimeData)
					{
						nTimePoint++;
						if(nMaxTimePoints == nTimePoint)
						{
							bContinue = false;
						}
					}
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
					loadIndices(br, line);

				}
				
				if(nMeshesN>nMaxMeshes )
					bContinue = false;
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

		System.out.println("Mesh "+Integer.toString(nMeshesN));
		vertices.clear();
		line = br.readLine();
		nLineN++;
		la = line.split("\\s+|,");		
//		int nVert = 1;
		VertexWRL currV = new VertexWRL ();
		currV.setXYZ( la[3], la[4], la[5]);
		vertices.add( currV );
		boolean bRead = true;
		while(bRead)
		{
			line = br.readLine();
			nLineN++;
			la = line.split("\\s+|,");
			currV = new VertexWRL ();
			currV.setXYZ( la[1], la[2], la[3]);
			vertices.add( currV );

//			nVert++;
			if(la.length>4)
				bRead = false;
		}
//		System.out.println("Loaded " +Integer.toString( nVert )+ " vertices");

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
			nVert++;
			if(la.length>4)
				bRead = false;
		}
		//System.out.println("Loaded " +Integer.toString( nVert )+ " normals");

		return true;
	}
	
	boolean loadIndices(final BufferedReader br, String linein) throws IOException
	{
		
		//calculate vertices per primitive (triangles or quads)
		nVertPerPrim = TriangleMaker.getVerticesNPerPrimitive(linein);
		nMeshesN ++;
		final Mesh currMesh = new NaiveFloatMesh();
		meshes.add( currMesh );
		String line = "";
		String[] la;

		addedInd.clear();

		newVindex = 0;

		la = linein.split("\\s+|,");
		final TriangleMaker tr = new TriangleMaker(currMesh);
		for(int i = 3; i<la.length;i++)
		{
			if(la[i].length()>0)
			{
				final int [] currInd = tr.addIndex( la[i] );
				if(currInd != null)
				{
					addIndices(currMesh, currInd);					
				}
			}
		}

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
					{
						final int [] currInd = tr.addIndex( la[i] );
						if(currInd != null)
						{
							addIndices(currMesh, currInd);
							
						}
					}
			}
		}
		
//		System.out.println("in vertN "+Integer.toString( addedInd.size() ));
//		System.out.println("added vertN "+Integer.toString( currMesh.vertices().size()-addedInd.size() ));
//		System.out.println(addedInd.size() );

		return true;
	}
	
	public void addIndices(final Mesh currMesh, final int [] currInd)
	{
		if(nVertPerPrim == 3)
		{
			addTriangle( currMesh, currInd);
		}
		else
		{
			final int[] tri1 = new int [] {currInd[0],currInd[2],currInd[1]};
			final int[] tri2 = new int [] {currInd[0],currInd[3],currInd[2]};
			addTriangle( currMesh, tri1);
			addTriangle( currMesh, tri2);
		}
	}
	
	public void addTriangle(final Mesh currMesh, final int [] currInd)
	{

		for(int k=0;k<3; k++)
		{
			
			if(addedInd.get(currInd[k]) == null)
			{
				addedInd.put( currInd[k], newVindex);
				addVertex(currMesh,vertices.get( currInd[k] ));
				currInd[k] = newVindex;
				newVindex++;

			}
			else
			{				
				//separate vertices per triangle,
				//allows wireframe mesh render 
				//but loads more data (adds extra vertices)
				if(bEnableWireGrid)
				{
					addVertex(currMesh,vertices.get( currInd[k] ));
					currInd[k] = newVindex;
					newVindex++;
				}
				//shared vertices per triangle,
				//does not always allow wireframe mesh render
				else
				{
					currInd[k] = addedInd.get(currInd[k]).intValue();
				}
			}
		}
		currMesh.triangles().addf( currInd[0], currInd[1], currInd[2]);
		
	}
	
	void addVertex(final Mesh currMesh, final VertexWRL v)
	{
		if(nVertPerPrim == 4)
		{
			addVertNegNormal(currMesh,v);
		}
		else
		{
			addVertPosNormal(currMesh,v);
		}
	}
	
	static void addVertPosNormal(final Mesh mesh, final VertexWRL v)
	{
		mesh.vertices().addf( v.xyz[0], v.xyz[1], v.xyz[2], 
				v.nxyz[0], v.nxyz[1], v.nxyz[2], v.uv[0], v.uv[1] );
	}
	static void addVertNegNormal(final Mesh mesh, final VertexWRL v)
	{
		mesh.vertices().addf( v.xyz[0], v.xyz[1], v.xyz[2], 
				-v.nxyz[0], -v.nxyz[1], -v.nxyz[2], v.uv[0], v.uv[1] );
	}
	
}
