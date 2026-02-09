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
    
	public AnimationPlayer(final BigVolumeBrowser bvb_, AnimationPanel aPanel_)
	{
		this.bvb = bvb_;
		this.aPanel = aPanel_;
		//this.timeSlider =  aPanel.timeSlider;
		timer = new Timer(1000 / 60, e -> tick());
	}
	
    public void play() 
    {
        if (playing) return;
		aPanel.butPlayStop.setIcon( aPanel.tabIconStop );
		aPanel.butPlayStop.setToolTipText( "Stop playing" );
        playing = true;
        startTime = System.nanoTime();
        timer.start();
    }
    
    public void stop() 
    {
        playing = false;
        timer.stop();
        aPanel.butPlayStop.setIcon( aPanel.tabIconPlay );
		aPanel.butPlayStop.setToolTipText( "Play" );
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

        if (t >= aPanel.kfAnim.nTotalTime) 
        {
            stop();
            t = aPanel.kfAnim.nTotalTime;
        }
        aPanel.updateScene( t );
        //timeline.apply(t);
        // component.repaint();
    }
    
    public boolean isPlaying()
    {
    	return playing;
    }
}
