package bvb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.Affine3DHelpers;
import bdv.util.BoundedRange;
import bdv.util.BoundedValueDouble;
import bdv.viewer.ConverterSetups;
import bvb.utils.Misc;
import bvvpg.source.converters.GammaConverterSetup;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class ClipRotationPanel extends JPanel implements ItemListener
{
	
	private BoundedValuePanelPG [] clipRotationPanels = new BoundedValuePanelPG[3];
	public JCheckBox cbCentered;
	public JLabel selectionWindow;
	private int nActiveWindow = -1;
	private List< ConverterSetup > csList = new ArrayList<>();
	private boolean blockUpdates = false;
	
	public ClipRotationPanel(SelectedSources sourceSelection) 
	{
		super();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints cd = new GridBagConstraints();

		setLayout(gridbag);

		cd.gridy = 0;
		cd.gridwidth = 1;
		cd.weightx = 0.1;
		cd.fill = GridBagConstraints.NONE;
		cd.anchor = GridBagConstraints.WEST; 
		cbCentered = new JCheckBox("Centered", false);
		cbCentered.addItemListener( this );
		cd.gridx = 0;
		this.add(cbCentered,cd);
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
			clipRotationPanels[d] = new BoundedValuePanelPG( new BoundedValueDouble( -180., 180, 0.0 ));

			this.add(clipRotationPanels[d],cd);
		}
		
		clipRotationPanels[0].changeListeners().add( () -> updateClipAxisRotation(0));
		clipRotationPanels[1].changeListeners().add( () -> updateClipAxisRotation(1));
		clipRotationPanels[2].changeListeners().add( () -> updateClipAxisRotation(2));
		
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
	
	private synchronized void updateGUI()
	{
		double [] angles = new double[3];
		boolean bFirstCS = true;
		boolean [] allAnglesEqual = new boolean [3];
		for (int d=0;d<3;d++)
		{
			allAnglesEqual[d] = true;
		}
		for ( final ConverterSetup cs: csList)
		{
			if(bFirstCS)
			{
				for(int d=0;d<3;d++)
				{
					angles[d] = getRotationAngleAxis(d, cs);
				}
			}
			else
			{
				for (int d=0; d<3; d++)
				{
					allAnglesEqual[d] &= (Double.compare( angles[d], getRotationAngleAxis(d, cs) )==0);
				}
			}
		}
		final double [] finalAngles = angles;
		final boolean [] isConsistent = allAnglesEqual;
		SwingUtilities.invokeLater( () -> {
			synchronized ( ClipRotationPanel.this )
			{
				blockUpdates = true;
				for (int d=0;d<3;d++)
				{

					clipRotationPanels[d].setConsistent( isConsistent[d] );
					clipRotationPanels[d].setValue( new BoundedValueDouble( -180., 180, finalAngles[d]*180/Math.PI ) );
				}
				blockUpdates = false;
			}
		} );
	}
	
	synchronized void updateClipAxisRotation(int nAxis)
	{
		if ( blockUpdates || csList== null || csList.isEmpty() )
			return;
		
		for ( final ConverterSetup cs : csList )
		{
			AffineTransform3D clipTr = ((GammaConverterSetup)cs).getClipTransform();
			
			final double [] center = Misc.getIntervalCenterShift( ((GammaConverterSetup)cs).getClipInterval() );
			
			clipTr.translate( center );
			final double[] qRotation = new double[4];
			
			Affine3DHelpers.extractRotationAnisotropic( clipTr, qRotation );
			double angleCurr = Misc.quaternionToAngle(nAxis,qRotation);
			
			double angleNew = clipRotationPanels[nAxis].getValue().getCurrentValue()*Math.PI/180.;
			clipTr.rotate( nAxis, angleNew-angleCurr );
			LinAlgHelpers.scale( center, -1.0, center );
			clipTr.translate( center );
			((GammaConverterSetup)cs).setClipTransform(clipTr);

		}
		updateGUI();
	}
	
	private double getRotationAngleAxis(int nAxis, ConverterSetup cs)
	{
		final AffineTransform3D clipTr = ((GammaConverterSetup)cs).getClipTransform().copy();
		
		final double [] center = Misc.getIntervalCenterShift( ((GammaConverterSetup)cs).getClipInterval() );
		clipTr.translate( center );
		final double[] qRotation = new double[4];
		
		Affine3DHelpers.extractRotationAnisotropic( clipTr, qRotation );
		return Misc.quaternionToAngle(nAxis,qRotation);
		
	}

	private synchronized void updateCS(int nSource, List< ConverterSetup > converterSetupList)
	{
		nActiveWindow = nSource;
		csList = converterSetupList;
		updateGUI();
	}
	
	@Override
	public void itemStateChanged( ItemEvent arg0 )
	{
		// TODO Auto-generated method stub
		
	}
	

}
