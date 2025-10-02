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
import bvb.core.BigVolumeBrowser;
import bvb.gui.ColorUserSettings;
import bvb.gui.GBCHelper;
import bvb.gui.JPanelConsistent;
import bvb.gui.NumberField;
import bvb.shapes.BasicShape;
import bvb.shapes.BasicSpots;

public class SpotsPropertiesPanel extends JPanel
{
	final BigVolumeBrowser bvb;
	
	final JPanelConsistent pColor;
	final JPanelConsistent pRender;
	final JPanelConsistent pPointSize;
	final JPanelConsistent pShape;
	final JPanelConsistent pSizeScale;
	
	final JPanelConsistent pMapLUT;
	final LUTSelectionPanel panelLUT;

	
	final JButton butColor;
	final NumberField nfSpSize;
	final NumberField nfSpSizeScale;

	final JComboBox<String> cbShape;
	final JComboBox<String> cbRender;
	final JComboBox<String> cbMapLUT;
	
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
		
		nfSpSizeScale = new NumberField(5);
		nfSpSizeScale.setLimits( 0.0, Double.MAX_VALUE );
		nfSpSizeScale.addListener( (v)->
		{
			double in = Math.max( Math.abs(v), 0.0001 );
			updateSizeScale(Math.abs( in ));
			//String.format("%.2f", in);
		} );
		pSizeScale = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pSizeScale.add( new JLabel("Size scale: "), gbc );
		gbc.gridx++;
		pSizeScale.add( nfSpSizeScale, gbc );
		
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

		String[] sRender = {"Filled", "Outline", "Gauss"};
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
		
		String[] sMapLUT = {"Single color", "X coord LUT", "Y coord LUT", "Z coord LUT", "Size LUT", "Param LUT"};
		
		cbMapLUT = new JComboBox< >(sMapLUT);
		cbMapLUT.addActionListener( (e) -> updateLUTMapping());

		pMapLUT = new JPanelConsistent(new GridBagLayout());
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		pMapLUT.add( new JLabel("Color Mapping: "), gbc );
		gbc.gridx++;
		pMapLUT.add( cbMapLUT, gbc );	
		
		panelLUT = new LUTSelectionPanel();
		panelLUT.setConsistent( true );

		panelLUT.changeListeners().add( ()-> updateLUT());
		
		allComp.add( butColor );
		allComp.add( nfSpSize );
		allComp.add( nfSpSizeScale );
		allComp.add( cbShape );
		allComp.add( cbRender );
		allComp.add( cbMapLUT );
		allComp.add( panelLUT.lutButton );
		
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
		topPanel.add(pSizeScale, gbc );
		
		gbc.gridy ++;		
		topPanel.add(pShape, gbc );
		
		gbc.gridy ++;
		topPanel.add(pRender, gbc );
		
		//Bottom Panel
		gbc.gridx = 0;
		gbc.gridy = 0;

		bottomPanel.add( pMapLUT, gbc );
		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.1;
		bottomPanel.add( panelLUT, gbc );

		spotsTabPane = new JTabbedPane(SwingConstants.TOP);
		
