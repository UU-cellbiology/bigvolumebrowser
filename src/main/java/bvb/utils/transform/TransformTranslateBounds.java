package bvb.utils.transform;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.RealInterval;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.SourceToConverterSetupBimap;
import bvb.shapes.BasicShape;
import bvb.utils.Bounds3D;
import bvb.utils.Misc;

public class TransformTranslateBounds
{
	private final SourceToConverterSetupBimap bimap;

	private final Map< Object, Bounds3D > objToBounds = new HashMap<>();
	
	public TransformTranslateBounds( final SourceToConverterSetupBimap bimap )
	{
		this.bimap = bimap;
	}
	
	public Bounds3D getBounds( final Object obj)
	{
		return objToBounds.compute( obj, this::getExtendedBounds );
	}
	
	
	public void setBounds( final Object obj, final Bounds3D bounds )
	{
		objToBounds.put( obj, bounds );
	}

	public Bounds3D getDefaultBounds( final Object obj )
	{
		Bounds3D bounds = null;

		if(obj instanceof ConverterSetup)
		{
			final ConverterSetup setup = (ConverterSetup)obj;
		
			final SourceAndConverter< ? > sac = bimap.getSource( setup );
			if ( sac != null )
			{			
				//get the range over all timepoints
				final RealInterval interval = Misc.getSourceBoundingBoxAllTP(sac.getSpimSource());
				bounds = new Bounds3D(interval);
				final double [] center = Misc.getIntervalCenterNegative( interval );
				bounds.translate( center );
				
			}
			else
			{
				System.out.println("error in estimation of center bounds, no source found");
			}
			return bounds;
		}
		if(obj instanceof BasicShape)
		{
			final RealInterval interval = ((BasicShape)obj).boundingBox();
			bounds = new Bounds3D(interval);
			final double [] center = Misc.getIntervalCenterNegative( interval );
			bounds.translate( center );
		}
		return null;
	}
	

	private Bounds3D getExtendedBounds( final Object obj, Bounds3D bounds )
	{
		if ( bounds == null )
			bounds = getDefaultBounds( obj );
		else
		{
			 bounds.join( getDefaultBounds( obj ) );
		}
		return bounds;
	}
}
