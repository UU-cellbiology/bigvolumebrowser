package bvb.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.UIUtils;
import bdv.viewer.ConverterSetups;
import bvvpg.source.converters.GammaConverterSetup;


public class SourcesRenderPanel extends JPanel implements ActionListener
{	
	
	final SelectedSources selectedSources;
	
	ButtonGroup renderMode = new ButtonGroup();
	
	JToggleButton [] butRender = new JToggleButton[2];
	
	JPanel panRender;
	
	ButtonGroup interpolationMode = new ButtonGroup();
	
	JToggleButton [] butInter = new JToggleButton[2];
	
	JPanel panInterpolation;
	
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
		
		super(new GridBagLayout());
		
		int nButtonSize = 45;
		
		selectedSources = selectedSources_;
		
		this.setBorder(new PanelTitle(" Render "));

		String[] sMethods = new String[2];
		sMethods[0] = "Max intensity";
		sMethods[1] = "Volumetric";
		
		String[] sInterpolation = new String[2];
		sInterpolation[0] = "Nearest";
		sInterpolation[1] = "Trilinear";
		
		GridBagConstraints gbc = new GridBagConstraints();
	
		URL icon_path = this.getClass().getResource("/icons/max_int.png");
		ImageIcon tabIcon = new ImageIcon(icon_path);
		butRender[0] = new JToggleButton(tabIcon);
		butRender[0].setToolTipText("Maximum intensity");
		
		icon_path = this.getClass().getResource("/icons/volumetric.png");
		tabIcon = new ImageIcon(icon_path);
		butRender[1] = new JToggleButton(tabIcon);
		butRender[1].setToolTipText("Volumetric");
		
		icon_path = this.getClass().getResource("/icons/nearest.png");
		tabIcon = new ImageIcon(icon_path);
		butInter[0] = new JToggleButton(tabIcon);
		butInter[0].setToolTipText("Nearest neighbor");
		
		icon_path = this.getClass().getResource("/icons/linear.png");
		tabIcon = new ImageIcon(icon_path);
		butInter[1] = new JToggleButton(tabIcon);
		butInter[1].setToolTipText("Trilinear");

		panRender = new JPanel(new GridBagLayout());		
		panInterpolation = new JPanel(new GridBagLayout());
		
		for(int i = 0; i<2; i++)
		{
			
			butRender[i].setPreferredSize(new Dimension(nButtonSize , nButtonSize ));
			butRender[i].addActionListener( this );
			renderMode.add( butRender[i] );
			panRender.add( butRender[i] );
			
			butInter[i].setPreferredSize(new Dimension(nButtonSize , nButtonSize ));
			butInter[i].addActionListener( this );
			interpolationMode.add( butInter[i] );
			panInterpolation.add( butInter[i] );
		}
		
		
		gbc.gridx=0;
		gbc.gridy=0;
		GBCHelper.alighLoose(gbc);
		this.add(new JLabel("Render:"),gbc);
		gbc.gridx++;
		this.add(panRender,gbc);			
		
		gbc.gridx = 0;
		gbc.gridy++;
		this.add(new JLabel("Voxels:"),gbc);
		gbc.gridx++;
		this.add( panInterpolation, gbc );
		
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
			butRender[nInterpM].setSelected( true );
			panRender.setBackground( consistentBg );
		}
		else
		{
			panRender.setBackground( inConsistentBg );
			renderMode.clearSelection();
		}
		
		if(bInterpConsistent)
		{
			butInter[nInterpM].setSelected( true );
			panInterpolation.setBackground( consistentBg );
		}
		else
		{
			panInterpolation.setBackground( inConsistentBg );
			interpolationMode.clearSelection();
		}
	}
	
	void setChoicesEnabled(boolean bEnabled)
	{
		for(int i=0; i<2; i++)
		{
			butInter[i].setEnabled( bEnabled );
			butRender[i].setEnabled( bEnabled );
		}
	}

	@Override
	public void actionPerformed( ActionEvent arg0 )
	{
		final List< ConverterSetup > csList = selectedSources.getSelectedSources();
		
		if(csList== null || csList.isEmpty())
			return;
		for(int i=0;i<2;i++)
		{
			if(arg0.getSource() == butInter[i])
			{
				for ( final ConverterSetup cs: csList)
				{
					((GammaConverterSetup)cs).setVoxelRenderInterpolation( i );
				}
				return;
			}
			if(arg0.getSource() == butRender[i])
			{
				for ( final ConverterSetup cs: csList)
				{
					((GammaConverterSetup)cs).setRenderType( i );
				}
				return;
			}
		}
	}
	
	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}
}
