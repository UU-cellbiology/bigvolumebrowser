package bvb.gui.transform;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import bvb.core.BigVolumeBrowser;
import bvb.utils.transform.TransformSetups;
import ij.Prefs;

public class TransformPanel extends JPanel 
{
	final BigVolumeBrowser bvb;
	
	final public TransformSetups transformSetups;
	
	final TransformScalePanel transformScalePanel;
	
	final TransformCenterPanel transformCentersPanel;
	
	final TransformRotationPanel transformRotationPanel;
	
	final JCheckBox cbTransformClip;
	
	final JButton butResetCurrent;
	
	final JButton butResetAll;
	
	final JTabbedPane tabTrPane;
	
	public TransformPanel(final BigVolumeBrowser bvb_)
	{
		super();
		bvb = bvb_;
		
		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);
		//this.setBorder(new PanelTitle(" Transform "));

		transformSetups = new TransformSetups(bvb);
		
		transformScalePanel = new TransformScalePanel(transformSetups);
		
		transformCentersPanel = new TransformCenterPanel(transformSetups);
		
		transformRotationPanel = new TransformRotationPanel(transformSetups);
		
		tabTrPane = new JTabbedPane(SwingConstants.TOP);
		//URL icon_path = this.getClass().getResource("/icons/rotate.png");
	    //ImageIcon tabIcon = new ImageIcon(icon_path);
		tabTrPane.addTab( "Scale", transformScalePanel );
		tabTrPane.addTab( "Center", transformCentersPanel );
		tabTrPane.addTab( "Rotate", transformRotationPanel );
		
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0.9;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		
		
		cbTransformClip = new JCheckBox("Modify clip", true);
		cbTransformClip.setSelected( transformSetups.bTransformClip );
		cbTransformClip.addItemListener((e)->
		{
			transformSetups.bTransformClip = cbTransformClip.isSelected();
			Prefs.set( "BVB.bTransformClip", transformSetups.bTransformClip );
		} );
		this.add(cbTransformClip,gbc);
		
		gbc.weightx = 0.05;
		gbc.fill = GridBagConstraints.NONE;
		URL icon_path = this.getClass().getResource("/icons/red_cross.png");
		ImageIcon icon = new ImageIcon(icon_path);
		butResetCurrent = new JButton(icon);
		butResetCurrent.setToolTipText( "Reset current panel" );
		butResetCurrent.addActionListener((e)->	resetPanelTransform()); 
		gbc.gridx ++;
		this.add(butResetCurrent,gbc);	
		
		icon_path = this.getClass().getResource("/icons/red_crossx2.png");
		icon = new ImageIcon(icon_path);
		butResetAll = new JButton(icon);
		butResetAll.setToolTipText( "Reset all clip" );
		butResetAll.addActionListener((e)->	resetFullTransform()); 

		gbc.gridx ++;
		this.add(butResetAll,gbc);	
		
		
		gbc.gridx = 0;
	    gbc.gridy ++;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 3;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
		
	    this.add(tabTrPane,gbc);
	    
	    setSourceListeners();
	    
	    updateGUI();
	    
	    Color [] colors = new Color[3];
	    colors[0] =  new Color(198,34,0);
	    colors[1] =  new Color(67,154,0);
	    colors[2] =  new Color(0,34,213);

	    this.setSliderColors( colors );
	    

	}
	public synchronized void updateGUI()
	{
		if(!transformSetups.selectedObjects.isAnythingSelected())
		{
			setPanelsEnabled(false);
			cbTransformClip.setEnabled( false );
			return;
		}
		
		setPanelsEnabled(true);
		cbTransformClip.setEnabled( true );
		transformScalePanel.updateGUI();
		transformCentersPanel.updateGUI();
	}
	
	private void setPanelsEnabled(boolean bEnabled)
	{
		transformScalePanel.setEnabled( bEnabled );
		transformCentersPanel.setEnabled( bEnabled );
		transformRotationPanel.setEnabled( bEnabled );
	}
	
	public void setSourceListeners()
	{
		
		transformSetups.selectedObjects.addObjectSelectionListener( () -> updateGUI());		
	    //add listener in case number of sources, etc change
		transformSetups.converterSetups.listeners().add( s -> updateGUI() );

	}
	
	void setSliderColors(Color [] colors)
	{		
		transformCentersPanel.setSliderColors( colors );
		transformRotationPanel.setSliderColors( colors );		
	}
	
	void resetPanelTransform()
	{
		switch(tabTrPane.getSelectedIndex())
		{
		case 0:
			transformScalePanel.resetScale();
			break;
		case 1:
			transformCentersPanel.resetCenters();
			break;
		case 2:
			transformRotationPanel.resetRotation();
			break;
		default:
		}
	}
	
	void resetFullTransform()
	{
		
		//for now
		transformScalePanel.resetScale();
		transformCentersPanel.resetCenters();
		transformRotationPanel.resetRotation();
	}
}
