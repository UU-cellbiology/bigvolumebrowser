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
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import net.imglib2.RealPoint;

import bvb.utils.Misc;
import ij.IJ;

public class SpotsParser extends SwingWorker<Void, Void> 
{
	public File fileSpots = null;
	
	public boolean bHeader = false;
	
	public String sSeparator = ",";
	
	public int [] nColIndices = null;
	
	public float fScale = 1.0f;
	
	public float fSizeScale = 1.0f;
	
	public boolean parseSize = false;
	
	final float [] xyz = new float[3];
	
	final float[] size_f = new float[1];

	final public ArrayList<RealPoint> vertices = new ArrayList<>();
	
	public float [] sizes = null;
	
	public long nTotSpots = 0;
	
	public boolean bDataCleanup = false;
	
	public double dPercMin = 1.0;
	
	public double dPercMax = 99.0;

	public boolean bExportCleanData = false;
	
	public String sExportFilename;
	
	public boolean bSpotsAdded = false;
	
	
	@Override
	protected Void doInBackground() throws Exception
	{		
		vertices.clear();
		ArrayList<Float> sizesList = new ArrayList<>();

		
		try ( BufferedReader br = new BufferedReader(new FileReader(fileSpots))) 
		{
			IJ.showStatus( "Importing "+ fileSpots.getName());
			IJ.log("Importing "+ fileSpots.getName());
			//file size in bytes
			final double filesize = Files.size( fileSpots.toPath() );
			long bytesRead = 0;
			final long bytesNewLine = Misc.getBytesPerNewLine(fileSpots);
			String line = "";
			String [] la;
			boolean bCoordinateParsedOK;
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
//				try {
//					Thread.sleep(1);
//				} catch (InterruptedException ignore) {}
				IJ.showProgress( bytesRead/filesize );
				la = line.split(sSeparator);
				bCoordinateParsedOK = parseCoordinates( la );
				if( bCoordinateParsedOK && !parseSize)
				{
					vertices.add( new RealPoint(xyz));					
				}
				//make sure we add spot only if sizes were parsed ok
				if(bCoordinateParsedOK && parseSize)
				{
					if( parseSizes( la ) )
					{
						vertices.add( new RealPoint(xyz));	
						sizesList.add( new Float(size_f[0] ));					
					}
					
				}
			}
			
		}
		sizes = new float[sizesList.size()];
		for(int i=0; i<sizesList.size(); i++)
		{
			sizes[i] = sizesList.get( i );
		}
		
		nTotSpots = vertices.size();
		
