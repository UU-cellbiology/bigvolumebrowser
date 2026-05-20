/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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

	void updateGUI()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(this::updateGUI);
			return;
		}

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

		blockUpdates = true;
		try
		{
			trDeskewPanel.setConsistent( isConsistent );
			trDeskewPanel.setValue( new BoundedValueDouble( bDeskewAngleBoundMax, bDeskewAngleBoundMin, finalAngle * 180/Math.PI) );
		}
		finally
		{
			blockUpdates = false;
		}
	}
	
	void updateDeskewAngle()
	{
	    if (!SwingUtilities.isEventDispatchThread())
	    {
	        SwingUtilities.invokeLater(this::updateDeskewAngle);
	        return;
	    }
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		blockUpdates = true;
		try
		{
			final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
			double angle = trDeskewPanel.getValue().getCurrentValue();
			angle = Math.max( angle, bDeskewAngleBoundMin );
			angle = Math.min( angle, bDeskewAngleBoundMax );
			angle *=  Math.PI / 180.;
			for ( final Object obj: objList)
			{	
				transformSetups.transformDeskew.setAngle( obj, angle);
				transformSetups.updateTransform( obj, null );
			}
		}
		finally
		{
			blockUpdates = false;
		}
		
		updateGUI();
	}
	
	public void resetDeskew()
	{
		
		if(!transformSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		blockUpdates = true;
		try
		{
			final List< Object > objList = transformSetups.selectedObjects.getSelectedObjects();
			for ( final Object obj: objList)
			{
				transformSetups.transformDeskew.setAngle( obj, 0.5 * Math.PI );
				transformSetups.updateTransform( obj, null );		
			}
		}
		finally
		{
			blockUpdates = false;
		}
		updateGUI();
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		trDeskewPanel.setEnabled( bEnabled );
	}
}
