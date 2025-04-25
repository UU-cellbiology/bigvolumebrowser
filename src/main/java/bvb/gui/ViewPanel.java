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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import bdv.tools.brightness.ColorIcon;
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import ij.Prefs;

public class ViewPanel extends JPanel
{
	
	final BigVolumeBrowser bvb;
//	JToggleButton butOrigin;
	JToggleButton butVBox;
	JButton butProjType;
	JButton butFullScreen;
	JButton butSettings;
	final ImageIcon [] projIcon = new ImageIcon[2];
	final String[] projToolTip = new String[2];
	
	public ColorUserSettings selectColors = new ColorUserSettings();
	
	public ViewPanel(final BigVolumeBrowser bvb_)
	{
		super();
		setLayout(new GridBagLayout());
		bvb = bvb_;
		//this.setBorder(new PanelTitle(" View "));
		
		//ORIGIN
//		URL icon_path = this.getClass().getResource("/icons/orig.png");
//	    ImageIcon tabIcon = new ImageIcon(icon_path);
//	    butOrigin = new JToggleButton(tabIcon);
//	    //butOrigin.setSelected(btdata.bShowOrigin);
//	    butOrigin.setToolTipText("Show XYZ axes");
	    
	    //BOX AROUND
		URL icon_path = this.getClass().getResource("/icons/boxvolume.png");
		ImageIcon tabIcon = new ImageIcon(icon_path);
	    butVBox = new JToggleButton(tabIcon);
	    //butVBox.setSelected(btdata.bVolumeBox);
	    butVBox.setToolTipText("Volume Box");
	    butVBox.setSelected( BVBSettings.bShowVolumeBoxes  );
	    butVBox.addItemListener(new ItemListener() {

	    	@Override
	    	public void itemStateChanged(ItemEvent e) 
	    	{
	    		if(e.getStateChange() == ItemEvent.SELECTED)
	    		{
	    			bvb.showVolumeBoxes( true );

	    		} 
	    		else 
	    		{
	    			bvb.showVolumeBoxes( false );
	    		}
	    	}
	    });
	    
	    //PROJECTION MATRIX
	    projToolTip[0] = "Perspective";
	    projToolTip[1] = "Orthographic";
		icon_path = this.getClass().getResource("/icons/proj_persp.png");
		projIcon[0] = new ImageIcon(icon_path);
		icon_path = this.getClass().getResource("/icons/proj_ortho.png");
		projIcon[1] = new ImageIcon(icon_path);

	    butProjType = new JButton(projIcon[bvb.bvvViewer.getProjectionType()]);
	    butProjType.setToolTipText( projToolTip[bvb.bvvViewer.getProjectionType() ]);
	    
	    butProjType.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent arg0 )
			{
				int newProj = 0; 
				if(bvb.bvvViewer.getProjectionType() == 0)
				{
					newProj = 1;
				}
				butProjType.setIcon( projIcon[newProj] );
				butProjType.setToolTipText( projToolTip[newProj]);
				bvb.bvvViewer.setProjectionType(newProj);
			}
	
		});
		
	    //FULL SCREEN
		icon_path = this.getClass().getResource("/icons/fullscreen.png");
	    tabIcon = new ImageIcon(icon_path);
	    butFullScreen = new JButton(tabIcon);
	    butFullScreen.setToolTipText("Full Screen");
	    butFullScreen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent arg0 )
			{
				bvb.makeFullScreen();								
			}
	    	
	    });	    
	    
		//SETTINGS
		icon_path = this.getClass().getResource("/icons/settings.png");
	    tabIcon = new ImageIcon(icon_path);
	    butSettings = new JButton(tabIcon);
	    butSettings.setToolTipText("Settings");
	    butSettings.addActionListener(new ActionListener()
	    		{
					@Override
					public void actionPerformed( ActionEvent arg0 )
					{
						
						dialSettings();
					}
	    	
	    		});
	    
	    GridBagConstraints gbc = new GridBagConstraints();

	    gbc.gridx = 0;
	    gbc.gridy = 0;
