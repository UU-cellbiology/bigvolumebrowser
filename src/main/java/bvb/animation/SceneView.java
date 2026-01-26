package bvb.animation;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import net.imglib2.realtransform.AffineTransform3D;

import bvvpg.core.VolumeViewerPanel;

/** class stores current viewer transform and the timepoint **/
public class SceneView
{
	final AffineTransform3D viewerTransform;
	
	int nTimeFrame;
	
	public SceneView(final AffineTransform3D viewerTransform_, final int nTimeFrame_ )
	{
		viewerTransform = new AffineTransform3D();
		viewerTransform.set( viewerTransform_ );
		nTimeFrame = nTimeFrame_;
	}
	public SceneView()
	{
		viewerTransform = new AffineTransform3D();
	}
	
	public AffineTransform3D getViewerTransform()
	{
		return viewerTransform;
	}
	
	public void setViewerTransform( final double... values )
	{
		viewerTransform.set( values );
	}
	
	
	public int getTimeFrame()
	{
		return nTimeFrame;
	}
	
	public void setTimeFrame(int nTimeFrame_)
	{
		nTimeFrame = nTimeFrame_;
		return;
	}

	public void save(final FileWriter writer)
	{
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		DecimalFormat df3 = new DecimalFormat ("#.#######", symbols);
		try
		{
			writer.write("TimePoint," + Integer.toString(nTimeFrame) + "\n");
			writer.write("ViewTransform");
			final double [] transform = new double [12];
			viewerTransform.toArray(transform);
			for (int m = 0; m<12; m++)
			{
				writer.write("," + df3.format(transform[m]));
			}
			writer.write("\n");
		
			
		}
		catch ( IOException exc )
		{
			exc.printStackTrace();
		}
		
	}
	
	public void load(final BufferedReader br) throws IOException
	{
		String[] line_array;
		String line;
		//time frame
		line = br.readLine();
		line_array = line.split(",");
		this.setTimeFrame(  Integer.parseInt( line_array[ 1 ] ) );
		//ViewTransform
		line = br.readLine();
		line_array = line.split(",");
		final double [] transform = new double [ 12 ];
		for(int m = 0; m < 12; m++)
		{
			transform[ m ] = Double.parseDouble( line_array[ m + 1 ] );
		}
		this.setViewerTransform( transform );
	}
	
	public static SceneView getCurrentSceneView(final VolumeViewerPanel viewer)
	{
		final AffineTransform3D transform = new AffineTransform3D();
		viewer.state().getViewerTransform(transform);
		int canvasW = viewer.getWidth();
		int canvasH = viewer.getHeight();
		transform.set( transform.get( 0, 3 ) - canvasW * 0.5, 0, 3 );
		transform.set( transform.get( 1, 3 ) - canvasH * 0.5, 1, 3 );
		transform.scale( 1.0/ canvasW );
		return new SceneView(transform, viewer.state().getCurrentTimepoint());
	}
	
	public static void setSceneView(final VolumeViewerPanel viewer, final SceneView scene)
	{
		final AffineTransform3D affine = new AffineTransform3D();
		affine.set( scene.getViewerTransform());
		final int width = viewer.getWidth();
		final int height = viewer.getHeight();
		affine.scale( width );
		affine.set( affine.get( 0, 3 ) + width  * 0.5 , 0, 3 );
		affine.set( affine.get( 1, 3 ) + height * 0.5, 1, 3 );
		viewer.state().setViewerTransform( affine );
		final int nTimePoint = scene.getTimeFrame();
		if(nTimePoint < viewer.state().getNumTimepoints())
		{
			viewer.state().setCurrentTimepoint(nTimePoint);
		}
	}
}
