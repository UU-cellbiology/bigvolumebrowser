package bvb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import bvb.core.BigVolumeBrowser;



public class BVBControlPanel< T extends RealType< T > & NativeType< T > > extends JPanel
{
	BigVolumeBrowser<T> bvb;
	public JFrame cpFrame;
	JTabbedPane tabPane;
	public ClipPanel clipPanel;
	final SelectedSources selectedSources;
	
	public BVBControlPanel(final BigVolumeBrowser<T> bvb_) 
	{
		super(new GridBagLayout());
		bvb = bvb_;
		selectedSources = new SelectedSources(bvb.bvvViewer);
		
		tabPane = new JTabbedPane(SwingConstants.LEFT);
		URL icon_path = this.getClass().getResource("/icons/cube_icon.png");
	    ImageIcon tabIcon = new ImageIcon(icon_path);

	    clipPanel = new ClipPanel(bvb.bvv.getBvvHandle().getConverterSetups(), selectedSources);
	    clipPanel.setBorder(new PanelTitle(" Clipping "));
	    tabPane.addTab("",tabIcon, clipPanel, "View/Clip");
	    tabPane.setSize(350, 300);
	    tabPane.setSelectedIndex(0);
	    GridBagConstraints cv = new GridBagConstraints();
	    cv.gridx = 0;
	    cv.gridy = 0;	    
	    cv.weightx = 0.5;
	    cv.weighty = 1.0;
	    cv.anchor = GridBagConstraints.NORTHWEST;
	    cv.gridwidth = GridBagConstraints.REMAINDER;
	    cv.fill = GridBagConstraints.HORIZONTAL;
	    cv.fill = GridBagConstraints.BOTH;
	  
	    this.add(tabPane,cv);
	}
}
