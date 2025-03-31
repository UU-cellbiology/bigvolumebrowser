package bvb.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.UIUtils;
import bvb.core.BigVolumeBrowser;
import bvb.utils.ClipSetups;
import bvvpg.source.converters.GammaConverterSetup;

public class ClipPanel extends JPanel implements ItemListener
{
	final BigVolumeBrowser bvb;
	final SelectedSources selectedSources;
	public JCheckBox cbClipEnabled;
	public JLabel selectionWindow;
	
	final ClipRangePanel clipRangePanel;
	final ClipRotationPanel clipRotationPanel;
	final ClipSetups clipSetups;
	
	/**
	 * Panel background if color reflects a set of sources all having the same color
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if color reflects a set of sources with different colors
	 */
	private Color inConsistentBg = Color.WHITE;
	
	public ClipPanel(final BigVolumeBrowser bvb_, final SelectedSources selectedSources_)
	{
		super();
		bvb = bvb_;
		selectedSources = selectedSources_;

		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);
		this.setBorder(new PanelTitle(" Clipping "));

		clipSetups = new ClipSetups(bvb.bvv.getBvvHandle().getConverterSetups());
		
		clipRangePanel = new ClipRangePanel(selectedSources, clipSetups);
	    clipRotationPanel = new ClipRotationPanel(selectedSources, clipSetups); 

		JTabbedPane tabClipPane = new JTabbedPane(SwingConstants.TOP);
		tabClipPane.addTab( "Range", clipRangePanel );
		tabClipPane.addTab( "Rotation", clipRotationPanel );
		
		GridBagConstraints gbc = new GridBagConstraints();
	
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		
		cbClipEnabled = new JCheckBox("Clipping", false);
		this.add(cbClipEnabled,gbc);
		cbClipEnabled.addItemListener( this );
		
		selectionWindow = new JLabel("Selected: None");
		gbc.gridx++;
		this.add(selectionWindow,gbc);
		
		gbc.gridx = 0;
	    gbc.gridy ++;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 2;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    this.add(tabClipPane,gbc);
	    
	    selectedSources.addSourceSelectionListener(  new SelectedSources.Listener()
		{			
			@Override
			public void selectedSourcesChanged( )
			{
				updateGUI();
			}
		} );
	    updateGUI();
	}
	
	
	private synchronized void updateGUI()
	{
		updateColors();
		switch (selectedSources.getActiveWindow())
		{
		case 0:
			selectionWindow.setText( "Selected: Sources");
			break;
		case 1:
			selectionWindow.setText( "Selected: Groups");
			break;
		default:
			selectionWindow.setText( "Selected: None");
		}	
		final List< ConverterSetup > csList = selectedSources.getSelectedSources();
		if(csList.size()==0)
		{
			clipRangePanel.setEnabled( false );
			clipRotationPanel.setEnabled( false );
			return;
		}
		clipRangePanel.setEnabled( true );
		clipRotationPanel.setEnabled( true );
		
		//consistent clipping
		boolean bClipConsistent = true;
		int bClipEnabled = -1;
		for ( final ConverterSetup cs: csList)
		{
			 
			if(bClipEnabled<0)
			{
				bClipEnabled = ((GammaConverterSetup)cs).clipActive()?1:0;	
			}
			else
			{
				bClipConsistent &= (bClipEnabled==(((GammaConverterSetup)cs).clipActive()?1:0));
			}
			
		}
		if(bClipConsistent)
		{
			cbClipEnabled.setBackground( consistentBg );
			cbClipEnabled.setSelected( bClipEnabled != 0 );
			clipRangePanel.setEnabled(bClipEnabled != 0);
			clipRotationPanel.setEnabled(bClipEnabled != 0);
		}
		else
		{
			clipRangePanel.setEnabled(cbClipEnabled.isSelected());
			clipRotationPanel.setEnabled(cbClipEnabled.isSelected());
			cbClipEnabled.setBackground( inConsistentBg );
		}
		
	}
	
	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}
	
	private void updateClipEnabled()
	{
		boolean bEnabled = cbClipEnabled.isSelected();
		
		final List< ConverterSetup > csList = selectedSources.getSelectedSources();
		
		for ( final ConverterSetup cs: csList)
		{
			  ((GammaConverterSetup)cs).setClipActive( bEnabled );
		}
	}
	@Override
	public void itemStateChanged( ItemEvent arg0 )
	{
		if(arg0.getSource() == cbClipEnabled)
			updateClipEnabled();
		updateGUI();
	}
}
