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
import javax.swing.JCheckBox;
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
	final JPanelConsistent pShaded;
	final JPanelConsistent pSizeScale;
	
	final JButton butColor;
	final NumberField nfSpSize;
	final NumberField nfSpSizeScale;

	final JComboBox<String> cbShape;
	final JComboBox<String> cbRender;
	
	final JTabbedPane spotsTabPane;
		
	final JPanel shapePanel;
	final SpotsColorCodePanel colorCodePanel;
	final SpotsOpacityPanel opacityPanel;
	
	public final JCheckBox cbShaded = new JCheckBox();
	
	final ArrayList<Component> allComp = new ArrayList<>();
	
	ColorUserSettings selectColors = new ColorUserSettings();
	
	private boolean blockUpdates = false;
	
	public SpotsPropertiesPanel(final BigVolumeBrowser bvb_)
	{
		super();	
		
		bvb = bvb_;
		
		colorCodePanel = new SpotsColorCodePanel(bvb);
		opacityPanel = new SpotsOpacityPanel(bvb);
		
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
		gbc.gridx = 0;
		gbc.gridy = 0;	
		GBCHelper.alighLeft(gbc);
	
		pColor = new JPanelConsistent(new GridBagLayout());

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
		pShaded = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pShape.add( new JLabel("Shape: "), gbc );
		gbc.gridx++;
		pShape.add( cbShape, gbc );
		pShaded.add( cbShaded );
		cbShaded.setToolTipText( "Round shaded" );
		pShaded.setToolTipText( "Round shaded" );
		cbShaded.addItemListener( (e)-> updateRoundShaded());
		
		
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
		
		allComp.add( butColor );
		allComp.add( nfSpSize );
		allComp.add( nfSpSizeScale );
		allComp.add( cbShape );
		allComp.add( cbShaded );
		allComp.add( cbRender );
		allComp.add( colorCodePanel );
		allComp.add( opacityPanel );
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		//GBCHelper.alighLoose(gbc);
		//GBCHelper.alighLeft(gbc);
		gbc.insets = new Insets(0,0,0,0);
					
		//Shape Panel
		shapePanel = new JPanel(new GridBagLayout());
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		shapePanel.add(pColor, gbc );
		
		gbc.gridy ++;	
		shapePanel.add(pPointSize, gbc );
		
		gbc.gridy ++;	
		shapePanel.add(pSizeScale, gbc );
	
		gbc.gridwidth = 1;	
		gbc.gridy ++;		
		shapePanel.add(pShape, gbc );
		gbc.gridx ++;		
		shapePanel.add(pShaded, gbc );
		
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy++;
		shapePanel.add(pRender, gbc );

		spotsTabPane = new JTabbedPane(SwingConstants.TOP);		
		spotsTabPane.addTab( "Shape", shapePanel);
		spotsTabPane.addTab( "Colorcode", colorCodePanel);
		spotsTabPane.addTab( "Opacity", opacityPanel);	
		spotsTabPane.setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );

	    gbc = new GridBagConstraints();	
	    gbc.insets = new Insets(0,0,0,0);
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 0.5;
	    //gbc.anchor = GridBagConstraints.NORTH;
	    this.add( spotsTabPane, gbc );
	}
	
	synchronized void updateGUI()
	{
		boolean bFirstMesh = true;
		
		boolean bColorSame = true;
		boolean bPointSizeSame = true;
		boolean bSizeScaleSame = true;
		boolean bShapeSame = true;
		boolean bShadedSame = true;
		boolean bRenderSame = true;
		boolean bAllMultiColor = true;
		float fPointSizeIn;
		float fSizeScaleIn;
		float fPointSize = -1.0f;
		float fSizeScale = -1.0f;

		Color currColor = Color.WHITE;
		int nRender = 0;
		int nShape = 0;
		int nShaded = 0;
		
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
					bAllMultiColor = spotsShape.isMultiColor();
					nShaded = spotsShape.getPointShade();
					bFirstMesh = false;
				}
				else
				{
					fPointSizeIn = spotsShape.getPointSize();
					fSizeScaleIn = spotsShape.getSizeScale();
					//we encoutered fixed size spots, let's update
					if(fPointSizeIn >= 0.0)
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
					bShadedSame &= (nShaded == spotsShape.getPointShade());
					bColorSame &= currColor.equals( spotsShape.getColor() );
					bAllMultiColor = (bAllMultiColor && spotsShape.isMultiColor());
				}
			}
		}
		
		final Color cColorFin = currColor;
		final float fPointSizeFin = fPointSize;	
		final float fSizeScaleFin = fSizeScale;	
		final int nShapeFin = nShape;
		final int nShadedFin = nShaded;
		final int nRenderFin = nRender;			
		
		final boolean bColorSameFin = bColorSame;
		final boolean bPointSizeSameFin = bPointSizeSame;
		final boolean bSizeScaleSameFin = bSizeScaleSame;
		final boolean bShapeSameFin = bShapeSame;
		final boolean bShadedSameFin = bShadedSame;
		final boolean bRenderSameFin = bRenderSame;
		final boolean bAllMultiColorFin = bAllMultiColor;

		SwingUtilities.invokeLater( () -> {
			synchronized ( SpotsPropertiesPanel.this )
			{
				blockUpdates = true;
				
				DecimalFormatSymbols symbols = new DecimalFormatSymbols();
				symbols.setDecimalSeparator('.');
				DecimalFormat df3 = new DecimalFormat ("#.######", symbols);
					
				pColor.setConsistent( bColorSameFin );
				pPointSize.setConsistent( bPointSizeSameFin );
				pSizeScale.setConsistent( bSizeScaleSameFin );
				pShape.setConsistent( bShapeSameFin );
				pRender.setConsistent( bRenderSameFin );
				butColor.setEnabled( true );
				pShaded.setConsistent( bShadedSameFin );
				
				if(bColorSameFin)
				{
					selectColors.setColor( cColorFin, 0 );
					butColor.setIcon(  new ColorIcon( cColorFin ) );
				}
				
				if(bAllMultiColorFin)
				{
					butColor.setIcon(  new ColorIcon( Color.GRAY ) );
					butColor.setEnabled( false );
				}

				if(fSizeScaleFin > 0)
				{
					nfSpSizeScale.setEnabled( true );
					nfSpSizeScale.setText( df3.format( fSizeScaleFin ) );
					//all panel is disabled
					if(!cbShape.isEnabled())
					{
						nfSpSizeScale.setEnabled( false );
					}
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
					nfSpSize.setText( df3.format( fPointSizeFin ));
					//all panel is disabled
					if(!cbShape.isEnabled())
					{
						nfSpSize.setEnabled( false );
					}

				}				

				if(bRenderSameFin)
				{
					cbRender.setSelectedIndex( nRenderFin );
				}

				if(bShapeSameFin)
				{
					cbShape.setSelectedIndex( nShapeFin );
				}
				
				if(bShadedSameFin)
				{
					cbShaded.setSelected( nShadedFin == 1 ? true :false );
				}

				colorCodePanel.updateGUI();
				opacityPanel.updateGUI();
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
	
	synchronized void updateRoundShaded()
	{
		if(!blockUpdates)
		{
			final int nShaded = cbShaded.isSelected()? 1 : 0;
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					((BasicSpots)sh).setPointShade( nShaded ); 
				}
			}
			bvb.repaintBVV();
			updateGUI();
		}
		
	}
	

}
