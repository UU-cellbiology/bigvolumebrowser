package bvb.shapes;

import com.jogamp.opengl.GL3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import org.joml.Matrix4fc;

import bvb.scene.AbstractClipTransformVis;

public abstract class AbstractClipTransformMulti extends AbstractBasicShape
{	
	final ConcurrentHashMap<AbstractClipTransformVis, Integer> visRendersTimeMap = new ConcurrentHashMap<>();
	
	@Override
	public boolean clipActive() 
	{	
		if(visRendersTimeMap.size() == 0 )
			return false;
		return ((AbstractClipTransformVis)visRendersTimeMap.keySet().toArray()[0]).clipActive();
	}
	@Override
	public void setClipActive(boolean bEnabled)
	{
		final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			visRender.setClipActive( bEnabled ); 
		}
	}
	
	@Override
	public void setClipInterval(final RealInterval clipInt) 
	{
		final List<AbstractClipTransformVis> visRenders = new ArrayList<>(visRendersTimeMap.keySet());
		for(final AbstractClipTransformVis visRender:visRenders)
		{
			visRender.setClipInterval( new FinalRealInterval(clipInt) );
			visRender.setClipActive( true );
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
				if(nTP<0 || nTP == nTimePoint_)
				{
					visRender.draw( gl, pvm, vm, screen_size, bWeightedOIT_ );
				}
			}
		}
	}
	
}
