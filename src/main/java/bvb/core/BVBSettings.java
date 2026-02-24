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
package bvb.core;

import java.awt.Color;

import ij.Prefs;

public class BVBSettings
{
	
	public static String sVersion = "0.0.8";
	
	/** background color of BVV canvas **/
	public static Color canvasBGColor = new Color((int)Prefs.get( "BVB.canvasBGColor", Color.BLACK.getRGB() ));

	/** background color of BVV canvas **/
	public static Color canvasOverlayColor = new Color((int)Prefs.get( "BVB.canvasOverlayColor", Color.WHITE.getRGB() ));
	
	/** status of displaying boxes around sources **/
	public static boolean bShowVolumeBoxes = Prefs.get("BVB.bShowVolumeBoxes", true);

	/** status of displaying boxes around sources **/
	public static boolean bShowClipBoxes = Prefs.get("BVB.bShowClipBoxes", true);
	
	/** animation speed during zoom in/out , i.e. duration of displayed transform in ms **/
	public static int nTransformAnimationDuration =  (int)Prefs.get("BVB.nTransformAnimationDuration",400);
	
	/** IO default or last folder **/	
	public static String lastDir = Prefs.get( "BVB.lastDir", "" );
	
	public static boolean bFocusOnSourcesOnLoad =  Prefs.get("BVB.bFocusOnSourcesOnLoad", true);
	
	public static boolean bShowMultiBox = Prefs.get( "BVB.bShowMultiBox", true );
	
	public static boolean bShowScaleBar = Prefs.get( "BVB.bShowScaleBar", true );
	
	public static double dFocusScreenFraction =  Prefs.get("BVB.dFocusScreenFraction", 0.95);
	
	public static boolean bLoadPyramidize = Prefs.get( "BVB.bLoadPyramidize", true );
	
	public static boolean bShowRandomShader = Prefs.get( "BVB.bShowRandomShader", true ); 
	
	/** whether to highlight selected boxes/clipboxes **/
	public static boolean bHighlightSelectedBoxes = Prefs.get( "BVB.bHighlightSelectedBoxes", true ); 
	
	/** highlight color of boxes/clipboxes **/
	public static Color boxHighlightColor = new Color((int)Prefs.get( "BVB.boxHighlightColor", new Color(0,153,255,255).getRGB() ));
	
	/** angle for lattice-light sheet data deskew in degrees**/
	public static double dLLSAngle = Prefs.get("BVB.dLLSAngle", 30.);
	
	public static int nDefaultWidthControlPanel = 400;
	
	public static int nDefaultHeightControlPanel = 600;
	
	public static int nAddedRAINumber = 1;
	
	public static boolean bWeightedOIT = Prefs.get( "BVB.bWeightedOIT", true );
	
	public static String sIconPath = "/bvb/icons/";
	
	public static String sShaderPath = "/bvb/scene/";
	
	public static String sUITheme = "";
	
	public static Color getInvertedColor(Color color_in)
	{		
		return  new Color(255-color_in.getRed(),255-color_in.getGreen(),255-color_in.getBlue(),color_in.getAlpha());
	}
}
