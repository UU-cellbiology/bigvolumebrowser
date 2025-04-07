package bvb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import bvb.core.BigVolumeBrowser;

public class ViewPanel extends JPanel
{
	JToggleButton butOrigin;
	JToggleButton butVBox;
	JButton butFullScreen;
	JButton butSettings;
	
	public ViewPanel(final BigVolumeBrowser bvb)
	{
		super();
		setLayout(new GridBagLayout());
		
		//this.setBorder(new PanelTitle(" View "));
		
		//ORIGIN
		URL icon_path = this.getClass().getResource("/icons/orig.png");
	    ImageIcon tabIcon = new ImageIcon(icon_path);
	    butOrigin = new JToggleButton(tabIcon);
	    //butOrigin.setSelected(btdata.bShowOrigin);
	    butOrigin.setToolTipText("Show XYZ axes");
	    
	    //BOX AROUND
		icon_path = this.getClass().getResource("/icons/boxvolume.png");
	    tabIcon = new ImageIcon(icon_path);
	    butVBox = new JToggleButton(tabIcon);
	    //butVBox.setSelected(btdata.bVolumeBox);
	    butVBox.setToolTipText("Volume Box");
	    //butVBox.addItemListener(new ItemListener() {
	    
	    //FULL SCREEN
		icon_path = this.getClass().getResource("/icons/fullscreen.png");
	    tabIcon = new ImageIcon(icon_path);
	    butFullScreen = new JButton(tabIcon);
	    //butVBox.setSelected(btdata.bVolumeBox);
	    butFullScreen.setToolTipText("Full Screen");
	    butFullScreen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent arg0 )
			{
				bvb.makeFullScreen();								
			}
	    	
	    });	    
	    
		//SETTINGS
		icon_path = this.getClass().getResource("/icons/settings.png");
	    tabIcon = new ImageIcon(icon_path);
	    butSettings = new JButton(tabIcon);
	    butSettings.setToolTipText("Settings");
	    //butSettings.addActionListener(this);
	    
	    GridBagConstraints gbc = new GridBagConstraints();

	    gbc.gridx = 0;
	    gbc.gridy = 0;
		this.add(butOrigin,gbc);
		
		gbc.gridx++;	    
		this.add(butVBox,gbc);
		
		gbc.gridx++;	    
		this.add(butFullScreen,gbc);
		
		gbc.gridx++;	    
		this.add(butSettings,gbc);

	}
}
