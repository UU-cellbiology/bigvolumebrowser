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
package bvb.gui;

import com.jogamp.opengl.GL3;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import org.joml.Matrix4fc;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bvb.core.BigVolumeBrowser;
import bvb.shapes.AbstractBasicShape;
import bvb.shapes.BasicShape;
import bvb.shapes.VolumeBox;
import bvb.utils.Misc;
import bvvpg.source.converters.GammaConverterSetup;


public class VolumeBBoxes extends AbstractBasicShape
{
	
	final BigVolumeBrowser bvb;
	
	private final Map < SourceAndConverter< ? >, VolumeBox > bvvSourceToBox;
	private final Map < BasicShape, VolumeBox > shapeToBox;
	
	private boolean bVisible = false;
	
	private boolean bLocked = false;
	
	private float lineThickness = 1.0f;
	
	private Color lineColor = Color.WHITE;
	
	private boolean bDashed = false;
	
	public VolumeBBoxes(final BigVolumeBrowser bvb_, boolean bDotted_ )
	{
		bvb = bvb_;
		
		bDashed = bDotted_;
		
		bvvSourceToBox = new HashMap<>();
		shapeToBox = new HashMap<>();
	}

	@Override
	public void draw(final GL3 gl, final Matrix4fc pvm, final Matrix4fc vm, final int[] screen_size , final int nTimePoint_)
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
					if(vbox != null)
					{
						vbox.draw( gl, pvm, vm, screen_size, -1);
					}
				}
			});
			
			shapeToBox.forEach( (sh, vbox)-> {
				if(sh.isVisible())
				{
					if(vbox != null)
					{
						vbox.draw( gl, pvm, vm, screen_size, -1);
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
		shapeToBox.forEach( (src, vbox)-> {
			vbox.setLineColor( lineColor );
		});
		
	}
	
	public void setLineThickness(final float fThickness)
	{
		lineThickness = fThickness;
		bvvSourceToBox.forEach( (src, vbox)-> {
			vbox.setLineThickness( lineThickness );
		});	
		shapeToBox.forEach( (src, vbox)-> {
			vbox.setLineThickness( lineThickness );
		});	
	}
	
	@Override
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
		
		for(final SourceAndConverter< ? > sac : sacList )
		{
			final Source< ? > src = sac.getSpimSource();
			if(src.isPresent( nTimePoint ))
			{
				final double [] min = src.getSource( nTimePoint, 0 ).minAsDoubleArray();
				final double [] max = src.getSource( nTimePoint, 0 ).maxAsDoubleArray();
				//extend to include all range
				for(int d=0; d<3; d++)
				{
					min[d] -= 0.5;
					max[d] += 0.5;
				}
				final FinalRealInterval interval = new FinalRealInterval(min, max);
				final AffineTransform3D transform = new AffineTransform3D();
				src.getSourceTransform( nTimePoint, 0, transform );
				final VolumeBox currBox = bvvSourceToBox.get( sac );
				if(currBox == null)
				{		
					final VolumeBox vb = new VolumeBox(interval, transform , lineThickness, lineColor, bDashed);
					bvvSourceToBox.put( sac, vb);
				}
				else
				{
					if(!currBox.compareIntervalTransform( interval, transform ))
					{
						currBox.setTransform(transform, false);
						currBox.setInterval( interval );
					}
				}
			}
			else
			{
				bvvSourceToBox.remove( sac );					
			}
		}
		
		for(final BasicShape sh:bvb.shapes)
		{
			if(sh.getTimePoint()<0 || sh.getTimePoint() == nTimePoint)
			{

				final FinalRealInterval interval = new FinalRealInterval(sh.boundingBoxNotTransformed());
				final AffineTransform3D transform = new AffineTransform3D();
				sh.getTransform( transform );

				final VolumeBox currBox = shapeToBox.get( sh );
				if(currBox == null)
				{				
					final VolumeBox vb = new VolumeBox(interval, transform, lineThickness, lineColor, bDashed); 
					shapeToBox.put( sh, vb);
				}
				else
				{
					if(!currBox.compareIntervalTransform( interval, transform ))
					{
						currBox.setTransform(transform, false);
						currBox.setInterval( interval );
					}
				}
			}
			else
			{
				shapeToBox.remove( sh );
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
		final int nTimePoint = bvb.bvvViewer.state().getCurrentTimepoint();
		for(SourceAndConverter< ? > sac : sacList )
		{
			GammaConverterSetup cs = (GammaConverterSetup)bvb.bvvHandle.getConverterSetups().getConverterSetup( sac );
	
			final VolumeBox currBox = bvvSourceToBox.get( sac );
			final Source< ? > src = sac.getSpimSource();
			if(cs.clipActive() && src.isPresent( nTimePoint ))
			{
				final AffineTransform3D transform = new AffineTransform3D();
				RealInterval interval = cs.getClipInterval();
				if(interval == null)
					interval = Misc.getSourceBoundingBox( src,nTimePoint,0 );
				cs.getClipTransform( transform );
				if(currBox == null)
				{
					cs.getClipTransform( transform );
					final VolumeBox vb = new VolumeBox(interval, transform, lineThickness, lineColor, bDashed);
					bvvSourceToBox.put( sac, vb);
				}
				else
				{
					if(!currBox.compareIntervalTransform( interval, transform ))
					{
						currBox.setTransform(transform, false);
						currBox.setInterval( interval );
						
					}
				}
			}
			else
			{
				bvvSourceToBox.remove( sac );
			}
			
		}
		
		for(final BasicShape sh:bvb.shapes)
		{
			final VolumeBox currBox = shapeToBox.get( sh );
			
			if(sh.clipActive() && (sh.getTimePoint()<0 || sh.getTimePoint() == nTimePoint))
			{
				final AffineTransform3D transform = new AffineTransform3D();
				RealInterval interval = sh.getClipInterval();
				if(interval == null)
					interval = new FinalRealInterval (sh.boundingBox());
				sh.getClipTransform( transform );
				if(currBox == null)
				{
					sh.getClipTransform( transform );
					final VolumeBox vb = new VolumeBox(interval, transform, lineThickness, lineColor, bDashed) ;
					shapeToBox.put( sh, vb);
				}
				else
				{
					if(!currBox.compareIntervalTransform( interval, transform ))
					{
						currBox.setTransform(transform, false);
						currBox.setInterval( interval );						
					}
				}
			}
			else
			{
				shapeToBox.remove( sh );
			}
		}

		bLocked = false;
	}

	@Override
	public void reload()
	{
		bvvSourceToBox.forEach( (src,vb) -> vb.reload() );
		shapeToBox.forEach( (sh,vb) -> vb.reload() );
	}

	@Override
	public RealInterval boundingBox()
	{
		return null;
	}

	@Override
	public boolean clipActive()
	{

		return false;
	}

	@Override
	public void setClipInterval( RealInterval clipInt )
	{
		
	}

	@Override
	public void setClipActive( boolean bEnabled )
	{
		
	}

	@Override
	public FinalRealInterval getClipInterval()
	{
		return null;
	}

	@Override
	public void getClipTransform( AffineTransform3D t )
	{
		
	}

	@Override
	public void setClipTransform( AffineTransform3D t )
	{
		
	}

	@Override
	public void getTransform( AffineTransform3D t )
	{
		
	}

	@Override
	public void setTransform( AffineTransform3D t )
	{
		
	}

	@Override
	public RealInterval boundingBoxNotTransformed()
	{
		return null;
	}
}
