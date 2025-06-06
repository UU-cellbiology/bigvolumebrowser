package bvb.gui.transform;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.BoundedValueDouble;
import bvb.shapes.BasicShape;
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
		
		if(transformSetups.selectedObjects.areSourcesSelected())
		{
			final List< ConverterSetup > csList = transformSetups.selectedObjects.getSelectedSources();
			for ( final ConverterSetup cs: csList)
			{
				if(bFirstCS)
				{
					angles = transformSetups.transformRotation.getAngles( cs );
					bFirstCS = false;
				}
				else
				{
					final double[] currAngles = transformSetups.transformRotation.getAngles( cs );
	
					for (int d=0; d<3; d++)
					{
						allAnglesEqual[d] &= (Math.abs( angles[d]-currAngles[d] )<0.00001);
					}
				}
			}
		}
		if(transformSetups.selectedObjects.areShapesSelected())
		{
			final List< BasicShape > shList = transformSetups.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shList)
			{
				if(bFirstCS)
				{
					angles = transformSetups.transformRotation.getAngles( sh );
					bFirstCS = false;
				}
				else
				{
					final double[] currAngles = transformSetups.transformRotation.getAngles( sh );
	
					for (int d=0; d<3; d++)
					{
						allAnglesEqual[d] &= (Double.compare( angles[d], currAngles[d] )==0);
					}
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
		if(transformSetups.selectedObjects.areSourcesSelected())
		{
			final List< ConverterSetup > csList = transformSetups.selectedObjects.getSelectedSources();
			for ( final ConverterSetup cs : csList )
			{			
				final double [] eAngles = transformSetups.transformRotation.getAngles( cs );
				final double [] prevAngles =  new double[3];
				for(int d=0;d<3;d++)
				{
					prevAngles[d] = eAngles[d];
				}
				
				eAngles[nAxis] = trRotationPanels[nAxis].getValue().getCurrentValue()*Math.PI/180.;

				transformSetups.transformRotation.setAngles( cs, eAngles );
				transformSetups.updateTransform( cs, prevAngles );
	
			}
		}
		if(transformSetups.selectedObjects.areShapesSelected())
		{
			final List< BasicShape > shList = transformSetups.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh : shList )
			{
				final double [] eAngles = transformSetups.transformRotation.getAngles( sh );
				eAngles[nAxis] = trRotationPanels[nAxis].getValue().getCurrentValue()*Math.PI/180.;
				
				transformSetups.transformRotation.setAngles( sh, eAngles );
				transformSetups.updateTransform( sh );
			}
		}
		
		blockUpdates = false;
		updateGUI();
	}
	
	public void resetRotation()
	{
		
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		blockUpdates = true;
		
		if(transformSetups.selectedObjects.areSourcesSelected())
		{
			final List< ConverterSetup > csList = transformSetups.selectedObjects.getSelectedSources();	
			for ( final ConverterSetup cs: csList)
			{			
				final double [] prevAngles =  new double[3];
				final double [] eAngles = transformSetups.transformRotation.getAngles( cs );
				for(int d=0;d<3;d++)
				{
					prevAngles[d] = eAngles [d];
				}
				transformSetups.transformRotation.setAngles( cs,  new double [3] );
				transformSetups.updateTransform( cs, prevAngles );			
			}
		}
		
		if(transformSetups.selectedObjects.areShapesSelected())
		{
			final List< BasicShape > shList = transformSetups.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh : shList )
			{
				final double [] eAngles = new double [3];
				transformSetups.transformRotation.setAngles( sh, eAngles );
				transformSetups.updateTransform( sh );	
			}
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
