package bvb.gui.shapes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.NumberField;
import bvb.shapes.BasicMeshColor;
import bvb.shapes.BasicShape;

public class GeneralPropertiesPanel extends JPanel
{
	final BigVolumeBrowser bvb;
	final JComboBox<String> cbBlending;
	final NumberField nfSilhouetteDecay;
	final NumberField nfCartesianStep;
	final NumberField nfCartesianFraction;
	
	public GeneralPropertiesPanel(final BigVolumeBrowser bvb_)
	{
		super();	
		
		bvb = bvb_;
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		//GBCHelper.alighLeft(gbc);
		
		String[] sBlending = {"Weighted OIT", "Alpha OVER"};
		cbBlending = new JComboBox< >(sBlending);
		cbBlending.addActionListener( (e)->{
			updateBlending();				
			});

		nfSilhouetteDecay = new NumberField(5);		
		nfSilhouetteDecay.setText( "1.0" );
		nfSilhouetteDecay.addListener( (v)->
		{
			updateSilhouetteDecay(Math.abs( v ));
		} );
		
		nfCartesianStep = new NumberField(5);		
		nfCartesianStep.setText( "2.0" );
		nfCartesianStep.addListener( (v)->
		{
			updateCartesianGrid();
		} );
		
		nfCartesianFraction = new NumberField(5);		
		nfCartesianFraction.setText( "0.2" );
		nfCartesianFraction.addListener( (v)->
		{
			updateCartesianGrid();
		} );
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		this.add( new JLabel("Transparency: "), gbc );
		gbc.gridx++;
		this.add( cbBlending, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		this.add( new JLabel("Mesh silhouette decay: "), gbc );
		gbc.gridx++;
		this.add( nfSilhouetteDecay, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		this.add( new JLabel("Mesh cartesian step: "), gbc );
		gbc.gridx++;
		this.add( nfCartesianStep, gbc);
		
		
		gbc.gridx = 0;
		gbc.gridy++;
		this.add( new JLabel("Mesh cartesian fraction: "), gbc );
		gbc.gridx++;
		this.add( nfCartesianFraction, gbc);
		
		//filler
		gbc.gridx=0;
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
			bvb.bvvViewer.showMessage( "weighted OIT" );
		}
		else
		{
			bvb.bvvViewer.showMessage( "alpha compositing" );			
		}
	}
	
	synchronized void updateSilhouetteDecay(final double v)
	{

		final float fv = (float)v;
		final List< BasicShape> shapeList = bvb.shapes;
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicMeshColor)
			{
				((BasicMeshColor)sh).setSilhouetteDecay( fv );
			}
		}
		bvb.repaintBVV();

	}
	
	synchronized void updateCartesianGrid()
	{
		final List< BasicShape> shapeList = bvb.shapes;
		final float valFr = Math.max(Math.min(Math.abs(Float.parseFloat( nfCartesianFraction.getText())),1.0f), 0.0f);
		final float valStep = Math.abs(Float.parseFloat(nfCartesianStep.getText()));
		
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicMeshColor)
			{
				((BasicMeshColor)sh).setCartesianGrid( valStep,valFr );
			}
		}
		bvb.repaintBVV();

	}
}
