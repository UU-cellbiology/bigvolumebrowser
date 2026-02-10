package bvb.animation;

import javax.swing.Timer;

import bvb.core.BigVolumeBrowser;

public class AnimationPlayer
{
	final BigVolumeBrowser bvb;
	final AnimationPanel aPanel;
	private boolean playing = false;
    private final Timer timer;
    private float playbackSpeed = 1.0f;
    private float currentTime = 0f;
    private long lastTickNanos;
    
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
        lastTickNanos = System.nanoTime();
        currentTime = aPanel.timeSlider.getValue() * (float) aPanel.kfAnim.nTotalTime / aPanel.tsSpan ;
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
    	
    	long now = System.nanoTime();
    	float deltaSeconds =
    			(now - lastTickNanos) / 1_000_000_000f;

    	lastTickNanos = now;

    	currentTime += deltaSeconds * playbackSpeed;

        if (!playing) 
        {
            timer.stop();
            return;
        }

        if (currentTime > aPanel.kfAnim.nTotalTime) 
        {
            //stop();
            //t = aPanel.kfAnim.nTotalTime;
            //loop
        	currentTime = 0;
        }
        aPanel.updateScene( currentTime );
        //float fTimePoint = (kfAnim.getTotalTime()) * (timeSlider.getValue() / (float)tsSpan);
        int nSliderPosition = ( int ) ( currentTime * aPanel.tsSpan/aPanel.kfAnim.getTotalTime() );
        aPanel.timeSlider.setValue( nSliderPosition );
        //timeline.apply(t);
        // component.repaint();
    }
    
    public boolean isPlaying()
    {
    	return playing;
    }
    
    public void setPlaybackSpeed (final float fSpeed)
    {
    	playbackSpeed = fSpeed;
    }
    public float getPlaybackSpeed ()
    {
    	return playbackSpeed;
    }
}
