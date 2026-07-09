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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.ColorTextOverlayAnimator;
import bvb.gui.NumberField;
import bvb.gui.ColorTextOverlayAnimator.TextPosition;
import bvb.shapes.BasicMeshShape;
import bvb.shapes.BasicShape;
import ij.Prefs;

public class GeneralPropertiesPanel extends JPanel
{
	final BigVolumeBrowser bvb;
	final JComboBox<String> cbBlending;
	final NumberField nfDepthDecay;
	final NumberField nfSilhouetteDecay;
	
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
		
		nfSilhouetteDecay = new NumberField(nDigitsFloatTextField);		
		nfSilhouetteDecay.setText( "1.00" );
		nfSilhouetteDecay.setLimits( 0.0, Double.MAX_VALUE );
		nfSilhouetteDecay.addListener( (v)->
		{
			updateSilhouetteDecay(Math.abs( v ));
		} );
		
		
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

		gbc.gridx = 0;
		gbc.gridy++;
		this.add( new JLabel("Mesh silhouette decay: "), gbc );
		gbc.gridx++;
		this.add( nfSilhouetteDecay, gbc);
			
		//filler
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.01;
		gbc.weighty = 0.01;
		this.add(new JLabel(), gbc);
		
	}

	void updateBlending()
	{
		BVBSettings.bWeightedOIT = (cbBlending.getSelectedIndex()==0)?true:false;
		bvb.repaintBVV();
		if(BVBSettings.bWeightedOIT)
		{
			bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "weighted OIT", 800, TextPosition.BOTTOM_RIGHT, BVBSettings.canvasOverlayColor )  );
		}
		else
		{
			bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "alpha compositing", 800, TextPosition.BOTTOM_RIGHT, BVBSettings.canvasOverlayColor )  );
		}
	}
	
	synchronized void updateDepthDecay(final double v)
	{

		BVBSettings.fOITDepthDecay = (float)Math.max( 0.01, Math.abs( v ));
		
		Prefs.set("BVB.fOITDepthDecay", BVBSettings.fOITDepthDecay);
		bvb.repaintBVV();

	}
	
	synchronized void updateSilhouetteDecay(final double v)
	{

		final float fv = (float)v;
		final List< BasicShape> shapeList = bvb.shapes;
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicMeshShape)
			{
				((BasicMeshShape)sh).setSilhouetteDecay( fv );
			}
		}
		bvb.repaintBVV();

	}
	
}
