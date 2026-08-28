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
package bvb.gui.shapes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.ColorTextOverlayAnimator;
import bvb.gui.NumberField;
import bvb.gui.ColorTextOverlayAnimator.TextPosition;
import bvb.shapes.BasicShape.AlphaType;
import ij.Prefs;

public class GeneralPropertiesPanel extends JPanel
{
	final BigVolumeBrowser bvb;
	final JComboBox<String> cbBlending;
	final NumberField nfDepthDecay;
	final JCheckBox cbMultiMesh;
	final JCheckBox cbMultiSpots;
	
	final JCheckBox cbSpotsSorting;

	
	public GeneralPropertiesPanel(final BigVolumeBrowser bvb_)
	{
		super();	
		
		bvb = bvb_;
		
		int nDigitsFloatTextField = 4;
		
		setLayout(new GridBagLayout());
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		DecimalFormat df3 = new DecimalFormat ("#.##", symbols);

		GridBagConstraints gbc = new GridBagConstraints();

		
		String[] sBlending = {"Weight OIT", "Alpha OVER"};
		cbBlending = new JComboBox< >(sBlending);
		cbBlending.addActionListener( (e)->{
			updateBlending();				
			});

		nfDepthDecay = new NumberField(nDigitsFloatTextField);		
		nfDepthDecay.setText( df3.format( BVBSettings.fOITDepthDecay ) );
		nfDepthDecay.setLimits( 0.0, Double.MAX_VALUE );
		nfDepthDecay.addListener( (v)->
		{
			updateDepthDecay(Math.abs( v ));
		} );
		
		cbMultiMesh = new JCheckBox("Meshes");
		cbMultiMesh.setSelected( BVBSettings.bMultiSampleMesh );
		cbMultiMesh.addItemListener( (e) -> updateMSAAMeshes() );
		cbMultiSpots = new JCheckBox("Spots");
		cbMultiSpots.setSelected( BVBSettings.bMultiSampleSpots );
		cbMultiSpots.addItemListener( (e) -> updateMSAASpots() );
		
		cbSpotsSorting = new JCheckBox("");
		cbSpotsSorting.setSelected( BVBSettings.bSortSpotsAlphaMode );
		cbSpotsSorting.addItemListener( (e) -> updateSpotsSorting() );
		if(BVBSettings.transparentAlpha == AlphaType.OIT)
			{cbSpotsSorting.setEnabled( false );}
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		this.add( new JLabel("Transparency: "), gbc );
		gbc.gridx++;
		this.add( cbBlending, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		this.add( new JLabel("Weighted OIT depth decay: "), gbc );
		gbc.gridx++;
		this.add( nfDepthDecay, gbc);
		
		JPanel pMSAA = new JPanel(new GridBagLayout());
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridx = 0;
		gbc2.gridy = 0;
		gbc2.insets = new Insets(0, 2, 0, 2);
		
		final JLabel lMSAA = new JLabel("MSAA: ");
		lMSAA.setToolTipText( "Multisample Anti-Aliasing" ); 
		pMSAA.add( lMSAA, gbc2 );
		gbc2.gridx ++;
		pMSAA.add(cbMultiMesh, gbc2 );
		gbc2.gridx++;
		pMSAA.add( cbMultiSpots, gbc2);

		gbc.gridwidth = 2;
		gbc.insets = new Insets(10, 0, 5, 0);
		gbc.gridx = 0;
		gbc.gridy++;
		this.add( pMSAA, gbc );
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		
		gbc.gridx = 0;
		gbc.gridy++;
		this.add( new JLabel("Sort spots in alpha mode: "), gbc );
		gbc.gridx++;
		this.add( cbSpotsSorting, gbc);
		//filler
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.01;
		gbc.weighty = 0.01;
		this.add(new JLabel(), gbc);
		
	}

	void updateBlending()
	{
		BVBSettings.transparentAlpha  =  AlphaType.fromId( cbBlending.getSelectedIndex() + 1);
		if(BVBSettings.transparentAlpha == AlphaType.OIT)
		{
			bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "weighted OIT", 800, TextPosition.BOTTOM_RIGHT, BVBSettings.canvasOverlayColor )  );
			cbSpotsSorting.setEnabled( false );
			BVBSettings.bSortSpotsAlphaMode = false;
		}
		else
		{
			bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "alpha compositing", 800, TextPosition.BOTTOM_RIGHT, BVBSettings.canvasOverlayColor )  );
			cbSpotsSorting.setEnabled( true );
			updateSpotsSorting();
		}
		bvb.repaintBVV();
	}
	
	void updateDepthDecay(final double v)
	{
		BVBSettings.fOITDepthDecay = (float)Math.max( 0.01, Math.abs( v ));		
		Prefs.set("BVB.fOITDepthDecay", BVBSettings.fOITDepthDecay);
		bvb.repaintBVV();
	}
	
	void updateMSAAMeshes()
	{
		BVBSettings.bMultiSampleMesh = cbMultiMesh.isSelected();		
		Prefs.set("BVB.bMultiSampleMesh", BVBSettings.bMultiSampleMesh);
		bvb.repaintBVV();
	}	
	
	void updateMSAASpots()
	{
		BVBSettings.bMultiSampleSpots = cbMultiSpots.isSelected();		
		Prefs.set("BVB.bMultiSampleSpots", BVBSettings.bMultiSampleSpots);
		bvb.repaintBVV();
	}	
	
	void updateSpotsSorting()
	{
		BVBSettings.bSortSpotsAlphaMode = cbSpotsSorting.isSelected();		
		Prefs.set("BVB.bSortSpotsAlphaMode", BVBSettings.bSortSpotsAlphaMode);
		bvb.repaintBVV();
	}
}
