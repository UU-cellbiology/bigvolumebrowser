package bvb.gui.shapes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

public class GeneralPropertiesPanel extends JPanel
{
	final BigVolumeBrowser bvb;
	final JComboBox<String> cbBlending;
	final NumberField nfSilhouetteDecay;
	final NumberField nfWireLineWidth;
	final NumberField nfCartesianStep;
	final NumberField nfCartesianFraction;
	
	public GeneralPropertiesPanel(final BigVolumeBrowser bvb_)
	{
		super();	
		
		bvb = bvb_;
		
		int nDigitsFloatTextField = 4;
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();

		
		String[] sBlending = {"Weight OIT", "Alpha OVER"};
		cbBlending = new JComboBox< >(sBlending);
		cbBlending.addActionListener( (e)->{
			updateBlending();				
			});

		nfSilhouetteDecay = new NumberField(nDigitsFloatTextField);		
		nfSilhouetteDecay.setText( "1.00" );
		nfSilhouetteDecay.setLimits( 0.0, Double.MAX_VALUE );
		nfSilhouetteDecay.addListener( (v)->
		{
			updateSilhouetteDecay(Math.abs( v ));
		} );
		
		nfWireLineWidth = new NumberField(3);
		nfWireLineWidth.setIntegersOnly( true );
		nfWireLineWidth.setText( "1.0" );
		nfWireLineWidth.setLimits( 0.0, Double.MAX_VALUE );
		nfWireLineWidth.addListener( (v)->
		{
			updateWireLineWidth();
		} );
		
		nfCartesianStep = new NumberField(nDigitsFloatTextField);		
		nfCartesianStep.setText( "2.0" );
		nfCartesianStep.setLimits( 0.0, Double.MAX_VALUE );
		nfCartesianStep.addListener( (v)->
		{
			updateCartesianGrid();
		} );
		
		nfCartesianFraction = new NumberField(nDigitsFloatTextField);		
		nfCartesianFraction.setText( "0.2" );
		nfCartesianFraction.setLimits( 0.0, Double.MAX_VALUE );
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
		this.add( new JLabel("Mesh wire line width: "), gbc );
		gbc.gridx++;
		this.add( nfWireLineWidth, gbc);
		
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
			bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "weighted OIT", 800, TextPosition.BOTTOM_RIGHT, BVBSettings.canvasOverlayColor )  );
		}
		else
		{
			bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "alpha compositing", 800, TextPosition.BOTTOM_RIGHT, BVBSettings.canvasOverlayColor )  );
		}
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
	
	synchronized void updateWireLineWidth()
	{
		final List< BasicShape> shapeList = bvb.shapes;
		final float valWidth = Math.abs(Float.parseFloat( nfWireLineWidth.getText()));
		
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicMeshShape)
			{
				((BasicMeshShape)sh).setWireLineWidth( valWidth );
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
			if(sh instanceof BasicMeshShape)
			{
				((BasicMeshShape)sh).setCartesianGrid( valStep,valFr );
			}
		}
		bvb.repaintBVV();

	}
}
