package bvb.core;

import java.awt.Color;

import ij.Prefs;

public class BVBSettings
{
	public static Color canvasBGColor = new Color((int)Prefs.get( "BVB.canvasBGColor", Color.BLACK.getRGB() ));
	public static boolean bShowVolumeBoxes = Prefs.get("BVB.bShowVolumeBoxes", true);
	
	
	public static Color getInvertedColor(Color color_in)
	{		
		return  new Color(255-color_in.getRed(),255-color_in.getGreen(),255-color_in.getBlue(),color_in.getAlpha());
	}
}
