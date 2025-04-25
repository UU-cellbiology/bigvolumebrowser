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
	/** background color of BVV canvas **/
	public static Color canvasBGColor = new Color((int)Prefs.get( "BVB.canvasBGColor", Color.BLACK.getRGB() ));
	
	/** status of displaying boxes around sources **/
	public static boolean bShowVolumeBoxes = Prefs.get("BVB.bShowVolumeBoxes", true);

	/** animation speed during zoom in/out , i.e. duration of displayed transform in ms **/
	public static int nTransformAnimationDuration =  (int)Prefs.get("BVB.nTransformAnimationDuration",400);
	
	/** IO default or last folder **/	
	public static String lastDir = Prefs.get( "BVB.lastDir", "" );
	
	public static boolean bFocusOnSourcesOnLoad =  Prefs.get("BVB.bFocusOnSourcesOnLoad", true);
	
	public static boolean bShowMultiBox = Prefs.get( "BVB.bShowMultiBox", true );
	public static boolean bShowScaleBar = Prefs.get( "BVB.bShowScaleBar", true );
	
	public static int nDefaultWidthControlPanel = 400;
	
	public static int nDefaultHeightControlPanel = 600;
	
	public static int nAddedRAINumber = 1;
	
	public static Color getInvertedColor(Color color_in)
	{		
		return  new Color(255-color_in.getRed(),255-color_in.getGreen(),255-color_in.getBlue(),color_in.getAlpha());
	}
}
