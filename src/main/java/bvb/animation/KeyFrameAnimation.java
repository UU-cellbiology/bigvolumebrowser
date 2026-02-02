package bvb.animation;

import java.util.ArrayList;

import javax.swing.DefaultListModel;

import net.imglib2.realtransform.AffineTransform3D;

import bvb.gui.AnisotropicTransformAnimator3D;

public class KeyFrameAnimation
{
	int nTotalTime;
	
	final public DefaultListModel<KeyFrame> keyFrames;
	
	final ArrayList<AnisotropicTransformAnimator3D> viewAnimate = new ArrayList<>(); 
	
	final ArrayList<Float> timeIntervals = new ArrayList<>();
	
	final ArrayList<KeyFrame> fullList = new ArrayList<>();
	
	public KeyFrameAnimation( final DefaultListModel<KeyFrame> keyFrames_)
	{
		keyFrames = keyFrames_;
	}
	
	public void setTotalTime(int t)
	{
		nTotalTime = t;
	}
	
	public int getTotalTime()
	{
		return nTotalTime;
	}
	
	public void updateTransitionTimeline()
	{
		timeIntervals.clear();
		viewAnimate.clear();
		fullList.clear();
		if(keyFrames.size() == 0)
			return;
		
		fullList.add( keyFrames.get( 0 ) );
		timeIntervals.add( new Float(0.0f) );
		for (int i = 0; i < keyFrames.size(); i++)
		{
			fullList.add( keyFrames.get( i ) );
			timeIntervals.add(keyFrames.get( i ).fMovieTimePoint);
		}
		fullList.add( keyFrames.get( keyFrames.size() - 1 ) );
		timeIntervals.add((float)nTotalTime);
		for(int i = 0; i < keyFrames.size() + 1; i++)
		{
			viewAnimate.add( new AnisotropicTransformAnimator3D(
					fullList.get(i).getSceneView().getViewerTransform(),
					fullList.get(i+1).getSceneView().getViewerTransform(),5) );
		}
	}
	
	SceneView getSceneView(float fTimePoint_in)
	{
		float fTimePoint;
		if(fTimePoint_in < 0)
		{
			fTimePoint = 0.0f;
		}
		else
		{
			if(fTimePoint_in > timeIntervals.get( timeIntervals.size()-1 ))
			{
				fTimePoint = timeIntervals.get( timeIntervals.size()-1 );
			}
			else
			{
				fTimePoint = fTimePoint_in;
			}
		}
		//find an interval where timepoint lies
		int nIndex = 0;
		for(; nIndex < timeIntervals.size(); nIndex++)
		{
			if(fTimePoint < timeIntervals.get( nIndex ))
			{
				break;
			}
			
		}
		
		double fraction;
		double dNorm;
		if(nIndex >= timeIntervals.size() - 1)
		{
			nIndex = timeIntervals.size() - 1;
			dNorm = (nTotalTime - timeIntervals.get( nIndex-1 ));			
		}
		else
		{
			dNorm  = (timeIntervals.get( nIndex ) - timeIntervals.get( nIndex-1 ));
		}
		if(dNorm < 0.00000001)
		{
			fraction = 0;
		}
		else
		{
			fraction = (fTimePoint - timeIntervals.get( nIndex-1 ))/dNorm;
		}
		
		//time frame
		int nIniFrame = fullList.get(nIndex-1).getSceneView().getTimeFrame();
		int nNextFrame = fullList.get(nIndex).getSceneView().getTimeFrame();
		int nTimeFrame = ( int ) ( nIniFrame + Math.round( fraction * (nNextFrame - nIniFrame)) );
		// transform camera view
		final AffineTransform3D finalAT = viewAnimate.get( nIndex - 1 ).get( fraction );
	
		return new SceneView( finalAT, nTimeFrame );
	}
}
