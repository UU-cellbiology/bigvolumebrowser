/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bvb.gui.clip;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;


import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.UIUtils;
import bvb.clip.ClipSetups;
import bvb.core.BigVolumeBrowser;
import bvb.gui.PanelTitle;
import bvb.gui.SelectedSources;
import bvb.utils.Bounds3D;
import bvvpg.source.converters.GammaConverterSetup;

public class ClipPanel extends JPanel implements ItemListener, ChangeListener
{
	
	final BigVolumeBrowser bvb;
	
	final ClipSetups clipSetups;
	
	public JCheckBox cbClipEnabled;
	public JButton butResetClip;
	public JCheckBox cbShowClipBoxes;
	//public JLabel selectionWindow;
	
	final ClipRangePanel clipRangePanel;
	final ClipRotationPanel clipRotationPanel;
	final ClipCenterPanel clipCenterPanel;

	/**
	 * Panel background if color reflects a set of sources all having the same color
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if color reflects a set of sources with different colors
	 */
	private Color inConsistentBg = Color.WHITE;
	
	public ClipPanel(final BigVolumeBrowser bvb_)
	{
		super();
		bvb = bvb_;


		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);
		this.setBorder(new PanelTitle(" Clip "));

		clipSetups = new ClipSetups(bvb);
		
		clipRangePanel = new ClipRangePanel(clipSetups);
	    clipRotationPanel = new ClipRotationPanel(clipSetups.selectedSources, clipSetups); 
	    clipCenterPanel = new ClipCenterPanel(clipSetups.selectedSources, clipSetups); 

		JTabbedPane tabClipPane = new JTabbedPane(SwingConstants.TOP);
		//URL icon_path = this.getClass().getResource("/icons/rotate.png");
	    //ImageIcon tabIcon = new ImageIcon(icon_path);
		tabClipPane.addTab( "Range", clipRangePanel );
		//tabClipPane.addTab("",tabIcon, clipRotationPanel , "Rotation");
		tabClipPane.addTab ("Rotate", clipRotationPanel);
		tabClipPane.addTab( "Center", clipCenterPanel );
		
		tabClipPane.addChangeListener((e) -> updateGUI());

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
					
		cbShowClipBoxes = new JCheckBox ("Box",false);
		//selectionWindow = new JLabel("Selected: None");
		gbc.gridx++;
		this.add(cbShowClipBoxes ,gbc);
		
		cbShowClipBoxes.addItemListener( new ItemListener() 
				{
					@Override
					public void itemStateChanged( ItemEvent e )
					{
						boolean bNewState =  (e.getStateChange() 
								== ItemEvent.SELECTED ? true: false);
						bvb.clipBoxes.setVisible( bNewState );
						if(bNewState)
						{
							bvb.clipBoxes.updateClipBoxes();
						}
						bvb.repaintBVV();
					}
				
				});
		
		butResetClip = new JButton("Reset");
		gbc.gridx ++;
		this.add(butResetClip,gbc);	
		butResetClip.addActionListener( new ActionListener() 
    		{
				@Override
				public void actionPerformed( ActionEvent arg0 )
				{
					
					resetClip();
				}
    	
    		});
    
		
		gbc.gridx = 0;
	    gbc.gridy ++;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 3;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    this.add(tabClipPane,gbc);


	    setSourceListeners();

	    updateGUI();
	    Color [] colors = new Color[3];
	    colors[0] =  new Color(198,34,0);
	    colors[1] =  new Color(67,154,0);
	    colors[2] =  new Color(0,34,213);

	    this.setSliderColors( colors );
	}
	
	
	private synchronized void updateGUI()
	{
		updateColors();
//		switch (selectedSources.getActiveWindow())
//		{
//		case 0:
//			selectionWindow.setText( "Selected: Sources");
//			break;
//		case 1:
//			selectionWindow.setText( "Selected: Groups");
//			break;
//		default:
//			selectionWindow.setText( "Selected: None");
//		}	
		final List< ConverterSetup > csList = clipSetups.selectedSources.getSelectedSources();
		if(csList== null || csList.isEmpty())
		{
			setPanelsEnabled(false);
			return;
		}
		setPanelsEnabled(true);
		
		//consistent clipping
		boolean bClipConsistent = true;
		int bClipEnabled = -1;
		for ( final ConverterSetup cs: csList)
		{
			 
			if(bClipEnabled < 0)
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
			setPanelsEnabled(bClipEnabled != 0);
		}
		else
		{
			setPanelsEnabled(cbClipEnabled.isSelected());
			cbClipEnabled.setBackground( inConsistentBg );
		}
		clipRangePanel.updateGUI();
		clipRotationPanel.updateGUI();
		clipCenterPanel.updateGUI();
		
	}
	
	private void setPanelsEnabled(boolean bEnabled)
	{
		
		clipRangePanel.setEnabled(bEnabled);
		clipRotationPanel.setEnabled(bEnabled);
		clipCenterPanel.setEnabled( bEnabled );
	}
	
	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}
	
	private void updateClipEnabled()
	{
		boolean bEnabled = cbClipEnabled.isSelected();
		
		final List< ConverterSetup > csList = clipSetups.selectedSources.getSelectedSources();
		if(csList== null || csList.isEmpty())
		{
			cbClipEnabled.setSelected( false );
			return;
		}
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


	@Override
	public void stateChanged( ChangeEvent arg0 )
	{
		updateGUI();
	}
	
	void setSliderColors(Color [] colors)
	{
		
		clipRangePanel.setSliderColors( colors );
		clipRotationPanel.setSliderColors( colors );
		clipCenterPanel.setSliderColors( colors );
		
	}
	
	void resetClip()
	{
		final List< ConverterSetup > csList = clipSetups.selectedSources.getSelectedSources();
		if(csList == null || csList.isEmpty())
		{
			return;
		}
		for ( final ConverterSetup cs: csList)
		{
			Bounds3D range3D = clipSetups.clipAxesBounds.getDefaultBounds( cs );

			if(range3D != null)
			{
				clipSetups.clipAxesBounds.setBounds( cs, range3D );
				clipSetups.clipRotationAngles.setAngles(cs, new double [3]);
				((GammaConverterSetup)cs).setClipInterval(new FinalRealInterval(range3D.getMinBound(),range3D.getMaxBound()));
				((GammaConverterSetup)cs).setClipTransform( new AffineTransform3D() );
				clipSetups.clipCenters.setCenters(cs, clipSetups.clipCenters.getCurrentOrDefaultCenters( cs ));
				clipSetups.clipCenterBounds.setBounds( cs, clipSetups.clipCenterBounds.getDefaultBounds( cs ) );
				clipSetups.updateClipTransform( (GammaConverterSetup) cs);
				((GammaConverterSetup)cs).setClipActive( true );
			}
		}
	}
	
	public void setSourceListeners()
	{
		
		clipSetups.selectedSources.addSourceSelectionListener(  new SelectedSources.Listener()
		{			
			@Override
			public void selectedSourcesChanged( )
			{
				updateGUI();
			}
		} );
		
	    //add listener in case number of sources, etc change
		clipSetups.converterSetups.listeners().add( s -> updateGUI() );

	}
}
