package bvb.io.shapes;

import com.github.mreutegg.laszip4j.LASPoint;
import com.github.mreutegg.laszip4j.LASReader;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import net.imglib2.RealPoint;

import bvb.scene.VisSpots;
import bvb.shapes.Spots;
import ij.IJ;

public class LASImport extends SwingWorker<Void, Void> 
{
	public File filein = null;
	
	public float fPointSize = 0.0f;

	public Spots spotsLAS = null;
	
	int nCount = 0;
	
	public boolean bSpotsRead = false;
	
	@Override
	protected Void doInBackground() throws Exception
	{
		final LASReader reader = new LASReader(filein);
		final ArrayList<RealPoint> vertices = new ArrayList<>();
		spotsLAS = new Spots(fPointSize, new Color(255,0,0,255), VisSpots.SHAPE_ROUND, VisSpots.RENDER_FILLED);
		
		long nRecords = reader.getHeader().getNumberOfPointRecords();
		
		int nMaxRecords = (int)nRecords;
		//System.out.println(nRecords);
		float [] colors = new float[(nMaxRecords)*4];
		for (LASPoint p : reader.getPoints()) 
	    {
	    	if(nCount >= nMaxRecords)
	    		break;
	        // read coordinates from point
			vertices.add( new RealPoint(new double[] {p.getX(), p.getY(), p.getZ()}));
			
			colors[nCount*4] = p.getRed()/255f;
			colors[nCount*4+1] = p.getGreen()/255f;
			colors[nCount*4+2] = p.getBlue()/255f;
			colors[nCount*4+3] = 1.0f;
	    	nCount++;
	    	IJ.showProgress( (double)nCount/((double)nMaxRecords) );

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
