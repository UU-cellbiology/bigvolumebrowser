package bvb.shapes;

import com.jogamp.opengl.GL3;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import org.joml.Matrix4fc;

import bvb.scene.AbstractClipTransformVis;

public abstract class AbstractClipTransformShape extends AbstractBasicShape
{
	
	AbstractClipTransformVis visRender = null;
	
	@Override
	public boolean clipActive() 
	{		
		return visRender.clipActive();
	}
	
	@Override
	public void setClipActive(boolean bEnabled)
	{
		if(visRender.clipActive() != bEnabled )
		{
			visRender.setClipActive( bEnabled ); 
		}
	}
	
	@Override
	public void setClipInterval(final RealInterval clipInt) 
	{
		visRender.setClipInterval( new FinalRealInterval(clipInt) );
		visRender.setClipActive( true );
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
	public void reload()
	{
		if(visRender != null)
			visRender.reload();
		
	}
	
	@Override
	public void draw( GL3 gl, Matrix4fc pvm, Matrix4fc vm, int[] screen_size , int nTimePoint_)
	{
		if(bVisible)
		{
			if(visRender != null)
			{
				if(nTimePoint<0 || nTimePoint == nTimePoint_)
				{
					visRender.draw( gl, pvm, vm, screen_size );
				}
			}
		}
	}
}
