package bvb.develop;

import net.imglib2.mesh.Mesh;

public class TriangleMaker
{
	final Mesh mesh;
	final int[] indices;
	int nCurrent;
	public int nMax = 0;
	
	public TriangleMaker( final Mesh mesh)
	{
		this.mesh = mesh;
		nCurrent = 0;
		indices = new int [4];
	}
	
	public boolean addIndex(String sInd)
	{
		
		int nInd = Integer.parseInt(sInd);
		if(nInd >nMax)
			nMax = nInd;
		if(nInd == -1)
		{
			if(nCurrent>4 && nCurrent<3)
			{				
				System.out.println("Something wrong with triangles");
				return false;
			}
			dropTriangles();
			nCurrent = 0;
		}
		else
		{
			indices[nCurrent] = nInd;
			nCurrent++;
		}
		return true;
	}
	
	void dropTriangles()
	{
		if(nCurrent == 3)
		{
			//counter clock-wise
			mesh.triangles().addf( indices[0], indices[2], indices[1]);
		}

		if(nCurrent == 4)
		{
			//counter clock-wise
			mesh.triangles().addf( indices[0], indices[2], indices[1]);
			mesh.triangles().addf( indices[0], indices[3], indices[2]);
		}
		
//		mesh.triangles().addf( indices[0], indices[1], indices[2]);
//		mesh.triangles().addf( indices[0], indices[2], indices[3]);
	}
}
