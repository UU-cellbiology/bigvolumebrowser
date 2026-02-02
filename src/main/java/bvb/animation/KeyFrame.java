package bvb.animation;


public class KeyFrame
{
	/** position in the movie timeline **/
	float fMovieTimePoint;
	/** camera scene view + timepoint **/
	SceneView scene;
	
	String name;
	
	/** ordered index in the animation list **/
	int nIndex = 0;
	
	public KeyFrame(final SceneView scene_, float fMovieTimePoint_)
	{
		scene = new SceneView(scene_.getViewerTransform(), scene_.getTimeFrame());
		name = "key" + Integer.toString(this.hashCode());
		fMovieTimePoint = fMovieTimePoint_;	
	}
	
	public KeyFrame (String kfName)
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
	
	public KeyFrame duplicate()
	{
		final KeyFrame out = new KeyFrame(scene, fMovieTimePoint);
		out.name = this.name;
		return out;
	}
}
