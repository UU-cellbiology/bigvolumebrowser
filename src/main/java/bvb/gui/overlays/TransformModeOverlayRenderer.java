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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import bdv.ui.UIUtils;
import bdv.viewer.OverlayRenderer;
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;

public class TransformModeOverlayRenderer  implements OverlayRenderer
{
	private BigVolumeBrowser bvb = null;
	
	boolean isEnabled = false;
	
	String sModeMessage = "transform mode";
	
	public void bindBVB(final BigVolumeBrowser bvb_)
    {
    	this.bvb  = bvb_;
    }
	@Override
	public void drawOverlays( Graphics g )
	{
		if(bvb != null && isEnabled)
		{
			Graphics2D graphics = (Graphics2D) g;
			final Font font = UIUtils.getFont( "monospaced.small.font" );
			final Rectangle clipBounds = g.getClipBounds();
	
			graphics.setColor( BVBSettings.canvasOverlayColor );
			graphics.setFont( font );
			graphics.drawString( sModeMessage, (float)(clipBounds.getWidth() - 170), (float)(clipBounds.getHeight() - 35) );

		}
	}
	
	public void setEnabled (boolean bEnabled)
	{
		isEnabled = bEnabled;
	}
	
	public boolean isEnabled()
	{
		return isEnabled;
	}

}
