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
package bvb.core;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;


import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import bvb.gui.SelectedSources;
import bvb.gui.TabPanelDataSources;
import bvb.gui.TabPanelInfo;
import bvb.gui.TabPanelShapes;
import bvb.gui.TabPanelView;



public class BVBControlPanel extends JPanel
{
	final BigVolumeBrowser bvb;
		
	public JFrame cpFrame;
	
	public JTabbedPane tabPane;

	final SelectedSources selectedSources;
	
	final public TabPanelDataSources tabPanelDataSources;
	
	final TabPanelView tabPanelView;
	
	final TabPanelShapes tabPanelShapes;
	
	final TabPanelInfo tabPanelInfo;
	
	
	public BVBControlPanel(final BigVolumeBrowser bvb_) 
	{
		super(new GridBagLayout());
		bvb = bvb_;
		this.selectedSources = bvb.selectedSources;
		
		tabPane = new JTabbedPane(SwingConstants.LEFT);
		
		
		URL  icon_path = this.getClass().getResource("/icons/load_sources.png");
		ImageIcon tabIcon = new ImageIcon(icon_path);
	    tabPanelDataSources = new TabPanelDataSources(bvb_);	    
		tabPane.addTab("",tabIcon, tabPanelDataSources, "Data sources");
		
		icon_path = this.getClass().getResource("/icons/view.png");
	    tabIcon = new ImageIcon(icon_path);
	    tabPanelView = new TabPanelView(bvb);
		tabPane.addTab("",tabIcon, tabPanelView, "View/Clip");
		
		icon_path = this.getClass().getResource("/icons/shapes.png");
		tabIcon = new ImageIcon(icon_path);
	    tabPanelShapes = new TabPanelShapes(bvb);	    
		tabPane.addTab("",tabIcon, tabPanelShapes, "Shapes");
		
		icon_path = this.getClass().getResource("/icons/shortcut.png");
	    tabIcon = new ImageIcon(icon_path);
	    tabPanelInfo = new TabPanelInfo();	    
		tabPane.addTab("",tabIcon, tabPanelInfo, "Shortcuts");
		
	    tabPane.setSize(350, 300);
	    tabPane.setSelectedIndex(0);
	    
	    
	    final GridBagConstraints gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;	    
	    gbc.weightx = 0.5;
	    gbc.weighty = 1.0;
	    gbc.anchor = GridBagConstraints.NORTHWEST;
	    gbc.gridwidth = GridBagConstraints.REMAINDER;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.fill = GridBagConstraints.BOTH;
	  
	    this.add(tabPane,gbc);
	    
	    //install actions from BVB	    
	    this.setActionMap(bvb.bvbActions.getActionMap());
	    this.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,bvb.bvbActions.getInputMap());

	}
	

	
}
