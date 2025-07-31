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
