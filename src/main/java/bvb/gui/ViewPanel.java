package bvb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class ViewPanel extends JPanel
{
	JToggleButton butOrigin;
	JToggleButton butVBox;
	JButton butSettings;
	
	public ViewPanel()
	{
		super();
		setLayout(new GridBagLayout());
		
		this.setBorder(new PanelTitle(" View "));
		
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
		this.add(butSettings,gbc);

	}
}
