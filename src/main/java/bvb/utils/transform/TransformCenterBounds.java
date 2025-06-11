package bvb.utils.transform;

import java.util.HashMap;
import java.util.Map;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.SourceToConverterSetupBimap;
import bvb.shapes.BasicShape;
import bvb.utils.Bounds3D;
import bvb.utils.Misc;

public class TransformCenterBounds
{
	private final SourceToConverterSetupBimap bimap;

	private final Map< Object, Bounds3D > objToBounds = new HashMap<>();
	
	public TransformCenterBounds( final SourceToConverterSetupBimap bimap )
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
				bounds = new Bounds3D(Misc.getSourceBoundingBoxAllTP(sac.getSpimSource()));
			}
			else
			{
				System.out.println("error in estimation of center bounds, no source found");
			}
			return bounds;
		}
		if(obj instanceof BasicShape)
		{
			return new Bounds3D(((BasicShape)obj).boundingBox());
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
