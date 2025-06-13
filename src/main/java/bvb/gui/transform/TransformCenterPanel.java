package bvb.gui.transform;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.util.BoundedValueDouble;
import bdv.viewer.Source;
import bvb.shapes.BasicShape;
import bvb.utils.BoundedValueDoubleBVB;
import bvb.utils.Bounds3D;
import bvb.utils.Misc;
import bvb.utils.transform.TransformSetups;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class TransformCenterPanel extends JPanel
{
	
	final TransformSetups transformSetups;
	
	private BoundedValuePanelPG [] centerPanels = new BoundedValuePanelPG[3];

	private boolean blockUpdates = false;
	
	public TransformCenterPanel(final TransformSetups transformSetups_) 
	{
		super();		

		transformSetups = transformSetups_;
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridwidth = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;

		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.99;
		final JPopupMenu [] menus = new JPopupMenu[3];
		
		for(int d=0;d<3;d++)
		{
			gbc.gridy++;
			
			centerPanels[d] = new BoundedValuePanelPG( new BoundedValueDouble( 0.0, 1.0, 0.5 ));
			menus[d] = new JPopupMenu();
			menus[d].add( runnableItem(  "set bounds ...", centerPanels[d]::setBoundsDialog ) );
			this.add(centerPanels[d],gbc);
		}

		menus[0].add( runnableItem(  "reset bounds", () -> resetBounds(0)));
		menus[1].add( runnableItem(  "reset bounds", () -> resetBounds(1)));
		menus[2].add( runnableItem(  "reset bounds", () -> resetBounds(2)));
	
		centerPanels[0].setPopup( () -> menus[0] );
		centerPanels[1].setPopup( () -> menus[1] );
		centerPanels[2].setPopup( () -> menus[2] );
		
		centerPanels[0].changeListeners().add( () -> updateTransformAxis(0));
		centerPanels[1].changeListeners().add( () -> updateTransformAxis(1));
		centerPanels[2].changeListeners().add( () -> updateTransformAxis(2));
		
		//add source selection listener
		transformSetups.selectedObjects.addObjectSelectionListener(()->updateGUI());
		
		//add listener in case number of sources, etc change
		transformSetups.converterSetups.listeners().add( s -> updateGUI() );
		
		updateGUI();
	}
	
	synchronized void updateGUI()
	{
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		
		BoundedValueDoubleBVB [] boundValue = new BoundedValueDoubleBVB[3];
		boolean bFirstCS = true;
		boolean [] allTrEqual = new boolean [3];
		for (int d=0;d<3;d++)
		{
			allTrEqual[d] = true;
		}
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final Bounds3D bounds = transformSetups.transformCenterBounds.getBounds( obj );
			final double [] minBound = bounds.getMinBound();
			final double [] maxBound = bounds.getMaxBound();
			
			final double [] center = transformSetups.transformCenters.getCenters( obj );
			
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
			synchronized ( TransformCenterPanel.this )
			{
				blockUpdates = true;
				for (int d=0;d<3;d++)
				{

					centerPanels[d].setConsistent( isConsistent[d] );
					centerPanels[d].setValue( finalTranslation[d] );
				}
				blockUpdates = false;
			}
		} );
	}
	
	synchronized void updateTransformAxis(int nAxis)
	{
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		blockUpdates = true;
		
		double currVal = centerPanels[nAxis].getValue().getCurrentValue();
		double minBound = centerPanels[nAxis].getValue().getRangeMin();
		double maxBound = centerPanels[nAxis].getValue().getRangeMax();
		
		minBound = Math.min( currVal, minBound );
		maxBound = Math.max( currVal, maxBound );
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final Bounds3D bounds = transformSetups.transformCenterBounds.getBounds( obj );
			
			if(minBound != bounds.getMinBound()[nAxis] || maxBound != bounds.getMaxBound()[nAxis])
			{
				bounds.getMinBound()[nAxis] = minBound;
				bounds.getMaxBound()[nAxis] = maxBound;
				transformSetups.transformCenterBounds.setBounds( obj, bounds );
			}
			final double [] oldCenters = transformSetups.transformCenters.getCenters( obj );
			final double [] newCenters = new double [3];
			for(int d=0; d<3; d++)
			{
				newCenters[d] = oldCenters[d];
			}
			newCenters[nAxis] = currVal;
			
			transformSetups.transformCenters.setCenters( obj, newCenters );
			transformSetups.updateTransform( obj, null );
		}
		blockUpdates = false;
		updateGUI();
	}

	
	public void resetBounds(int nAxis)
	{
		
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		blockUpdates = true;
		
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final Bounds3D range3D = transformSetups.transformCenterBounds.getDefaultBounds( obj );
			Bounds3D currBounds = transformSetups.transformCenterBounds.getBounds( obj);
			currBounds.getMinBound()[nAxis] = range3D.getMinBound()[nAxis];
			currBounds.getMaxBound()[nAxis] = range3D.getMaxBound()[nAxis];
		}
		blockUpdates = false;
		updateGUI();
	}
	
	public void resetCenters()
	{
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		blockUpdates = true;

		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			
			RealInterval interval = null;
			
			if(obj instanceof ConverterSetup)
			{
				final Source< ? > src = transformSetups.converterSetups.getSource( (ConverterSetup)obj ).getSpimSource();
				
				final AffineTransform3D srcTrFixed = new AffineTransform3D();
				AffineTransform3D srcTrIc = new AffineTransform3D();
				
				//get both transforms just in case
				(( TransformedSource< ? > )src).getFixedTransform( srcTrFixed );
				(( TransformedSource< ? > )src).getIncrementalTransform( srcTrIc );
				interval = Misc.getSourceBoundingBoxAllTP(src);
				
				//remove both transforms
				srcTrIc = srcTrIc.inverse().preConcatenate( srcTrFixed.inverse() );
				interval = srcTrIc.estimateBounds( interval );
			}
			if(obj instanceof BasicShape)
			{
				interval = ((BasicShape)obj).boundingBoxNotTransformed();
			}
			
			final double [] centers = Misc.getIntervalCenter( interval );
			transformSetups.transformCenters.setCenters( obj, centers );
			transformSetups.updateTransform( obj, null );
		}
		
		blockUpdates = false;
		updateGUI();
		transformSetups.updateBVV();
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(int i=0;i<3;i++)
		{
			centerPanels[i].setEnabled( bEnabled );
		}
	}
	
	void setSliderColors(Color [] colors)
	{
		for(int i=0;i<3;i++)
		{
			centerPanels[i].setSliderForeground( colors[i] );	
		}
	}
	
	private JMenuItem runnableItem( final String text, final Runnable action )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> action.run() );
		return item;
	}
}