		IJ.log( "Parsed  " +Long.toString( nTotSpots )+" spots.");
		if(bDataCleanup)
		{
			dataCleanup();
		}
		return null;
	}
	
	boolean parseCoordinates(final String [] la)
	{
		float coord = 0.0f;
		for(int d=0;d<3;d++)
		{
			coord = 0.0f;
			if(nColIndices[d] >= 0)
			{
				if(nColIndices[d]>la.length-1)
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
				if(Float.isInfinite( xyz[d]))
				{
					System.err.println("Spots file import warning: found infinite coord value, skipping.");
					return false;
				}
			}
			
		}
		return true;
	}
	
	boolean parseSizes(final String [] la)
	{
		float sizeOut = 0.0f;
		int nNum = 0;
		float finsize = 0.0f;
		for(int d=3; d<6; d++)
		{
			finsize = 0.0f;
			if(nColIndices[d] >= 0)
			{
				if(nColIndices[d]>la.length-1)
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
     		System.err.println("Error spots import: "+ e.toString() );
     	}
    	IJ.showProgress( 1.0 );
    	IJ.showStatus( "Finished spots import. Loaded "+Integer.toString( vertices.size()) +" spots.");   	
    }
    /** filters vertices and sizes so that they are between
     * provided percentile min and percentile max **/
    
    void dataCleanup()
    {
    	//let's cleanup
    	//load everything to float
    	final int nInN = vertices.size();
    	int nColN = 4;
    	double dProgressTotal = (nColN-1)+3;
    	double dProgressCurrent = 0.0;
    	
    	final int indMin = ( int ) Math.max( 0, Math.round( dPercMin*nInN/100. ) );
    	final int indMax = ( int ) Math.min( nInN-1, Math.round( dPercMax*nInN/100. ) );
    	IJ.showStatus( "Cleaning up spots data..");
    	IJ.showProgress( dProgressCurrent/dProgressTotal);
    	
    	if(parseSize)
    	{
    		nColN = 5;
    	}

    	final float [][] allData = new float[nInN][nColN];

    	for(int i = 0; i< nInN; i++)
    	{
    		for(int d=0;d<3;d++)
    		{
    			allData[i][d] = vertices.get( i ).getFloatPosition( d );
    		}
    		if(parseSize)
    		{
    			allData[i][3] = sizes[i];
    		}
    	}
    	dProgressCurrent++;
    	IJ.showProgress( dProgressCurrent/dProgressTotal);
    	
    	//now let's sort by each column and mark outliers
    	for(int nCol = 0; nCol<nColN-1; nCol++)
    	{
        	dProgressCurrent++;
        	IJ.showProgress( dProgressCurrent/dProgressTotal);

    		final int nColX = nCol;
    		Arrays.sort(allData, (a, b) -> Float.compare(a[nColX], b[nColX]));
    		for(int i=0;i<indMin; i++)
    		{
    			allData[i][nColN-1] = 1.0f;
    		}
    		for(int i=indMax;i<nInN; i++)
    		{
    			allData[i][nColN-1] = 1.0f;
    		}
    	}
    	
    	vertices.clear();
    	int nTotFiltCount = 0;
      	dProgressCurrent++;
    	IJ.showProgress( dProgressCurrent/dProgressTotal);
    	for(int i=0; i<nInN;i++)
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
        	for(int i=0; i<nInN;i++)
        	{
        		if(allData[i][nColN-1]<0.5f)
        		{
        			sizes[nCount] = allData[i][3];
        			nCount++;
        		}
        	}
    	}
      	dProgressCurrent++;
    	IJ.showProgress( dProgressCurrent/dProgressTotal);
    	String finOut = "Cleanup done: " +Integer.toString( nTotFiltCount )+" spots left from "+Integer.toString( nInN ); 
    	IJ.showStatus( finOut );
    	IJ.log( finOut  );
    	nTotSpots = nTotFiltCount;
    	final float fInverseScale = 1.0f/fScale;
    	final float fInverseSizeScale = 1.0f/fSizeScale;
    	String sUnits = "um";
    	if(fInverseScale>2.0)
    		sUnits = "nm";
    	if(fInverseScale<0.5)
    		sUnits = "mm";
    	String sSize = "diameter";
    	if(fSizeScale>5.0f)
    	{
    		sSize  = "SD";
    	}
    	if(fSizeScale<5.0f && fSizeScale>1.1f)
    	{
    		sSize  = "radius";
    	}
    	//export cleaned up data
    	if(bExportCleanData)
    	{
    		IJ.log( "Exporting cleaned up data to "+ sExportFilename +".");
    		final File file = new File(sExportFilename);
    		IJ.showStatus( "Exporting cleaned up spots data..");
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
				writer.write("\n");
		    	for(int i=0; i<nInN;i++)
		    	{
		    		if(allData[i][nColN-1]<0.5f)
		    		{
		    			for(int j=0;j<nColN-2;j++)
		    			{
		    				writer.write(df3.format(allData[i][j]*fInverseScale) + ",");
		    			}
		    			if(parseSize)
		    				writer.write(df3.format(allData[i][nColN-2]*fInverseScale*fInverseSizeScale) + "\n");
		    			else
		    				writer.write(df3.format(allData[i][nColN-2]*fInverseScale) + "\n");
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
