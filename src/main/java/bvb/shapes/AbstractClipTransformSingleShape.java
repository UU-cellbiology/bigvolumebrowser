/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import org.joml.Matrix4fc;

import bvb.scene.AbstractClipTransformVis;

public abstract class AbstractClipTransformSingleShape extends AbstractBasicShape
{
	
	AbstractClipTransformVis visRender = null;
	
	@Override
	public int getClipState() 
	{		
		return visRender.getClipState();
	}
	
	@Override
	public void setClipState(final int nClipType)
	{
		if(visRender.getClipState() != nClipType )
		{
			visRender.setClipState( nClipType ); 
		}
	}
	
	@Override
	public void setClipInterval(final RealInterval clipInt) 
	{
		visRender.setClipInterval( new FinalRealInterval(clipInt) );
	}
	
	@Override
	public FinalRealInterval getClipInterval() 
	{
		return visRender.getClipInterval();
	}

	@Override
	public void getClipTransform(final AffineTransform3D t) 
	{	
		visRender.getClipTransform( t );

	}
	
	@Override
	public void setClipTransform(final AffineTransform3D t) 
	{
		visRender.setClipTransform( t );
	}	
	
	@Override
	public void getTransform(final AffineTransform3D t)
	{
		visRender.getTransform( t );
	}
	
	@Override
	public void setTransform(final AffineTransform3D t)
	{
		visRender.setTransform( t );
	}
	
	@Override
	public void reload()
	{
		if(visRender != null)
			visRender.reload();
		
	}
	
	@Override
	public void draw( final GL3 gl, final Matrix4fc pvm, final Matrix4fc vm, final int[] screen_size , final int nTimePoint_, final boolean bWeightedOIT_)
	{
		if(bVisible)
		{
			if(visRender != null)
			{
				if(nTimePoint<0 || nTimePoint == nTimePoint_)
				{
					visRender.draw( gl, pvm, vm, screen_size, nTimePoint_, bWeightedOIT_);
				}
			}
		}
	}
	
}
