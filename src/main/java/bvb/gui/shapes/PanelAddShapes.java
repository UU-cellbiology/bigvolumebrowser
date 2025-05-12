package bvb.gui.shapes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import bvb.core.BigVolumeBrowser;
import bvb.gui.PanelTitle;

public class PanelAddShapes extends JPanel
{
	JButton butSpots;
	
	JButton butMesh;
	
	final BigVolumeBrowser bvb;
	public PanelAddShapes(final BigVolumeBrowser bvb_)
	{
		super(new GridBagLayout());	
		
		bvb = bvb_;
		this.setBorder(new PanelTitle(" Add geometry "));
	    GridBagConstraints gbc = new GridBagConstraints();
		URL icon_path = this.getClass().getResource("/icons/spots.png");
	    ImageIcon tabIcon = new ImageIcon(icon_path);
	    butSpots = new JButton(tabIcon);
	    butSpots.setToolTipText("Import spots from text file");
	    
		icon_path = this.getClass().getResource("/icons/mesh.png");
	    tabIcon = new ImageIcon(icon_path);
	    butMesh = new JButton(tabIcon);
	    butMesh.setToolTipText("Import meshes");
	    
	    gbc.insets = new Insets(4,3,4,3);

	    gbc.gridx = 0;
	    gbc.gridy = 0;

	    this.add( butSpots,gbc);

	    gbc.gridx++;
	    this.add( butMesh,gbc);

//	    gbc.gridx++;
//	    this.add( butBDVXML,gbc);
	}
}
