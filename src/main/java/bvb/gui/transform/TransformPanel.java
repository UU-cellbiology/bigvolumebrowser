package bvb.gui.transform;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

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
	
	JButton butCenterTranslate;
	
	final ImageIcon [] translateIcon = new ImageIcon[2];
	
	final String[] translateToolTip = new String[2];
	
	final JTabbedPane tabTrPane;
	
	public TransformPanel(final BigVolumeBrowser bvb_)
	{
		super();
		bvb = bvb_;
		
		GridBagLayout gridbag = new GridBagLayout();

		setLayout(gridbag);

		transformSetups = new TransformSetups(bvb);
		
		transformScalePanel = new TransformScalePanel(transformSetups);
		
		transformCentersPanel = new TransformCenterPanel(transformSetups);
		
		transformRotationPanel = new TransformRotationPanel(transformSetups);

		
		tabTrPane = new JTabbedPane(SwingConstants.TOP);

		tabTrPane.addTab( "Center", transformCentersPanel );
		tabTrPane.addTab( "Rotate", transformRotationPanel );
		tabTrPane.addTab( "Scale", transformScalePanel );
		
		if(!transformSetups.bLocalCoordinates)
		{
			tabTrPane.setTitleAt( 0, "Translate" );
		}	
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0.3;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		
		
		cbTransformClip = new JCheckBox("Modify clip", true);
		cbTransformClip.setSelected( transformSetups.bTransformClip );
		cbTransformClip.addItemListener((e)->
		{
			transformSetups.bTransformClip = cbTransformClip.isSelected();
			Prefs.set( "BVB.bTransformClip", transformSetups.bTransformClip );
		} );
		this.add(cbTransformClip,gbc);
		
		
		translateToolTip[0] = "Using translate panel";
		translateToolTip[1] = "Using center panel";

		URL icon_path = this.getClass().getResource("/icons/transform_translate.png");
		translateIcon[0] = new ImageIcon(icon_path);
		icon_path = this.getClass().getResource("/icons/transform_center.png");
		translateIcon[1] = new ImageIcon(icon_path);
		gbc.gridx ++;
		gbc.weightx = 0.6;
		butCenterTranslate = new JButton(translateIcon[transformSetups.bLocalCoordinates?1:0]);
		butCenterTranslate.setToolTipText(translateToolTip[transformSetups.bLocalCoordinates?1:0]);

		butCenterTranslate.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent arg0 )
			{
				transformSetups.bLocalCoordinates = !transformSetups.bLocalCoordinates;
				int ind = 0;
				
				if(transformSetups.bLocalCoordinates)
				{
					tabTrPane.setTitleAt( 0, "Center" );
					ind = 1;
				}
				else
					tabTrPane.setTitleAt( 0, "Translate" );
				butCenterTranslate.setIcon( translateIcon[ind]  );
				butCenterTranslate.setToolTipText(translateToolTip[ind]);
				Prefs.set( "BVB.bCenterPanel", transformSetups.bLocalCoordinates );
				transformCentersPanel.updateGUI();
				//updateGUI();
			}
	
		});
		this.add(butCenterTranslate,gbc);			
		
		gbc.weightx = 0.05;
		gbc.fill = GridBagConstraints.NONE;
		icon_path = this.getClass().getResource("/icons/red_cross.png");
		ImageIcon icon = new ImageIcon(icon_path);
		butResetCurrent = new JButton(icon);
		butResetCurrent.setToolTipText( "Reset current panel" );
		butResetCurrent.addActionListener((e)->	resetPanelTransform()); 
		gbc.gridx ++;
		this.add(butResetCurrent,gbc);	
		
		icon_path = this.getClass().getResource("/icons/red_crossx2.png");
		icon = new ImageIcon(icon_path);
		butResetAll = new JButton(icon);
		butResetAll.setToolTipText( "Reset all transforms" );
		butResetAll.addActionListener((e)->	resetFullTransform()); 

		gbc.gridx ++;
		this.add(butResetAll,gbc);	
		
		
		gbc.gridx = 0;
	    gbc.gridy ++;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 4;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
		
	    this.add(tabTrPane,gbc);
	    
	    setupListeners();
	    
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
		transformRotationPanel.updateGUI();
	}
	
	private void setPanelsEnabled(boolean bEnabled)
	{
		transformScalePanel.setEnabled( bEnabled );
		transformCentersPanel.setEnabled( bEnabled );
		transformRotationPanel.setEnabled( bEnabled );
	}
	
	public void setupListeners()
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
		if(!transformSetups.selectedObjects.isAnythingSelected())
			return;
		
		final double [] unitScale = new double [3];
		
		for (int d=0; d<3;d++)
		{
			unitScale [d] = 1.0;
		}
		
		transformCentersPanel.resetCenters();
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final double [] prevAngles =  new double[3];
			final double [] eAngles = transformSetups.transformRotation.getAngles( obj );
			for(int d=0;d<3;d++)
			{
				prevAngles[d] = eAngles [d];
			}
			transformSetups.transformRotation.setAngles( obj,  new double [3] );
			transformSetups.transformScale.setScale( obj, unitScale );
			transformSetups.updateTransform( obj, prevAngles );
		}
		updateGUI();
	}
	
	
}
