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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import bdv.tools.brightness.ColorIcon;
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.io.dto.SceneStateDTO;
import bvb.io.dto.SerializationIO;

import ij.IJ;
import ij.Prefs;
import ij.io.SaveDialog;

public class ViewPanel extends JPanel
{	
	final BigVolumeBrowser bvb;

	
	final JButton butToggleVisibility;

	final JToggleButton butVBox;
	
	final JButton butProjType;
	
	final JButton butAxesGizmo;

	final ImageIcon [] projIcon = new ImageIcon[2];
	final String[] projToolTip = new String[2];

	final JButton butCenter;

	final JButton [] butAlign = new JButton [3];

	final JButton butRotateCoords;
	
	final ImageIcon [] rotIcon = new ImageIcon[2];
	final String[] rotToolTip = new String[2];
	
	final JButton [] butRotate = new JButton [3];

	final JButton butSettings;
	final JButton butSave;
	final JButton butLoad;
	
	int nRotationCoord = 0;
	
	public ColorUserSettings selectColors = new ColorUserSettings();

	String [] sAxesNames = new String [] {"X", "Y", "Z"};
	
	public ViewPanel(final BigVolumeBrowser bvb_)
	{
		super();
		setLayout(new GridBagLayout());

		URL icon_path;
		ImageIcon tabIcon;
		bvb = bvb_;
	
		
		//TOGGLE VISIBILITY
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "toggle_visibility.png");
		tabIcon = new ImageIcon(icon_path);

		butToggleVisibility = new JButton( tabIcon );
		butToggleVisibility.setToolTipText("Toggle visibility of selected objects\n(shortcut V)");
		butToggleVisibility.addActionListener((e) -> bvb.bvbActions.actionToggleVisibility());
		
