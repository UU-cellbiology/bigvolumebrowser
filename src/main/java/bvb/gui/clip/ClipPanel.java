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
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.ui.UIUtils;
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.JPanelConsistent;
import bvb.utils.Bounds3D;
import bvb.utils.clip.ClipSetups;
import bvvpg.source.converters.Clippable3D;
import ij.Prefs;

public class ClipPanel extends JPanel
{
	
	final BigVolumeBrowser bvb;
	
	final public ClipSetups clipSetups;
	
	final JComboBox<String> cbClipState;
	final JPanelConsistent pClipState;
	
	JButton butCoordSystem;
	
	JCheckBox cbShowClipBoxes;

	JButton butResetClipCurrent;

	JButton butResetClipAll;

	final ImageIcon [] coordIcon = new ImageIcon[2];
	final String[] coordToolTip = new String[2];
	
	final JTabbedPane tabClipPane;
	
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

		tabClipPane = new JTabbedPane(SwingConstants.TOP);

		tabClipPane.addTab( "Range", clipRangePanel );
		tabClipPane.addTab ("Rotate", clipRotationPanel);
		tabClipPane.addTab( "Center", clipCenterPanel );
		
		tabClipPane.addChangeListener((e) -> updateGUI());

		GridBagConstraints gbc = new GridBagConstraints();
	
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		
		String[] sClipType= {"Off", "Out", "In"};
		cbClipState = new JComboBox< >(sClipType);
		cbClipState.setSelectedIndex( 0 );
		
		cbClipState.addActionListener( (e)->{
			updateClipState();				
			});
		
		pClipState = new JPanelConsistent(new GridBagLayout());
		GridBagConstraints gbcp = new GridBagConstraints();
		gbcp.gridx = 0;
		gbcp.gridy = 0;
		pClipState.add( new JLabel("Clipping"), gbcp );
		gbcp.gridx++;
		pClipState.add( cbClipState, gbcp );

		this.add(pClipState,gbc);
				
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
		
		gbc.weightx = 0.1;
		//gbc.anchor = GridBagConstraints.EAST;
		icon_path = this.getClass().getResource("/icons/red_cross.png");
		ImageIcon icon = new ImageIcon(icon_path);
		butResetClipCurrent = new JButton(icon);
		butResetClipCurrent.setToolTipText( "Reset current panel" );
		butResetClipCurrent.addActionListener( (e)->resetPanelClip() );   
		gbc.gridx ++;
		this.add(butResetClipCurrent,gbc);	
		
		icon_path = this.getClass().getResource("/icons/red_crossx2.png");
		icon = new ImageIcon(icon_path);
		butResetClipAll = new JButton(icon);
		butResetClipAll.setToolTipText( "Reset all clip" );
		gbc.gridx ++;
		this.add(butResetClipAll,gbc);	
		
		butResetClipAll.addActionListener( (e)->resetClip());   
		
		gbc.gridx = 0;
	    gbc.gridy ++;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 5;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    this.add(tabClipPane,gbc);

	    setupListeners();
	    
	    Color [] colors = new Color[3];
	    colors[0] =  new Color(198,34,0);
	    colors[1] =  new Color(67,154,0);
	    colors[2] =  new Color(0,34,213);

	    this.setSliderColors( colors );
	    
	    updateGUI();
	}
	
	
	public synchronized void updateGUI()
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
		int nClipState = -1;
		
		final List< Object > objList = clipSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final Clippable3D objCl = (Clippable3D)obj;
			if(nClipState < 0)
			{
				nClipState = objCl.getClipState();	
			}
			else
			{
				bClipConsistent &= (nClipState==(objCl.getClipState()));
			}
		}
		
		
		if(bClipConsistent)
		{
			pClipState.setBackground( consistentBg );
			cbClipState.setSelectedIndex( nClipState );
			setPanelsEnabled(nClipState != 0);
		}
		else
		{
			pClipState.setBackground( inConsistentBg );
			setPanelsEnabled(true);

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
	
	private void updateClipState()
	{
		int nClipState = cbClipState.getSelectedIndex();
		
		
		if(!clipSetups.selectedObjects.isAnythingSelected())
		{
			cbClipState.setSelectedIndex( 0 );
			return;
		}
		final List< Object > objList = clipSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final Clippable3D objCl = (Clippable3D)obj;
			objCl.setClipState( nClipState );
		}

		bvb.updateSceneRender();

		updateGUI();
	}
	
	
	void setSliderColors(Color [] colors)
	{		
		clipRangePanel.setSliderColors( colors );
		clipRotationPanel.setSliderColors( colors );
		clipCenterPanel.setSliderColors( colors );		
	}
	
	void resetPanelClip()
	{
		
		switch(tabClipPane.getSelectedIndex())
		{
		case 0:
			clipRangePanel.resetRange();
			break;
		case 1:
			clipRotationPanel.resetRotation();
			break;
		case 2:
			clipCenterPanel.resetCenters();
			break;
		default:
		}
	}
	
	void resetClip()
	{
		if(!clipSetups.selectedObjects.isAnythingSelected())
		{
			return;
		}
		final List< Object > objList = clipSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final Clippable3D objCl = (Clippable3D)obj;
			Bounds3D range3D = clipSetups.clipAxesBounds.getDefaultBounds( objCl );
			
			if(range3D != null)
			{
				clipSetups.clipAxesBounds.setBounds( objCl, range3D );
				clipSetups.clipRotation.setAngles(objCl, new double [3]);
				objCl.setClipInterval(new FinalRealInterval(range3D.getMinBound(),range3D.getMaxBound()));
				objCl.setClipTransform( new AffineTransform3D() );
				clipSetups.clipCenters.setCenters(objCl, clipSetups.clipCenters.getCurrentOrDefaultCenters( objCl ));
				clipSetups.clipCenterBounds.setBounds( objCl, clipSetups.clipCenterBounds.getDefaultBounds( objCl ) );
				clipSetups.updateClipTransform( objCl, null);
				//objCl.setClipActive( true );
			}
		}
		bvb.updateSceneRender();

		updateGUI();
	}
	
	public void setupListeners()
	{
		//add listener on selected objects
		clipSetups.selectedObjects.addObjectSelectionListener(  ()-> updateGUI());		
	    //add listener in case number of sources, etc change
		clipSetups.converterSetups.listeners().add( s -> updateGUI() );

	}
}
