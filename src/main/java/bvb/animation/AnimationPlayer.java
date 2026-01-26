package bvb.animation;

import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.SwingWorker;

import bvb.core.BigVolumeBrowser;

public class AnimationPlayer extends SwingWorker<Void, String>
{
	
		final BigVolumeBrowser bvb;
		final AnimationPanel aPanel;
		final JSlider timeSlider;
		
		public boolean bLoopBackAndForth = false;

		JButton butPlayStop = null;
		ImageIcon tabIconPlay = null;
		boolean bUpdateSliderOff = true;
		
		public AnimationPlayer(final BigVolumeBrowser bvb_, AnimationPanel aPanel_)
		{
			this.bvb = bvb_;
			this.aPanel = aPanel_;
			this.timeSlider =  aPanel.timeSlider;

		}

		@Override
		protected Void doInBackground() throws Exception
		{
			int currVal = timeSlider.getValue();
			long dWaitPure = Math.round( 1000.0f * aPanel.kfAnim.getTotalTime() / aPanel.tsSpan);
			int dInc = 1;
			long dWait;
			if(aPanel.bUpdateSlider == false)
			{
				bUpdateSliderOff = true;
				aPanel.bUpdateSlider = true;
			}
			else
			{
				bUpdateSliderOff = false;
			}
			while(true)
			{
				dWait = Math.round( dWaitPure / aPanel.fPlaySpeedFactor);
				Thread.sleep(Math.round( dWait ));
				currVal += dInc;
				if(currVal > timeSlider.getMaximum())
				{
					if(!aPanel.bPlayerBackForth)
					{
						currVal = timeSlider.getMinimum();
					}
					else
					{
						dInc = -1;
						currVal = timeSlider.getMaximum()-1;
					}
				}
				if(currVal < timeSlider.getMinimum())
				{
					dInc = 1;
					currVal = 1;
				}				
				
				timeSlider.setValue( currVal );
				
				if(isCancelled())
				{
					return null;	
				}
			}

		}
	    /*
	     * Executed in event dispatching thread
	     */
	    @Override
	    public void done() 
	    {
	    	//see if we have some errors
	    	try {

	    		get();
	    	} 
	    	catch (ExecutionException e) 
	    	{
	    		e.getCause().printStackTrace();
	    		String msg = String.format("Unexpected error during playing: %s", 
	    				e.getCause().toString());
	    		System.out.println(msg);
	    	} 
	    	catch (InterruptedException e) 
	    	{
	    		// Process e here
	    	}
	    	catch (Exception e)
	    	{

	    	}
		

	    	if(butPlayStop != null && tabIconPlay!= null)
	    	{
	    		butPlayStop.setIcon( tabIconPlay );
	    		butPlayStop.setToolTipText( "Play" );
	    		if(bUpdateSliderOff)
	    		{
	    			aPanel.bUpdateSlider = false;
	    		}
	    	}
	    	//unlock user interaction
	    	bvb.setInputLock( false );

	    }
}
