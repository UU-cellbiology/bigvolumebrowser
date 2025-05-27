package bvb.io.shapes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import net.imglib2.RealPoint;

import ij.IJ;

public class SpotsParser extends SwingWorker<Void, Void> 
{
	public File fileSpots = null;
	
	public boolean bHeader = false;
	
	public String sSeparator = ",";
	
	public int [] nColIndices = null;
	
	public float fScale = 1.0f;
	
	final float [] xyz = new float[3];

	final public ArrayList<RealPoint> vertices = new ArrayList<>();
	
	@Override
	protected Void doInBackground() throws Exception
	{		
		vertices.clear();
		
		try ( BufferedReader br = new BufferedReader(new FileReader(fileSpots))) 
		{
			IJ.showStatus( "Importing "+ fileSpots.getName());
			//file size in bytes
			final double filesize = Files.size( fileSpots.toPath() );
			long bytesRead = 0;
			final long bytesNewLine = getBytesPerNewLine();
			String line = "";
			String [] la;
			
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
				vertices.add(parseCoordinates(la) );
			}
					
			return null;
			
		}
	}
	
	int getBytesPerNewLine() throws FileNotFoundException, IOException
	{
		String line1 = "";
		String line2 = "";
		int nCount = 0;
		//read two lines
		try ( BufferedReader br = new BufferedReader(new FileReader(fileSpots))) 
		{
			line1 = br.readLine();
			line2 = br.readLine();

		}

		char [] cFirst = new char[1];
		line2.getChars( 0, 1, cFirst, 0 );
		try ( BufferedReader br = new BufferedReader(new FileReader(fileSpots))) 
		{
			char [] cbuf = new char [line1.length()];
			
			br.read( cbuf, 0, line1.length());
			cbuf = new char[1];
			char val = (char)br.read();
			while(val != cFirst[0])
			{
				val = (char)br.read();
				nCount++;
			}
		}
		return nCount;
	}
	
	RealPoint parseCoordinates(final String [] la)
	{
		for(int d=0;d<3;d++)
		{
			if(nColIndices[d] >= 0)
				xyz[d] = fScale * Float.parseFloat( la[nColIndices[d]] );
			
		}
		return new RealPoint(xyz);
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
