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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import net.imglib2.RealPoint;

import bvb.utils.Misc;
import ij.IJ;

public class SpotsParser extends SwingWorker<Void, String> 
{
	
	public File fileSpots = null;
	
	public boolean bHeader = false;
	
	public String sSeparator = ",";
	
	public int [] nColIndices = null;
	
	public float fScale = 1.0f;
	
	public float fSizeScale = 1.0f;
	
	public boolean parseSize = false;
	
	public boolean parseTime = false;
	
	public boolean parseProperty = false;
	
	final float [] xyz = new float[3];
	
	final float[] size_f = new float[1];
	
	final float[] time_f = new float[1];
	
	final float[] property_f = new float[1];

	final public ArrayList<RealPoint> vertices = new ArrayList<>();
	
	public float [] sizes = null;
	
	public float [] times = null;
	
	public float [] property = null;
	
	ArrayList<Float> timesList = new ArrayList<>();
	
	public long nTotSpots = 0;
	
	public boolean bDataCleanup = false;
	
	public double dPercMin = 1.0;
	
	public double dPercMax = 99.0;

	public boolean bExportCleanData = false;
	
	public boolean [] bCleanupCols = new boolean[5];
	
	public String sExportFilename;
	
	public boolean bSpotsAdded = false;
	
	@Override
    protected void process(List<String> chunks) 
	{
		String message = chunks.get( chunks.size() - 1 );
		if(message.startsWith( "Progress " ))
		{
			IJ.showProgress(Double.parseDouble( message.substring( 9, message.length() )));
		}
		else if (message.startsWith( "Log " ))
		{
			IJ.log( message.substring( 4, message.length() ));
		}
		else
		{
			IJ.showMessage( message );
		}

    }
	
	@Override
	protected Void doInBackground() throws Exception
	{		
		ArrayList<Float> sizesList = new ArrayList<>();
		
		ArrayList<Float> propertyList = new ArrayList<>();
		
		vertices.clear();
		
		try ( BufferedReader br = new BufferedReader(new FileReader(fileSpots))) 
		{
			publish("Importing "+ fileSpots.getName());
			publish("Log Importing "+ fileSpots.getName());
			//file size in bytes
			final double filesize = Files.size( fileSpots.toPath() );
			long bytesRead = 0;
			final long bytesNewLine = Misc.getBytesPerNewLine(fileSpots);
			String line = "";
			String [] la;
			boolean bAllParsedOK;
			//header
			if(bHeader)
			{
				line = br.readLine();
				bytesRead += line.getBytes().length + bytesNewLine;
			}
			while(true)
			{
				line = br.readLine();
				if(line == null)
					break;
				bytesRead += line.getBytes().length + bytesNewLine;
				publish("Progress " + Double.toString( bytesRead/filesize ));

				la = line.split(sSeparator);
				bAllParsedOK = parseCoordinates( la );
				
				if(parseSize)
				{
					bAllParsedOK &= parseSizes( la );						
				}
				if(parseTime)
				{
					bAllParsedOK &= parseColumn(la, 3, "time", time_f);					
				}
				if(parseProperty)
				{
					bAllParsedOK &= parseColumn(la, 7, "property", property_f);					
				}
				if(bAllParsedOK)
				{
					vertices.add( new RealPoint(xyz));
					if(parseSize)
					{
						sizesList.add( new Float(size_f[0] ));					
					}
					if(parseTime)
					{
						timesList.add( new Float(time_f[0] ));					
					}
					if(parseProperty)
					{
						propertyList.add( new Float(property_f[0] ));	
					}
				}
			}
			
		}
		
		if(parseSize)
		{
			sizes = new float[sizesList.size()];
	
			for(int i = 0; i < sizesList.size(); i++)
			{
				sizes[i] = sizesList.get( i );
			}
		}
		
		if(parseTime)
		{
			times = new float[timesList.size()];
			
			for(int i=0; i < timesList.size(); i++)
			{
				times[i] = timesList.get( i );
			}
			
		}
		
		if(parseProperty)
		{
			property = new float[propertyList.size()];
			
			for(int i = 0; i < propertyList.size(); i++)
			{
				property[i] = propertyList.get( i );
			}
			
		}
		
		nTotSpots = vertices.size();
		publish("Log Parsed  " + Long.toString( nTotSpots ) + " spots.");
		if(bDataCleanup)
		{
			dataCleanup();
		}
		return null;
	}
	
