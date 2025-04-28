package bvb.io.meshes;

import net.imglib2.mesh.Mesh;

public class TriangleMaker
{
	final Mesh mesh;
	final int[] indices;
	int nCurrent;
	//public int nNumVertPerPoly = 0;
	
	public TriangleMaker( final Mesh mesh)
	{
		this.mesh = mesh;
		nCurrent = 0;
		indices = new int [4];
	}
	public static int getVerticesNPerPrimitive(String linein)
	{
		String[] la;
		la = linein.split("\\s+|,");
		if(la[9].equals( "-1" ))
			return 3;
		return 4;
		
	}
	public int[] addIndex(String sInd)
	{
		
		int nInd = Integer.parseInt(sInd);

		if(nInd == -1)
		{
			if(nCurrent>4 && nCurrent<3)
			{				
				System.out.println("Something wrong with triangles");
				return null;
			}
			dropTriangles();
			nCurrent = 0;
			return indices;
		}
		indices[nCurrent] = nInd;
		nCurrent++;
		return null;

	}
	
	void dropTriangles()
	{
		if(nCurrent == 3)
		{
			//counter clock-wise
			//mesh.triangles().addf( indices[0], indices[2], indices[1]);
		}

		if(nCurrent == 4)
		{
			//counter clock-wise

//			mesh.triangles().addf( indices[0], indices[2], indices[1]);
	//		mesh.triangles().addf( indices[0], indices[3], indices[2]);

		}
		
//		mesh.triangles().addf( indices[0], indices[1], indices[2]);
//		mesh.triangles().addf( indices[0], indices[2], indices[3]);
	}
}
