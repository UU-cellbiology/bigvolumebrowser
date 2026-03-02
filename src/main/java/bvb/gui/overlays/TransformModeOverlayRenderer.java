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