	boolean parseCoordinates(final String [] la)
	{
		float coord = 0.0f;
		for(int d = 0; d < 3; d++)
		{
			coord = 0.0f;
			if(nColIndices[d] >= 0)
			{
				if(nColIndices[d] > la.length - 1)
				{
					System.err.println("Spots file import warning: number of columns is wrong.");
					return false;
				}
				
				try
				{
					coord = Float.parseFloat( la[nColIndices[d]] );
					if(Float.isInfinite( coord ))
					{
						System.err.println("Spots file import warning: found infinite coord value, skipping.");
						return false;
					}
					if(Float.isNaN( coord ))
					{
						System.err.println("Spots file import warning: found NaN coord value, skipping.");
						return false;
					}
					
				}
				catch(NumberFormatException e)
				{
					System.err.println("Spots file import warning: failed to parse coordinate.");
					return false;
				}
				
				xyz[d] = fScale * coord;
				//sanity check
				if(Float.isInfinite( xyz[d] ))
				{
					System.err.println("Spots file import warning: found infinite coord value, skipping.");
					return false;
				}
			}
			
		}
		return true;
	}
	
	boolean parseColumn(final String [] la, final int nColIndex, String sColName, final float [] fRecord)
	{
		
		float finVal = 0.0f;
		try
		{
			finVal = Float.parseFloat( la[nColIndices[nColIndex]] );
			if(Float.isInfinite( finVal ))
			{
				System.err.println("Spots file import warning: found infinite "+sColName+" value, skipping.");
				return false;
			}
			if(Float.isNaN( finVal ))
			{
				System.err.println("Spots file import warning: found NaN "+sColName+" value, skipping.");
				return false;
			}
		}
		catch(NumberFormatException e)
		{
			System.err.println("Spots file import warning: failed to parse "+sColName+" column.");
			return false;
		}	
		fRecord[0] = finVal;	
		
		return true;

	}
	
	
	boolean parseSizes(final String [] la)
	{
		float sizeOut = 0.0f;
		int nNum = 0;
		float finsize = 0.0f;
		for(int d = 4; d < 7; d++)
		{
			finsize = 0.0f;
			if(nColIndices[d] >= 0)
			{
				if(nColIndices[d] > la.length-1)
				{
					System.err.println("Spots file import warning: number of columns is wrong.");
					return false;
				}
				try
				{
					finsize = Float.parseFloat( la[nColIndices[d]] );
					if(Float.isInfinite( finsize ))
					{
						System.err.println("Spots file import warning: found infinite size value, skipping.");
						return false;
					}
					if(Float.isNaN( finsize ))
					{
						System.err.println("Spots file import warning: found NaN size value, skipping.");
						return false;
					}
					nNum++;
				}
				catch(NumberFormatException e)
				{
					System.err.println("Spots file import warning: failed to parse size column.");
					return false;
				}
				sizeOut += fScale * Math.abs(finsize)*fSizeScale;
			}
			
		}
		
		//sanity check		
		if(nNum > 0 && Float.isFinite( sizeOut ))
		{
			size_f[0] = sizeOut/nNum;	
		}
		else
		{
			System.err.println("Spots file import warning: found infinite size value, skipping.");
			return false;
		}
		return true;
	}
	
