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

import com.jogamp.opengl.GL3;

import java.awt.image.BufferedImage;

import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;

import org.joml.Matrix4fc;

import bvb.core.BigVolumeBrowser;
import bvb.scene.VisMeshTexture;


public class MeshTexture extends AbstractBasicShape
{
	
	final BigVolumeBrowser bvb;
	
	VisMeshTexture meshVis = null;
	
	int nTimePoint = -1;
	
	String sName = "";
	
	RealInterval boundingBox = null;
	
//	public MeshTexture(String sFilename, BigVolumeBrowser bvb_)
//	{
//		bvb  = bvb_;
//		//load mesh from file
//		Mesh nmesh = loadMeshFromFile( sFilename );
//		
//		//Mesh nmesh = createMeshWithNoise(15);
//		
//		if(nmesh != null)
//		{			
//			meshVis = new VisMeshTexture( nmesh );
//			boundingBox = Meshes.boundingBox( nmesh );
//			setName(Misc.getSourceStyleName( sFilename ));
//		}
//		else
//		{
//			System.err.println("Sorry, cannot load mesh. Only STL and PLY formats are supported for now.");
//		}
//	}
	
	public MeshTexture(final Mesh nmesh, final BufferedImage imageTexture, BigVolumeBrowser bvb_)
	{
		bvb  = bvb_;
		
		if(nmesh != null)
		{
			meshVis = new VisMeshTexture( nmesh, imageTexture );
			boundingBox = Meshes.boundingBox( nmesh );
		}
	}
	
	public RealInterval boundingBox()
	{
		return boundingBox;
	}
	
	public void setTimePoint(final int nTP)
	{
		this.nTimePoint = nTP;
	}
	
	public int getTimePoint()
	{
		return nTimePoint;
	}
	

	@Override
	public void draw( GL3 gl, Matrix4fc pvm, Matrix4fc vm, int[] screen_size )
	{
		if(bVisible)
		{
			if(meshVis != null)
			{
				if(nTimePoint<0 || nTimePoint == bvb.bvvViewer.state().getCurrentTimepoint())
				{
					meshVis.draw( gl, pvm, vm, screen_size );
				}
			}
		}
	}
	public void setName(String sName_)
	{
		sName = sName_;
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

	@Override
	public void reload()
	{
		meshVis.reload();
		
	}

	@Override
	public void setVisible( boolean bVisible_ )
	{
		bVisible = bVisible_;
		bvb.repaintBVV();
		
	}
	

}
