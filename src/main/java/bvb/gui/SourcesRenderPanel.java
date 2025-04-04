package bvb.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.UIUtils;
import bdv.viewer.ConverterSetups;
import bvvpg.source.converters.GammaConverterSetup;


public class SourcesRenderPanel extends JPanel implements ActionListener
{	
	
	final SelectedSources selectedSources;
	
	public JComboBox<String> cbRenderMethod;
	
	public JComboBox<String> cbInterpolation; 
	
	/**
	 * Panel background if color reflects a set of sources all having the same color
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if color reflects a set of sources with different colors
	 */
	private Color inConsistentBg = Color.WHITE;

	public SourcesRenderPanel(final ConverterSetups convSetups, final SelectedSources selectedSources_)
	{
		super();
		selectedSources = selectedSources_;
		
		setLayout(new GridBagLayout());
		
		this.setBorder(new PanelTitle(" Render "));

		String[] sMethods = new String[2];
		sMethods[0] = "Max intensity";
		sMethods[1] = "Volumetric";
		
		String[] sInterpolation = new String[2];
		sInterpolation[0] = "Nearest";
		sInterpolation[1] = "Trilinear";
		
		GridBagConstraints gbc = new GridBagConstraints();

		cbRenderMethod = new JComboBox<>(sMethods);
		cbRenderMethod.addActionListener(this);
		
		gbc.gridx=0;
		gbc.gridy=0;
		GBCHelper.alighLoose(gbc);
		this.add(new JLabel("View:"),gbc);
		gbc.gridx++;
		this.add(cbRenderMethod,gbc);		
		
		cbInterpolation = new JComboBox<>(sInterpolation);
		
		cbInterpolation.addActionListener(this);
		
		gbc.gridx=0;
		gbc.gridy++;
		this.add(new JLabel("Interpolation:"),gbc);
		gbc.gridx++;
		this.add(cbInterpolation,gbc);	
		
	    selectedSources.addSourceSelectionListener(  new SelectedSources.Listener()
		{			
			@Override
			public void selectedSourcesChanged( )
			{
				updateGUI();
			}
		} );
	    
	    //add listener in case number of sources, etc change
	    convSetups.listeners().add( s -> updateGUI() );
	    updateGUI();
	}
	
	synchronized void updateGUI()
	{
		updateColors();
		
		final List< ConverterSetup > csList = selectedSources.getSelectedSources();
		if(csList== null || csList.isEmpty())
		{
			setChoicesEnabled(false);
			return;
		}
		setChoicesEnabled(true);
		
		boolean bRenderConsistent = true;
		boolean bInterpConsistent = true;
		int nRenderM = -1;
		int nInterpM = -1;
		for ( final ConverterSetup cs: csList)
		{
			 
			if(nRenderM < 0)
			{
				nRenderM = ((GammaConverterSetup)cs).getRenderType();
				nInterpM = ((GammaConverterSetup)cs).getVoxelRenderInterpolation();
			}
			else
			{
				bRenderConsistent &= (nRenderM == ((GammaConverterSetup)cs).getRenderType());
				bInterpConsistent &= (nInterpM == ((GammaConverterSetup)cs).getVoxelRenderInterpolation());
			}
		}
		
		if(bRenderConsistent)
		{
			cbRenderMethod.setBackground( consistentBg );
			cbRenderMethod.setSelectedIndex( nRenderM );
		}
		else
		{
			cbRenderMethod.setBackground( inConsistentBg );
		}
		
		if(bInterpConsistent)
		{
			cbInterpolation.setBackground( consistentBg );
			cbInterpolation.setSelectedIndex( nInterpM );
		}
		else
		{
			cbInterpolation.setBackground( inConsistentBg );
		}
	}
	
	void setChoicesEnabled(boolean bEnabled)
	{
		cbRenderMethod.setEnabled( bEnabled );
		cbInterpolation.setEnabled( bEnabled );
	}

	@Override
	public void actionPerformed( ActionEvent arg0 )
	{
		final List< ConverterSetup > csList = selectedSources.getSelectedSources();
		
		if(csList== null || csList.isEmpty())
			return;
		
		for ( final ConverterSetup cs: csList)
		{
			if(arg0.getSource() == cbRenderMethod)
			{			
			  ((GammaConverterSetup)cs).setRenderType( cbRenderMethod.getSelectedIndex() );
			}
			if(arg0.getSource() == cbInterpolation)
			{			
			  ((GammaConverterSetup)cs).setVoxelRenderInterpolation( cbInterpolation.getSelectedIndex() );
			}

		}
	}
	
	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}
}
