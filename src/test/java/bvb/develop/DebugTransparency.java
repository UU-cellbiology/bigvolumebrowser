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
package bvb.develop;

import java.awt.Color;
import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.ValuePair;

import bvb.core.BigVolumeBrowser;
import bvb.shapes.MeshShape;
import bvvpg.vistools.BvvStackSource;
import ij.ImageJ;
import mpicbg.spim.data.generic.AbstractSpimData;

public class DebugTransparency
{
	public static void main( final String[] args )
	{
		new ImageJ();
	
		//start BVB
		BigVolumeBrowser bvb = new BigVolumeBrowser(); 		
		
		bvb.startBVB("");
		
		MeshShape cube = new MeshShape("/home/eugene/Desktop/projects/BVB/stl/3D_model_of_a_Cube.stl");
		cube.setColor( new Color(0,255,255,255) );
		AffineTransform3D tr = new AffineTransform3D();
		tr.translate( -0.5,-0.5,-0.5 );
		cube.setTransform( tr );
		bvb.addShape( cube );
		ValuePair< AbstractSpimData< ? >, List< BvvStackSource< ? > > > vp = bvb.loadBioFormats( "/home/eugene/Desktop/projects/BVB/30x30x30_white.tif" );
		BvvStackSource< ? > source = vp.getB().get( 0 );
		
		source.setLUT( "Grays" );//.setColor( null );
		source.setRenderType( 1 );
		source.setDisplayRange( 0,1 );
		
		
	}
}
