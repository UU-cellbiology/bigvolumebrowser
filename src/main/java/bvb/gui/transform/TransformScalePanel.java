package bvb.gui.transform;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.UIUtils;
import bdv.util.Affine3DHelpers;
import bdv.viewer.Source;
import bvb.utils.transform.TransformSetups;

public class TransformScalePanel extends JPanel
{
	final TransformSetups transformSetups;
	
	private boolean blockUpdates = false;
	
	private SpinnerModel [] models = new SpinnerModel[3];
	
	private JSpinner [] spinners = new JSpinner[3];
	
	private JLabel [] axesLabels = new JLabel[3];
	
	private JLabel [] voxelSize = new JLabel[3];
	
	final JButton butResetScale;
	
	/**
	 * Panel background if color reflects a set of sources all having the same color
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if color reflects a set of sources with different colors
	 */
	private Color inConsistentBg = Color.WHITE;
	
	final DecimalFormat formatter = new DecimalFormat("#.####", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
	
	
	public TransformScalePanel(final TransformSetups transformSetups_) 
	{
		super();		

		transformSetups = transformSetups_;
		
		setLayout(new GridBagLayout());
		
		butResetScale = new JButton ("Reset");
		butResetScale.setToolTipText( "Reset scale" );
		butResetScale.addActionListener( (e)->resetScale());
		
		String [] axesTitles = new String[] {"X","Y","Z"};
		String [] voxelSizeS = new String[] {"NA","NA","NA"};
		
		formatter.setRoundingMode( RoundingMode.DOWN );
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridy = 0;
		gbc.gridx = 2;
		final JLabel vxLabel = new JLabel("Voxel");
		vxLabel.setToolTipText( "Final voxel size" );
		this.add( vxLabel , gbc );
		gbc.insets = new Insets(0,4,0,4);
		gbc.gridy = 1;
		gbc.gridx = 0;
		
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
			gbc.gridx++;
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.0;
			gbc.insets = new Insets(0,4,0,4);			
			voxelSize[d] =  new JLabel (voxelSizeS[d]);
			voxelSize[d].setToolTipText( "Final voxel size" );
			this.add( voxelSize[d], gbc );
			gbc.gridy++;
		}
		spinners[0].addChangeListener( (e)-> updateScaleAxis(0) );
		spinners[1].addChangeListener( (e)-> updateScaleAxis(1) );
		spinners[2].addChangeListener( (e)-> updateScaleAxis(2) );


		gbc.gridx=0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.fill = GridBagConstraints.NONE;
		this.add( butResetScale, gbc );
		
		transformSetups.selectedSources.addSourceSelectionListener(()->updateGUI());
		
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
		double [] voxelSizes = new double[3];
		boolean bFirstCS = true;
		boolean [] allScalesEqual = new boolean [3];
		boolean [] allVoxelsEqual = new boolean [3];
		for (int d=0;d<3;d++)
		{
			allScalesEqual[d] = true;
			allVoxelsEqual[d] = true;
		}
		
		for ( final ConverterSetup cs: csList)
		{
			if(bFirstCS)
			{
				scales = transformSetups.transformScale.getScale( cs );
				voxelSizes = getVoxelSize( cs );
				bFirstCS = false;
			}
			else
			{
				final double[] currScales = transformSetups.transformScale.getScale( cs );
				final double[] currVoxels = getVoxelSize( cs );

				for (int d=0; d<3; d++)
				{
					allScalesEqual[d] &= (Double.compare( scales[d], currScales[d] )==0);
					allVoxelsEqual[d] &= (Double.compare( voxelSizes[d], currVoxels[d] )==0);
				}
			}
		}
		final double [] finalScales = scales;
		final double [] finalVoxels = voxelSizes;
		final boolean [] isConsistent = allScalesEqual;
		final boolean [] isConsistentVox = allVoxelsEqual;
		SwingUtilities.invokeLater( () -> {
			synchronized ( TransformScalePanel.this )
			{
				blockUpdates = true;
				for (int d=0;d<3;d++)
				{
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
					if(isConsistentVox[d])
					{
						voxelSize[d].setText( formatter.format( finalVoxels[d] ));
					}
					else
					{
						voxelSize[d].setText("NC");
					}
						
				}
				blockUpdates = false;
			}
		} );
	}
	
	private double[] getVoxelSize( final ConverterSetup cs )
	{
		final Source< ? > src = transformSetups.converterSetups.getSource( cs ).getSpimSource();
		AffineTransform3D viewTr = new AffineTransform3D();
		src.getSourceTransform( transformSetups.bvb.bvvViewer.state().getCurrentTimepoint(), 0, viewTr);

		final double [] finScales = new double[3];
		for(int d=0; d<3; d++)
		{
			finScales[d] = Affine3DHelpers.extractScale( viewTr, d );
		}
		
		return finScales;
	}

	void updateScaleAxis(int nAxis)
	{
		final List< ConverterSetup > csList = transformSetups.selectedSources.getSelectedSources();
		if ( blockUpdates || csList == null || csList.isEmpty() )
			return;
		blockUpdates = true;
		final double currVal = ((Double)spinners[nAxis].getValue()).doubleValue();
		for ( final ConverterSetup cs : csList )
		{
			final double [] oldScale = transformSetups.transformScale.getScale( cs );
			final double [] newScale = new double [3];
			for(int d=0;d<3;d++)
			{
				newScale[d] = oldScale[d];
			}
			newScale[nAxis] = currVal;
			transformSetups.transformScale.setScale( cs, newScale );
			transformSetups.updateTransform( cs );
		}
		blockUpdates = false;
		updateGUI();
	}
	
	void resetScale()
	{
		final List< ConverterSetup > csList = transformSetups.selectedSources.getSelectedSources();
		if(csList == null || csList.isEmpty())
		{
			return;
		}
		
		final double [] unitScale = new double [3];
		
		for (int d=0; d<3;d++)
		{
			unitScale [d] = 1.0;
		}
		for ( final ConverterSetup cs: csList)
		{
			
			transformSetups.transformScale.setScale( cs, unitScale );
			transformSetups.updateTransform( cs );			
		}
		updateGUI();
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
