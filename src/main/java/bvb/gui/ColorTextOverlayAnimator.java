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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import bdv.viewer.animate.AbstractAnimator;
import bdv.viewer.animate.OverlayAnimator;

public class ColorTextOverlayAnimator extends AbstractAnimator implements OverlayAnimator
{
	protected final Font font;

	protected final String text;

	protected final double fadeInTime;

	protected final double fadeOutTime;
	
	protected Color fontColor = Color.WHITE;

	public static enum TextPosition
	{
		CENTER,
		BOTTOM_RIGHT
	}

	protected final TextPosition position;

	public ColorTextOverlayAnimator( final String text, final long duration, final Color colorin  )
	{
		this( text, duration, TextPosition.BOTTOM_RIGHT, colorin );
	}

	public ColorTextOverlayAnimator( final String text, final long duration, final TextPosition position, final Color colorin )
	{
		this( text, duration, position, 0.2, 0.5, colorin );
	}

	public ColorTextOverlayAnimator( final String text, final long duration, final TextPosition position, final double fadeInTime, final double fadeOutTime, final Color colorin  )
	{
		this( text, duration, position, fadeInTime, fadeOutTime, new Font( "SansSerif", Font.BOLD, 20 ), colorin );
	}

	public ColorTextOverlayAnimator( final String text, final long duration, final TextPosition position, final double fadeInTime, final double fadeOutTime, final Font font, final Color colorin )
	{
		super( duration );
		this.text = text;
		this.font = font;
		this.fadeInTime = fadeInTime;
		this.fadeOutTime = fadeOutTime;
		this.position = position;
		this.fontColor = new Color(colorin.getRed(), colorin.getGreen(), colorin.getBlue(), colorin.getAlpha());
	}

	@Override
	public void paint( final Graphics2D g, final long time )
	{
		setTime( time );

		final FontRenderContext frc = g.getFontRenderContext();
		final TextLayout layout = new TextLayout( text, font, frc );
		final Rectangle2D bounds = layout.getBounds();
		final float x, y;
		if ( position == TextPosition.BOTTOM_RIGHT )
		{
			x = ( float ) ( g.getClipBounds().getWidth() - bounds.getWidth() - 10 );
			y = ( float ) ( g.getClipBounds().getHeight() - 10 );
		}
		else // if ( position == TextPosition.CENTER )
		{
			x = ( float ) ( g.getClipBounds().getWidth() - bounds.getWidth() ) / 2;
			y = ( float ) ( g.getClipBounds().getHeight() - bounds.getHeight() ) / 2;
		}

		final double t = ratioComplete();
		final float alpha;
		if ( t <= fadeInTime )
			alpha = ( float ) Math.sin( ( Math.PI / 2 ) * t / fadeInTime );
		else if ( t >= 1.0 - fadeOutTime )
			alpha = ( float ) Math.sin( ( Math.PI / 2 ) * ( 1.0 - t ) / ( fadeOutTime ) );
		else
			alpha = 1;

		g.setColor( new Color( fontColor.getRed()/255.0f, fontColor.getGreen()/255.0f, fontColor.getBlue()/255.0f, alpha ) );
		layout.draw( g, x, y );
	}


	@Override
	public boolean requiresRepaint()
	{
		return !isComplete();
	}
}
