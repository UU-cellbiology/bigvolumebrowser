package bvb.gui.shapes;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.imglib2.type.numeric.ARGBType;

import bdv.util.BoundedValueDouble;
import bvb.core.BigVolumeBrowser;
import bvb.gui.GBCHelper;
import bvb.gui.JPanelConsistent;
import bvb.shapes.BasicShape;
import bvb.shapes.BasicSpots;
import bvvpg.ui.panels.BoundedRangePanelPG;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class SpotsColorCodePanel extends JPanel
{
	final BigVolumeBrowser bvb;
	
	final JPanelConsistent pMapLUT;
	
	final LUTSelectionPanel panelLUT;
	
	final JComboBox<String> cbMapLUT;
		
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
		String[] sMapLUT = {"Single color", "X coord LUT", "Y coord LUT", "Z coord LUT", "Size LUT", "Param LUT"};		
 
		cbMapLUT = new JComboBox< >(sMapLUT);
		cbMapLUT.addActionListener( (e) -> updateLUTMapping());
		pMapLUT = new JPanelConsistent(new GridBagLayout());
		
		lutRangePanel = new BoundedRangePanelPG();
		
		lutGammaPanel = new BoundedValuePanelPG(new BoundedValueDouble(0.01,5.0,1.0) );
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		pMapLUT.add( new JLabel("Color Mapping: "), gbc );
		gbc.gridx++;
		pMapLUT.add( cbMapLUT, gbc );	
		
		panelLUT = new LUTSelectionPanel();
		panelLUT.setConsistent( true );
		
		panelLUT.changeListeners().add( ()-> updateLUT());
		panelLUT.cbInverted.addItemListener( (e)-> updateLUTInversion());
		
		gbc.gridx = 0;
		gbc.gridy = 0;

		this.add( pMapLUT, gbc );
		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.1;
		this.add( panelLUT, gbc );
		
		gbc.gridy++;
		this.add( lutRangePanel, gbc);
		gbc.gridy++;
		this.add( lutGammaPanel, gbc);

		//filler
		gbc.gridy++;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 0.1;
		this.add( new JLabel(), gbc );
		
		allComp.add( cbMapLUT );
		allComp.add( panelLUT.lutButton );
		allComp.add( panelLUT.cbInverted );
		allComp.add( lutRangePanel );
		allComp.add( lutGammaPanel );
	}
	
	
	synchronized void updateGUI()
	{
		boolean bFirstMesh = true;
		
		boolean bColorSame = true;
		boolean bMapLUTSame = true;
		boolean bLUTSame = true;
		boolean bLUTInvertedSame = true;
		
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
					bLUTInverted = spotsShape.isInvertedLUT();
					bFirstMesh = false;
				}
				else
				{
					bMapLUTSame &= (nMapLUT == spotsShape.getMapLUTMode());
					bColorSame &= currColor.equals( spotsShape.getColor() );		
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

		SwingUtilities.invokeLater( () -> {
			synchronized ( SpotsColorCodePanel.this )
			{
				blockUpdates = true;

				pMapLUT.setConsistent( bMapLUTSameFin );
				panelLUT.setConsistent( bLUTSameFin );
				panelLUT.setEnabled( true );
				
				lutRangePanel.setEnabled( true );
				lutGammaPanel.setEnabled( true );
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
					}
				}
				if(bLUTSameFin)
				{
					panelLUT.setICMbyName( sLUTFin );
				}						
				
				blockUpdates = false;
			}
		} );
		
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
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(final Component nC:allComp)
		{
			nC.setEnabled( bEnabled );
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
	
	synchronized void updateLUTInversion()
	{
		if(!blockUpdates)
		{
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
	}
}
