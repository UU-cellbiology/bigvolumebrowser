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

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.imglib2.type.numeric.ARGBType;

import bdv.util.BoundedRange;
import bdv.util.BoundedValueDouble;
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.GBCHelper;
import bvb.gui.JPanelConsistent;
import bvb.shapes.BasicShape;
import bvb.shapes.BasicSpots;
import bvb.utils.BoundedValueDoubleBVB;
import bvb.utils.Misc;
import bvvpg.ui.panels.BoundedRangePanelPG;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class SpotsColorCodePanel extends JPanel
{
	final BigVolumeBrowser bvb;
	
	final JPanelConsistent pMapLUT;
	
	final LUTSelectionPanel panelLUT;
	
	final JComboBox<String> cbMapLUT;
	
	final public SpotsMapSetups spotsLUTSetup = new SpotsMapSetups();
	
	final JButton butResetToDefault;
		
	final ArrayList<Component> allComp = new ArrayList<>();
	
	private boolean blockUpdates = false;
	
	private final BoundedRangePanelPG lutRangePanel;
	
	private final BoundedValuePanelPG lutGammaPanel;
	
	public SpotsColorCodePanel(final BigVolumeBrowser bvb_)
	{
		super();	
		
		bvb = bvb_;
		
		setLayout(new GridBagLayout());
	
		GridBagConstraints gbc = new GridBagConstraints();	
		GBCHelper.alighLeft(gbc);
		String[] sMapLUT = {"None", "X coord", "Y coord", "Z coord", "Size", "Property"};		
 
		cbMapLUT = new JComboBox< >(sMapLUT);
		cbMapLUT.addActionListener( (e) -> updateLUTMapping());
		pMapLUT = new JPanelConsistent(new GridBagLayout());
		
		lutRangePanel = new BoundedRangePanelPG();
		lutRangePanel.changeListeners().add( () -> updateLUTMapRangeBounds());
		final JPopupMenu menuLutRange = new JPopupMenu();
		menuLutRange.add( runnableItem(  "set bounds ...", lutRangePanel::setBoundsDialog ) );
		menuLutRange.add( runnableItem(  "shrink bounds to selection", lutRangePanel::shrinkBoundsToRange ) );
		lutRangePanel.setPopup( () -> menuLutRange );	
		lutRangePanel.setToolTipText( "LUT mapping range" );
		
		lutGammaPanel = new BoundedValuePanelPG(new BoundedValueDouble(0.01,5.0,1.0) );
		lutGammaPanel.changeListeners().add(  () -> updateLUTMapGamma() );
		lutGammaPanel.setToolTipText( "LUT mapping gamma" );
		
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;

		pMapLUT.add( new JLabel("Color Mapping: "), gbc );
		gbc.gridx++;
		pMapLUT.add( cbMapLUT, gbc );	
		
		panelLUT = new LUTSelectionPanel();
		panelLUT.setConsistent( true );
		
		panelLUT.changeListeners().add( ()-> updateLUT());
		panelLUT.cbInverted.addItemListener( (e)-> updateLUTInversion());
		
		
		URL icon_path = this.getClass().getResource(BVBSettings.sIconPath + "red_cross.png");
		ImageIcon icon = new ImageIcon(icon_path);
		butResetToDefault = new JButton(icon);
		butResetToDefault.setToolTipText( "Reset to default" );
		butResetToDefault.addActionListener( (e)->resetLUTMapRangeBounds() );   
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		this.add( pMapLUT, gbc );
		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.1;
		this.add( panelLUT, gbc );
		
		gbc.gridy++;
		this.add( lutRangePanel, gbc);
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.weightx = 0.2;
		this.add( lutGammaPanel, gbc);
		
		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		//gbc.anchor = GridBagConstraints.EAST;
		this.add( butResetToDefault, gbc);
		
		//filler
		gbc.gridwidth = 2;
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 0.1;
		this.add( new JLabel(), gbc );
		
		allComp.add( cbMapLUT );
		allComp.add( panelLUT.lutButton );
		allComp.add( panelLUT.cbInverted );
		allComp.add( lutRangePanel );
		allComp.add( lutGammaPanel );
		allComp.add( butResetToDefault );
	}

	void updateGUI()
	{
	    if (!SwingUtilities.isEventDispatchThread())
	    {
	        SwingUtilities.invokeLater(this::updateGUI);
	        return;
	    }
	    
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;
		boolean bFirstMesh = true;
		
		boolean bColorSame = true;
		boolean bMapLUTSame = true;
		boolean bLUTSame = true;
		boolean bLUTInvertedSame = true;	
		BoundedRange range = null;
		BoundedValueDoubleBVB gamma = null;
		boolean allRangesEqual = true;
		boolean gammaEqual = true;
		
		Color currColor = Color.WHITE;
		boolean bLUTInverted = false;
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
					nMapLUT = spotsShape.getMapLUTMode();
					sLUT = spotsShape.getLUTName();
					if(sLUT == "" || spotsShape.getMapLUTMode() == 0)
					{
						bLUTSame = false;
					}
					else
					{
						range = spotsLUTSetup.getMapRange( spotsShape, nMapLUT - 1 );
						gamma = spotsLUTSetup.getMapGamma( spotsShape, nMapLUT - 1 );
					}
					bLUTInverted = spotsShape.isInvertedLUT();


					bFirstMesh = false;
				}
				else
				{
					int nCurrMapLUTMode = spotsShape.getMapLUTMode();
					bMapLUTSame &= (nMapLUT == nCurrMapLUTMode);
					bColorSame &= currColor.equals( spotsShape.getColor() );
					if(spotsShape.isMultiColor())
					{
						bColorSame = false;
					}
					if(bLUTSame)
					{
						if(spotsShape.getLUTName() == "" || nCurrMapLUTMode == 0)
						{
							bLUTSame = false;
						}
						else
						{
							bLUTSame &= sLUT.equals( spotsShape.getLUTName());
						}
					}
					if(nCurrMapLUTMode != 0)
					{
						if(range == null )
						{
							range = spotsLUTSetup.getMapRange( spotsShape, nMapLUT - 1 );							
						}

						allRangesEqual &= Misc.compareBoundedRanges(range, spotsLUTSetup.getMapRange( spotsShape, nMapLUT - 1) );
						if(gamma == null )
						{
							gamma = spotsLUTSetup.getMapGamma( spotsShape, nMapLUT -1 );							
						}
						gammaEqual &= Misc.compareBoundedValues( gamma, spotsLUTSetup.getMapGamma( spotsShape, nMapLUT - 1 ) );

					}
					bLUTInvertedSame &= (bLUTInverted == spotsShape.isInvertedLUT()); 
				}
			}
		}

		final int nMapLUTFin = nMapLUT;
		final String sLUTFin = sLUT;
		final Color cColorFin = currColor;
		final boolean bColorSameFin = bColorSame;
		final boolean bMapLUTSameFin = bMapLUTSame;
		final boolean bLUTSameFin = (bLUTSame && bLUTInvertedSame);
		final boolean bLUTInvertedFin = bLUTInverted;

		final BoundedRange finalRange = range;
		final BoundedValueDoubleBVB finalGamma = gamma;
		final boolean allRangesEqualFin = allRangesEqual;
		final boolean gammaEqualFin = gammaEqual;

		blockUpdates = true;
		try
		{

			pMapLUT.setConsistent( bMapLUTSameFin );
			panelLUT.setConsistent( bLUTSameFin );
			panelLUT.setEnabled( true );
			panelLUT.cbInverted.setSelected( bLUTInvertedFin );

			lutRangePanel.setEnabled( true );
			lutRangePanel.setConsistent( allRangesEqualFin );

			lutGammaPanel.setEnabled( true );
			lutGammaPanel.setConsistent( gammaEqualFin );

			if(finalRange != null)
			{
				lutRangePanel.setRange( finalRange );
			}
			if(finalGamma != null)
			{
				lutGammaPanel.setValue( finalGamma );
			}

			if(bMapLUTSameFin)
			{
				cbMapLUT.setSelectedIndex(nMapLUTFin);
				if(nMapLUTFin == 0)
				{
					if(bColorSameFin)
					{
						panelLUT.setColor(new ARGBType(cColorFin.getRGB()));
					}
					else
					{
						panelLUT.setColor(null);
					}
					panelLUT.setEnabled( false );
					panelLUT.setConsistent( true );
					lutRangePanel.setEnabled( false );
					lutGammaPanel.setEnabled( false );
					lutRangePanel.setConsistent( true );
					lutGammaPanel.setConsistent( true );
					lutRangePanel.setRange( new BoundedRange(0,1,0,1) );

				}
			}
			if(bLUTSameFin)
			{
				panelLUT.setICMbyName( sLUTFin );
			}						
		}
		finally
		{
			blockUpdates = false;
		}

	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
	    if (!SwingUtilities.isEventDispatchThread())
	    {
	        SwingUtilities.invokeLater(() -> setEnabled(bEnabled));
	        return;
	    }

	    if(blockUpdates)
	    	return;
	    blockUpdates = true;
	    try
	    {
	    	for(final Component nC:allComp)
	    	{
	    		nC.setEnabled( bEnabled );
	    	}
	    }
	    finally
	    {
	    	blockUpdates = false;
	    }
	}
	
	void updateLUTMapping()
	{
	    if (!SwingUtilities.isEventDispatchThread())
	    {
	        SwingUtilities.invokeLater(this::updateLUTMapping);
	        return;
	    }
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;
		
		final int nMapLUTMode = cbMapLUT.getSelectedIndex();
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicSpots)
			{
				((BasicSpots)sh).setMapLUTMode( nMapLUTMode );
				if(nMapLUTMode != 0)
				{
					final float [] range = spotsLUTSetup.getMapRangeFloat( ((BasicSpots)sh), nMapLUTMode - 1 );
					((BasicSpots)sh).setMapLUTRange( range[0], range[1] );
				}
			}
		}
		bvb.repaintBVV();
		updateGUI();

	}

	
	void updateLUT()
	{
	    if (!SwingUtilities.isEventDispatchThread())
	    {
	        SwingUtilities.invokeLater(this::updateLUT);
	        return;
	    }
	    
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;
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
	
	void updateLUTInversion()
	{
		
	    if (!SwingUtilities.isEventDispatchThread())
	    {
	        SwingUtilities.invokeLater(this::updateLUTInversion);
	        return;
	    }
	    
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;

		final boolean bInvLUT = panelLUT.cbInverted.isSelected();
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicSpots)
			{
				((BasicSpots)sh).setInvertedLUT( bInvLUT );
			}
		}
		bvb.repaintBVV();
		updateGUI();		
	}
	
	public void updateLUTMapRangeBounds()
	{
	    if (!SwingUtilities.isEventDispatchThread())
	    {
	        SwingUtilities.invokeLater(this::updateLUTMapRangeBounds);
	        return;
	    }
		
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;		

		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();

		final BoundedRange rangeUI = lutRangePanel.getRange();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicSpots)
			{
				final int nMapMode = ((BasicSpots)sh).getMapLUTMode();
				if(nMapMode != 0)
				{
					((BasicSpots)sh).setMapLUTRange((float)rangeUI.getMin(), (float)rangeUI.getMax());

					float [] rangeStored = spotsLUTSetup.getMapRangeFloat( ((BasicSpots)sh), nMapMode - 1 );
					rangeStored[0] = (float)rangeUI.getMin();
					rangeStored[1] = (float)rangeUI.getMax();
					rangeStored[2] = (float)rangeUI.getMinBound();
					rangeStored[3] = (float)rangeUI.getMaxBound();
				}
			}
		}

		bvb.repaintBVV();
		updateGUI();
		
	}
	
	public synchronized void updateLUTMapGamma()
	{
	    if (!SwingUtilities.isEventDispatchThread())
	    {
	        SwingUtilities.invokeLater(this::updateLUTMapGamma);
	        return;
	    }
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;

		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		BoundedValueDouble gammaCurr = lutGammaPanel.getValue();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicSpots)
			{
				final int nMapMode = ((BasicSpots)sh).getMapLUTMode();
				if(nMapMode != 0)
				{
					((BasicSpots)sh).setMapLUTGamma( (float) gammaCurr.getCurrentValue() );   
					float [] rangeStored = spotsLUTSetup.getMapRangeFloat( ((BasicSpots)sh), nMapMode - 1 );
					rangeStored[4] = (float) gammaCurr.getCurrentValue();
					rangeStored[5] = (float) gammaCurr.getRangeMin();
					rangeStored[6] = (float) gammaCurr.getRangeMax();
				}
			}
		}

		bvb.repaintBVV();
		updateGUI();
	}
	
	
	void resetLUTMapRangeBounds()
	{
	    if (!SwingUtilities.isEventDispatchThread())
	    {
	        SwingUtilities.invokeLater(this::resetLUTMapRangeBounds);
	        return;
	    }
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;

		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicSpots)
			{
				final int nMapMode = ((BasicSpots)sh).getMapLUTMode() - 1;
				if(nMapMode >= 0)
				{
					final float [][] rangeDef = spotsLUTSetup.getDefaultRanges( (BasicSpots)sh );
					final float [] currRange = spotsLUTSetup.getMapRangeFloat( (BasicSpots)sh , nMapMode );
					for(int i = 0; i < 7; i++)
					{
						currRange[i] = rangeDef[nMapMode][i];
					}
					((BasicSpots)sh).setMapLUTGamma( currRange[4] );
					((BasicSpots)sh).setMapLUTRange( currRange[0], currRange[1] );
				}
			}
		}

		bvb.repaintBVV();
		updateGUI();

	}
	
	private JMenuItem runnableItem( final String text, final Runnable action )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> action.run() );
		return item;
	}
}
