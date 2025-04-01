package bvb.utils;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import bdv.viewer.ConverterSetups;
import bvvpg.source.converters.GammaConverterSetup;

public class ClipSetups
{
	final public ClipRotationAngles clipRotationAngles = new ClipRotationAngles();
	
	final public ClipAxesBounds clipAxesBounds;
	
	final public ClipCenters clipCenters;
	
	final public ConverterSetups converterSetups;
	
	public ClipSetups (final ConverterSetups converterSetups_)
	{
		converterSetups = converterSetups_;
		clipAxesBounds = new ClipAxesBounds(converterSetups);
		clipCenters = new ClipCenters(converterSetups);
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

//		AffineTransform3D clipPr = new AffineTransform3D();
	//	cs.getClipTransform( clipPr );
//		FinalRealInterval intold = cs.getClipInterval();
//		
//		double [ ] min = intold.minAsDoubleArray();
//		double [ ] max = intold.maxAsDoubleArray();
//		clipPr.apply( min, min );
//		clipPr.apply( max, max );
		//FinalRealInterval newInt = clipPr.estimateBounds( intold );
//		FinalRealInterval newInt = new FinalRealInterval(min,max);
		
		AffineTransform3D clipTr = new AffineTransform3D();

		final double [] center =  Misc.getIntervalCenter( cs.getClipInterval() );
		final double [] centerNew = clipCenters.getCenters( cs );
//		double [] centerNew =  new double [3];
//		for (int d=0; d<3; d++)
//		{
//			centerNew[d] = 100;
//		}
		//final double [] center = new double [3];
		//final double [] center_old = Misc.getIntervalCenterNegative( cs.getClipInterval() );
		//LinAlgHelpers.scale( center_old, -1.0, center );
		//clipPr.apply( center, center );
		//LinAlgHelpers.scale( center, -1.0, center );
//		final double [] centerold = Misc.getIntervalCenterShift( cs.getClipInterval() );
//		final double [] center = Misc.getIntervalCenterShift( newInt );
//		double dq = 0.0;
//		for(int d=0;d<3;d++)
//		{
//			dq+=Math.sqrt( Math.pow(center[d]-center_old[d],2) );
//		}
//		System.out.println(dq);
//		if(dq>4)
//		{
//			dq--;
//		}
	
		LinAlgHelpers.scale( center, -1.0, center );

		clipTr.translate( center );
		clipTr = clipTr.preConcatenate( clipRot );
		LinAlgHelpers.scale( center, -1.0, center );
		AffineTransform3D tr = new AffineTransform3D();
		
		tr.translate( centerNew );
		clipTr.translate( centerNew );	
		//clipTr = clipTr.preConcatenate( tr);
		
		double [] diff = new double [3];
		for (int d=0;d<3;d++)
		{
			diff[d] = centerNew[d]-center[d];
			//diff[d] = center[d]-centerNew[d];
		}
//		tr.identity();
//		tr.translate( diff );
		//clipTr.translate( diff );
		//clipTr = clipTr.preConcatenate( tr);
		cs.setClipTransform(clipTr);

		final double [] centerInt = Misc.getIntervalCenter( cs.getClipInterval() );
	
		String s = "";
		for (int d=0;d<3;d++)
			s = s + Double.toString( centerInt[d] )+" ";
		System.out.println("bake center before: " +s);

		clipTr.apply( centerInt, centerInt );
		s = "";
		for (int d=0;d<3;d++)
			s = s + Double.toString( centerInt[d] )+" ";
		System.out.println("bake center New: " +s);
		s = "";
		for (int d=0;d<3;d++)
		{			
			s = s + Double.toString( diff[d] )+" ";
		}
		System.out.println("bake diff: " +s);
		
		
	}
	
	
}
