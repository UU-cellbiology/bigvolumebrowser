/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
package bvb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import bvb.core.BigVolumeBrowser;
import bvb.gui.clip.ClipPanel;
import bvb.gui.transform.TransformPanel;

public class TabPanelView extends JPanel
{
	final BigVolumeBrowser bvb;
	
	final public ViewPanel viewPanel;

	final public SourcesRenderPanel sourcesRenderPanel;

	public ClipPanel clipPanel;
	
	public TransformPanel transformPanel;
	
	public TabPanelView(final BigVolumeBrowser bvb_)
	{
		super(new GridBagLayout());	
		
		bvb = bvb_;   
	    
	    viewPanel = new ViewPanel(bvb);
	    
	    sourcesRenderPanel = new SourcesRenderPanel(bvb.bvv.getBvvHandle().getConverterSetups(), bvb.selectedSources);
	    
	    clipPanel = new ClipPanel(bvb);		
	    
	    transformPanel = new TransformPanel(bvb);
	   
	    GridBagConstraints gbc = new GridBagConstraints();
	    
	    //add panels to Navigation
	    gbc.insets = new Insets(4,3,4,3);
	    //View
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 1;
	    gbc.anchor = GridBagConstraints.NORTHWEST;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    JPanel both = new JPanel(new GridBagLayout());
	    
	    GridBagConstraints c = new GridBagConstraints();
	    c.insets = new Insets(3,0,3,0);
	    both.add( viewPanel,c);
	    c.gridx++;
	    both.add( sourcesRenderPanel,c );
	    
	    both.setBorder(new PanelTitle(" View "));

	    this.add( both, gbc );

	    gbc.gridy++;
	    this.add(clipPanel,gbc);
	    
	    gbc.gridy++;
	    this.add(transformPanel,gbc);
	    
        // Blank/filler component
	    gbc.gridy++;
	    gbc.weightx = 0.01;
	    gbc.weighty = 0.01;
	    this.add(new JLabel(), gbc);	
	}
	


	public void resetClipPanel()
	{
		this.remove( clipPanel );
		clipPanel = new ClipPanel(bvb);
		clipPanel.setSourceListeners();
		
	    GridBagConstraints gbc = new GridBagConstraints();
	    
	    //add panels to Navigation
	    gbc.insets = new Insets(4,3,4,3);
	    //View
	    gbc.gridx = 0;
	    gbc.gridy = 1;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 1;
	    gbc.anchor = GridBagConstraints.NORTHWEST;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add( clipPanel,gbc );
	}
}
	
