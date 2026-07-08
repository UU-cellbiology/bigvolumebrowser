package bvb.animation;

import ij.Prefs;

public class RenderSettings
{
	int nRenderFPS = (int)Prefs.get("BVB.nRenderFPS", 30.0);
	
	int nRenderWidth = (int)Prefs.get("BVB.nRenderWidth", 1280);
	
	int nRenderHeight = (int)Prefs.get("BVB.nRenderHeight", 720);
	
	boolean bRenderCurrentWindowSize = Prefs.get("BVB.bRenderCurrentWindowSize", false);
	
	boolean bRenderMultiBox =  Prefs.get("BVB.bRenderMultiBox", false);
	
	boolean bRenderScaleBar =  Prefs.get("BVB.bRenderScaleBar", false);
	
	boolean bRenderAxesGizmo =  Prefs.get("BVB.bRenderAxesGizmo", false);
	
	int nRenderFrameTimeLimit = (int)Prefs.get("BVB.nRenderFrameTimeLimit", 60);
	
	/** 0 - show in Fiji, 1 - save as PNG **/
	int nRenderOutput = (int)Prefs.get("BVB.nRenderOutput", 0);
	
	String sRenderSavePath = null;
}
