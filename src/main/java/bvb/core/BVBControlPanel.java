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
import bvb.gui.TabPanelView;



public class BVBControlPanel extends JPanel
{
	final BigVolumeBrowser bvb;
	
	public int nDefaultWidth = 400;
	
	public int nDefaultHeight = 600;
	
	public JFrame cpFrame;
	
	JTabbedPane tabPane;

	final SelectedSources selectedSources;
	
	final TabPanelView tabPanelView;
	
	
	public BVBControlPanel(final BigVolumeBrowser bvb_) 
	{
		super(new GridBagLayout());
		bvb = bvb_;
		this.selectedSources = bvb.selectedSources;
		
		tabPane = new JTabbedPane(SwingConstants.LEFT);
		
		URL icon_path = this.getClass().getResource("/icons/cube_icon.png");
	    ImageIcon tabIcon = new ImageIcon(icon_path);
	    tabPanelView = new TabPanelView(bvb, selectedSources);
		tabPane.addTab("",tabIcon, tabPanelView, "View/Clip");
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
