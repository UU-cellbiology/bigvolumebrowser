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
package bvb.animation;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import bvb.animation.utils.Timeline;
import bvb.core.BVBSettings;
import bvb.core.BVVSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.GBCHelper;
import bvb.gui.GetFolderDialog;
import bvb.gui.NumberField;
import bvb.io.dto.SerializationIO;
import bvb.io.dto.StoryDTO;
import ij.IJ;
import ij.Prefs;
import ij.io.SaveDialog;

public class AnimationPanelDialogs
{
	final BigVolumeBrowser bvb;
	
	final AnimationPanel aPanel;
	
	public AnimationPanelDialogs(	final BigVolumeBrowser bvb_, final AnimationPanel aPanel_)
	{
		bvb = bvb_;
		aPanel = aPanel_;
	}

	boolean dialRenderSettings()
	{
		final JPanel panRenderSettings = new JPanel();
		panRenderSettings.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		GBCHelper.alighLeft(gbc);
		
		final NumberField nfFPS = new NumberField(4);
		nfFPS.setIntegersOnly( true );
		nfFPS.setText(Integer.toString( aPanel.nRenderFPS ));
		
		final NumberField nfWidth = new NumberField(4);
		nfWidth.setIntegersOnly( true );
		nfWidth.setText(Integer.toString( aPanel.nRenderWidth ));
		
		final NumberField nfHeight = new NumberField(4);
		nfHeight.setIntegersOnly( true );
		nfHeight.setText(Integer.toString( aPanel.nRenderHeight));
		
		final JCheckBox cbCurrentWindow = new JCheckBox("");
		
		cbCurrentWindow.addItemListener((e)->{
			boolean bNFState =  !(e.getStateChange() 
					== ItemEvent.SELECTED ? true : false);
			nfWidth.setEnabled( bNFState );
			nfHeight.setEnabled( bNFState );
		});
		cbCurrentWindow.setSelected( aPanel.bRenderCurrentWindowSize );

		final Dimension currDimensionsWindow = bvb.bvvViewer.getSize();
		
		String sCurrSize = Integer.toString( currDimensionsWindow.width )+
				" x " + Integer.toString( currDimensionsWindow.height );
		gbc.gridx = 0;
		gbc.gridy = 0;	
		panRenderSettings.add(new JLabel("Render FPS:"),gbc);
		gbc.gridx++;
		panRenderSettings.add(nfFPS, gbc);	
		
		
		gbc.gridx = 0;
		gbc.gridy++;	
		panRenderSettings.add(new JLabel("Movie width (px):"),gbc);
		gbc.gridx++;
		panRenderSettings.add(nfWidth, gbc);			
		
		gbc.gridx = 0;
		gbc.gridy++;	
		panRenderSettings.add(new JLabel("Movie height (px):"),gbc);
		gbc.gridx++;
		panRenderSettings.add(nfHeight, gbc);	
		
		gbc.gridx = 0;
		gbc.gridy++;	
		panRenderSettings.add(new JLabel("Or use current [" + sCurrSize + "]"),gbc);
		gbc.gridx++;
		gbc.gridheight = 2;
		panRenderSettings.add(cbCurrentWindow, gbc);		
		gbc.gridheight = 1;
		gbc.gridx = 0;
		gbc.gridy++;	
		panRenderSettings.add(new JLabel("canvas/window size"),gbc);
		gbc.gridx++;
		panRenderSettings.add(new JLabel(), gbc);	
		
		int reply = JOptionPane.showConfirmDialog(null, panRenderSettings, "Render settings", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (reply == JOptionPane.OK_OPTION) 
		{
			aPanel.nRenderFPS = Integer.parseInt( nfFPS.getText());
			Prefs.set("BVB.nRenderFPS", (double)aPanel.nRenderFPS);
			
			aPanel.bRenderCurrentWindowSize = cbCurrentWindow.isSelected();
			Prefs.set("BVB.bRenderCurrentWindowSize", aPanel.bRenderCurrentWindowSize);
			
			if(!aPanel.bRenderCurrentWindowSize)
			{
				aPanel.nRenderWidth = Integer.parseInt( nfWidth.getText());
				Prefs.set("BVB.nRenderWidth", (double)aPanel.nRenderWidth);
				
				aPanel.nRenderHeight = Integer.parseInt( nfHeight.getText());
				Prefs.set("BVB.nRenderHeight", (double)aPanel.nRenderHeight);
			}
			
			aPanel.sRenderSavePath = GetFolderDialog.getSelectedFolder( "Save animation frames to folder.." );
			
			if(aPanel.sRenderSavePath == null)
			{
				IJ.showStatus( "animation aborted.");
				return false;
			}
			return true;
		}
		return false;
		
	}
	
	void dialPlayerSettings()
	{
		final JPanel panPlayerSettings = new JPanel();
		panPlayerSettings.setLayout(new GridBagLayout());
		
		GridBagConstraints cd = new GridBagConstraints();
		GBCHelper.alighLeft(cd);

		DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
		decimalFormatSymbols.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("0.000", decimalFormatSymbols);
		
		final NumberField nfSpeedFactor = new NumberField(4);
		nfSpeedFactor.setText(df.format(aPanel.player.getPlaybackSpeed()));
		
		cd.gridx = 0;
		cd.gridy = 0;	
		panPlayerSettings.add(new JLabel("Play speed (0.01-100):"),cd);
		cd.gridx++;
		panPlayerSettings.add(nfSpeedFactor, cd);	
		
		JCheckBox cbBackForth = new JCheckBox();
		cbBackForth.setSelected( Prefs.get("BVB.bPlayerBackForth", false) );
		cd.gridy++;
		cd.gridx = 0;
		panPlayerSettings.add(new JLabel("Loop back and forth"),cd);
		cd.gridx++;
		panPlayerSettings.add(cbBackForth,cd);
		
		int reply = JOptionPane.showConfirmDialog(null, panPlayerSettings, "Play preview settings", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (reply == JOptionPane.OK_OPTION) 
		{
			
			aPanel.player.setPlaybackSpeed( ( float ) Math.min(Math.max( 0.01,
											Math.abs( Float.parseFloat( nfSpeedFactor.getText()))),100));
			
			aPanel.bPlayerBackForth = cbBackForth.isSelected();
			Prefs.set("BVB.bPlayerBackForth", aPanel.bPlayerBackForth);
		}
	}
		
	boolean dialChangeTotalTime(boolean bLarger)
	{
		final JPanel panelTotTimeSettings = new JPanel();
		panelTotTimeSettings.setLayout(new GridBagLayout());
		
		GridBagConstraints cd = new GridBagConstraints();
		GBCHelper.alighLeft(cd);
		
		final String[] sTotTimeOptionsL  = { "Add at the start", "Add at the end", "Stretch"};
		final String[] sTotTimeOptionsS = { "Cut at the start", "Cut at the end", "Compress"};
		final JComboBox<String> cbTotTimeOptions;
		if(bLarger)
		{
			cbTotTimeOptions = new JComboBox<>(sTotTimeOptionsL);
		}
		else
		{
			cbTotTimeOptions = new JComboBox<>(sTotTimeOptionsS);
		}
		cbTotTimeOptions.setSelectedIndex(aPanel.nChangeTotalTimeMode);
		cd.gridx = 0;
		cd.gridy = 0;	

		panelTotTimeSettings.add(cbTotTimeOptions, cd);	
		int reply = JOptionPane.showConfirmDialog(null, panelTotTimeSettings, "Change total time", 
				JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (reply == JOptionPane.OK_OPTION) 
		{
			aPanel.nChangeTotalTimeMode = cbTotTimeOptions.getSelectedIndex();
			Prefs.set("BVB.nChangeTotalTimeMode", aPanel.nChangeTotalTimeMode);
			return true;
		}
		return false;
		
	}
	
	KeyFrameScene dialEditKeyFrame(final int nInd)
	{
		final DefaultListModel< KeyFrameScene > listModel = aPanel.listModel;
		final KeyFrameAnimation kfAnim = aPanel.kfAnim;
		final KeyFrameScene keyFrame = listModel.get( nInd );
		
		DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
		decimalFormatSymbols.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("0.0", decimalFormatSymbols);
		
		final JPanel panEdit = new JPanel();
		panEdit.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		GBCHelper.alighLeft(gbc);
		
		JTextField tfName = new JTextField(listModel.get( nInd ).name); 
		
		NumberField nfTimePoint = new NumberField(4);		
		nfTimePoint.setText(df.format(listModel.get( nInd ).fMovieTimePoint));
		String[] easingNames = Timeline.easingRegistry.getAllNames();
		final JComboBox<String> cbEasing = new JComboBox<>(easingNames);
		cbEasing.setSelectedItem( keyFrame.easing.getId() );
		
		gbc.gridx = 0;
		gbc.gridy = 0;	
		panEdit.add(new JLabel("Name:"),gbc);
		gbc.gridx++;
		panEdit.add(tfName, gbc);	
		
		gbc.gridx = 0;
		gbc.gridy++;	
		panEdit.add(new JLabel("Time position:"),gbc);
		gbc.gridx++;
		panEdit.add(nfTimePoint, gbc);	
		
		gbc.gridx = 0;
		gbc.gridy++;	
		panEdit.add(new JLabel("Easing:"),gbc);
		gbc.gridx++;
		panEdit.add(cbEasing, gbc);	

		
		int reply = JOptionPane.showConfirmDialog(null, panEdit, "Edit KeyFrame", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				
		if (reply == JOptionPane.OK_OPTION) 
		{
			boolean bUpdateKF = false;

			if(tfName.getText().length() > 0)
			{
				if(!tfName.getText().equals( keyFrame.name ))
				{
					listModel.setElementAt( keyFrame, nInd );
					bUpdateKF = true;
				}
			}
			else
				return null;
			
			float fNewTime = Math.min(Math.max(0, Float.parseFloat( nfTimePoint.getText())), kfAnim.nTotalTime);
			
			if(Math.abs( listModel.get( nInd ).fMovieTimePoint - fNewTime) > 0.001)
				{ bUpdateKF = true;	}
			
			String easingID = (String)cbEasing.getSelectedItem();
			
			if(!easingID.equals( keyFrame.easing.getId() ))
				{ bUpdateKF = true;}
			
			if(bUpdateKF)
			{
				keyFrame.name = tfName.getText();
				keyFrame.fMovieTimePoint = fNewTime;
				listModel.setElementAt( keyFrame, nInd );
				aPanel.sortListModel();
				keyFrame.easing = Timeline.easingRegistry.get( (String)cbEasing.getSelectedItem() );
				return keyFrame;
			}
			return null;
		}
		return null;
	}
	
	void dialPanelSettings()
	{
		JPanel pAnimSettings = new JPanel();
		
		GridBagConstraints gbc = new GridBagConstraints();
	
		pAnimSettings.setLayout(new GridBagLayout());
		
		JCheckBox cbMultiBox = new JCheckBox();
		cbMultiBox.setSelected( aPanel.bRenderMultiBox);
		
		JCheckBox cbScaleBar = new JCheckBox();
		cbScaleBar.setSelected( aPanel.bRenderScaleBar);

		JCheckBox cbAxesGizmo = new JCheckBox();
		cbAxesGizmo.setSelected( aPanel.bRenderAxesGizmo);
		
		NumberField nfFrameRenderMax = new NumberField(4);
		nfFrameRenderMax.setIntegersOnly(true);
		nfFrameRenderMax.setText(Integer.toString(aPanel.nRenderFrameTimeLimit));
		
		gbc.gridx = 0;
		gbc.gridy = 0;	
		GBCHelper.alighLoose(gbc);
		pAnimSettings.add(new JLabel("Render BVV MultiBox: "),gbc);
		gbc.gridx++;
		pAnimSettings.add(cbMultiBox,gbc);	
		
		gbc.gridx = 0;
		gbc.gridy++;
		pAnimSettings.add(new JLabel("Render scale bar: "),gbc);
		gbc.gridx++;
		pAnimSettings.add(cbScaleBar,gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		pAnimSettings.add(new JLabel("Render axes gizmo: "),gbc);
		gbc.gridx++;
		pAnimSettings.add(cbAxesGizmo,gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		pAnimSettings.add(new JLabel("Maximum frame render limit (s): "),gbc);
		gbc.gridx++;
		pAnimSettings.add(nfFrameRenderMax,gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		pAnimSettings.add(new JLabel("OpenGL viewport resolution "+ 
				Integer.toString( BVVSettings.renderWidth )
				+"x"+Integer.toString( BVVSettings.renderHeight) + " (px)"),gbc);
		
		int reply = JOptionPane.showConfirmDialog(null, pAnimSettings, "Animation Settings", 
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (reply == JOptionPane.OK_OPTION) 
		{
			//multibox
			aPanel.bRenderMultiBox = cbMultiBox.isSelected();
			Prefs.set("BVB.bRenderMultiBox", aPanel.bRenderMultiBox );

			//scale bar
			aPanel.bRenderScaleBar = cbScaleBar.isSelected();
			Prefs.set("BVB.bRenderScaleBar", aPanel.bRenderScaleBar );
			
			//axes gizmo
			aPanel.bRenderAxesGizmo = cbAxesGizmo.isSelected();
			Prefs.set("BVB.bRenderAxesGizmo", aPanel.bRenderAxesGizmo );
			
			aPanel.nRenderFrameTimeLimit = Integer.parseInt(nfFrameRenderMax.getText());
			Prefs.set("BVB.nRenderFrameTimeLimit", aPanel.nRenderFrameTimeLimit);
		}
	
	}
	
	void dialStorylineLoad()
	{
		String filename;
		JFileChooser chooser = new JFileChooser(BVBSettings.lastDir);
		chooser.setDialogTitle( "Load BVB animation timeline" );
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "BVB animation timeline", "json");
        chooser.setFileFilter(filter);
        
        int returnVal = chooser.showOpenDialog(null);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
	        aPanel.setEnabled( false );

            BVBSettings.lastDir = chooser.getSelectedFile().getParent();
            Prefs.set( "BVB.lastDir",  BVBSettings.lastDir );
            filename =  chooser.getSelectedFile().getPath();
            StoryDTO storyDTO = null;
    
            try
            {
            	storyDTO = SerializationIO.MAPPER.readValue(new File(filename), StoryDTO.class);
            }
            catch ( IOException exc )
            {
            	exc.printStackTrace();
            }
 
            if(storyDTO != null)
            {
            	if(!storyDTO.BVBVersion.equals( BVBSettings.sVersion ))
            	{
            		IJ.log( "BVB animation timeline was made in version " + storyDTO.BVBVersion
            				+ ", but current plugin version is " +BVBSettings.sVersion + ".");
            		IJ.log( "Trying to load timeline anyway.");

            	}
            	
        		aPanel.checkObjectsPresence( storyDTO.bvbObjects, filename );
        		aPanel.restoreStory( storyDTO );
            }
            else
            {
            	IJ.log( "BVB: Error while loading animation timeline. See console for the full log." );
            }
            aPanel.setEnabled( true );


        }
	}

	
	void dialStorylineSave()
	{
		if(aPanel.listModel.size() > 0)
		{
			String filename;
			
			filename = SerializationIO.getTimestamp() + "_animationTimelineBVB";
			SaveDialog sd = new SaveDialog("Save storyline ", BVBSettings.lastDir, filename, ".json");
	        String path = sd.getDirectory();
	        if (path == null)
	        	return;
	        aPanel.setEnabled( false );
	        BVBSettings.lastDir = path;
	        Prefs.set( "BVB.lastDir", BVBSettings.lastDir );
	        filename = path + sd.getFileName();
	        StoryDTO story = new StoryDTO();
	        story.BVBVersion = BVBSettings.sVersion;
	        story.keyFrameAnimation = aPanel.kfAnim.toDTO();
	        story.bvbObjects = bvb.objectHashStorage.toDTO();
	        story.timeline = aPanel.timeline.toDTO();

	        try
			{
	        	SerializationIO.MAPPER.writeValue( new File(filename), story );
			}
			catch ( IOException exc )
			{
				exc.printStackTrace();
				IJ.log( "BVB: Error while saving animation timeline. See console for the full log." );
			}	        
	        aPanel.setEnabled( true );

		}
		else
		{
			IJ.showStatus( "BVB: cannot save animation timeline, at least 2 keyframes are required." );
		}
	}
	void showAnimationModeWarning()
	{
		JPanel pWarning = new JPanel(new GridBagLayout());
		
		String message  = "<html>During the render BVB window becomes locked.<br />"
				+ "You can interrupt it at any time by pressing the <b>Esc</b> button.</html>";
		String[] options = {"OK"};
		
		JCheckBox cbShowAgain = new JCheckBox("Do not show this message again");
		
		cbShowAgain.setSelected( false );
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5,0,5,0);
		gbc.gridx = 0;
		gbc.gridy = 0;
		pWarning.add( new JLabel(message), gbc );
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.EAST;
		pWarning.add( cbShowAgain, gbc );
		
		JOptionPane.showOptionDialog(null, pWarning, "Animation render mode", 
				JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
		
		
		aPanel.bShowAnimationWarning = !cbShowAgain.isSelected();
		Prefs.get( "BVB.bShowAnimationWarning", aPanel.bShowAnimationWarning );
	}
}
