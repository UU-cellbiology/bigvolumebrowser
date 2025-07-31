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
