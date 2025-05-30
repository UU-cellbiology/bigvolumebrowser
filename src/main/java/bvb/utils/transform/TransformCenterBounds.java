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

	private final Map< ConverterSetup, Bounds3D > setupToBounds = new HashMap<>();
	private final Map< BasicShape, Bounds3D > shapeToBounds = new HashMap<>();
	
	public TransformCenterBounds( final SourceToConverterSetupBimap bimap )
	{
		this.bimap = bimap;
	}
	
	public Bounds3D getBounds( final ConverterSetup setup )
	{
		return setupToBounds.compute( setup, this::getExtendedBounds );
	}
	
	public Bounds3D getBounds( final BasicShape shape )
	{
		return shapeToBounds.compute( shape, this::getExtendedBoundsShape );
	}
	
	public void setBounds( final ConverterSetup setup, final Bounds3D bounds )
	{
		setupToBounds.put( setup, bounds );
	}

	public void setBounds( final BasicShape shape, final Bounds3D bounds )
	{
		shapeToBounds.put( shape, bounds );
	}

	public Bounds3D getDefaultBounds( final ConverterSetup setup )
	{
		Bounds3D bounds = null;

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
	
	public Bounds3D getDefaultBounds( final BasicShape shape)
	{		
		return new Bounds3D(shape.boundingBox());
	}

	private Bounds3D getExtendedBounds( final ConverterSetup setup, Bounds3D bounds )
	{
		if ( bounds == null )
			bounds = getDefaultBounds( setup );
		else
		{
			 bounds.join( getDefaultBounds( setup ) );
		}
		return bounds;
	}
	
	private Bounds3D getExtendedBoundsShape( final BasicShape shape, Bounds3D bounds )
	{
		if ( bounds == null )
			bounds = getDefaultBounds( shape );
		else
		{
			 bounds.join( getDefaultBounds( shape ) );
		}
		return bounds;
	}

}
