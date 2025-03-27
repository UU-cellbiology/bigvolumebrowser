package bvb.gui;


import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.UIUtils;
import bdv.util.BoundedRange;
import bdv.util.Bounds;
import bdv.viewer.ConverterSetups;
import bvb.utils.Bounds3D;
import bvb.utils.ClipAxesBounds;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.source.converters.GammaConverterSetup;
import bvvpg.ui.panels.BoundedRangeEditorPG;
import bvvpg.ui.panels.BoundedRangePanelPG;
import bvvpg.vistools.Bvv;

public class ClipPanel extends JPanel implements ItemListener
{

	private static final long serialVersionUID = 1885320351623882576L;
	private BoundedRangePanelPG [] clipAxesPanels = new BoundedRangePanelPG[3];

	public JCheckBox cbClipEnabled;
	public JLabel selectionWindow;
	private final ClipAxesBounds clipAxesBounds;
	private int nActiveWindow = -1;
	private List< ConverterSetup > csList = new ArrayList<>();

	private boolean blockUpdates = false;
	
	/**
	 * Panel background if color reflects a set of sources all having the same color
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if color reflects a set of sources with different colors
	 */
	private Color inConsistentBg = Color.WHITE;
	

	public ClipPanel(final ConverterSetups converterSetups, SelectedSources sourceSelection) 
	{
		super();
		
		clipAxesBounds = new ClipAxesBounds(converterSetups);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints cd = new GridBagConstraints();

		setLayout(gridbag);

		cd.gridy = 0;
		cd.gridwidth = 1;
		cd.weightx = 0.1;
		cd.fill = GridBagConstraints.NONE;
		cd.anchor = GridBagConstraints.WEST;
		cbClipEnabled = new JCheckBox("Clipping", false);
		cbClipEnabled.addItemListener( this );
		cd.gridx = 0;
		this.add(cbClipEnabled,cd);
		selectionWindow = new JLabel("Selected: None");
		cd.gridx++;
		this.add(selectionWindow,cd);
		
		cd.gridwidth = 3;
		cd.gridy = 1;
		cd.gridx = 0;
		cd.fill = GridBagConstraints.BOTH;
		cd.weightx = 1.0;
		for(int d=0;d<3;d++)
		{
			cd.gridy++;
			clipAxesPanels[d] = new BoundedRangePanelPG();
			this.add(clipAxesPanels[d],cd);
		}
		clipAxesPanels[0].changeListeners().add(new BoundedRangePanelPG.ChangeListener()
		{
			
			@Override
			public void boundedRangeChanged()
			{
				updateAxisClipRange(0);				
			}
		});
		clipAxesPanels[1].changeListeners().add(new BoundedRangePanelPG.ChangeListener()
		{
			
			@Override
			public void boundedRangeChanged()
			{
				updateAxisClipRange(1);				
			}
		});
		clipAxesPanels[2].changeListeners().add(new BoundedRangePanelPG.ChangeListener()
		{
			
			@Override
			public void boundedRangeChanged()
			{
				updateAxisClipRange(2);				
			}
		});
		//add source selection listener
		sourceSelection.addSourceSelectionListener(  new SelectedSources.Listener()
		{
			
			@Override
			public void selectedSourcesChanged(int nWindow, List< ConverterSetup > converterSetupList )
			{
				updateCS(nWindow, converterSetupList );
			}
		} );

		updateGUI();
	}
	




	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}

	private void updateClipEnabled()
	{
		boolean bEnabled = cbClipEnabled.isSelected();
		for ( final ConverterSetup cs: csList)
		{
			  ((GammaConverterSetup)cs).setClipActive( bEnabled );
		}
	}
	@Override
	public void itemStateChanged( ItemEvent arg0 )
	{
		if(arg0.getSource() == cbClipEnabled)
			updateClipEnabled();
		updateGUI();
	}
	private synchronized void updateCS(int nSource, List< ConverterSetup > converterSetupList)
	{
		nActiveWindow = nSource;
		csList = converterSetupList;
		updateGUI();
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		setEnabledSliders(bEnabled);
		cbClipEnabled.setEnabled( bEnabled );
		
	}
	
	public void setEnabledSliders(boolean bEnabled)
	{
		for(int i=0;i<3;i++)
		{
			clipAxesPanels[i].setEnabled( bEnabled );
		}
	}
	
	private synchronized void updateGUI()
	{
		updateColors();
		switch (nActiveWindow)
		{
		case 0:
			selectionWindow.setText( "Selected: Sources");
			break;
		case 1:
			selectionWindow.setText( "Selected: Groups");
			break;
		default:
			selectionWindow.setText( "Selected: None");
		}	
		if(csList.size()==0)
		{
			setEnabled(false);
			return;
		}
		setEnabled(true);	
		//consistent clipping
		boolean bClipConsistent = true;
		int bClipEnabled = -1;
		for ( final ConverterSetup cs: csList)
		{
			 
			if(bClipEnabled<0)
			{
				bClipEnabled = ((GammaConverterSetup)cs).clipActive()?1:0;	
			}
			else
			{
				bClipConsistent &= (bClipEnabled==(((GammaConverterSetup)cs).clipActive()?1:0));
			}
			
		}
		if(bClipConsistent)
		{
			cbClipEnabled.setBackground( consistentBg );
			cbClipEnabled.setSelected( bClipEnabled !=0 );
			setEnabledSliders(bClipEnabled !=0);
		}
		else
		{
			setEnabledSliders(cbClipEnabled.isSelected());
			cbClipEnabled.setBackground(inConsistentBg );
		}
		
		BoundedRange [] range = new BoundedRange[3];
		boolean bFirstCS = true;
		boolean [] allRangesEqual = new boolean [3];
		for (int d=0;d<3;d++)
		{
			allRangesEqual[d] = true;
		}

		//update bounds
		final double [] min = new double [3];
		final double [] max = new double [3];

		for ( final ConverterSetup cs: csList)
		{
			final Bounds3D bounds = clipAxesBounds.getBounds( cs );
			final double [] minBound = bounds.getMinBound();
			final double [] maxBound = bounds.getMaxBound();
			final FinalRealInterval clipInterval = ((GammaConverterSetup)cs).getClipInterval();
			if(clipInterval == null)
			{
				for(int d=0;d<3;d++)
				{
					min[d] = minBound[d];
					max[d] = maxBound[d];
				}
			}
			else
			{
				clipInterval.realMin( min );
				clipInterval.realMax( max );
			}
			if(bFirstCS)
			{
				for (int d=0; d<3; d++)
				{
					range[d] = new BoundedRange( minBound[d], maxBound[d], min[d], max[d] );
				}
				bFirstCS = false;
			}
			else
			{
				for (int d=0; d<3; d++)
				{
					final BoundedRange axisRange = new BoundedRange( minBound[d], maxBound[d], min[d], max[d] );
					allRangesEqual[d] &= range[d].equals( axisRange );
					range[d] = range[d].join( axisRange );
				}
			}
		}
		final BoundedRange [] finalRange = range;
		final boolean [] isConsistent = allRangesEqual;
		SwingUtilities.invokeLater( () -> {
			synchronized ( ClipPanel.this )
			{
				blockUpdates = true;
				for (int d=0;d<3;d++)
				{

					clipAxesPanels[d].setConsistent( isConsistent[d] );
					clipAxesPanels[d].setRange( finalRange[d] );
					//rangeAlphaPanel.setEnabled( true );
					//rangeAlphaPanel.setRange( finalRange );
					//rangeAlphaPanel.setConsistent( isConsistent );
				}
				blockUpdates = false;
			}
		} );
	}
	
	public void updateAxisClipRange( int nAxis)
	{
		if ( blockUpdates || csList== null || csList.isEmpty() )
			return;
		//System.out.println(nAxis);
		final BoundedRange range = clipAxesPanels[nAxis].getRange();

		for ( final ConverterSetup cs : csList )
		{
			FinalRealInterval clipInt = ((GammaConverterSetup)cs).getClipInterval();

			if(clipInt == null)
			{
				final Bounds3D bounds = clipAxesBounds.getBounds( cs );
				clipInt  = new FinalRealInterval(bounds.getMinBound(),bounds.getMaxBound());
			}
			final double [] min = clipInt.minAsDoubleArray();
			final double [] max = clipInt.maxAsDoubleArray();
			min[nAxis] = range.getMin();
			max[nAxis] = range.getMax();
			((GammaConverterSetup)cs).setClipInterval( new FinalRealInterval(min,max) );
		}
		updateGUI();
	}
}
