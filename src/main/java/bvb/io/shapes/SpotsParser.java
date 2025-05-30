package bvb.io.shapes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
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

	
	@Override
	protected Void doInBackground() throws Exception
	{		
		vertices.clear();
		ArrayList<Float> sizesList = new ArrayList<>();

		
		try ( BufferedReader br = new BufferedReader(new FileReader(fileSpots))) 
		{
			IJ.showStatus( "Importing "+ fileSpots.getName());
			
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
				sizeOut += fScale * Math.abs(finsize);
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
}
