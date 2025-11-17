/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bvb.io.shapes;

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
		if(la.length<10)
		{
			return -1;
		}
		if(la[9].equals( "-1" ))
			return 3;
		
		if(la.length<12)
		{
			return -1;
		}
		if(la[11].equals( "-1" ))
			return 4;
		
		return -1;
			
		
	}
	public int[] addIndex(String sInd)
	{
		int nInd = -2;
		try
		{
			nInd = Integer.parseInt(sInd);
		}
		catch(NumberFormatException e)
		{
			return null;
		}
		
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