//		this.add(butOrigin,gbc);
//		
//		gbc.gridx++;	    
		this.add(butVBox,gbc);
		
		gbc.gridx++;	    
		this.add(butProjType,gbc);
		
		gbc.gridx++;	    
		this.add(butFullScreen,gbc);
		
		gbc.gridx++;	    
		this.add(butSettings,gbc);

	}
	
	public void dialSettings()
	{
		JPanel pViewSettings = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
//		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
//		symbols.setDecimalSeparator('.');
//		DecimalFormat df3 = new DecimalFormat ("#.#####", symbols);
		
		JButton butCanvasBGColor = new JButton( new ColorIcon( BVBSettings.canvasBGColor ) );	
		butCanvasBGColor.addActionListener( e -> {
			Color newColor = JColorChooser.showDialog(bvb.controlPanel.cpFrame, "Choose background color", BVBSettings.canvasBGColor );
			if (newColor != null)
			{
				selectColors.setColor(newColor, 0);

				butCanvasBGColor.setIcon(new ColorIcon(newColor));
			}
			
		});
		
		NumberField nfAnimationDuration = new NumberField(5);
		nfAnimationDuration.setIntegersOnly(true);
		nfAnimationDuration.setText(Integer.toString(BVBSettings.nTransformAnimationDuration));
		
		JCheckBox cbZoomLoad = new JCheckBox();
		cbZoomLoad.setSelected(BVBSettings.bFocusOnSourcesOnLoad);
		
		
		JCheckBox cbShowScaleBar = new JCheckBox();
		cbShowScaleBar.setSelected(BVBSettings.bShowScaleBar);
		
		JCheckBox cbShowMultiBox = new JCheckBox();
		cbShowMultiBox.setSelected(BVBSettings.bShowMultiBox);
		
		
		gbc.gridx=0;
		gbc.gridy=0;	
		GBCHelper.alighLoose(gbc);
		
		pViewSettings.add(new JLabel("Background color: "), gbc);
		gbc.gridx++;
		pViewSettings.add(butCanvasBGColor, gbc);
		
		gbc.gridx=0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Show scale bar "), gbc);
		gbc.gridx++;
		pViewSettings.add(cbShowScaleBar, gbc);
		
		gbc.gridx=0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Show MultiBox "), gbc);
		gbc.gridx++;
		pViewSettings.add(cbShowMultiBox, gbc);
		
		gbc.gridx=0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Transform animation duration (ms): "), gbc);
		gbc.gridx++;
		pViewSettings.add(nfAnimationDuration,gbc);
		
		gbc.gridx=0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Focus on loaded sources "), gbc);
		gbc.gridx++;
		pViewSettings.add(cbZoomLoad, gbc);
		
		
		int reply = JOptionPane.showConfirmDialog(null, pViewSettings, "View/Navigation Settings", 
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (reply == JOptionPane.OK_OPTION) 
		{
			boolean bRepaintBVV = false;
			
			Color tempC;
			
			tempC = selectColors.getColor(0);
			if(tempC != null)
			{
				bRepaintBVV = true;
				bvb.setCanvasBGColor(tempC);
				
			}
			
			BVBSettings.nTransformAnimationDuration = Integer.parseInt(nfAnimationDuration.getText());
			Prefs.set("BVB.nTransformAnimationDuration",BVBSettings.nTransformAnimationDuration);
			
			BVBSettings.bFocusOnSourcesOnLoad = cbZoomLoad.isSelected();
			Prefs.set("BVB.bFocusOnSourcesOnLoad", BVBSettings.bFocusOnSourcesOnLoad);

			BVBSettings.bShowScaleBar = cbShowScaleBar.isSelected();
			Prefs.set("BVB.bShowScaleBar", BVBSettings.bShowScaleBar);
			bdv.util.Prefs.showScaleBar(BVBSettings.bShowScaleBar);
			
			BVBSettings.bShowMultiBox = cbShowMultiBox.isSelected();
			Prefs.set("BVB.bShowMultiBox", BVBSettings.bShowMultiBox);
			bdv.util.Prefs.showMultibox( BVBSettings.bShowMultiBox );
			

			if(bRepaintBVV)
			{
				bvb.repaintBVV();
			}
		}
	}
	

}
