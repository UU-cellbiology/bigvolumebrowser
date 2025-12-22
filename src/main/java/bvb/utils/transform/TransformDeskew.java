package bvb.utils.transform;

import java.util.HashMap;
import java.util.Map;

public class TransformDeskew
{
	
	private final Map< Object, Double> objToAngles;
	
	public TransformDeskew( )
	{
		objToAngles = new HashMap<>();		
	}
	
	public double getAngle( final Object obj )
	{
		Double outD =  objToAngles.get( obj );
		if(outD == null)
		{
			setAngle(obj, Math.PI*0.5);
			return Math.PI*0.5;
		}
		return outD.doubleValue();
	}
	
	public void setAngle( final Object obj, final double angle)
	{
		objToAngles.put( obj, new Double(angle) );
	}
}
