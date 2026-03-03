/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
package bvb.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.ConverterSetups;
import bvb.core.BVBSettings;
import bvvpg.source.converters.GammaConverterSetup;


public class SourcesRenderPanel extends JPanel implements ActionListener
{	
	
	final SelectedObjects selectedSources;
	
	final ButtonGroup renderMode = new ButtonGroup();
	
	final JToggleButton [] butRender = new JToggleButton[3];
	
	final JPanelConsistent panRender;
	
	
	final ButtonGroup lightMode = new ButtonGroup();
	
	final JToggleButton [] butLight = new JToggleButton[3];
	
	final JPanelConsistent panLighting;

	
	final ButtonGroup interpolationMode = new ButtonGroup();
	
	final JToggleButton [] butInter = new JToggleButton[2];
	
	final JPanelConsistent panInterpolation;
	
	private boolean blockUpdates = false;


	public SourcesRenderPanel(final ConverterSetups convSetups, final SelectedObjects selectedSources_)
	{
		
		super(new GridBagLayout());
		
		int nButtonSize = 50;
		
		selectedSources = selectedSources_;
		
		URL icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "max_int.png");
		ImageIcon tabIcon = new ImageIcon(icon_path);
		butRender[0] = new JToggleButton(tabIcon);
		butRender[0].setToolTipText("Maximum intensity");
		
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "volumetric.png");
		tabIcon = new ImageIcon(icon_path);
		butRender[1] = new JToggleButton(tabIcon);
		butRender[1].setToolTipText("Volumetric");
		
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "surface.png");
		tabIcon = new ImageIcon(icon_path);
		butRender[2] = new JToggleButton(tabIcon);
		butRender[2].setToolTipText("Surface");

		
		icon_path = this.getClass().getResource(BVBSettings.sIconPath +"light_plain.png");
		tabIcon = new ImageIcon(icon_path);
		butLight[0] = new JToggleButton(tabIcon);
		butLight[0].setToolTipText("Plain");
		
		icon_path = this.getClass().getResource(BVBSettings.sIconPath +"light_shaded.png");
		tabIcon = new ImageIcon(icon_path);
		butLight[1] = new JToggleButton(tabIcon);
		butLight[1].setToolTipText("Shaded");
		
		icon_path = this.getClass().getResource(BVBSettings.sIconPath +"light_shiny.png");
		tabIcon = new ImageIcon(icon_path);
		butLight[2] = new JToggleButton(tabIcon);
		butLight[2].setToolTipText("Shiny");
		
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "nearest.png");
		tabIcon = new ImageIcon(icon_path);
		butInter[0] = new JToggleButton(tabIcon);
		butInter[0].setToolTipText("Nearest neighbor");
		
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "linear.png");
		tabIcon = new ImageIcon(icon_path);
		butInter[1] = new JToggleButton(tabIcon);
		butInter[1].setToolTipText("Trilinear");

		panRender = new JPanelConsistent(new GridBagLayout());		
		panLighting = new JPanelConsistent(new GridBagLayout());
		panInterpolation = new JPanelConsistent(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panRender.add( new JLabel(),gbc );
		gbc.gridwidth = 3;
		gbc.gridx ++;
		panRender.add( new JLabel("Render"),gbc );
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panInterpolation.add( new JLabel("Voxels"),gbc );
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		panRender.add( new JLabel("Mode"),gbc );
		panLighting.add( new JLabel("Light "),gbc );
		for(int i = 0; i < 3; i++)
		{	
			gbc.gridx++;	
			butRender[i].setPreferredSize(new Dimension(nButtonSize , nButtonSize ));
			butRender[i].addActionListener( this );
			renderMode.add( butRender[i] );
			panRender.add( butRender[i], gbc );			
			butLight[i].setPreferredSize(new Dimension(nButtonSize , nButtonSize ));
			butLight[i].addActionListener( this );
			lightMode.add( butLight[i] );
			panLighting.add( butLight[i], gbc );				
		}
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		for(int i = 0; i < 2; i++)
		{
			butInter[i].setPreferredSize(new Dimension(nButtonSize , nButtonSize ));
			butInter[i].addActionListener( this );
			interpolationMode.add( butInter[i] );
			panInterpolation.add( butInter[i], gbc );
			gbc.gridy++;
		}		
		
		
		gbc = new GridBagConstraints();
		
		gbc.insets =  new Insets(1,10,1,10);
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		this.add(panRender,gbc);	
		gbc.gridy ++;
		this.add(panLighting,gbc);	
		gbc.gridy = 0;
		gbc.gridx++;
		gbc.gridheight = 2;
		gbc.fill = SwingConstants.VERTICAL;
		JSeparator sp = new JSeparator(SwingConstants.VERTICAL);
		this.add(sp,gbc);
		
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		this.add( panInterpolation, gbc );
		
	    selectedSources.addObjectSelectionListener(()->	updateGUI());
	    
	    //add listener in case number of sources, etc change
	    convSetups.listeners().add( s -> updateGUI() );
	    updateGUI();
	}
	
	synchronized void updateGUI()
	{
		
		final List< ConverterSetup > csList = selectedSources.getSelectedConverterSetups();
		if(csList == null || csList.isEmpty())
		{
			setChoicesEnabled(false);
			return;
		}
		
		setChoicesEnabled(true);
		
		boolean bRenderConsistent = true;
		boolean bInterpConsistent = true;
		boolean bLightConsistent = true;
		int nRenderM = -1;
		int nInterpM = -1;
		int nLightM = -1;
		for ( final ConverterSetup cs: csList)
		{
			final GammaConverterSetup csG = ((GammaConverterSetup)cs);
			if(nRenderM < 0)
			{
				nRenderM = csG.getRenderType();
				nInterpM = csG.getVoxelRenderInterpolation();
				nLightM = csG.getLightingType();
			}
			else
			{
				bRenderConsistent &= (nRenderM == csG.getRenderType());
				bInterpConsistent &= (nInterpM == csG.getVoxelRenderInterpolation());
				bLightConsistent &= (nLightM == csG.getLightingType());
			}
		}
		
		final boolean bRenderFin = bRenderConsistent;
		final boolean bInterpFin = bInterpConsistent;
		final boolean bLightFin = bLightConsistent;
		final int nRenderFin = nRenderM;
		final int nInterpFin = nInterpM;
		final int nLightFin = nLightM;
		
		SwingUtilities.invokeLater( () -> {
			synchronized ( SourcesRenderPanel.this )
			{
				blockUpdates = true;

				panRender.setConsistent( bRenderFin );
				if(bRenderFin)
				{
					butRender[nRenderFin].setSelected( true );
				}
				else
				{
					renderMode.clearSelection();
				}
				
				panLighting.setConsistent( bLightFin );
				if(bLightFin)
				{
					butLight[nLightFin].setSelected( true );
				}
				else
				{
					lightMode.clearSelection();
				}
				
				panInterpolation.setConsistent( bInterpFin );
				if(bInterpFin)
				{
					butInter[nInterpFin].setSelected( true );					
				}
				else
				{
					interpolationMode.clearSelection();
				}
			}
			blockUpdates = false;
		} );
	}
	
	void setChoicesEnabled(boolean bEnabled)
	{
		
		for(int i = 0; i < 3; i++)
		{
			butRender[i].setEnabled( bEnabled );
			butLight[i].setEnabled( bEnabled );
		}
		for(int i = 0; i < 2; i++)
		{
			butInter[i].setEnabled( bEnabled );
		}
	}

	@Override
	public synchronized void actionPerformed( ActionEvent arg0 )
	{
		if(!blockUpdates)
		{
			final List< ConverterSetup > csList = selectedSources.getSelectedConverterSetups();
			
			if(csList== null || csList.isEmpty())
				return;
			for(int i = 0; i < 2; i++)
			{
				if(arg0.getSource() == butInter[i])
				{
					for ( final ConverterSetup cs: csList)
					{
						((GammaConverterSetup)cs).setVoxelRenderInterpolation( i );
					}
					return;
				}
			}
			for(int i = 0; i < 3; i++)
			{
				if(arg0.getSource() == butRender[i])
				{
					for ( final ConverterSetup cs: csList)
					{
						((GammaConverterSetup)cs).setRenderType( i );
					}
					return;
				}
			}
			for(int i = 0; i < 3; i++)
			{
				if(arg0.getSource() == butLight[i])
				{
					for ( final ConverterSetup cs: csList)
					{
						((GammaConverterSetup)cs).setLightingType( i );
					}
					return;
				}
			}
		}
	}
}
