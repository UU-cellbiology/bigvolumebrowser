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
package bvb.shapes;

import java.awt.image.BufferedImage;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.realtransform.AffineTransform3D;

import bvb.scene.VisMeshTexture;


public class MeshTexture extends AbstractClipTransformSingleShape
{
	
	RealInterval boundBox = null;
	
	public MeshTexture(final Mesh nmesh, final BufferedImage imageTexture)
	{
	
		if(nmesh != null)
		{
			visRender = new VisMeshTexture( nmesh, imageTexture );
			boundBox = Meshes.boundingBox( nmesh );
		}
	}
	
	@Override
	public RealInterval boundingBox()
	{
		final AffineTransform3D t = new AffineTransform3D();
		visRender.getTransform( t );
		
		return t.estimateBounds( boundBox );
	}
	
	@Override
	public RealInterval boundingBoxNotTransformed()
	{
		
		return new FinalRealInterval(boundBox);
	}
	
	
	@Override
	public String toString()
	{
		if(sName.equals( "" ))
		{
			return "mesh"+this.hashCode();
		}
		return sName;
	}

}