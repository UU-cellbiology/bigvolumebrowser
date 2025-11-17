package bvb.gui.shapes;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
import bvb.core.BigVolumeBrowser;
import bvb.gui.GBCHelper;
import bvb.gui.JPanelConsistent;
import bvb.gui.NumberField;
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
	
	final JPanelConsistent pExtraAlpha;
	
	final JPanelConsistent pMapInverted;
	
	public final JCheckBox cbInverted = new JCheckBox("Inv");
	
	final JComboBox<String> cbMapAlpha;
	
	final SpotsMapSetups spotsAlphaSetup = new SpotsMapSetups();
	
	final JButton butResetToDefault;
		
	final ArrayList<Component> allComp = new ArrayList<>();
	
	private boolean blockUpdates = false;
	
	final NumberField nfExtraAlpha;
	
	private final BoundedRangePanelPG alphaRangePanel;
	private final BoundedValuePanelPG alphaGammaPanel;	
	
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
		alphaRangePanel.setToolTipText( "Opacity mapping range" );	
		
		alphaGammaPanel = new BoundedValuePanelPG(new BoundedValueDouble(0.01,5.0,1.0) );
		alphaGammaPanel.changeListeners().add(  () -> updateAlphaMapGamma() );
		
		final JPopupMenu menuGammaRange = new JPopupMenu();
		menuGammaRange.add( runnableItem(  "set bounds ...", alphaGammaPanel::setBoundsDialog ) );
		alphaGammaPanel.setPopup( () -> menuGammaRange );
		alphaGammaPanel.setToolTipText( "Opacity mapping gamma" );
		
		nfExtraAlpha = new NumberField(6);		
		nfExtraAlpha.addListener( (v)->
		{
			updateExtraAlpha(v);
		} );
		
		pExtraAlpha = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pExtraAlpha.add( new JLabel(" Extra α coefficient: "), gbc );
		gbc.gridx++;
		pExtraAlpha.add( nfExtraAlpha, gbc );
		
		
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
		this.add(pExtraAlpha, gbc);
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
		allComp.add( nfExtraAlpha );
		allComp.add( butResetToDefault );
	}

	synchronized void updateGUI()
	{
		
		boolean bFirstSpots = true;	
		boolean bMapAlphaSame = true;
		boolean bAlphaInvertedSame = true;	
		BoundedRange range = null;
		BoundedValueDoubleBVB gamma = null;
		float extraAlpha = 1.0f;
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
				
				if(bFirstSpots)
				{
					nMapAlpha = spotsShape.getMapAlphaMode();
				
					if(nMapAlpha != 0)
					{
						range = spotsAlphaSetup.getMapRange( spotsShape, nMapAlpha - 1 );
						gamma = spotsAlphaSetup.getMapGamma( spotsShape, nMapAlpha - 1 );
					}
					bAlphaInverted = spotsShape.isInvertedAlpha();							
					extraAlpha = spotsShape.getExtraAlphaCoefficient();
					bFirstSpots = false;
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
					extraAlphaEqual &= Misc.compareRelativeDouble(extraAlpha, spotsShape.getExtraAlphaCoefficient());					
				}
			}
		}
			
		final int nMapAlphaFin = nMapAlpha;
		final boolean bMapAlphaSameFin = bMapAlphaSame;
		final boolean bAlphaInvertedSameFin = bAlphaInvertedSame;
		
		final BoundedRange finalRange = range;
		final BoundedValueDoubleBVB finalGamma = gamma;
		final float finalExtraAlpha = extraAlpha;
		final boolean allRangesEqualFin = allRangesEqual;
		final boolean gammaEqualFin = gammaEqual;
		final boolean extraAlphaEqualFin = extraAlphaEqual;

		SwingUtilities.invokeLater( () -> {
			synchronized ( SpotsOpacityPanel.this )
			{
				
				DecimalFormatSymbols symbols = new DecimalFormatSymbols();
				symbols.setDecimalSeparator('.');
				DecimalFormat df3 = new DecimalFormat ("#.######", symbols);
				blockUpdates = true;

				pExtraAlpha.setConsistent( extraAlphaEqualFin );
				nfExtraAlpha.setText(  df3.format(finalExtraAlpha) );
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
	
	public void updateExtraAlpha(double extraAlpha)
	{
		if(!blockUpdates)
		{
			blockUpdates = true;
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
	
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof BasicSpots)
				{
					((BasicSpots)sh).setExtraAlphaCoefficient( ( float ) extraAlpha );
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