		spotsTabPane.addTab( "Shape", topPanel);
		spotsTabPane.addTab( "Colorcode", bottomPanel);
	    gbc = new GridBagConstraints();	
	    gbc.gridx=0;
	    gbc.gridy=0;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 0.5;
	    this.add( spotsTabPane, gbc );
	}
	
	synchronized void updateGUI()
	{
		boolean bFirstMesh = true;
		
		boolean bColorSame = true;
		boolean bPointSizeSame = true;
		boolean bSizeScaleSame = true;
		boolean bShapeSame = true;
		boolean bRenderSame = true;
		boolean bMapLUTSame = true;
		boolean bLUTSame = true;
		float fPointSizeIn;
		float fSizeScaleIn;
		float fPointSize = -1.0f;
		float fSizeScale = -1.0f;

		Color currColor = Color.WHITE;
		int nRender = 0;
		int nShape = 0;
		int nMapLUT = 0;
		String sLUT = "";
		
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
					if(fPointSizeIn >= 0.0)
					{
						fPointSize = fPointSizeIn;
					}
					else
					{
						fSizeScale = spotsShape.getSizeScale();
					}
					nShape = spotsShape.getPointShape();
					nRender = spotsShape.getRenderType();
					nMapLUT = spotsShape.getMapLUTMode();
					sLUT = spotsShape.getLUTName();
					if(sLUT == "" || spotsShape.getMapLUTMode() == 0)
					{
						bLUTSame = false;
					}
					bFirstMesh = false;
				}
				else
				{
					fPointSizeIn = spotsShape.getPointSize();
					fSizeScaleIn = spotsShape.getSizeScale();
					//we encoutered fixed size spots, let's update
					if(fPointSizeIn>=0.0)
					{
						if(fPointSize < 0.0)
						{	
							fPointSize = fPointSizeIn;
						}						
						else
						{
							bPointSizeSame &= Math.abs( fPointSize - fPointSizeIn)<0.0001;
						}
					}
					else
					{
						if(fSizeScale < 0.0)
						{	
							fSizeScale = fSizeScaleIn;
						}	
						else
						{
							bSizeScaleSame &= Math.abs( fSizeScale - fSizeScaleIn)<0.0001;
						}
					}

					bRenderSame &= (nRender ==  spotsShape.getRenderType());
					bShapeSame &= (nShape == spotsShape.getPointShape());
					bColorSame &= currColor.equals( spotsShape.getColor() );
					bMapLUTSame &= (nMapLUT == spotsShape.getMapLUTMode());
					
					
					if(bLUTSame)
					{
						if(spotsShape.getLUTName() == "" || spotsShape.getMapLUTMode() == 0)
						{
							bLUTSame = false;
						}
						else
						{
							bLUTSame &= sLUT.equals( spotsShape.getLUTName());
						}
					}
				}
			}
		}
		
		final Color cColorFin = currColor;
		final float fPointSizeFin = fPointSize;	
		final float fSizeScaleFin = fSizeScale;	
		final int nShapeFin = nShape;
		final int nRenderFin = nRender;			
		final int nMapLUTFin = nMapLUT;
		final String sLUTFin = sLUT;
		
		final boolean bColorSameFin = bColorSame;
		final boolean bPointSizeSameFin = bPointSizeSame;
		final boolean bSizeScaleSameFin = bSizeScaleSame;
		final boolean bShapeSameFin = bShapeSame;
		final boolean bRenderSameFin = bRenderSame;
		final boolean bMapLUTSameFin = bMapLUTSame;
		final boolean bLUTSameFin = bLUTSame;

		SwingUtilities.invokeLater( () -> {
			synchronized ( SpotsPropertiesPanel.this )
			{
				blockUpdates = true;
				
				DecimalFormatSymbols symbols = new DecimalFormatSymbols();
				symbols.setDecimalSeparator('.');
				DecimalFormat df3 = new DecimalFormat ("#.######", symbols);
				for (int d = 0; d < 3; d++)
				{					
					pColor.setConsistent( bColorSameFin );
					pPointSize.setConsistent( bPointSizeSameFin );
					pSizeScale.setConsistent( bSizeScaleSameFin );
					pShape.setConsistent( bShapeSameFin );
					pRender.setConsistent( bRenderSameFin );
					pMapLUT.setConsistent( bMapLUTSameFin );
					panelLUT.setConsistent( bLUTSameFin );
					
					if(bColorSameFin)
					{
						selectColors.setColor( cColorFin, 0 );
						butColor.setIcon(  new ColorIcon( cColorFin ) );
					}
		
					if(fSizeScaleFin > 0)
					{
						nfSpSizeScale.setEnabled( true );
						nfSpSizeScale.setText( df3.format( fSizeScaleFin) );
					}
					else
					{
						nfSpSizeScale.setEnabled( false );
						nfSpSizeScale.setText( "none" );
					}
					

					//all various sizes
					if(fPointSizeFin < 0.0)
					{
						nfSpSize.setText( "various");
						nfSpSize.setEnabled( false );
					}
					//all fixed sizes
					else
					{
						nfSpSize.setEnabled( true );
						nfSpSize.setText( df3.format( fPointSizeFin));
					}

					

					if(bRenderSameFin)
					{
						cbRender.setSelectedIndex( nRenderFin );
					}

					if(bShapeSameFin)
					{
						cbShape.setSelectedIndex( nShapeFin );
					}
					panelLUT.setEnabled( true );
					if(bMapLUTSameFin)
					{
						cbMapLUT.setSelectedIndex(nMapLUTFin);
						if(nMapLUTFin == 0)
						{
							panelLUT.setColor( null );
							panelLUT.setEnabled( false );
							panelLUT.setConsistent( true );
						}
					}
					if(bLUTSameFin)
					{
						panelLUT.setICMbyName( sLUTFin );
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
					((BasicSpots)sh).setMapLUTMode( 0 );
				}
			}
			bvb.repaintBVV();
			updateGUI();
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
					if(((BasicSpots)sh).getPointSize() >= 0.0f)
					{
						((BasicSpots)sh).setPointSize( fv );
					}
				}
			}
			bvb.updateSceneRender();
			updateGUI();
		}
	}
	
	synchronized void updateSizeScale(final double v)
	{
		if(!blockUpdates)
		{
			final float fv = (float)v;
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					if(((BasicSpots)sh).getPointSize() < 0.0f)
					{
						((BasicSpots)sh).setSizeScale( fv );
					}
				}
			}
			bvb.updateSceneRender();
			updateGUI();
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
			updateGUI();
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
			updateGUI();
		}
	}
	
	synchronized void updateLUTMapping()
	{
		if(!blockUpdates)
		{
			final int nMapLUT = cbMapLUT.getSelectedIndex();
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					((BasicSpots)sh).setMapLUTMode( nMapLUT );
				}
			}
			bvb.repaintBVV();
			updateGUI();
		}
	}
	
	synchronized void updateLUT()
	{
		if(!blockUpdates)
		{
			final String sLUT = panelLUT.getICMName();
			if(sLUT == null)
				return;
			if(sLUT == "")
				return;
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					((BasicSpots)sh).setLUT( sLUT );
				}
			}
			bvb.repaintBVV();
			updateGUI();
		}
	}
	

}
