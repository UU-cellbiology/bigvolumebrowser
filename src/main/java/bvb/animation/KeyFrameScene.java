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