		//BOX AROUND
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "boxvolume.png");
		tabIcon = new ImageIcon(icon_path);
	    butVBox = new JToggleButton(tabIcon);
	    //butVBox.setSelected(btdata.bVolumeBox);
	    butVBox.setToolTipText("Volume Box");
	    butVBox.setSelected( BVBSettings.bShowVolumeBoxes  );
	    butVBox.addItemListener((e)->
	    		bvb.showVolumeBoxes( e.getStateChange() == ItemEvent.SELECTED ));  
	    
	    //PROJECTION MATRIX
	    projToolTip[0] = "Perspective";
	    projToolTip[1] = "Orthographic";
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "proj_persp.png");
		projIcon[0] = new ImageIcon(icon_path);
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "proj_ortho.png");
		projIcon[1] = new ImageIcon(icon_path);

	    butProjType = new JButton( projIcon[ bvb.bvvViewer.getProjectionType() ] );
	    butProjType.setToolTipText( projToolTip[ bvb.bvvViewer.getProjectionType() ]);
	    
	    butProjType.addActionListener((e)->
	    {
	    	int newProj = 0; 
	    	if(bvb.bvvViewer.getProjectionType() == 0)
	    	{
	    		newProj = 1;
	    	}
	    	butProjType.setIcon( projIcon[newProj] );
	    	butProjType.setToolTipText( projToolTip[newProj]);
	    	bvb.bvvViewer.setProjectionType( newProj );
	    }); 
	    
	    
	    
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + "axesGizmo.png");
	    tabIcon = new ImageIcon(icon_path);
	    butAxesGizmo = new JButton(tabIcon);
	    butAxesGizmo.setToolTipText( "Toggle axes align gizmo" );
	    butAxesGizmo.addActionListener((e) ->
	    {
	    	boolean bToggle = !bvb.axisOverlay.isEnabled();
	    	Prefs.set( "BVB.bShowAxisOverlay", bToggle  );
	    	bvb.axisOverlay.setEnabled( bToggle  );
	    	bvb.repaintBVV();
	    });	  
	        
	    
		//Center selected objects	
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "center.png");
		tabIcon = new ImageIcon(icon_path);

		butCenter = new JButton( tabIcon );
		butCenter.setToolTipText("Center view on selected objects\n(shortcut C)");
		butCenter.addActionListener((e) -> bvb.bvbActions.actionCenterView());
		
	    //ALIGN TO AXES
	    for(int d = 0; d < 3; d++)
	    {
			icon_path = this.getClass().getResource(BVBSettings.sIconPath + "align" + sAxesNames[d]+".png");
			tabIcon = new ImageIcon(icon_path);
	    	butAlign[d] = new JButton(tabIcon);
	    	if(d != 1)
	    	{
	    		butAlign[d].setToolTipText( "Align " + sAxesNames[d] + " axis towards camera\n"
	    				+ "(shortcut Shift + " + sAxesNames[d] + ")" ); 
	    	}
	    	else
	    	{
	    		butAlign[d].setToolTipText( "Align " + sAxesNames[d] + " axis towards camera\n"
	    				+ "(shortcut Shift + " + sAxesNames[d] + " / Shift + A)" ); 	    		
	    	}
	    }
	    
	    butAlign[0].addActionListener((e) -> bvb.bvbActions.alignToAxis( 0 ) );
	    butAlign[1].addActionListener((e) -> bvb.bvbActions.alignToAxis( 1 ) );
	    butAlign[2].addActionListener((e) -> bvb.bvbActions.alignToAxis( 2 ));
	    
	    //ROTATE AROUND AXES
	    rotToolTip[0] = "Rotate along camera view coordinates";
	    rotToolTip[1] = "Rotate along world/scene coordinates";
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "camera.png");
		rotIcon[0] = new ImageIcon(icon_path);
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "frame_global.png");
		rotIcon[1] = new ImageIcon(icon_path);
		
		butRotateCoords = new JButton( rotIcon[ nRotationCoord ] );
		butRotateCoords.setToolTipText( rotToolTip[ nRotationCoord ]);

		butRotateCoords.addActionListener((e)->
	    {
	    	if(nRotationCoord == 0)
	    	{
	    		nRotationCoord = 1;
	    	}
	    	else
	    		nRotationCoord = 0;
	    	butRotateCoords.setIcon( rotIcon[nRotationCoord] );
	    	butRotateCoords.setToolTipText( rotToolTip[nRotationCoord]);
	    }); 
		
	    for(int d = 0; d < 3; d++)
	    {
			icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme 
					+ "rotate" + sAxesNames[d] + ".png");
			tabIcon = new ImageIcon(icon_path);
	    	butRotate[d] = new JButton(tabIcon);
	    	butRotate[d].setToolTipText( "Rotate around " + sAxesNames[d] + " axis" );	    	
	    }	    
	    
	    butRotate[0].addActionListener((e) -> bvb.bvbActions.rotate( 0, nRotationCoord == 0 ));
	    butRotate[1].addActionListener((e) -> bvb.bvbActions.rotate( 1, nRotationCoord == 0 ));
	    butRotate[2].addActionListener((e) -> bvb.bvbActions.rotate( 2, nRotationCoord == 0 ));
	    
		//SETTINGS
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "settings.png");
	    tabIcon = new ImageIcon(icon_path);
	    butSettings = new JButton(tabIcon);
	    butSettings.setToolTipText("Settings");
	    butSettings.addActionListener((e) -> dialSettings());	
	    
	    //SAVE/LOAD
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "scene_save.png");
	    tabIcon = new ImageIcon(icon_path);
	    butSave = new JButton(tabIcon);
	    butSave.setToolTipText("Save scene state");
	    butSave.addActionListener( (e) -> dialSceneStateSave() );

		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "scene_load.png");
	    tabIcon = new ImageIcon(icon_path);
	    butLoad = new JButton(tabIcon);
	    butLoad.setToolTipText("Load scene state");
	    butLoad.addActionListener ( (e) -> dialSceneStateLoad() );

	    GridBagConstraints gbc = new GridBagConstraints();

	    gbc.gridx = 0;
	    gbc.gridy = 0;

	    this.add(butToggleVisibility,gbc);
		
		gbc.gridx++;
    	this.add(butVBox,gbc);
		
		gbc.gridx++;	    
		this.add(butProjType,gbc);

		gbc.gridx++;	    
		this.add(butAxesGizmo,gbc);

		gbc.gridy ++;	    
		gbc.gridx = 0;
    	this.add(butCenter,gbc);
	    
	    for(int d = 0; d < 3; d++)
	    {
	    	gbc.gridx++;
			this.add(butAlign[d],gbc);
	    }

		gbc.gridy ++;	    
		gbc.gridx = 0;
		this.add(butRotateCoords, gbc);

	    for(int d = 0; d < 3; d++)
	    {
	    	gbc.gridx++;
			this.add(butRotate[d],gbc);
	    }
	    gbc.gridy = 0;
	    gbc.gridx++;
	    gbc.gridheight = 3;
	    gbc.fill = SwingConstants.VERTICAL;
	    gbc.insets = new Insets(0,10,0,10);
		JSeparator sp = new JSeparator(SwingConstants.VERTICAL);
		this.add(sp,gbc);
		
	    gbc.gridy = 0;
	    gbc.gridx++;
	    gbc.gridheight = 1;
	    gbc.insets = new Insets(0,0,0,0);
	    gbc.fill = GridBagConstraints.NONE;
		this.add(butSettings, gbc);
		
	    gbc.gridy++;
		this.add(butSave, gbc);

		gbc.gridy++;
		this.add(butLoad, gbc);

	}
	
	public void dialSettings()
	{
		JPanel pViewSettings = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		DecimalFormat df3 = new DecimalFormat ("#.##", symbols);
		
		JButton butCanvasBGColor = new JButton( new ColorIcon( BVBSettings.canvasBGColor ) );	
		butCanvasBGColor.addActionListener( e -> {
			Color newColor = JColorChooser.showDialog(pViewSettings, "Choose background color", BVBSettings.canvasBGColor );
			if (newColor != null)
			{
				selectColors.setColor(newColor, 0);
				butCanvasBGColor.setIcon(new ColorIcon(newColor));
			}
			
		});
		
		NumberField nfAnimationDuration = new NumberField(5);
		nfAnimationDuration.setIntegersOnly(true);
		nfAnimationDuration.setText(Integer.toString(BVBSettings.nTransformAnimationDuration));
		
		NumberField nfFocusScreenFraction = new NumberField(4);
		nfFocusScreenFraction.setText( df3.format( BVBSettings.dFocusScreenFraction ) );
		
		JCheckBox cbZoomLoad = new JCheckBox();
		cbZoomLoad.setSelected(BVBSettings.bFocusOnSourcesOnLoad);		
		
		JCheckBox cbShowScaleBar = new JCheckBox();
		cbShowScaleBar.setSelected(BVBSettings.bShowScaleBar);
		
		JCheckBox cbShowMultiBox = new JCheckBox();
		cbShowMultiBox.setSelected(BVBSettings.bShowMultiBox);
		
		JCheckBox cbHighLightBox = new JCheckBox();
		cbHighLightBox.setSelected(BVBSettings.bHighlightSelectedBoxes);
		
		JButton butHighLightColor = new JButton( new ColorIcon( BVBSettings.boxHighlightColor) );	
		butHighLightColor.addActionListener( e -> {
			Color newColor = JColorChooser.showDialog(pViewSettings, "Choose highlight color", BVBSettings.boxHighlightColor );
			if (newColor != null)
			{
				selectColors.setColor(newColor, 1);
				butHighLightColor.setIcon(new ColorIcon(newColor));
			}			
		});
		
		JCheckBox cbPyramidize = new JCheckBox();
		cbPyramidize.setSelected(BVBSettings.bLoadPyramidize);
		
		JCheckBox cbBGShader = new JCheckBox();
		cbBGShader.setSelected(BVBSettings.bShowRandomShader);
				
		gbc.gridx = 0;
		gbc.gridy = 0;	
		GBCHelper.alighLoose(gbc);
		
		pViewSettings.add(new JLabel("Background color: "), gbc);
		gbc.gridx++;
		pViewSettings.add(butCanvasBGColor, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Show scale bar "), gbc);
		gbc.gridx++;
		pViewSettings.add(cbShowScaleBar, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Show MultiBox"), gbc);
		gbc.gridx++;
		pViewSettings.add(cbShowMultiBox, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Highlight selected objects"), gbc);
		gbc.gridx++;
		pViewSettings.add(cbHighLightBox, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Highlight color: "), gbc);
		gbc.gridx++;
		pViewSettings.add(butHighLightColor, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Transform animation duration (ms): "), gbc);
		gbc.gridx++;
		pViewSettings.add(nfAnimationDuration,gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Screen fraction on zoom/focus: "), gbc);
		gbc.gridx++;
		pViewSettings.add(nfFocusScreenFraction, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Focus on loaded objects "), gbc);
		gbc.gridx++;
		pViewSettings.add(cbZoomLoad, gbc);

		// not sure if we need it, skip for now
		gbc.gridx=0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Pyramidize loaded sources "), gbc);
		gbc.gridx++;
		pViewSettings.add(cbPyramidize, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Show random shader on startup"), gbc);
		gbc.gridx++;
		pViewSettings.add(cbBGShader, gbc);
				
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
			
			tempC = selectColors.getColor(1);
			if(tempC != null)
			{
				bRepaintBVV = true;
				BVBSettings.boxHighlightColor = tempC;
				Prefs.set( "BVB.boxHighlightColor", tempC.getRGB() );				
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
			SwingUtilities.invokeLater(() -> {
				bvb.multiBoxOverlayBVB.setEnabled(  BVBSettings.bShowMultiBox );
				bvb.repaintBVV();
			});
			
			BVBSettings.bHighlightSelectedBoxes = cbHighLightBox.isSelected();
			Prefs.set("BVB.bHighlightSelectedBoxes", BVBSettings.bHighlightSelectedBoxes);
			
			BVBSettings.dFocusScreenFraction = Double.parseDouble(nfFocusScreenFraction.getText());
			Prefs.set("BVB.dFocusScreenFraction",BVBSettings.dFocusScreenFraction);
					
			BVBSettings.bLoadPyramidize = cbPyramidize.isSelected();
			Prefs.set("BVB.bLoadPyramidize", BVBSettings.bLoadPyramidize);
			
			BVBSettings.bShowRandomShader = cbBGShader.isSelected();
			Prefs.set("BVB.bShowRandomShader", BVBSettings.bShowRandomShader);
			
			if(bRepaintBVV)
			{
				bvb.repaintBVV();
			}
		}
	}
	
	void dialSceneStateSave()
	{
		String filename;

		filename = BVBSettings.lastDir + "/" + SerializationIO.getTimestamp() + "_sceneStateBVB";
		SaveDialog sd = new SaveDialog("Save BVB scene state", filename, ".json");
		String path = sd.getDirectory();
		if (path == null)
			return;
		BVBSettings.lastDir = path;
		Prefs.set( "BVB.lastDir", BVBSettings.lastDir );
		filename = path + sd.getFileName();
		SceneStateDTO scene = SceneStateDTO.captureState( bvb );

		try
		{
			SerializationIO.MAPPER.writeValue( new File(filename), scene );
		}
		catch ( IOException exc )
		{
			exc.printStackTrace();
			IJ.log( "BVB: Error while saving scene state. See console for the full log." );
		}	        
	}
	
	void dialSceneStateLoad()
	{
		String filename;
		JFileChooser chooser = new JFileChooser(BVBSettings.lastDir);
		chooser.setDialogTitle( "Load BVB scene state" );
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "BVB scene state", "json");
        chooser.setFileFilter(filter);
        
        int returnVal = chooser.showOpenDialog(null);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
            BVBSettings.lastDir = chooser.getSelectedFile().getParent();
            Prefs.set( "BVB.lastDir",  BVBSettings.lastDir );
            filename =  chooser.getSelectedFile().getPath();
            SceneStateDTO sceneDTO = null;
    
            try
            {
            	sceneDTO = SerializationIO.MAPPER.readValue(new File(filename), SceneStateDTO.class);
            }
            catch ( IOException exc )
            {
            	exc.printStackTrace();
            }
 
            if(sceneDTO != null)
            {
            	if(!sceneDTO.BVBVersion.equals( BVBSettings.sVersion ))
            	{
            		IJ.log( "BVB scene state was made in version " + sceneDTO.BVBVersion
            				+ ", but current plugin version is " + BVBSettings.sVersion + ".");
            		IJ.log( "Trying to load stored data anyway.");

            	}
            	
            	SceneStateDTO.restoreState( bvb, sceneDTO, filename );
     
            }
            else
            {
            	IJ.log( "BVB: Error while loading scene state. See console for the full log." );
            }
        }
	}
}
