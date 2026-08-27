/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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

import com.github.mreutegg.laszip4j.LASPoint;
import com.github.mreutegg.laszip4j.LASReader;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import net.imglib2.RealPoint;

import bvb.scene.VisSpots;
import bvb.shapes.Spots;
import ij.IJ;

public class LASImport extends SwingWorker<Void, String> 
{
	public File filein = null;
	
	public float fPointSize = 0.0f;

	public Spots spotsLAS = null;
	
	int nCount = 0;
	
	public boolean bSpotsRead = false;
	
	@Override
    protected void process(List<String> chunks) 
	{
		String message = chunks.get( chunks.size() - 1 );
		if(message.startsWith( "Progress " ))
		{
			IJ.showProgress(Double.parseDouble( message.substring( 9, message.length() )));
		}

    }
	
	@Override
	protected Void doInBackground() throws Exception
	{
		final LASReader reader = new LASReader(filein);
		
		final ArrayList<RealPoint> vertices = new ArrayList<>();
		
		spotsLAS = new Spots(fPointSize, new Color(255, 0, 0, 255), VisSpots.SHAPE_ROUND, VisSpots.RENDER_FILLED);
		
		long nRecords = reader.getHeader().getNumberOfPointRecords();
		
		int nMaxRecords = (int)nRecords;
		//System.out.println(nRecords);
		float [] colors = new float[(nMaxRecords) * 4];
		for (LASPoint p : reader.getPoints()) 
	    {
	    	if(nCount >= nMaxRecords)
	    		break;
	        // read coordinates from point
			vertices.add( new RealPoint(new double[] {p.getX(), p.getY(), p.getZ()}));
			
			colors[nCount * 4] = p.getRed()/255f;
			colors[nCount * 4 + 1] = p.getGreen()/255f;
			colors[nCount * 4 + 2] = p.getBlue()/255f;
			colors[nCount * 4 + 3] = 1.0f;
	    	nCount++;
	    	publish("Progress " + Double.toString( (double)nCount/((double)nMaxRecords)));
	    	//IJ.showProgress(  );

	    }

	    spotsLAS.setPoints( vertices, null, null );
	    spotsLAS.setColors( colors );
	    return null;
	}
	
    @Override
    public void done() 
    {
    	try
		{
			get();
		}
		catch ( InterruptedException | ExecutionException exc )
		{
			exc.printStackTrace();
		}
     	catch (Exception e)
     	{
     		System.err.println("Error spots import: " + e.toString() );
     	}
    	
    	IJ.log("Loaded " + nCount +" points from "+ filein.getName());
    	IJ.showProgress( 1.0 );
    }
}
