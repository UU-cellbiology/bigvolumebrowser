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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
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
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.SelectedObjects;
import bvb.shapes.BasicShape;
import bvb.utils.Bounds3D;
import bvb.utils.clip.ClipSetups;
import bvvpg.source.converters.GammaConverterSetup;
import ij.Prefs;

public class ClipPanel extends JPanel implements ItemListener, ChangeListener
{
	
	final BigVolumeBrowser bvb;
	
	final public ClipSetups clipSetups;
	
	public JCheckBox cbClipEnabled;
	public JButton butResetClip;
	public JCheckBox cbShowClipBoxes;
	public JButton butCoordSystem;
	
	final ImageIcon [] coordIcon = new ImageIcon[2];
	final String[] coordToolTip = new String[2];
	
	final ClipRangePanel clipRangePanel;
	final public ClipRotationPanel clipRotationPanel;
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
		
		//this.setBorder(new PanelTitle(" Clip "));

		clipSetups = new ClipSetups(bvb);
		
		clipRangePanel = new ClipRangePanel( clipSetups );
	    clipRotationPanel = new ClipRotationPanel( clipSetups ); 
	    clipCenterPanel = new ClipCenterPanel( clipSetups ); 

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
		
		gbc.gridx++;
		
		//CLIP COORDINATE SYSTEM
	    //PROJECTION MATRIX
	    coordToolTip[0] = "Global world coordinates";
	    coordToolTip[1] = "Local volume coordinates";
		URL icon_path = this.getClass().getResource("/icons/clip_global.png");
		coordIcon[0] = new ImageIcon(icon_path);
		icon_path = this.getClass().getResource("/icons/clip_local.png");
		coordIcon[1] = new ImageIcon(icon_path);
		
		butCoordSystem = new JButton(coordIcon[clipSetups.bLocalCoordinates?1:0]);
		butCoordSystem.setToolTipText(coordToolTip[clipSetups.bLocalCoordinates?1:0]);
//		Dimension butDim = butCoordSystem.getPreferredSize();
//		butDim.width = 40;
//		butCoordSystem.setPreferredSize( butDim );
		butCoordSystem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent arg0 )
			{
				clipSetups.bLocalCoordinates = !clipSetups.bLocalCoordinates;
				int ind = 0;
				
				if(clipSetups.bLocalCoordinates)
				{
					ind = 1;
				}
				butCoordSystem.setIcon( coordIcon[ind]  );
				butCoordSystem.setToolTipText(coordToolTip[ind]);
				Prefs.set( "BVB.bClipLocalCoordinates", clipSetups.bLocalCoordinates );
				updateGUI();
			}
	
		});
					
		this.add(butCoordSystem ,gbc);

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
						BVBSettings.bShowClipBoxes = bNewState;
						Prefs.get("BVB.bShowClipBoxes", BVBSettings.bShowClipBoxes);
						
						if(bNewState)
						{
							bvb.clipBoxes.updateClipBoxes();
						}
						bvb.repaintBVV();
					}
				
				});
		cbShowClipBoxes.setSelected( BVBSettings.bShowClipBoxes );
		
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
	    gbc.gridwidth = 4;
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
		
		if(!clipSetups.selectedObjects.isAnythingSelected())
		{
			setPanelsEnabled(false);
			return;
		}
		setPanelsEnabled(true);
		
		//consistent clipping
		boolean bClipConsistent = true;
		int bClipEnabled = -1;
		if(clipSetups.selectedObjects.areSourcesSelected())
		{
			final List< ConverterSetup > csList = clipSetups.selectedObjects.getSelectedSources();
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
		}
		if(clipSetups.selectedObjects.areShapesSelected())
		{
			final List< BasicShape > shList = clipSetups.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh : shList )
			{
				if(bClipEnabled < 0)
				{
					bClipEnabled = sh.clipActive()?1:0;	
				}
				else
				{
					bClipConsistent &= (bClipEnabled==(sh.clipActive()?1:0));
				}	
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
		
		
		if(!clipSetups.selectedObjects.isAnythingSelected())
		{
			cbClipEnabled.setSelected( false );
			return;
		}
		if(clipSetups.selectedObjects.areSourcesSelected())
		{
			final List< ConverterSetup > csList = clipSetups.selectedObjects.getSelectedSources();
			for ( final ConverterSetup cs: csList)
			{
				  ((GammaConverterSetup)cs).setClipActive( bEnabled );
			}
		}
		if(clipSetups.selectedObjects.areShapesSelected())
		{
			final List< BasicShape > shList = clipSetups.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh : shList )
			{
				sh.setClipActive( bEnabled );
			}
			bvb.updateSceneRender();
		}
		if(bEnabled)
		{
			bvb.clipBoxes.updateClipBoxes();
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
		if(!clipSetups.selectedObjects.isAnythingSelected())
		{
			return;
		}
		
		if(clipSetups.selectedObjects.areSourcesSelected())
		{
			final List< ConverterSetup > csList = clipSetups.selectedObjects.getSelectedSources();
			for ( final ConverterSetup cs: csList)
			{
				Bounds3D range3D = clipSetups.clipAxesBounds.getDefaultBounds( cs );
	
				if(range3D != null)
				{
					clipSetups.clipAxesBounds.setBounds( cs, range3D );
					clipSetups.clipRotation.setAngles(cs, new double [3]);
					((GammaConverterSetup)cs).setClipInterval(new FinalRealInterval(range3D.getMinBound(),range3D.getMaxBound()));
					((GammaConverterSetup)cs).setClipTransform( new AffineTransform3D() );
					clipSetups.clipCenters.setCenters(cs, clipSetups.clipCenters.getCurrentOrDefaultCenters( cs ));
					clipSetups.clipCenterBounds.setBounds( cs, clipSetups.clipCenterBounds.getDefaultBounds( cs ) );
					clipSetups.updateClipTransform( (GammaConverterSetup) cs, null);
					((GammaConverterSetup)cs).setClipActive( true );
				}
			}
		}
		
		if(clipSetups.selectedObjects.areShapesSelected())
		{
			final List< BasicShape > shList = clipSetups.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh : shList )
			{
				Bounds3D range3D = clipSetups.clipAxesBounds.getDefaultBounds( sh );
				
				if(range3D != null)
				{
					clipSetups.clipAxesBounds.setBounds( sh, range3D );
					clipSetups.clipRotation.setAngles(sh, new double [3]);
					sh.setClipInterval(new FinalRealInterval(range3D.getMinBound(),range3D.getMaxBound()));
					sh.setClipTransform( new AffineTransform3D() );
					clipSetups.clipCenters.setCenters(sh, clipSetups.clipCenters.getCurrentOrDefaultCenters( sh ));
					clipSetups.clipCenterBounds.setBounds( sh, clipSetups.clipCenterBounds.getDefaultBounds( sh ) );
					clipSetups.updateClipTransform(sh);
					sh.setClipActive( true );
				}
			}
			bvb.updateSceneRender();
		}
		updateGUI();
	}
	
	public void setSourceListeners()
	{
		
		clipSetups.selectedObjects.addObjectSelectionListener(  new SelectedObjects.Listener()
		{			
			@Override
			public void selectedObjectsChanged( )
			{
				updateGUI();
			}
		} );
		
	    //add listener in case number of sources, etc change
		clipSetups.converterSetups.listeners().add( s -> updateGUI() );

	}
}
