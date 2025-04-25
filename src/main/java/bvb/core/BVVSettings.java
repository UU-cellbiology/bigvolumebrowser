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
	public static int renderWidth = (int)Prefs.get("BVB.renderWidth", 800.);	
	public static int renderHeight = (int)Prefs.get("BVB.renderHeight", 600.);
	static int numDitherSamples = (int)Prefs.get("BVB.numDitherSamples", 3.);
	static int cacheBlockSize = (int)Prefs.get("BVB.cacheBlockSize", 32.);
	static int maxCacheSizeInMB = (int)Prefs.get("BVB.maxCacheSizeInMB", 500.);
	static int ditherWidth =(int)Prefs.get("BVB.ditherWidth", 3.);

}
