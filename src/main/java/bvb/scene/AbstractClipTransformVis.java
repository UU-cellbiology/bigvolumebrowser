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
package bvb.scene;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

public abstract class AbstractClipTransformVis implements BasicVis
{
	int clipState = 0;
	
	FinalRealInterval clipInt = null;
	
	AffineTransform3D clipTransform = new AffineTransform3D();
	
	AffineTransform3D transform = new AffineTransform3D();
	
	public int getClipState() 
	{		
		return clipState;
	}
	
	public void setClipState(final int nClipType)
	{
		if(clipState != nClipType )
		{
			clipState = nClipType;
		}
	}
	
	public void setClipInterval(final RealInterval clipInt) 
	{
		this.clipInt = new FinalRealInterval(clipInt);
	}
	
	public FinalRealInterval getClipInterval() 
	{
		return clipInt;
	}

	public void getClipTransform(final AffineTransform3D t) 
	{	
		t.set( clipTransform );			
		return;
	}
	
	public void setClipTransform(final AffineTransform3D t) 
	{
		clipTransform.set( t );
	}	
	
	public void getTransform(final AffineTransform3D t) 
	{	
		t.set( transform );			
		return;
	}
	
	public void setTransform(final AffineTransform3D t) 
	{
		transform.set( t );
	}

}
