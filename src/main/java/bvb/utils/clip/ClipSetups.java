package bvb.utils.clip;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import bdv.viewer.ConverterSetups;
import bvb.utils.Misc;
import bvvpg.source.converters.GammaConverterSetup;

public class ClipSetups
{
	final public ClipRotationAngles clipRotationAngles = new ClipRotationAngles();
	
	final public ClipAxesBounds clipAxesBounds;
	
	final public ClipCenters clipCenters;
	
	final public ClipCenterBounds clipCenterBounds;
	
	final public ConverterSetups converterSetups;
	
	public ClipSetups (final ConverterSetups converterSetups_)
	{
		converterSetups = converterSetups_;
		clipAxesBounds = new ClipAxesBounds(converterSetups);
		clipCenters = new ClipCenters(converterSetups);
		clipCenterBounds = new ClipCenterBounds(converterSetups);
	}
	
	public synchronized void updateClipTransform( final GammaConverterSetup cs)
	{
		final double [] eAngles = clipRotationAngles.getAngles( cs );
		
		final double[] qRotation = new double[4];
		final double[] q = new double[4];

		final double[] dAxis = new double[3];
		dAxis[0] = 1.0;
		LinAlgHelpers.quaternionFromAngleAxis( dAxis, eAngles[0], qRotation );
		for (int d=1;d<3;d++)
		{
			dAxis[d-1] = 0.0;
			dAxis[d] = 1.0;
			LinAlgHelpers.quaternionFromAngleAxis( dAxis, eAngles[d], q);
			LinAlgHelpers.quaternionMultiply( q, qRotation, qRotation );
		}
		
		final double [][] rotMatrix = new double [3][4];  
		LinAlgHelpers.quaternionToR( qRotation, rotMatrix );
		AffineTransform3D clipRot = new AffineTransform3D();
		
		clipRot.set( rotMatrix );
		
		AffineTransform3D clipTr = new AffineTransform3D();

		final double [] center =  Misc.getIntervalCenterNegative( cs.getClipInterval() );
		
		final double [] centerNew = clipCenters.getCenters( cs );

		clipTr.translate( center );
		
		clipTr = clipTr.preConcatenate( clipRot );

		clipTr.translate( centerNew );	

		cs.setClipTransform(clipTr);
		
	}
	
	
}
