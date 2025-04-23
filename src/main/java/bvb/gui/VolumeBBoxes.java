package bvb.gui;

import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import org.joml.Matrix4fc;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bvb.core.BigVolumeBrowser;
import bvb.shapes.Shape;
import bvb.shapes.VolumeBox;
import bvb.utils.Misc;
import bvvpg.source.converters.GammaConverterSetup;


public class VolumeBBoxes implements Shape
{
	
	final BigVolumeBrowser bvb;
	
	private final Map < SourceAndConverter< ? >, VolumeBox > bvvSourceToBox;
	
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
			bvvSourceToBox.forEach( (sac, vbox)-> {
				if(bvb.bvvViewer.state().isSourceVisible( sac ))
				{
					if(vbox!=null)
					{
						vbox.draw( gl, pvm, vm, screen_size );
					}
				}
			});

			bLocked = false;
		}
		
	}
	
	public void setLineColor(final Color color)
	{
		lineColor = new Color(color.getRed(),color.getGreen(),color.getBlue(),color.getAlpha());
		
		bvvSourceToBox.forEach( (src, vbox)-> {
			vbox.setLineColor( lineColor );
		});
		
	}
	
	public void setLineThickness(final float fThickness)
	{
		lineThickness = fThickness;
		bvvSourceToBox.forEach( (src, vbox)-> {
			vbox.setLineThickness( lineThickness );
		});		
	}
	
	public void setVisible (boolean bVisible_)
	{
		bVisible = bVisible_;
	}
	
	public synchronized void updateVolumeBoxes()
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
		
		final int nTimePoint = bvb.bvvViewer.state().getCurrentTimepoint();
		List< SourceAndConverter< ? > > sacList = bvb.bvvViewer.state().getSources();
		
		for(SourceAndConverter< ? > sac : sacList )
		{
			final Source< ? > src = sac.getSpimSource();
			if(src.isPresent( nTimePoint ))
			{
				final FinalRealInterval srcInt = Misc.getSourceBoundingBox(src,nTimePoint,0);
				final VolumeBox currBox = bvvSourceToBox.get( sac );
				if(currBox == null)
				{
					bvvSourceToBox.put( sac, new VolumeBox(srcInt, null, lineThickness, lineColor) );
				}
				else
				{
					if(!currBox.interval.equals( srcInt ))
					{
						currBox.setInterval( srcInt );
					}
				}
			}
			else
			{
				bvvSourceToBox.remove( sac );
			}
		}

		bLocked = false;
	}
	
	public synchronized void updateClipBoxes()
	{
		if(!bVisible)
			return;
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
		
		
		List< SourceAndConverter< ? > > sacList = bvb.bvvViewer.state().getSources();
		
		for(SourceAndConverter< ? > sac : sacList )
		{
			GammaConverterSetup cs = (GammaConverterSetup)bvb.bvvHandle.getConverterSetups().getConverterSetup( sac );
	
			final VolumeBox currBox = bvvSourceToBox.get( sac );
			
			if(cs.clipActive())
			{
				final AffineTransform3D transform = new AffineTransform3D();
				FinalRealInterval interval = cs.getClipInterval();
				if(interval == null)
					break;
				cs.getClipTransform( transform );
				if(currBox == null)
				{
					cs.getClipTransform( transform );
					bvvSourceToBox.put( sac, new VolumeBox(cs.getClipInterval(), transform, lineThickness, lineColor) );
				}
				else
				{
					if(!currBox.compareIntervalTransformm( interval, transform ))
					{
						currBox.setTransform(transform, false);
						currBox.setInterval( interval );
						
					}
				}
			}
			
		}

		bLocked = false;
	}

	@Override
	public void reload()
	{
		
	}
}
