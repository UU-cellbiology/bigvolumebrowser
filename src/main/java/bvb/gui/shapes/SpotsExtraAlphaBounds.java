package bvb.gui.shapes;

import java.util.HashMap;
import java.util.Map;

import bdv.util.Bounds;
import bvb.shapes.BasicSpots;


public class SpotsExtraAlphaBounds
{
	private final Map< BasicSpots, Bounds> spotsToBounds = new HashMap<>();
	
	public Bounds getBounds( final BasicSpots obj )
	{
		Bounds out = spotsToBounds.get( obj );
		if(out == null)
		{
			out = new Bounds(0.0, 1.0);
			setBounds( obj, out );
		}		
		return out;
	}
	public void setBounds( final BasicSpots obj, final Bounds bounds )
	{
		 spotsToBounds.put( obj, bounds );
	}

	

}
