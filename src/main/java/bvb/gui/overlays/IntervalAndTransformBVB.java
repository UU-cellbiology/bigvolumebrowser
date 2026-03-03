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
package bvb.gui.overlays;

import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

public class IntervalAndTransformBVB implements MultiBoxOverlayBVB.IntervalAndTransform
{
	protected boolean isVisible;
	
	protected boolean isShape;

	protected ModifiableRealInterval sourceInterval;

	protected AffineTransform3D sourceToViewer;

	public IntervalAndTransformBVB()
	{
		isVisible = false;
		isShape = false;
		sourceInterval = new ModifiableRealInterval( 3 );
		sourceToViewer = new AffineTransform3D();
	}

	public void set( final boolean visible, final boolean shape, final Interval sourceInterval, final AffineTransform3D sourceToViewer )
	{
		setVisible( visible );
		setShape( shape );
		setSourceInterval( sourceInterval );
		setSourceToViewer( sourceToViewer );
	}

	public void setVisible( final boolean visible )
	{
		isVisible = visible;
	}
	
	public void setShape( final boolean shape)
	{
		isShape = shape;
	}

	public void setSourceInterval( final Interval interval )
	{
		sourceInterval.set( interval );
	}
	
	public void setSourceInterval( final RealInterval interval )
	{
		sourceInterval.set( interval );
	}

	public void setSourceToViewer( final AffineTransform3D t )
	{
		sourceToViewer.set( t );
	}

	@Override
	public boolean isVisible()
	{
		return isVisible;
	}

	@Override
	public RealInterval getSourceInterval()
	{
		return sourceInterval;
	}

	@Override
	public AffineTransform3D getSourceToViewer()
	{
		return sourceToViewer;
	}

	@Override
	public boolean isShape()
	{
		return isShape;
	}

}
