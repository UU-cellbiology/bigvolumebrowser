package bvb.gui.shapes;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import bdv.tools.brightness.ColorIcon;
import bdv.util.BoundedValueDouble;
import bvb.core.BigVolumeBrowser;
import bvb.gui.ColorUserSettings;
import bvb.gui.GBCHelper;
import bvb.gui.JPanelConsistent;
import bvb.gui.NumberField;
import bvb.shapes.BasicShape;
import bvb.shapes.BasicSpots;
import bvvpg.ui.panels.BoundedRangePanelPG;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class SpotsPropertiesPanel extends JPanel
{
	final BigVolumeBrowser bvb;
	
	final JPanelConsistent pColor;
	final JPanelConsistent pRender;
	final JPanelConsistent pPointSize;
	final JPanelConsistent pShape;
	
	final JPanelConsistent pSMLMNorm;

	
	final NumberField nfSpSize;
	final JButton butColor;
	final JComboBox<String> cbShape;
	final JComboBox<String> cbRender;

	
	final NumberField nfSMLMNorm;

	final BoundedRangePanelPG  smlmRange = new BoundedRangePanelPG();
	final BoundedValuePanelPG  smlmGammaRange;
	
	final JTabbedPane spotsTabPane;
		
	final JPanel topPanel;
	final JPanel bottomPanel;
	
	final ArrayList<Component> allComp = new ArrayList<>();
	
	ColorUserSettings selectColors = new ColorUserSettings();
	
	private boolean blockUpdates = false;
	
	public SpotsPropertiesPanel(final BigVolumeBrowser bvb_)
	{
		super();	
		
		bvb = bvb_;
		
		setLayout(new GridBagLayout());
	
		
		GridBagConstraints gbc = new GridBagConstraints();		

		butColor = new JButton( new ColorIcon( Color.WHITE ) );
		butColor.addActionListener( e -> {
			Color newColor = JColorChooser.showDialog(null, "Choose spots color", 
					selectColors.getColor( 0 ));
			if (newColor != null)
			{
				selectColors.setColor(newColor, 0);
				updateColors();
			}
			
		});
		
		GBCHelper.alighLeft(gbc);
		pColor = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pColor.add( new JLabel("Color: "), gbc );
		gbc.gridx++;
		pColor.add( butColor, gbc );
		
		nfSpSize = new NumberField(5);
		nfSpSize.setLimits( 0.0, Double.MAX_VALUE );
		nfSpSize.addListener( (v)->
		{
			double in = Math.max( Math.abs(v), 0.0001 );
			updatePointSize(Math.abs( in ));
			//String.format("%.2f", in);
		} );
		pPointSize = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pPointSize.add( new JLabel("Spot size: "), gbc );
		gbc.gridx++;
		pPointSize.add( nfSpSize, gbc );
		
		String[] sShapes = {"Round", "Square"};
		cbShape = new JComboBox< >(sShapes);
		cbShape.addActionListener( (e)->{
			updateShape();				
			});
		pShape = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pShape.add( new JLabel("Shape: "), gbc );
		gbc.gridx++;
		pShape.add( cbShape, gbc );

		String[] sRender = {"Filled", "Outline", "Gauss", "SMLM"};
		cbRender = new JComboBox< >(sRender);
		cbRender.addActionListener( (e)->{
			updateRender();				
			});
		pRender = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pRender.add( new JLabel("Render: "), gbc );
		gbc.gridx++;
		pRender.add( cbRender, gbc );
		
		
		nfSMLMNorm = new NumberField(5);
		nfSMLMNorm.setLimits( 0.0, Double.MAX_VALUE );
		nfSMLMNorm.addListener( (v)->
		{
			updateSMLMNorm(Math.abs( v ));
			//String.format("%.2f", in);
		} );
		pSMLMNorm = new JPanelConsistent(new GridBagLayout());
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		pSMLMNorm.add( new JLabel("SMLM norm: "), gbc );
		gbc.gridx++;
		pSMLMNorm.add( nfSMLMNorm, gbc );
		
		
		smlmGammaRange =  new BoundedValuePanelPG(new BoundedValueDouble( 0.01, 5.0, 1.0 ) ) ;
		smlmGammaRange.changeListeners().add( () -> updateSMLMGamma());
		
		
		allComp.add( butColor );
		allComp.add( nfSpSize );
		allComp.add( cbShape );
		allComp.add( cbRender );
		allComp.add( nfSMLMNorm );
		allComp.add( smlmGammaRange );
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		//GBCHelper.alighLoose(gbc);
		gbc.insets = new Insets(0,0,0,0);
		
		//split panels		
		topPanel = new JPanel(new GridBagLayout());
		bottomPanel = new JPanel(new GridBagLayout());
		//Top Panel
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		topPanel.add(pColor, gbc );
		
		gbc.gridy ++;	
		topPanel.add(pPointSize, gbc );
		
		gbc.gridy ++;		
		topPanel.add(pShape, gbc );
		
		gbc.gridy ++;
		topPanel.add(pRender, gbc );
		
		//Bottom Panel
		gbc.gridx = 0;
		gbc.gridy = 0;

		bottomPanel.add( pSMLMNorm, gbc );
		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.1;
		bottomPanel.add( smlmGammaRange, gbc );

		spotsTabPane = new JTabbedPane(SwingConstants.TOP);
		
		spotsTabPane.addTab( "Shape", topPanel);
		spotsTabPane.addTab( "Color", bottomPanel);
	    gbc = new GridBagConstraints();	
	    gbc.gridx=0;
	    gbc.gridy=0;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 0.5;
	    this.add( spotsTabPane, gbc );
	}
	
	synchronized void updateGUI(boolean updateText)
	{
		boolean bFirstMesh = true;
		
		boolean bColorSame = true;
		boolean bPointSizeSame = true;
		boolean bShapeSame = true;
		boolean bRenderSame = true;
		float fPointSizeIn;
		float fPointSize = -1.0f;
		Color currColor = Color.WHITE;
		int nRender = 0;
		int nShape = 0;
		float fSMLMNorm = -1.0f;
		float fSMLMGamma = -1.0f;
		
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicSpots)
			{
				final BasicSpots spotsShape = (BasicSpots)sh;
				if(bFirstMesh)
				{
					currColor = spotsShape.getColor();
					fPointSizeIn = spotsShape.getPointSize();
					if(fPointSizeIn>0.0)
					{
						fPointSize = spotsShape.getPointSize();
					}
					nShape = spotsShape.getPointShape();
					nRender = spotsShape.getRenderType();

					bFirstMesh = false;
				}
				else
				{
					bRenderSame &= (nRender ==  spotsShape.getRenderType());
					bShapeSame &= (nShape == spotsShape.getPointShape());
					bColorSame &= currColor.equals( spotsShape.getColor() );
					bPointSizeSame &= Math.abs( fPointSize - spotsShape.getPointSize())<0.0001;

				}
			}
		}
		
		final Color cColorFin = currColor;
		final float fPointSizeFin = fPointSize;		
		final int nShapeFin = nShape;
		final int nRenderFin = nRender;		
		final float fSMLMNormFin = fSMLMNorm;		
		final float fSMLMGammaFin = fSMLMGamma;		

		
		final boolean bColorSameFin = bColorSame;
		final boolean bPointSizeSameFin = bPointSizeSame;
		final boolean bShapeSameFin = bShapeSame;
		final boolean bRenderSameFin = bRenderSame;


		SwingUtilities.invokeLater( () -> {
			synchronized ( SpotsPropertiesPanel.this )
			{
				blockUpdates = true;
				
				DecimalFormatSymbols symbols = new DecimalFormatSymbols();
				symbols.setDecimalSeparator('.');
				DecimalFormat df3 = new DecimalFormat ("#.######", symbols);
				for (int d=0;d<3;d++)
				{					
					pColor.setConsistent( bColorSameFin );
					pPointSize.setConsistent( bPointSizeSameFin );
					pShape.setConsistent( bShapeSameFin );
					pRender.setConsistent( bRenderSameFin );
					
					if(bColorSameFin)
					{
						selectColors.setColor( cColorFin, 0 );
						butColor.setIcon(  new ColorIcon( cColorFin ) );
					}
					
					if(bPointSizeSameFin)
					{
						if(bPointSizeSameFin && fPointSizeFin < 0.0)
						{
							nfSpSize.setText( "various");
							nfSpSize.setEnabled( false );
						}
						else
						{
							nfSpSize.setEnabled( true );
							nfSpSize.setText( df3.format( fPointSizeFin));
						}

					}
					if(updateText)
					{
						nfSMLMNorm.setText( df3.format( fSMLMNormFin) );
						smlmGammaRange.getValue().setCurrentValue( fSMLMGammaFin );
					}
					if(bRenderSameFin)
					{
						cbRender.setSelectedIndex( nRenderFin );
					}

					if(bShapeSameFin)
					{
						cbShape.setSelectedIndex( nShapeFin );
					}
						
				}
				blockUpdates = false;
			}
		} );
		
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(final Component nC:allComp)
		{
			nC.setEnabled( bEnabled );
		}
	}
	
	synchronized void updateColors()
	{
		if(!blockUpdates)
		{
			final Color cColor = selectColors.getColor( 0 );
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					((BasicSpots)sh).setColor( cColor );
				}
			}
			bvb.repaintBVV();
			updateGUI(false);
		}
	}
	
	synchronized void updatePointSize(final double v)
	{
		if(!blockUpdates)
		{
			final float fv = (float)v;
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					if(((BasicSpots)sh).getPointSize()>0.0f)
					{
						((BasicSpots)sh).setPointSize( fv );
					}
				}
			}
			bvb.updateSceneRender();
			updateGUI(false);
		}
	}
	
	synchronized void updateShape()
	{
		if(!blockUpdates)
		{
			final int nShapeType = cbShape.getSelectedIndex();
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					((BasicSpots)sh).setPointShape( nShapeType );
				}
			}
			bvb.repaintBVV();
			updateGUI(false);
		}
	}
	synchronized void updateRender()
	{
		if(!blockUpdates)
		{
			final int nRenderType = cbRender.getSelectedIndex();
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					((BasicSpots)sh).setRenderType( nRenderType );
				}
			}
			bvb.repaintBVV();
			updateGUI(false);
		}
	}
	
	synchronized void updateSMLMNorm (double fGaussNorm)
	{
		if(!blockUpdates)
		{
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					//((Spots)sh).setSMLMNorm( (float)fGaussNorm );
				}
			}
			bvb.repaintBVV();
			updateGUI(false);
		}
	}
	
	synchronized void updateSMLMGamma ()
	{
		if(!blockUpdates)
		{
			float fGaussGamma = (float)smlmGammaRange.getValue().getCurrentValue();
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					//((Spots)sh).setSMLMGamma( fGaussGamma ); 
				}
			}
			bvb.repaintBVV();
			updateGUI(false);
		}
	}
}
