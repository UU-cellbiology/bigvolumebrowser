package bvb.gui.clip;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;


import bdv.tools.brightness.ConverterSetup;
import bdv.util.BoundedValueDouble;
import bvb.gui.SelectedSources;
import bvb.utils.BoundedValueDoubleBVB;
import bvb.utils.Bounds3D;
import bvb.utils.clip.ClipSetups;
import bvvpg.source.converters.GammaConverterSetup;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class ClipCenterPanel extends JPanel
{
	final SelectedSources sourceSelection;

	final ClipSetups clipSetups;

	private BoundedValuePanelPG [] clipCenterPanels = new BoundedValuePanelPG[3];

	private boolean blockUpdates = false;
	
	
	public ClipCenterPanel(SelectedSources sourceSelection_, final ClipSetups clipSetups_) 
	{
		super();
		
		sourceSelection = sourceSelection_;
		
		clipSetups = clipSetups_;
		
		GridBagLayout gridbag = new GridBagLayout();
		
		GridBagConstraints cd = new GridBagConstraints();

		setLayout(gridbag);
		
		cd.gridwidth = 0;
		cd.gridy = 0;
		cd.gridx = 0;
		cd.fill = GridBagConstraints.BOTH;
		cd.weightx = 1.0;
		for(int d=0;d<3;d++)
		{
			cd.gridy++;
			
			clipCenterPanels[d] = new BoundedValuePanelPG( new BoundedValueDouble( 0.0, 1.0, 0.5 ));

			this.add(clipCenterPanels[d],cd);
		}
		
		clipCenterPanels[0].changeListeners().add( () -> updateClipCenter(0));
		clipCenterPanels[1].changeListeners().add( () -> updateClipCenter(1));
		clipCenterPanels[2].changeListeners().add( () -> updateClipCenter(2));
		
		//add source selection listener
		sourceSelection.addSourceSelectionListener(  new SelectedSources.Listener()
		{			
			@Override
			public void selectedSourcesChanged()
			{
				updateGUI();
			}
		} );
		
		//add listener in case number of sources, etc change
		clipSetups.converterSetups.listeners().add( s -> updateGUI() );
		
		updateGUI();
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(int i=0;i<3;i++)
		{
			clipCenterPanels[i].setEnabled( bEnabled );
		}
	}
	
	synchronized void updateGUI()
	{
		final List< ConverterSetup > csList = sourceSelection.getSelectedSources();
		if ( blockUpdates || csList == null || csList.isEmpty() )
			return;	
		
		
		BoundedValueDoubleBVB [] boundValue = new BoundedValueDoubleBVB[3];
		boolean bFirstCS = true;
		boolean [] allCenterEqual = new boolean [3];
		for (int d=0;d<3;d++)
		{
			allCenterEqual[d] = true;
		}

		for ( final ConverterSetup cs: csList)
		{
			final Bounds3D bounds = clipSetups.clipCenterBounds.getBounds( cs );
			final double [] minBound = bounds.getMinBound();
			final double [] maxBound = bounds.getMaxBound();
			
			double [] center = new double [3];
			center = clipSetups.clipCenters.getCenters( cs );
			if(bFirstCS)
			{
				for (int d=0; d<3; d++)
				{
					boundValue[d] = new BoundedValueDoubleBVB( minBound[d], maxBound[d], center[d]);
				}
				bFirstCS = false;
			}
			else
			{
				for (int d=0; d<3; d++)
				{
					final BoundedValueDoubleBVB centerRange = new BoundedValueDoubleBVB( minBound[d], maxBound[d], center[d]);
					allCenterEqual[d] &= boundValue[d].equals( centerRange );
					boundValue[d] = boundValue[d].join( centerRange );
				}
			}
		}
		
		
		final BoundedValueDoubleBVB [] finalCenter = boundValue;
		final boolean [] isConsistent = allCenterEqual;
		SwingUtilities.invokeLater( () -> {
			synchronized ( ClipCenterPanel.this )
			{
				blockUpdates = true;
				for (int d=0;d<3;d++)
				{

					clipCenterPanels[d].setConsistent( isConsistent[d] );
					clipCenterPanels[d].setValue( finalCenter[d] );
				}
				blockUpdates = false;
			}
		} );
	}
	
	synchronized void updateClipCenter(int nAxis)
	{
		final List< ConverterSetup > csList = sourceSelection.getSelectedSources();
		if ( blockUpdates || csList == null || csList.isEmpty() )
			return;
		blockUpdates = true;
		for ( final ConverterSetup cs : csList )
		{
			
			final double [] newCenter = clipSetups.clipCenters.getCenters( cs );
			newCenter[nAxis] = clipCenterPanels[nAxis].getValue().getCurrentValue();
			
			clipSetups.clipCenters.setCenters( cs, newCenter );
			clipSetups.updateClipTransform( ( GammaConverterSetup ) cs );

		}
		blockUpdates = false;
		updateGUI();
	}
}
