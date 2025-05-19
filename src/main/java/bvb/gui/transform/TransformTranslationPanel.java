package bvb.gui.transform;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.BoundedValueDouble;
import bvb.gui.SelectedSources;
import bvb.transform.TransformSetups;
import bvb.utils.BoundedValueDoubleBVB;
import bvb.utils.Bounds3D;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class TransformTranslationPanel extends JPanel
{
	final SelectedSources sourceSelection;
	
	final TransformSetups transformSetups;
	
	private BoundedValuePanelPG [] translationPanels = new BoundedValuePanelPG[3];

	private boolean blockUpdates = false;
	
	public TransformTranslationPanel(final TransformSetups transformSetups_) 
	{
		super();		

		transformSetups = transformSetups_;
		
		sourceSelection = transformSetups.selectedSources;
		
		GridBagLayout gridbag = new GridBagLayout();
		
		GridBagConstraints cd = new GridBagConstraints();

		setLayout(gridbag);
		
		cd.gridwidth = 0;
		cd.gridy = 0;
		cd.gridx = 0;
		cd.fill = GridBagConstraints.BOTH;
		cd.weightx = 1.0;
		final JPopupMenu [] menus = new JPopupMenu[3];
		for(int d=0;d<3;d++)
		{
			cd.gridy++;
			
			translationPanels[d] = new BoundedValuePanelPG( new BoundedValueDouble( 0.0, 1.0, 0.5 ));
			menus[d] = new JPopupMenu();
			menus[d].add( runnableItem(  "set bounds ...", translationPanels[d]::setBoundsDialog ) );
			this.add(translationPanels[d],cd);
		}

		menus[0].add( runnableItem(  "reset bounds", () -> resetBounds(0)));
		menus[1].add( runnableItem(  "reset bounds", () -> resetBounds(1)));
		menus[2].add( runnableItem(  "reset bounds", () -> resetBounds(2)));
	
		translationPanels[0].setPopup( () -> menus[0] );
		translationPanels[1].setPopup( () -> menus[1] );
		translationPanels[2].setPopup( () -> menus[2] );
		
		translationPanels[0].changeListeners().add( () -> updateTransformAxis(0));
		translationPanels[1].changeListeners().add( () -> updateTransformAxis(1));
		translationPanels[2].changeListeners().add( () -> updateTransformAxis(2));
		
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
		transformSetups.converterSetups.listeners().add( s -> updateGUI() );
		
		updateGUI();
	}
	
	synchronized void updateTransformAxis(int nAxis)
	{
		final List< ConverterSetup > csList = sourceSelection.getSelectedSources();
		if ( blockUpdates || csList == null || csList.isEmpty() )
			return;
		blockUpdates = true;
		double currVal = translationPanels[nAxis].getValue().getCurrentValue();
		double minBound = translationPanels[nAxis].getValue().getRangeMin();
		double maxBound = translationPanels[nAxis].getValue().getRangeMax();
		minBound = Math.min( currVal, minBound );
		maxBound = Math.max( currVal, maxBound );
		for ( final ConverterSetup cs : csList )
		{
			final Bounds3D bounds = transformSetups.transformTranslationBounds.getBounds( cs );
			
			if(minBound != bounds.getMinBound()[nAxis] || maxBound != bounds.getMaxBound()[nAxis])
			{
				bounds.getMinBound()[nAxis] = minBound;
				bounds.getMaxBound()[nAxis] = maxBound;
				transformSetups.transformTranslationBounds.setBounds( cs, bounds );
			}
			final double [] newTranslation = transformSetups.transformTranslation.getTranslation( cs );
			newTranslation[nAxis] = currVal;
			
			transformSetups.transformTranslation.setTranslation( cs, newTranslation );
			transformSetups.updateTranslation(cs);
		//	clipSetups.updateClipTransform( ( GammaConverterSetup ) cs );
			//update bounds
		
		}
		blockUpdates = false;
		updateGUI();
	}
	
	void updateGUI()
	{
		final List< ConverterSetup > csList = sourceSelection.getSelectedSources();
		if ( blockUpdates || csList == null || csList.isEmpty() )
			return;	
		
		
		BoundedValueDoubleBVB [] boundValue = new BoundedValueDoubleBVB[3];
		boolean bFirstCS = true;
		boolean [] allTrEqual = new boolean [3];
		for (int d=0;d<3;d++)
		{
			allTrEqual[d] = true;
		}

		for ( final ConverterSetup cs: csList)
		{
			final Bounds3D bounds = transformSetups.transformTranslationBounds.getBounds( cs );
			final double [] minBound = bounds.getMinBound();
			final double [] maxBound = bounds.getMaxBound();
			
			double [] center = new double [3];
			center = transformSetups.transformTranslation.getTranslation( cs );
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
					final BoundedValueDoubleBVB translationRange = new BoundedValueDoubleBVB( minBound[d], maxBound[d], center[d]);
					allTrEqual[d] &= boundValue[d].equals( translationRange );
					boundValue[d] = boundValue[d].join( translationRange );
				}
			}
		}
		
		
		final BoundedValueDoubleBVB [] finalTranslation = boundValue;
		final boolean [] isConsistent = allTrEqual;
		SwingUtilities.invokeLater( () -> {
			synchronized ( TransformTranslationPanel.this )
			{
				blockUpdates = true;
				for (int d=0;d<3;d++)
				{

					translationPanels[d].setConsistent( isConsistent[d] );
					translationPanels[d].setValue( finalTranslation[d] );
				}
				blockUpdates = false;
			}
		} );
	}
	
	public void resetBounds(int nAxis)
	{
		final List< ConverterSetup > csList = sourceSelection.getSelectedSources();
		if ( blockUpdates || csList== null || csList.isEmpty() )
			return;
		Bounds3D range3D = null;
		for ( final ConverterSetup cs : csList )
		{
			if(range3D == null)
				range3D = transformSetups.transformTranslationBounds.getDefaultBounds( cs );
			else
				range3D = range3D.join( transformSetups.transformTranslationBounds.getDefaultBounds( cs ) );			
		}
		if(range3D != null)
		{
			double currVal = translationPanels[nAxis].getValue().getCurrentValue();
			double bmin = range3D.getMinBound()[nAxis];
			double bmax = range3D.getMaxBound()[nAxis];
			currVal = Math.min( bmax, currVal );
			currVal = Math.max( bmin, currVal );
			translationPanels[nAxis].setValue( new BoundedValueDouble(bmin, bmax, currVal) );
			updateTransformAxis(nAxis);
		}
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(int i=0;i<3;i++)
		{
			translationPanels[i].setEnabled( bEnabled );
		}
	}
	
	
	
	private JMenuItem runnableItem( final String text, final Runnable action )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> action.run() );
		return item;
	}
}
