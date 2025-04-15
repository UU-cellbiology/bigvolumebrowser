package bvb.core;

import java.awt.Color;

import ij.Prefs;

public class BVBSettings
{
	/** background color of BVV canvas **/
	public static Color canvasBGColor = new Color((int)Prefs.get( "BVB.canvasBGColor", Color.BLACK.getRGB() ));
	
	/** status of displaying boxes around sources **/
	public static boolean bShowVolumeBoxes = Prefs.get("BVB.bShowVolumeBoxes", true);

	/** animation speed during zoomin in/out , i.e. duration of displayed transform in ms **/
	public static long nTransformAnimationDuration =  (int)Prefs.get("BVB.nTransformAnimationDuration",400);
		
	/** IO default or last folder **/	
	public static String lastDir = Prefs.get( "BVB.lastDir", "" );
	
	public static int nDefaultWidthControlPanel = 400;
	
	public static int nDefaultHeightControlPanel = 600;
	
	public static Color getInvertedColor(Color color_in)
	{		
		return  new Color(255-color_in.getRed(),255-color_in.getGreen(),255-color_in.getBlue(),color_in.getAlpha());
	}
}
