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
package bvb.gui;

import java.awt.Color;
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
import javax.swing.UIManager;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.UIUtils;
import bdv.viewer.ConverterSetups;
import bvvpg.source.converters.GammaConverterSetup;


public class SourcesRenderPanel extends JPanel implements ActionListener
{	
	
	final SelectedSources selectedSources;
	
	ButtonGroup renderMode = new ButtonGroup();
	
	JToggleButton [] butRender = new JToggleButton[2];
	
	JPanel panRender;
	
	ButtonGroup interpolationMode = new ButtonGroup();
	
	JToggleButton [] butInter = new JToggleButton[2];
	
	JPanel panInterpolation;
	
	/**
	 * Panel background if color reflects a set of sources all having the same color
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if color reflects a set of sources with different colors
	 */
	private Color inConsistentBg = Color.WHITE;

	public SourcesRenderPanel(final ConverterSetups convSetups, final SelectedSources selectedSources_)
	{
		
		super(new GridBagLayout());
		
		int nButtonSize = 50;
		
		selectedSources = selectedSources_;
		
		String[] sMethods = new String[2];
		sMethods[0] = "Max intensity";
		sMethods[1] = "Volumetric";
		
		String[] sInterpolation = new String[2];
		sInterpolation[0] = "Nearest";
		sInterpolation[1] = "Trilinear";
		
		GridBagConstraints gbc = new GridBagConstraints();
	
		URL icon_path = this.getClass().getResource("/icons/max_int.png");
		ImageIcon tabIcon = new ImageIcon(icon_path);
		butRender[0] = new JToggleButton(tabIcon);
		butRender[0].setToolTipText("Maximum intensity");
		
		icon_path = this.getClass().getResource("/icons/volumetric.png");
		tabIcon = new ImageIcon(icon_path);
		butRender[1] = new JToggleButton(tabIcon);
		butRender[1].setToolTipText("Volumetric");
		
		icon_path = this.getClass().getResource("/icons/nearest.png");
		tabIcon = new ImageIcon(icon_path);
		butInter[0] = new JToggleButton(tabIcon);
		butInter[0].setToolTipText("Nearest neighbor");
		
		icon_path = this.getClass().getResource("/icons/linear.png");
		tabIcon = new ImageIcon(icon_path);
		butInter[1] = new JToggleButton(tabIcon);
		butInter[1].setToolTipText("Trilinear");

		panRender = new JPanel(new GridBagLayout());		
		panInterpolation = new JPanel(new GridBagLayout());
		
		for(int i = 0; i<2; i++)
		{
			
			butRender[i].setPreferredSize(new Dimension(nButtonSize , nButtonSize ));
			butRender[i].addActionListener( this );
			renderMode.add( butRender[i] );
			panRender.add( butRender[i] );
			
			butInter[i].setPreferredSize(new Dimension(nButtonSize , nButtonSize ));
			butInter[i].addActionListener( this );
			interpolationMode.add( butInter[i] );
			panInterpolation.add( butInter[i] );
		}
		
		gbc.insets =  new Insets(1,10,1,10);
		gbc.gridx = 0;
		gbc.gridy = 0;
		this.add(new JLabel("Render"),gbc);
		
		gbc.gridx++;
		gbc.gridheight = 2;
		gbc.fill = SwingConstants.VERTICAL;
		JSeparator sp = new JSeparator(SwingConstants.VERTICAL);
		this.add(sp,gbc);
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridheight = 1;
		gbc.gridx++;
		this.add(new JLabel("Voxels"),gbc);

		gbc.gridx=0;
		gbc.gridy++;
		this.add(panRender,gbc);			
		
		gbc.gridx+=2;
		this.add( panInterpolation, gbc );
		
	    selectedSources.addSourceSelectionListener(  new SelectedSources.Listener()
		{			
			@Override
			public void selectedSourcesChanged( )
			{
				updateGUI();
			}
		} );
	    
	    //add listener in case number of sources, etc change
	    convSetups.listeners().add( s -> updateGUI() );
	    updateGUI();
	}
	
	synchronized void updateGUI()
	{
		updateColors();
		
		final List< ConverterSetup > csList = selectedSources.getSelectedSources();
		if(csList== null || csList.isEmpty())
		{
			setChoicesEnabled(false);
			return;
		}
		setChoicesEnabled(true);
		
		boolean bRenderConsistent = true;
		boolean bInterpConsistent = true;
		int nRenderM = -1;
		int nInterpM = -1;
		for ( final ConverterSetup cs: csList)
		{
			 
			if(nRenderM < 0)
			{
				nRenderM = ((GammaConverterSetup)cs).getRenderType();
				nInterpM = ((GammaConverterSetup)cs).getVoxelRenderInterpolation();
			}
			else
			{
				bRenderConsistent &= (nRenderM == ((GammaConverterSetup)cs).getRenderType());
				bInterpConsistent &= (nInterpM == ((GammaConverterSetup)cs).getVoxelRenderInterpolation());
			}
		}
		
		if(bRenderConsistent)
		{
			butRender[nRenderM].setSelected( true );
			panRender.setBackground( consistentBg );
		}
		else
		{
			panRender.setBackground( inConsistentBg );
			renderMode.clearSelection();
		}
		
		if(bInterpConsistent)
		{
			butInter[nInterpM].setSelected( true );
			panInterpolation.setBackground( consistentBg );
		}
		else
		{
			panInterpolation.setBackground( inConsistentBg );
			interpolationMode.clearSelection();
		}
	}
	
	void setChoicesEnabled(boolean bEnabled)
	{
		for(int i=0; i<2; i++)
		{
			butInter[i].setEnabled( bEnabled );
			butRender[i].setEnabled( bEnabled );
		}
	}

	@Override
	public synchronized void actionPerformed( ActionEvent arg0 )
	{
		final List< ConverterSetup > csList = selectedSources.getSelectedSources();
		
		if(csList== null || csList.isEmpty())
			return;
		for(int i=0;i<2;i++)
		{
			if(arg0.getSource() == butInter[i])
			{
				for ( final ConverterSetup cs: csList)
				{
					((GammaConverterSetup)cs).setVoxelRenderInterpolation( i );
				}
				return;
			}
			if(arg0.getSource() == butRender[i])
			{
				for ( final ConverterSetup cs: csList)
				{
					((GammaConverterSetup)cs).setRenderType( i );
				}
				return;
			}
		}
	}
	
	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}
}
