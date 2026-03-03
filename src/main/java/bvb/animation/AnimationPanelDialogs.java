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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import bvb.animation.utils.Timeline;
import bvb.core.BVVSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.GBCHelper;
import bvb.gui.GetFolderDialog;
import bvb.gui.NumberField;
import ij.IJ;
import ij.Prefs;

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
		
		GridBagConstraints cd = new GridBagConstraints();
		GBCHelper.alighLeft(cd);
		
		final NumberField nfFPS = new NumberField(4);
		nfFPS.setIntegersOnly( true );
		nfFPS.setText(Integer.toString( aPanel.nRenderFPS ));
		final NumberField nfWidth = new NumberField(4);
		nfWidth.setIntegersOnly( true );
		nfWidth.setText(Integer.toString( aPanel.nRenderWidth ));
		final NumberField nfHeight = new NumberField(4);
		nfHeight.setIntegersOnly( true );
		nfHeight.setText(Integer.toString( aPanel.nRenderHeight));
		
		cd.gridx = 0;
		cd.gridy = 0;	
		panRenderSettings.add(new JLabel("Render FPS:"),cd);
		cd.gridx++;
		panRenderSettings.add(nfFPS, cd);	
		
		cd.gridx = 0;
		cd.gridy++;	
		panRenderSettings.add(new JLabel("Render width (px):"),cd);
		cd.gridx++;
		panRenderSettings.add(nfWidth, cd);			
		
		cd.gridx = 0;
		cd.gridy++;	
		panRenderSettings.add(new JLabel("Render height (px):"),cd);
		cd.gridx++;
		panRenderSettings.add(nfHeight, cd);			
		
		int reply = JOptionPane.showConfirmDialog(null, panRenderSettings, "Render settings", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (reply == JOptionPane.OK_OPTION) 
		{
			aPanel.nRenderFPS = Integer.parseInt( nfFPS.getText());
			Prefs.set("BVB.nRenderFPS", (double)aPanel.nRenderFPS);
			
			aPanel.nRenderWidth = Integer.parseInt( nfWidth.getText());
			Prefs.set("BVB.nRenderWidth", (double)aPanel.nRenderWidth);
			
			aPanel.nRenderHeight = Integer.parseInt( nfHeight.getText());
			Prefs.set("BVB.nRenderHeight", (double)aPanel.nRenderHeight);
			
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
		
		GridBagConstraints cd = new GridBagConstraints();
	
		pAnimSettings.setLayout(new GridBagLayout());
		
		JCheckBox cbMultiBox = new JCheckBox();
		cbMultiBox.setSelected( aPanel.bRenderMultiBox);
		
		JCheckBox cbScaleBar = new JCheckBox();
		cbScaleBar.setSelected( aPanel.bRenderScaleBar);
		
		NumberField nfFrameRenderMax = new NumberField(4);
		nfFrameRenderMax.setIntegersOnly(true);
		nfFrameRenderMax.setText(Integer.toString(aPanel.nRenderFrameTimeLimit));
		
		cd.gridx = 0;
		cd.gridy = 0;	
		GBCHelper.alighLoose(cd);
		pAnimSettings.add(new JLabel("Render BVV MultiBox: "),cd);
		cd.gridx++;
		pAnimSettings.add(cbMultiBox,cd);	
		
		cd.gridx = 0;
		cd.gridy++;
		pAnimSettings.add(new JLabel("Render scale bar: "),cd);
		cd.gridx++;
		pAnimSettings.add(cbScaleBar,cd);
		
		cd.gridx = 0;
		cd.gridy++;
		pAnimSettings.add(new JLabel("Maximum frame render limit (s): "),cd);
		cd.gridx++;
		pAnimSettings.add(nfFrameRenderMax,cd);
		
		cd.gridx = 0;
		cd.gridy++;
		cd.gridwidth = 2;
		pAnimSettings.add(new JLabel("OpenGL viewport resolution "+ 
				Integer.toString( BVVSettings.renderWidth )
				+"x"+Integer.toString( BVVSettings.renderHeight) + " (px)"),cd);
		
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
			
			aPanel.nRenderFrameTimeLimit = Integer.parseInt(nfFrameRenderMax.getText());
			Prefs.set("BVB.nRenderFrameTimeLimit", aPanel.nRenderFrameTimeLimit);
		}
	
	}
}
