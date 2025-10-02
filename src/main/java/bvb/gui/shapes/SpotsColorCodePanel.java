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

import bvb.core.BigVolumeBrowser;
import bvb.gui.GBCHelper;
import bvb.gui.JPanelConsistent;
import bvb.shapes.BasicShape;
import bvb.shapes.BasicSpots;

public class SpotsColorCodePanel extends JPanel
{
	final BigVolumeBrowser bvb;
	
	final JPanelConsistent pMapLUT;
	final LUTSelectionPanel panelLUT;
	
	final JComboBox<String> cbMapLUT;
		
	final ArrayList<Component> allComp = new ArrayList<>();
	
	private boolean blockUpdates = false;
	
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
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		pMapLUT.add( new JLabel("Color Mapping: "), gbc );
		gbc.gridx++;
		pMapLUT.add( cbMapLUT, gbc );	
		
		panelLUT = new LUTSelectionPanel();
		panelLUT.setConsistent( true );
		
		panelLUT.changeListeners().add( ()-> updateLUT());
		
		gbc.gridx = 0;
		gbc.gridy = 0;

		this.add( pMapLUT, gbc );
		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.1;
		this.add( panelLUT, gbc );
		
		allComp.add( cbMapLUT );
		allComp.add( panelLUT.lutButton );
	}
	
	
	synchronized void updateGUI()
	{
		boolean bFirstMesh = true;
		
		boolean bColorSame = true;
		boolean bMapLUTSame = true;
		boolean bLUTSame = true;
		
		Color currColor = Color.WHITE;
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
				}
			}
		}
			
		final int nMapLUTFin = nMapLUT;
		final String sLUTFin = sLUT;
		final Color cColorFin = currColor;
		final boolean bColorSameFin = bColorSame;
		final boolean bMapLUTSameFin = bMapLUTSame;
		final boolean bLUTSameFin = bLUTSame;

		SwingUtilities.invokeLater( () -> {
			synchronized ( SpotsColorCodePanel.this )
			{
				blockUpdates = true;

				pMapLUT.setConsistent( bMapLUTSameFin );
				panelLUT.setConsistent( bLUTSameFin );
				panelLUT.setEnabled( true );
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
}
