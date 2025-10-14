package bvb.gui.shapes;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import bdv.util.BoundedRange;
import bdv.util.BoundedValueDouble;
import bdv.util.Bounds;
import bvb.core.BigVolumeBrowser;
import bvb.gui.GBCHelper;
import bvb.gui.JPanelConsistent;
import bvb.shapes.BasicShape;
import bvb.shapes.BasicSpots;
import bvb.utils.BoundedValueDoubleBVB;
import bvb.utils.Misc;
import bvvpg.ui.panels.BoundedRangePanelPG;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class SpotsOpacityPanel extends JPanel
{
	final BigVolumeBrowser bvb;
	
	final JPanelConsistent pMapAlpha;
	
	final JPanelConsistent pMapInverted;
	
	public final JCheckBox cbInverted = new JCheckBox("Inv");
	
	final JComboBox<String> cbMapAlpha;
	
	final SpotsMapSetups spotsAlphaSetup = new SpotsMapSetups();
	
	final SpotsExtraAlphaBounds extraAlphaBounds = new SpotsExtraAlphaBounds();
	
	final JButton butResetToDefault;
		
	final ArrayList<Component> allComp = new ArrayList<>();
	
	private boolean blockUpdates = false;
	
	private final BoundedRangePanelPG alphaRangePanel;
	private final BoundedValuePanelPG alphaGammaPanel;	
	private final BoundedValuePanelPG extraAlphaPanel;
	
	public SpotsOpacityPanel(final BigVolumeBrowser bvb_)
	{
		super();	
		
		bvb = bvb_;
		
		setLayout(new GridBagLayout());
	
		GridBagConstraints gbc = new GridBagConstraints();	
		GBCHelper.alighLeft(gbc);
		String[] sMapAlpha = {"None", "X coord", "Y coord", "Z coord", "Size", "Property"};		
 
		cbMapAlpha = new JComboBox< >(sMapAlpha);
		cbMapAlpha.addActionListener( (e) -> updateAlphaMapping());
		pMapAlpha = new JPanelConsistent(new GridBagLayout());
		
		alphaRangePanel = new BoundedRangePanelPG();
		alphaRangePanel.changeListeners().add( () -> updateAlphaMapRangeBounds());
		final JPopupMenu menuLutRange = new JPopupMenu();
		menuLutRange.add( runnableItem(  "set bounds ...", alphaRangePanel::setBoundsDialog ) );
		menuLutRange.add( runnableItem(  "shrink bounds to selection", alphaRangePanel::shrinkBoundsToRange ) );
		alphaRangePanel.setPopup( () -> menuLutRange );	
		
		alphaGammaPanel = new BoundedValuePanelPG(new BoundedValueDouble(0.01,5.0,1.0) );
		alphaGammaPanel.changeListeners().add(  () -> updateAlphaMapGamma() );
		
		extraAlphaPanel = new BoundedValuePanelPG(new BoundedValueDouble(0.0,1.0,1.0) );
		extraAlphaPanel.changeListeners().add( () -> updateExtraAlpha() );
		extraAlphaPanel.setToolTipText( "Extra opacity\n coefficient" );
		final JPopupMenu menuExtraAlpha = new JPopupMenu();
		menuExtraAlpha.add( runnableItem(  "set bounds ...", extraAlphaPanel::setBoundsDialog ) );
		extraAlphaPanel.setPopup( () -> menuExtraAlpha );	
		
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		pMapAlpha.add( new JLabel("α mapping: "), gbc );
		gbc.gridx++;
		pMapAlpha.add( cbMapAlpha, gbc );	
			
		pMapInverted = new JPanelConsistent(new GridBagLayout());
		pMapInverted.setConsistent( true );
		gbc.gridx = 0;
		gbc.gridy = 0;
		pMapInverted.add( cbInverted, gbc );
		cbInverted.addItemListener( (e)-> updateAlphaInversion());
		
				
		URL icon_path = this.getClass().getResource("/icons/red_cross.png");
		ImageIcon icon = new ImageIcon(icon_path);
		butResetToDefault = new JButton(icon);
		butResetToDefault.setToolTipText( "Reset to default" );
		butResetToDefault.addActionListener( (e)->resetAlphaMapRangeBounds() );   
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		this.add(extraAlphaPanel, gbc);
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0,3,0,0);
		this.add( pMapAlpha, gbc );
		gbc.gridx++;
		gbc.insets = new Insets(0,0,0,0);
		this.add( pMapInverted, gbc );		
			
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		
		gbc.gridy++;
		this.add( alphaRangePanel, gbc);
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.weightx = 0.2;
		this.add( alphaGammaPanel, gbc);
		
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
		
		allComp.add( cbMapAlpha );
		allComp.add( cbInverted );
		allComp.add( alphaRangePanel );
		allComp.add( alphaGammaPanel );
		allComp.add( butResetToDefault );
	}

	synchronized void updateGUI()
	{
		boolean bFirstMesh = true;
	
		boolean bMapAlphaSame = true;
		boolean bAlphaInvertedSame = true;	
		BoundedRange range = null;
		BoundedValueDoubleBVB gamma = null;
		BoundedValueDoubleBVB extraAlpha = null;
		boolean allRangesEqual = true;
		boolean gammaEqual = true;		
		boolean extraAlphaEqual = true;
		
		boolean bAlphaInverted = false;
		int nMapAlpha = 0;
		
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicSpots)
			{
				final BasicSpots spotsShape = (BasicSpots)sh;
				
				final Bounds boundExtraAlpha = extraAlphaBounds.getBounds( spotsShape );
				double currExtraAlpha = spotsShape.getExtraAlphaCoefficient();
				
				if(bFirstMesh)
				{
					nMapAlpha = spotsShape.getMapAlphaMode();
				
					if(nMapAlpha != 0)
					{
						range = spotsAlphaSetup.getMapRange( spotsShape, nMapAlpha - 1 );
						gamma = spotsAlphaSetup.getMapGamma( spotsShape, nMapAlpha - 1 );
					}
					bAlphaInverted = spotsShape.isInvertedAlpha();							
					bFirstMesh = false;
					extraAlpha = new BoundedValueDoubleBVB( boundExtraAlpha.getMinBound(), boundExtraAlpha.getMaxBound(), currExtraAlpha);
				}
				else
				{
					int nCurrMapAlphaMode = spotsShape.getMapAlphaMode();
					bMapAlphaSame &= (nMapAlpha == nCurrMapAlphaMode);

					if(nCurrMapAlphaMode != 0)
					{
						if(range == null )
						{
							range = spotsAlphaSetup.getMapRange( spotsShape, nMapAlpha - 1 );							
						}
						
						allRangesEqual &= Misc.compareBoundedRanges(range, spotsAlphaSetup.getMapRange( spotsShape, nMapAlpha - 1) );
						if(gamma == null )
						{
							gamma = spotsAlphaSetup.getMapGamma( spotsShape, nMapAlpha -1 );							
						}
						gammaEqual &= Misc.compareBoundedValues( gamma, spotsAlphaSetup.getMapGamma( spotsShape, nMapAlpha - 1 ) );
						
					}
					bAlphaInvertedSame &= (bAlphaInverted == spotsShape.isInvertedAlpha()); 
					extraAlphaEqual &= Misc.compareBoundedValues(extraAlpha, new BoundedValueDoubleBVB( boundExtraAlpha.getMinBound(), boundExtraAlpha.getMaxBound(), currExtraAlpha));					
				}
			}
		}
			
		final int nMapAlphaFin = nMapAlpha;
		final boolean bMapAlphaSameFin = bMapAlphaSame;
		final boolean bAlphaInvertedSameFin = bAlphaInvertedSame;
		
		final BoundedRange finalRange = range;
		final BoundedValueDoubleBVB finalGamma = gamma;
		final BoundedValueDoubleBVB finalExtraAlpha = extraAlpha;
		final boolean allRangesEqualFin = allRangesEqual;
		final boolean gammaEqualFin = gammaEqual;
		final boolean extraAlphaEqualFin = extraAlphaEqual;

		SwingUtilities.invokeLater( () -> {
			synchronized ( SpotsOpacityPanel.this )
			{
				blockUpdates = true;

				extraAlphaPanel.setConsistent( extraAlphaEqualFin );
				extraAlphaPanel.setValue( finalExtraAlpha );
				pMapAlpha.setConsistent( bMapAlphaSameFin );
				pMapInverted.setConsistent( bAlphaInvertedSameFin );
				
				cbInverted.setEnabled( true );
				alphaRangePanel.setEnabled( true );
				alphaRangePanel.setConsistent( allRangesEqualFin );

				alphaGammaPanel.setEnabled( true );
				alphaGammaPanel.setConsistent( gammaEqualFin );
				
				if(finalRange != null)
				{
					alphaRangePanel.setRange( finalRange );
				}
				if(finalGamma != null)
				{
					alphaGammaPanel.setValue( finalGamma );
				}

				if(bMapAlphaSameFin)
				{
					cbMapAlpha.setSelectedIndex(nMapAlphaFin);
					if(nMapAlphaFin == 0)
					{
						cbInverted.setEnabled( false );
						alphaRangePanel.setEnabled( false );
						alphaGammaPanel.setEnabled( false );
						alphaRangePanel.setConsistent( true );
						alphaGammaPanel.setConsistent( true );
						alphaRangePanel.setRange( new BoundedRange(0,1,0,1) );

					}
				}				
				
				blockUpdates = false;
			}
		} );
		
	}
	
	synchronized void updateAlphaMapping()
	{
		if(!blockUpdates)
		{
			final int nMapAlphaMode = cbMapAlpha.getSelectedIndex();
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					((BasicSpots)sh).setMapAlphaMode( nMapAlphaMode );
					if(nMapAlphaMode != 0)
					{
						final float [] range = spotsAlphaSetup.getMapRangeFloat( ((BasicSpots)sh), nMapAlphaMode - 1 );
						((BasicSpots)sh).setMapAlphaRange( range[0], range[1] );
					}
				}
			}
			bvb.repaintBVV();
			updateGUI();
		}
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(final Component nC:allComp)
		{
			nC.setEnabled( bEnabled );
		}
	}
	
	
	synchronized void updateAlphaInversion()
	{
		if(!blockUpdates)
		{
			final boolean bInvAlpha = cbInverted.isSelected();
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					((BasicSpots)sh).setInvertedAlpha( bInvAlpha );
				}
			}
			bvb.repaintBVV();
			updateGUI();
		}
	}
	
	public synchronized void updateAlphaMapRangeBounds()
	{
		if(!blockUpdates)
		{
			blockUpdates = true;
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		
			final BoundedRange rangeUI = alphaRangePanel.getRange();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					final int nMapMode = ((BasicSpots)sh).getMapAlphaMode();
					if(nMapMode != 0)
					{
						((BasicSpots)sh).setMapAlphaRange((float)rangeUI.getMin(), (float)rangeUI.getMax());
					
						float [] rangeStored = spotsAlphaSetup.getMapRangeFloat( ((BasicSpots)sh), nMapMode - 1 );
						rangeStored[0] = (float)rangeUI.getMin();
						rangeStored[1] = (float)rangeUI.getMax();
						rangeStored[2] = (float)rangeUI.getMinBound();
						rangeStored[3] = (float)rangeUI.getMaxBound();
					}
				}
			}
	
			bvb.repaintBVV();
			blockUpdates = false;
			updateGUI();
		}
	}
	
	public synchronized void updateAlphaMapGamma()
	{
		if(!blockUpdates)
		{
			blockUpdates = true;
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			BoundedValueDouble gammaCurr = alphaGammaPanel.getValue();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					final int nMapMode = ((BasicSpots)sh).getMapAlphaMode();
					if(nMapMode != 0)
					{
						((BasicSpots)sh).setMapAlphaGamma( (float) gammaCurr.getCurrentValue() );   
						float [] rangeStored = spotsAlphaSetup.getMapRangeFloat( ((BasicSpots)sh), nMapMode - 1 );
						rangeStored[4] = (float) gammaCurr.getCurrentValue();
						rangeStored[5] = (float) gammaCurr.getRangeMin();
						rangeStored[6] = (float) gammaCurr.getRangeMax();
					}
				}
			}
	
			bvb.repaintBVV();
			blockUpdates = false;
			updateGUI();
		}
	}
	
	public void updateExtraAlpha()
	{
		if(!blockUpdates)
		{
			blockUpdates = true;
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			final float extraAlpha = (float) extraAlphaPanel.getValue().getCurrentValue();
			final double minB = Math.min(extraAlpha, extraAlphaPanel.getValue().getRangeMin());
			final double maxB = Math.max(extraAlpha, extraAlphaPanel.getValue().getRangeMax());

			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					((BasicSpots)sh).setExtraAlphaCoefficient( extraAlpha );
					extraAlphaBounds.setBounds( (BasicSpots)sh, new Bounds(minB, maxB));
				}
			}
	
			bvb.repaintBVV();
			blockUpdates = false;
			updateGUI();
		}
	}
	
	
	void resetAlphaMapRangeBounds()
	{
		if(!blockUpdates)
		{
			blockUpdates = true;
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					final int nMapMode = ((BasicSpots)sh).getMapAlphaMode() - 1;
					if(nMapMode >= 0)
					{
						final float [][] rangeDef = spotsAlphaSetup.getDefaultRanges( (BasicSpots)sh );
						final float [] currRange = spotsAlphaSetup.getMapRangeFloat( (BasicSpots)sh , nMapMode );
						for(int i = 0; i < 7; i++)
						{
							currRange[i] = rangeDef[nMapMode][i];
						}
						((BasicSpots)sh).setMapAlphaGamma( currRange[4] );
						((BasicSpots)sh).setMapAlphaRange( currRange[0], currRange[1] );
					}
				}
			}
	
			bvb.repaintBVV();
			blockUpdates = false;
			updateGUI();
		}
		
	}
	
	private JMenuItem runnableItem( final String text, final Runnable action )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> action.run() );
		return item;
	}
}
