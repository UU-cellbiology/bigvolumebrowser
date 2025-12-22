package bvb.gui.transform;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import bdv.util.BoundedValueDouble;
import bvb.utils.transform.TransformSetups;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class TransformDeskewPanel extends JPanel
{
	final TransformSetups transformSetups;

	private final BoundedValuePanelPG trDeskewPanel;
	
	private boolean blockUpdates = false;
	
	final double bDeskewAngleBoundMax = 178.0;
	final double bDeskewAngleBoundMin = 2.0;
	
	public TransformDeskewPanel(final TransformSetups transformSetups_) 
	{
		super();
		
		transformSetups = transformSetups_;

		trDeskewPanel = new BoundedValuePanelPG( new BoundedValueDouble( bDeskewAngleBoundMax, bDeskewAngleBoundMin, 90.0 ));		
		
		trDeskewPanel.changeListeners().add( () -> updateDeskewAngle());

		GridBagLayout gridbag = new GridBagLayout();
		
		GridBagConstraints gbc = new GridBagConstraints();

		setLayout(gridbag);
		gbc.gridwidth = 0;
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.CENTER;

		this.add(new JLabel("YZ angle (degrees)"),gbc);		
		gbc.gridy ++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		this.add(trDeskewPanel,gbc);
		
		updateGUI();
	}
	
	synchronized void updateGUI()
	{
		
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		double angle = 90.;
		boolean bFirstObj = true;
		boolean allAnglesEqual = true;
		
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			if(bFirstObj)
			{
				angle = transformSetups.transformDeskew.getAngle( obj );
				bFirstObj = false;
			}
			else
			{
					allAnglesEqual &= (Math.abs( angle - transformSetups.transformDeskew.getAngle( obj ) )<0.00001);
			}
		}
		
		final double finalAngle = angle;
		final boolean isConsistent = allAnglesEqual;
		
		SwingUtilities.invokeLater( () -> {
			synchronized ( TransformDeskewPanel.this )
			{
				blockUpdates = true;
	
				trDeskewPanel.setConsistent( isConsistent );
				trDeskewPanel.setValue( new BoundedValueDouble( bDeskewAngleBoundMax, bDeskewAngleBoundMin, finalAngle * 180/Math.PI) );

				blockUpdates = false;
			}
		} );
	}
	
	void updateDeskewAngle()
	{
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		blockUpdates = true;
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		final double angle = trDeskewPanel.getValue().getCurrentValue() * Math.PI / 180.;
		for ( final Object obj: objList)
		{	
			transformSetups.transformDeskew.setAngle( obj, angle);
			transformSetups.updateTransform( obj, null );

		}
		blockUpdates = false;
		
		updateGUI();
	}
	
	public void resetDeskew()
	{
		
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		blockUpdates = true;
		final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			transformSetups.transformDeskew.setAngle( obj, 0.5 * Math.PI );
			transformSetups.updateTransform( obj, null );		
		}
		blockUpdates = false;
		updateGUI();
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		trDeskewPanel.setEnabled( bEnabled );
	}
}
