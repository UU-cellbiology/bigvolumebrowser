package bvb.develop;

import net.imglib2.mesh.Mesh;

public class TriangleMaker
{
	final Mesh mesh;
	final int[] indices;
	int nCurrent;
	public int nMax =0;
	
	public TriangleMaker( final Mesh mesh)
	{
		this.mesh = mesh;
		nCurrent = 0;
		indices = new int [4];
	}
	
	public void addIndex(String sInd)
	{
		if(sInd.equals( "]" ))
		{
			int dd =10;
		}
		
		int nInd = Integer.parseInt(sInd);
		if(nInd >nMax)
			nMax = nInd;
		if(nInd == -1)
		{
			if(nCurrent!=4)
				System.out.println("Something wrong with triangles");
			dropTriangles();
			nCurrent = 0;
		}
		else
		{
			indices[nCurrent] = nInd;
			nCurrent++;
		}
	}
	
	void dropTriangles()
	{
		//counter clock-wise
		mesh.triangles().addf( indices[0], indices[2], indices[1]);
		mesh.triangles().addf( indices[0], indices[3], indices[2]);
	}
}
