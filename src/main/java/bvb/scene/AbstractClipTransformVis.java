package bvb.scene;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

public abstract class AbstractClipTransformVis implements BasicVis
{
	private boolean clipActive = false;
	
	private FinalRealInterval clipInt = null;
	
	private AffineTransform3D clipTransform = new AffineTransform3D();
	
	public boolean clipActive() 
	{		
		return clipActive;
	}
	
	public void setClipActive(boolean bEnabled)
	{
		if(clipActive != bEnabled )
		{
			clipActive = bEnabled;
		}
	}
	
	public void setClipInterval(final RealInterval clipInt) 
	{
		this.clipInt = new FinalRealInterval(clipInt);
		clipActive = true;
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
	

}
