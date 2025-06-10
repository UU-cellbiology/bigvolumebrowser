package bvb.gui.transform;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import bdv.util.BoundedValueDouble;
import bvb.utils.transform.TransformSetups;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class TransformRotationPanel extends JPanel
{
	final TransformSetups transformSetups;

	private BoundedValuePanelPG [] trRotationPanels = new BoundedValuePanelPG[3];

	private boolean blockUpdates = false;
	
	private double dRange = 180.;
	
	public TransformRotationPanel(final TransformSetups transformSetups_) 
	{
		super();
		
		transformSetups = transformSetups_;
		
		GridBagLayout gridbag = new GridBagLayout();
		
		GridBagConstraints gbc = new GridBagConstraints();

		setLayout(gridbag);
		
		gbc.gridwidth = 0;
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		
		for(int d=0;d<3;d++)
		{
			gbc.gridy++;
			trRotationPanels[d] = new BoundedValuePanelPG( new BoundedValueDouble( -dRange, dRange, 0.0 ));

			this.add(trRotationPanels[d],gbc);
		}
		
		trRotationPanels[0].changeListeners().add( () -> updateAxisRotation(0));
		trRotationPanels[1].changeListeners().add( () -> updateAxisRotation(1));
		trRotationPanels[2].changeListeners().add( () -> updateAxisRotation(2));
		
		//add source selection listener
		transformSetups.selectedObjects.addObjectSelectionListener(()-> updateGUI());
		
		//add listener in case number of sources, etc change
		transformSetups.converterSetups.listeners().add( s -> updateGUI() );

		updateGUI();
	}
	
	synchronized void updateGUI()
	{
		
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		double [] angles = new double[3];
		boolean bFirstCS = true;
		boolean [] allAnglesEqual = new boolean [3];
		for (int d=0;d<3;d++)
		{
			allAnglesEqual[d] = true;
		}
		
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			if(bFirstCS)
			{
				angles = transformSetups.transformRotation.getAngles( obj );
				bFirstCS = false;
			}
			else
			{
				final double[] currAngles = transformSetups.transformRotation.getAngles( obj );

				for (int d=0; d<3; d++)
				{
					allAnglesEqual[d] &= (Math.abs( angles[d]-currAngles[d] )<0.00001);
				}
			}
		}
		
		final double [] finalAngles = angles;
		final boolean [] isConsistent = allAnglesEqual;
		SwingUtilities.invokeLater( () -> {
			synchronized ( TransformRotationPanel.this )
			{
				blockUpdates = true;
				for (int d=0;d<3;d++)
				{

					trRotationPanels[d].setConsistent( isConsistent[d] );
					trRotationPanels[d].setValue( new BoundedValueDouble( -dRange, dRange, finalAngles[d]*180/Math.PI ) );
				}
				blockUpdates = false;
			}
		} );
	}
	
	void updateAxisRotation(int nAxis)
	{
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		blockUpdates = true;
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{			
			final double [] eAngles = transformSetups.transformRotation.getAngles( obj );
			final double [] prevAngles =  new double[3];
			for(int d=0;d<3;d++)
			{
				prevAngles[d] = eAngles[d];
			}
			
			eAngles[nAxis] = trRotationPanels[nAxis].getValue().getCurrentValue()*Math.PI/180.;

			transformSetups.transformRotation.setAngles( obj, eAngles );
			transformSetups.updateTransform( obj, prevAngles );

		}
		
		blockUpdates = false;
		updateGUI();
	}
	
	public void resetRotation()
	{
		
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		blockUpdates = true;
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final double [] prevAngles =  new double[3];
			final double [] eAngles = transformSetups.transformRotation.getAngles( obj );
			for(int d=0;d<3;d++)
			{
				prevAngles[d] = eAngles [d];
			}
			transformSetups.transformRotation.setAngles( obj,  new double [3] );
			transformSetups.updateTransform( obj, prevAngles );		
		}
		blockUpdates = false;
		updateGUI();
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(int i=0;i<3;i++)
		{
			trRotationPanels[i].setEnabled( bEnabled );
		}
	}
	
	void setSliderColors(Color [] colors)
	{
		for(int i=0;i<3;i++)
		{
			trRotationPanels[i].setSliderForeground( colors[i] );	
		}
	}
}
