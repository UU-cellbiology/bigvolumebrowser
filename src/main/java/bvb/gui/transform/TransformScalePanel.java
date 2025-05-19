package bvb.gui.transform;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.UIUtils;
import bdv.util.BoundedValueDouble;
import bvb.gui.SelectedSources;
import bvb.gui.clip.ClipRotationPanel;
import bvb.transform.TransformSetups;
import bvb.utils.BoundedValueDoubleBVB;
import bvb.utils.Bounds3D;

public class TransformScalePanel extends JPanel
{
	final TransformSetups transformSetups;
	
	private boolean blockUpdates = false;
	
	private SpinnerModel [] models = new SpinnerModel[3];
	
	private JSpinner [] spinners = new JSpinner[3];
	
	private JLabel [] axesLabels = new JLabel[3];
	
	final JButton butResetScale;
	
	/**
	 * Panel background if color reflects a set of sources all having the same color
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if color reflects a set of sources with different colors
	 */
	private Color inConsistentBg = Color.WHITE;
	
	public TransformScalePanel(final TransformSetups transformSetups_) 
	{
		super();		

		transformSetups = transformSetups_;
		
		setLayout(new GridBagLayout());
		
		butResetScale = new JButton ("Reset");
		butResetScale.setToolTipText( "Reset scale" );
		
		String [] axesTitles = new String[] {"X","Y","Z"};
		
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0,4,0,4);
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		for(int d=0;d<3;d++)
		{
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 0;
			gbc.weightx = 0.0;
			gbc.insets = new Insets(0,4,0,4);
			axesLabels[d] = new JLabel (axesTitles[d]);
			this.add( axesLabels[d], gbc );
			gbc.gridx++;
			gbc.weightx = 0.1;
			gbc.insets = new Insets(0,0,0,0);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			models[d] = new SpinnerNumberModel(1.0,0.001,1000., 0.1);
			spinners[d] = new JSpinner(models[d]);
			this.add( spinners[d], gbc );
			gbc.gridy++;
		}


		gbc.gridx=0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.fill = GridBagConstraints.NONE;
		this.add( butResetScale, gbc );
		
		transformSetups.selectedSources.addSourceSelectionListener(  new SelectedSources.Listener()
		{			
			@Override
			public void selectedSourcesChanged()
			{
				updateGUI();
			}
		} );
		 updateColors();
		//add listener in case number of sources, etc change
		transformSetups.converterSetups.listeners().add( s -> updateGUI() );
		updateGUI();

	}
	
	void updateGUI()
	{
		
		final List< ConverterSetup > csList = transformSetups.selectedSources.getSelectedSources();
		if ( blockUpdates || csList == null || csList.isEmpty() )
			return;	
		
		double [] scales = new double[3];
		boolean bFirstCS = true;
		boolean [] allScalesEqual = new boolean [3];
		for (int d=0;d<3;d++)
		{
			allScalesEqual[d] = true;
		}
		
		for ( final ConverterSetup cs: csList)
		{
			if(bFirstCS)
			{
				scales = transformSetups.transformScale.getScale( cs );
				bFirstCS = false;
			}
			else
			{
				final double[] currScales = transformSetups.transformScale.getScale( cs );

				for (int d=0; d<3; d++)
				{
					allScalesEqual[d] &= (Double.compare( scales[d], currScales[d] )==0);
				}
			}
		}
		final double [] finalScales = scales;
		final boolean [] isConsistent = allScalesEqual;
		SwingUtilities.invokeLater( () -> {
			synchronized ( TransformScalePanel.this )
			{
				blockUpdates = true;
				for (int d=0;d<3;d++)
				{

					//clipRotationPanels[d].setConsistent( isConsistent[d] );
					spinners[d].setValue( finalScales[d]);
					if(isConsistent[d])
					{
						axesLabels[d].setBackground( consistentBg );
						spinners[d].setBackground( consistentBg );
					}
					else
					{
						axesLabels[d].setBackground( inConsistentBg );
						spinners[d].setBackground( inConsistentBg );
					}
						
				}
				blockUpdates = false;
			}
		} );
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(int i=0;i<3;i++)
		{
			spinners[i].setEnabled( bEnabled );
		}
	}
	
	private void updateColors()
	{
		consistentBg = UIManager.getColor("FormattedTextField.background");
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}
}
