package bvb.shapes;

import com.jogamp.opengl.GL3;

import java.util.ArrayList;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import org.joml.Matrix4fc;

import bvb.scene.AbstractClipTransformVis;

public abstract class AbstractClipTransformMulti extends AbstractBasicShape
{
	
	final ArrayList<AbstractClipTransformVis> visRenders = new ArrayList<>();
	final ArrayList<Integer> timePoints = new ArrayList<>();

	
	@Override
	public boolean clipActive() 
	{	
		if(visRenders.size() ==0 )
			return false;
		return visRenders.get( 0 ).clipActive();
	}
	@Override
	public void setClipActive(boolean bEnabled)
	{
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			visRender.setClipActive( bEnabled ); 
		}
	}
	
	@Override
	public void setClipInterval(final RealInterval clipInt) 
	{
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			visRender.setClipInterval( new FinalRealInterval(clipInt) );
			visRender.setClipActive( true );
		}
	}
	
	@Override
	public FinalRealInterval getClipInterval() 
	{
		if(visRenders.size()==0)
		{
			return null;
		}
		return visRenders.get( 0 ).getClipInterval();
	}

	@Override
	public void getClipTransform(final AffineTransform3D t) 
	{	
		if(visRenders.size()==0)
		{
			t.set( new AffineTransform3D());
			return;
		}
		visRenders.get(0).getClipTransform( t );

	}
	
	@Override
	public void setClipTransform(final AffineTransform3D t) 
	{		
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			visRender.setClipTransform( t );
		}
	}	
	
	@Override
	public void getTransform(final AffineTransform3D t)
	{
		if(visRenders.size()==0)
		{
			t.set( new AffineTransform3D());
			return;
		}
		visRenders.get(0).getTransform( t );
	}
	
	@Override
	public void setTransform(final AffineTransform3D t)
	{
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			visRender.setTransform( t );
		}
	}
	
	@Override
	public void reload()
	{
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			visRender.reload();
		}		
	}
	
	@Override
	public void draw( final GL3 gl, final Matrix4fc pvm, final Matrix4fc vm, final int[] screen_size , final int nTimePoint_)
	{
		if(bVisible)
		{
			for(int i=0; i<visRenders.size(); i++)
			{
				if(timePoints.get( i )<0 || timePoints.get( i ) == nTimePoint_)
				{
					visRenders.get( i ).draw( gl, pvm, vm, screen_size );
				}
			}
		}
	}
	
}
