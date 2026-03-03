/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
    private float fIncSign = 1;
    
    private boolean bSliderUpdateState;
    
	public AnimationPlayer(final BigVolumeBrowser bvb_, AnimationPanel aPanel_)
	{
		this.bvb = bvb_;
		this.aPanel = aPanel_;
		timer = new Timer(1000 / 30, e -> tick());
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
        fIncSign = 1;
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

    	currentTime += fIncSign * deltaSeconds * playbackSpeed;

        if (!playing) 
        {
            timer.stop();
            return;
        }

        if (currentTime > aPanel.kfAnim.nTotalTime) 
        {
        	if(!aPanel.bPlayerBackForth)
			{
        		currentTime = 0;
			}
        	else 
        	{
        		fIncSign = -1;
        		currentTime = aPanel.kfAnim.nTotalTime;
        	}
        }
		if(currentTime < 0)
		{
			fIncSign = 1;
			currentTime = 0;
		}	
        aPanel.updateScene( currentTime );	
        
        // update slider
        int nSliderPosition = ( int ) ( currentTime * aPanel.tsSpan/aPanel.kfAnim.getTotalTime() );
        aPanel.timeSlider.setValue( nSliderPosition );

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
