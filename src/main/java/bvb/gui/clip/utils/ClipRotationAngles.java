package bvb.gui.clip.utils;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.Affine3DHelpers;
import bvb.utils.Misc;
import bvvpg.source.converters.GammaConverterSetup;

public class ClipRotationAngles
{
	

	private final Map< ConverterSetup, double[]> setupToAngles = new HashMap<>();
	public ClipRotationAngles( )
	{


	}
	public double[] getAngles( final ConverterSetup setup )
	{
		double [] out =  setupToAngles.get( setup );
		if(out == null)
		{
			out = getCurrentEulerAngles(setup);
			setAngles(setup, out);
		}
		
		return out;
	}
	
	public void setAngles( final ConverterSetup setup, final double[] eAngles)
	{
		setupToAngles.put( setup, eAngles );
	}
	
	public double [] getCurrentEulerAngles(final ConverterSetup setup)
	{
		final AffineTransform3D clipTr = new AffineTransform3D();
		((GammaConverterSetup)setup).getClipTransform(clipTr);
		final FinalRealInterval interval = ((GammaConverterSetup)setup).getClipInterval(); 
		final double [] center;
		if(interval == null)
		{
			center = new double[3];
		}
		else
		{
			center = Misc.getIntervalCenterNegative( interval);
		}
		clipTr.translate( center );
		final double[] qRotation = new double[4];

		Affine3DHelpers.extractRotationAnisotropic( clipTr, qRotation );
		return Misc.quaternionToEulerAngles(qRotation);
		
	}
}
