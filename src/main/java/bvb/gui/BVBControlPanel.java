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

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import bvb.core.BigVolumeBrowser;



public class BVBControlPanel< T extends RealType< T > & NativeType< T > > extends JPanel
{
	BigVolumeBrowser<T> bvb;
	public JFrame cpFrame;
	JTabbedPane tabPane;
	public ClipRangePanel clipRangePanel;
	final SelectedSources selectedSources;
	
	public BVBControlPanel(final BigVolumeBrowser<T> bvb_) 
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

	    clipRangePanel = new ClipRangePanel(bvb.bvv.getBvvHandle().getConverterSetups(), selectedSources);
	    ClipRotationPanel clipRotationPanel = new ClipRotationPanel(selectedSources); 
		
	    //Clipping Panel
		JPanel panClip = new JPanel(new GridBagLayout()); 
		panClip.setBorder(new PanelTitle(" Clipping "));

		JTabbedPane tabClipPane = new JTabbedPane(SwingConstants.TOP);
		tabClipPane.addTab( "Range", clipRangePanel );
		tabClipPane.addTab( "Rotation", clipRotationPanel );
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 1.0;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    panClip.add(tabClipPane,gbc);
		
	    //add panels to Navigation
	    gbc.insets = new Insets(4,4,2,2);
	    //View
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 1;
	    gbc.anchor = GridBagConstraints.WEST;
		
	    panTabView.add(panClip,gbc);
	    
	    return panTabView;
	}
}
