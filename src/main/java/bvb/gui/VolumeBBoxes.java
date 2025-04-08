package bvb.gui;

import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;

import org.joml.Matrix4fc;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bvb.core.BigVolumeBrowser;
import bvb.shapes.Shape;
import bvb.shapes.VolumeBox;
import bvb.utils.Misc;


public class VolumeBBoxes implements Shape
{
	final ArrayList <VolumeBox> volumeBoxes = new ArrayList<>();
	
	final BigVolumeBrowser bvb;
	
	private final Map < Source< ? >, FinalRealInterval > bvvSourceToBox;
	
	private boolean bVisible = false;
	
	private boolean bLocked = false;
	
	private float lineThickness = 1.0f;
	
	private Color lineColor = Color.WHITE;
	
	public VolumeBBoxes(final BigVolumeBrowser bvb_)
	{
		bvb = bvb_;
		
		bvvSourceToBox =  new HashMap<>();
	}

	@Override
	public void draw( GL3 gl, Matrix4fc pvm, Matrix4fc vm, int[] screen_size )
	{
		if(bVisible)
		{
			while(bLocked)
			{
				try
				{
					Thread.sleep(100);
				}
				catch ( InterruptedException exc )
				{
					exc.printStackTrace();
				}
			}
			bLocked = true;
			for(VolumeBox vBox :volumeBoxes)
			{
				vBox.draw( gl, pvm, vm, screen_size );
			}
			bLocked = false;
		}
		
	}
	
	public void setVisible (boolean bVisible_)
	{
		bVisible = bVisible_;
	}
	
	public void updateVolumeBoxes()
	{
		while(bLocked)
		{
			try
			{
				Thread.sleep(100);
			}
			catch ( InterruptedException exc )
			{
				exc.printStackTrace();
			}
		}
		bLocked = true;
		final List< SourceAndConverter< ? > > sources = bvb.bvvViewer.state().getSources();
		volumeBoxes.clear();
		for(SourceAndConverter< ? > srcConv : sources)
		{
			final Source< ? > src = srcConv.getSpimSource();
			final FinalRealInterval srcInt = Misc.getSourceBoundingBox(src,bvb.bvvViewer.state().getCurrentTimepoint(),0);
			final FinalRealInterval currInt = bvvSourceToBox.get( src );
			if(currInt == null)
			{
				bvvSourceToBox.put( src, srcInt );
			}
			else
			{
				if(!currInt.equals( srcInt ))
				{
					bvvSourceToBox.put( src, srcInt );
				}
			}
		}
		bvvSourceToBox.forEach( (src, interval)-> {
			volumeBoxes.add( new VolumeBox(interval,lineThickness, lineColor));
		});
		bLocked = false;
	}
}
