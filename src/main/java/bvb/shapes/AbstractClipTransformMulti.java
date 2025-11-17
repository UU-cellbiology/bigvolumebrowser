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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;

import org.joml.Matrix4fc;

import bvb.scene.AbstractClipTransformVis;

public abstract class AbstractClipTransformMulti extends AbstractBasicShape
{	
	final ConcurrentHashMap<AbstractClipTransformVis, Integer> visRendersTimeMap = new ConcurrentHashMap<>();
	
	RealInterval boundBox = Intervals.createMinMaxReal( Double.POSITIVE_INFINITY,
			Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
			Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY  );

	@Override
	public RealInterval boundingBox()
	{
		if(visRendersTimeMap.size() == 0)
			return null;
		
		final AffineTransform3D t = new AffineTransform3D();
		((AbstractClipTransformVis)visRendersTimeMap.keySet().toArray()[0]).getTransform( t );
		
		return t.estimateBounds( boundBox );
	}	
		
	@Override
	public RealInterval boundingBoxNotTransformed()
	{		
		return new FinalRealInterval(boundBox);
	}
	
	@Override
	public int getClipState() 
	{	
		if(visRendersTimeMap.size() == 0 )
			return 0;
		return ((AbstractClipTransformVis)visRendersTimeMap.keySet().toArray()[0]).getClipState();
	}
	
	@Override
	public void setClipState(final int nClipType)
	{
		final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			visRender.setClipState( nClipType ); 
		}
	}
	
	@Override
	public void setClipInterval(final RealInterval clipInt) 
	{
		final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			visRender.setClipInterval( new FinalRealInterval(clipInt) );
		}
	}
	
	@Override
	public FinalRealInterval getClipInterval() 
	{
		if(visRendersTimeMap.size() == 0)
		{
			return null;
		}
		return ((AbstractClipTransformVis)visRendersTimeMap.keySet().toArray()[0]).getClipInterval();
	}

	@Override
	public void getClipTransform(final AffineTransform3D t) 
	{	
		if(visRendersTimeMap.size() == 0)
		{
			t.set( new AffineTransform3D());
			return;
		}
		((AbstractClipTransformVis)visRendersTimeMap.keySet().toArray()[0]).getClipTransform( t );

	}
	
	@Override
	public void setClipTransform(final AffineTransform3D t) 
	{
		final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			visRender.setClipTransform( t );
		}
	}	
	
	@Override
	public void getTransform(final AffineTransform3D t)
	{
		if(visRendersTimeMap.size() == 0)
		{
			t.set( new AffineTransform3D());
			return;
		}
		((AbstractClipTransformVis)visRendersTimeMap.keySet().toArray()[0]).getTransform( t );
	}
	
	@Override
	public void setTransform(final AffineTransform3D t)
	{
		final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			visRender.setTransform( t );
		}
	}
	
	@Override
	public void reload()
	{
		final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			visRender.reload();
		}		
	}
	
	@Override
	public void draw( final GL3 gl, final Matrix4fc pvm, final Matrix4fc vm, final int[] screen_size , final int nTimePoint_, final boolean bWeightedOIT_)
	{
		if(bVisible)
		{
			final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
			for(final AbstractClipTransformVis visRender:visRenders)
			{
				final int nTP = visRendersTimeMap.get( visRender );
				if(nTP < 0 || nTP == nTimePoint_)
				{
					visRender.draw( gl, pvm, vm, screen_size, nTimePoint_, bWeightedOIT_ );
				}
			}
		}
	}
	
}
