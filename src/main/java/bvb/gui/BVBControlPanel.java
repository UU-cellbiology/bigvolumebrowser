package bvb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import bvb.core.BigVolumeBrowser;



public class BVBControlPanel extends JPanel
{
	final BigVolumeBrowser bvb;
	public JFrame cpFrame;
	JTabbedPane tabPane;

	final SelectedSources selectedSources;
	
	public BVBControlPanel(final BigVolumeBrowser bvb_) 
	{
		super(new GridBagLayout());
		bvb = bvb_;
		selectedSources = new SelectedSources(bvb.bvvViewer);
		
		tabPane = new JTabbedPane(SwingConstants.LEFT);
		URL icon_path = this.getClass().getResource("/icons/cube_icon.png");
	    ImageIcon tabIcon = new ImageIcon(icon_path);   
		tabPane.addTab("",tabIcon, panelView(), "View/Clip");
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
	}
	
	JPanel panelView()
	{
		JPanel panTabView = new JPanel(new GridBagLayout());
		
	    final GridBagConstraints gbc = new GridBagConstraints();

	    ClipPanel clipPanel = new ClipPanel(bvb, selectedSources);		
	    //add panels to Navigation
	    gbc.insets = new Insets(4,4,2,2);
	    //View
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 1;
	    gbc.anchor = GridBagConstraints.WEST;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    panTabView.add(clipPanel,gbc);
	    
	    return panTabView;
	}
}
