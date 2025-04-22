package bvb.core;


import ij.Prefs;

/** BVV canvas rendering parameters, 
 * can be changed/adjusted somewhere else.
 **/
public class BVVSettings
{
	
	//parameters that can be changed at runtime
	public static double dCam = Prefs.get("BVB.dCam", 2000.);
	public static double dClipFar = Prefs.get("BVB.dClipFar", 1000.);
	public static double dClipNear = Prefs.get("BVB.dClipNear", 1000.);
	
	
	// parameters that require bvv restart, 
	// see https://github.com/ekatrukha/BigTrace/wiki/Volume-Render-Settings
	static int renderWidth = (int)Prefs.get("BVB.renderWidth", 800.);	
	static int renderHeight = (int)Prefs.get("BVB.renderHeight", 600.);
	static int numDitherSamples = (int)Prefs.get("BVB.numDitherSamples", 3.);
	static int cacheBlockSize = (int)Prefs.get("BVB.cacheBlockSize", 32.);
	static int maxCacheSizeInMB = (int)Prefs.get("BVB.maxCacheSizeInMB", 500.);
	static int ditherWidth =(int)Prefs.get("BVB.ditherWidth", 3.);

}