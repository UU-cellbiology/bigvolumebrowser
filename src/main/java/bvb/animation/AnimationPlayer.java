package bvb.animation;

import javax.swing.Timer;

import bvb.core.BigVolumeBrowser;

public class AnimationPlayer
{
	final BigVolumeBrowser bvb;
	final AnimationPanel aPanel;
	private boolean playing = false;
    private final Timer timer;
    private long startTime;
    
    private boolean bSliderUpdateState;
    
	public AnimationPlayer(final BigVolumeBrowser bvb_, AnimationPanel aPanel_)
	{
		this.bvb = bvb_;
		this.aPanel = aPanel_;
		//this.timeSlider =  aPanel.timeSlider;
		timer = new Timer(1000 / 60, e -> tick());
	}
	
    public void play() 
    {
        if (playing) 
        	return;
        bSliderUpdateState = aPanel.bUpdateSlider;
        aPanel.bUpdateSlider = false;
		aPanel.butPlayStop.setIcon( aPanel.tabIconStop );
		aPanel.butPlayStop.setToolTipText( "Stop playing" );
        playing = true;
        startTime = System.nanoTime();
        timer.start();
    }
    
    public void stop() 
    {
        playing = false;
        aPanel.bUpdateSlider = bSliderUpdateState;
        timer.stop();
        aPanel.butPlayStop.setIcon( aPanel.tabIconPlay );
		aPanel.butPlayStop.setToolTipText( "Play Preview" );
    }
    
    private void tick() 
    {
        float t =
            (System.nanoTime() - startTime) / 1_000_000_000f;

        if (!playing) 
        {
            timer.stop();
            return;
        }

        if (t > aPanel.kfAnim.nTotalTime) 
        {
            //stop();
            //t = aPanel.kfAnim.nTotalTime;
            //loop
        	t = 0;
            startTime = System.nanoTime();
        }
        aPanel.updateScene( t );
        //float fTimePoint = (kfAnim.getTotalTime()) * (timeSlider.getValue() / (float)tsSpan);
        int nSliderPosition = ( int ) ( t*aPanel.tsSpan/aPanel.kfAnim.getTotalTime() );
        aPanel.timeSlider.setValue( nSliderPosition );
        //timeline.apply(t);
        // component.repaint();
    }
    
    public boolean isPlaying()
    {
    	return playing;
    }
}
