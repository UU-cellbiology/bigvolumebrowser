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
package bvb.animation;

import net.imglib2.realtransform.AffineTransform3D;

import bvvpg.core.VolumeViewerPanel;

/** class stores current viewer transform and BVV timepoint **/
public class SceneView
{
	final AffineTransform3D viewerTransform;
	
	int nTimeFrame;
	
	public SceneView(final AffineTransform3D viewerTransform_, final int nTimeFrame_ )
	{
		viewerTransform = new AffineTransform3D();
		viewerTransform.set( viewerTransform_ );
		nTimeFrame = nTimeFrame_;
	}
	public SceneView()
	{
		viewerTransform = new AffineTransform3D();
	}
	
	public AffineTransform3D getViewerTransform()
	{
		return viewerTransform;
	}
	
	public void setViewerTransform( final double... values )
	{
		viewerTransform.set( values );
	}
	
	
	public int getTimeFrame()
	{
		return nTimeFrame;
	}
	
	public void setTimeFrame(int nTimeFrame_)
	{
		nTimeFrame = nTimeFrame_;
		return;
	}
	
	public static SceneView getCurrentSceneView(final VolumeViewerPanel viewer)
	{
		final AffineTransform3D transform = new AffineTransform3D();
		viewer.state().getViewerTransform(transform);
		int canvasW = viewer.getWidth();
		int canvasH = viewer.getHeight();
		transform.set( transform.get( 0, 3 ) - canvasW * 0.5, 0, 3 );
		transform.set( transform.get( 1, 3 ) - canvasH * 0.5, 1, 3 );
		transform.scale( 1.0/ canvasH );
		return new SceneView(transform, viewer.state().getCurrentTimepoint());
	}
	
	public static void setSceneView(final VolumeViewerPanel viewer, final SceneView scene)
	{
		final AffineTransform3D affine = new AffineTransform3D();
		affine.set( scene.getViewerTransform());
		final int width = viewer.getWidth();
		final int height = viewer.getHeight();
		affine.scale( height );
		affine.set( affine.get( 0, 3 ) + width  * 0.5, 0, 3 );
		affine.set( affine.get( 1, 3 ) + height * 0.5, 1, 3 );
		viewer.state().setViewerTransform( affine );
		final int nTimePoint = scene.getTimeFrame();
		if(nTimePoint < viewer.state().getNumTimepoints())
		{
			viewer.state().setCurrentTimepoint(nTimePoint);
		}
	}
}
