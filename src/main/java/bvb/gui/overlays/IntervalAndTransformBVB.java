package bvb.gui.overlays;

import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

public class IntervalAndTransformBVB implements MultiBoxOverlayBVB.IntervalAndTransform
{
	protected boolean isVisible;
	
	protected boolean isShape;

	protected ModifiableRealInterval sourceInterval;

	protected AffineTransform3D sourceToViewer;

	public IntervalAndTransformBVB()
	{
		isVisible = false;
		isShape = false;
		sourceInterval = new ModifiableRealInterval( 3 );
		sourceToViewer = new AffineTransform3D();
	}

	public void set( final boolean visible, final boolean shape, final Interval sourceInterval, final AffineTransform3D sourceToViewer )
	{
		setVisible( visible );
		setShape( shape );
		setSourceInterval( sourceInterval );
		setSourceToViewer( sourceToViewer );
	}

	public void setVisible( final boolean visible )
	{
		isVisible = visible;
	}
	
	public void setShape( final boolean shape)
	{
		isShape = shape;
	}

	public void setSourceInterval( final Interval interval )
	{
		sourceInterval.set( interval );
	}
	
	public void setSourceInterval( final RealInterval interval )
	{
		sourceInterval.set( interval );
	}

	public void setSourceToViewer( final AffineTransform3D t )
	{
		sourceToViewer.set( t );
	}

	@Override
	public boolean isVisible()
	{
		return isVisible;
	}

	@Override
	public RealInterval getSourceInterval()
	{
		return sourceInterval;
	}

	@Override
	public AffineTransform3D getSourceToViewer()
	{
		return sourceToViewer;
	}

	@Override
	public boolean isShape()
	{
		return isShape;
	}

}