    /*
     * Executed in event dispatching thread
     */
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
    	IJ.showProgress( 1.0 );
    	IJ.showStatus( "Finished spots import. Loaded " + Integer.toString( vertices.size()) + " spots.");   	
    }
    
    /** filters vertices and sizes so that they are between
     * provided percentile min and percentile max **/    
    void dataCleanup()
    {
    	//let's cleanup
    	//load everything to float
    	final int nInN = vertices.size();
    	// 3D coordinates + filtermark
    	int nColN = 4;
    	double dProgressTotal  = 0.0;
    	for(int i = 0; i < 5; i++)
    	{
    		if (bCleanupCols[i])
    			{dProgressTotal++;}
    	}
    	dProgressTotal += 3;
    	double dProgressCurrent = 0.0;
    	int nColSize = 0;
    	int nColTime = 0;
    	int nColProperty = 0;
    	
    	final int indMin = ( int ) Math.max( 0, Math.round( dPercMin*nInN/100. ) );
    	final int indMax = ( int ) Math.min( nInN - 1, Math.round( dPercMax*nInN/100. ) );
	    publish("Cleaning up spots data..");
	    publish("Progress " + Double.toString(  dProgressCurrent/dProgressTotal ));

    	// 3D coordinates + size + filtermark (always last)
    	if(parseSize)
    	{
    		nColSize = nColN - 1;
    		nColN ++;
    	}
    	if(parseTime)
    	{
    		nColTime = nColN - 1;
    		nColN ++;
    	}
    	if(parseProperty)
    	{
    		nColProperty = nColN - 1;
    		nColN ++;
    	}

    	final float [][] allData = new float[nInN][nColN];

    	for(int i = 0; i < nInN; i++)
    	{
    		for(int d = 0; d < 3; d++)
    		{
    			allData[i][d] = vertices.get( i ).getFloatPosition( d );
    		}
    		if(parseSize)
    		{
    			allData[i][nColSize] = sizes[i];
    		}
    		if(parseTime)
    		{
    			allData[i][nColTime] = times[i];
    		}
    		if(parseProperty)
    		{
    			allData[i][nColProperty] = property[i];
    		}
    	}
    	dProgressCurrent++;
    	publish("Progress " + Double.toString( dProgressCurrent/dProgressTotal ));
    	
    	//now let's sort by each column and mark outliers
    	//filter only by user specified columns 
    	int nColLast =  nColN - 1;
    	for(int nCol = 0; nCol < nColLast; nCol++)
    	{
    		boolean bFilter = false;
    		if(nCol < 3 && bCleanupCols[nCol])
    		{
    			bFilter = true;
    		}
        	if(parseSize && nCol == nColSize && bCleanupCols[3])
        	{
        		bFilter = true;
        	}
        	if(parseProperty && nCol == nColProperty && bCleanupCols[4])
        	{
        		bFilter = true;
        	}
    			
    		if(bFilter)
    		{
	        	dProgressCurrent++;
	        	publish("Progress " + Double.toString( dProgressCurrent/dProgressTotal ));
	
	    		final int nColX = nCol;
	    		Arrays.sort(allData, (a, b) -> Float.compare(a[nColX], b[nColX]));
	    		for(int i = 0; i < indMin; i++)
	    		{
	    			allData[i][nColN-1] = 1.0f;
	    		}
	    		for(int i = indMax; i < nInN; i++)
	    		{
	    			allData[i][nColN-1] = 1.0f;
	    		}
    		}
    	}
    	
    	vertices.clear();
    	int nTotFiltCount = 0;
      	dProgressCurrent++;
      	publish("Progress " + Double.toString( dProgressCurrent/dProgressTotal ));

    	for(int i = 0; i < nInN; i++)
    	{
    		if(allData[i][nColN-1]<0.5f)
    		{
    			vertices.add( new RealPoint(allData[i][0], allData[i][1],allData[i][2]) );
    			nTotFiltCount ++;
    		}
    		
    	}
    	
    	if(parseSize)
    	{
    		sizes = new float[nTotFiltCount];
    		int nCount = 0;
        	for(int i = 0; i < nInN; i++)
        	{
        		if(allData[i][nColN-1]<0.5f)
        		{
        			sizes[nCount] = allData[i][nColSize];
        			nCount++;
        		}
        	}
    	}
    	if(parseTime)
    	{
    		times = new float[nTotFiltCount];
    		int nCount = 0;
        	for(int i = 0; i < nInN; i++)
        	{
        		if(allData[i][nColN - 1] < 0.5f)
        		{
        			times[nCount] = allData[i][nColTime];
        			nCount++;
        		}
        	}
    	}
    	if(parseProperty)
    	{
    		times = new float[nTotFiltCount];
    		int nCount = 0;
        	for(int i = 0; i < nInN; i++)
        	{
        		if(allData[i][nColN - 1] < 0.5f)
        		{
        			property[nCount] = allData[i][nColProperty];
        			nCount++;
        		}
        	}
    	}
    	
    	
      	dProgressCurrent++;
      	publish("Progress " + Double.toString( dProgressCurrent/dProgressTotal ));

    	String finOut = "Cleanup done: " +Integer.toString( nTotFiltCount )+" spots left from "+Integer.toString( nInN ); 
    	publish(finOut);
    	publish("Log " + finOut  );
    	nTotSpots = nTotFiltCount;
    	final float fInverseScale = 1.0f/fScale;
    	final float fInverseSizeScale = 1.0f/fSizeScale;
    	String sUnits = "um";
    	if(fInverseScale > 2.0)
    		sUnits = "nm";
    	if(fInverseScale < 0.5)
    		sUnits = "mm";
    	String sSize = "diameter";
    	if(fSizeScale > 5.0f)
    	{
    		sSize  = "SD";
    	}
    	if(fSizeScale < 5.0f && fSizeScale > 1.1f)
    	{
    		sSize  = "radius";
    	}
    	//export cleaned up data
    	if(bExportCleanData)
    	{
    		
    		publish( "Log Exporting cleaned up data to "+ sExportFilename +".");
    		final File file = new File(sExportFilename);
    		publish("Exporting cleaned up spots data..");
    	
			try (FileWriter writer = new FileWriter(file))
			{
				DecimalFormatSymbols symbols = new DecimalFormatSymbols();
				symbols.setDecimalSeparator('.');
				DecimalFormat df3 = new DecimalFormat ("#.######", symbols);
				//let's just write axes
				writer.write( "X("+sUnits+"),Y("+sUnits+"),Z("+sUnits+")" );
				
				
				if(parseSize)
				{
					writer.write(","+sSize+"("+sUnits+")");	
				}
				if(parseTime)
				{
					writer.write(", T");	
				}
				if(parseProperty)
				{
					writer.write(", Property");	
				}
				writer.write("\n");
		    	for(int i = 0; i < nInN;i++)
		    	{
		    		if(allData[i][nColN-1]<0.5f)
		    		{
		    			//coordinates
		    			for(int j = 0; j < 3; j++)
		    			{
		    				writer.write(df3.format(allData[i][j]*fInverseScale));
		    				if(j < 2)
		    				{
		    					writer.write(",");
		    				}
		    			}
		    			if(parseSize)
		    			{
		    				writer.write("," + df3.format(allData[i][nColSize]*fInverseScale*fInverseSizeScale));
		    			}
		    			if(parseTime)
		    			{
		    				writer.write("," + df3.format(allData[i][nColTime]));
		    			}
		    			if(parseProperty)
		    			{
		    				writer.write("," + df3.format(allData[i][nColProperty]));
		    			}
		    			writer.write("\n");
		    		}
		    	}
				writer.close();
			}
			catch ( IOException exc )
			{
				exc.printStackTrace();
			}
    	}
    }
}
