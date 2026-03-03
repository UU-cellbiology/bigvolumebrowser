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

import bvb.animation.utils.Easing;
import bvb.animation.utils.Timeline;
import bvb.io.dto.KeyFrameSceneDTO;

public class KeyFrameScene
{
	/** position in the movie timeline **/
	float fMovieTimePoint;
	/** camera scene view + timepoint **/
	SceneView scene;
	
	String name;
	
	public Easing easing;
	
	/** ordered index in the animation list **/
	int nIndex = 0;
	
	public KeyFrameScene(final SceneView scene, float fMovieTimePoint, Easing easing)
	{
		this.scene = new SceneView(scene.getViewerTransform(), scene.getTimeFrame());
		name = "key" + Integer.toString(this.hashCode());
		this.fMovieTimePoint = fMovieTimePoint;	
		this.easing = easing;
	}
	
	public KeyFrameScene(final KeyFrameSceneDTO dto)
	{
		final AffineTransform3D viewerTransform = new AffineTransform3D();
		viewerTransform.set( dto.transformMatrix );
		scene = new SceneView(viewerTransform, dto.nTimeFrame);
		name = dto.name;
		nIndex = dto.nIndex;
		fMovieTimePoint = dto.fMovieTimePoint;
    	easing = Timeline.easingRegistry.get( dto.easingId );
	}
	
	public KeyFrameScene (String kfName)
	{
		name = kfName;
	}
	
	@Override
	public String toString()
	{
		return "[" + Integer.toString( nIndex )+"] " + name;
	}
	
	public int getIndex()
	{
		return nIndex;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String sName)
	{
		name = sName;
		return;
	}
	
	public SceneView getSceneView()
	{
		return scene;
	}
	
	public void setScene(SceneView scene_)
	{
		scene = scene_;
		return;
	}
	
	public void setMovieTimePoint(float fMovieTimePoint_)
	{
		fMovieTimePoint = fMovieTimePoint_;
		return;
	}
	
	public float getMovieTimePoint()
	{
		return fMovieTimePoint;
	}
	
	public KeyFrameScene duplicate()
	{
		final KeyFrameScene out = new KeyFrameScene(scene, fMovieTimePoint, easing);
		out.name = this.name;
		return out;
	}
	
	public String getCurrentID()
	{
		return Integer.toString( nIndex ) + name;
	}
	
	public KeyFrameSceneDTO toDTO()
	{
		final KeyFrameSceneDTO out = new KeyFrameSceneDTO();
		out.fMovieTimePoint = fMovieTimePoint;
		out.name = name;
		out.nIndex = nIndex;
		out.nTimeFrame = scene.nTimeFrame;
		out.id = getCurrentID();
		out.easingId = easing.getId();
		scene.getViewerTransform().toArray( out.transformMatrix );
		return out;		
	}
}
