package bvb.gui.transform;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import bdv.tools.brightness.ConverterSetup;
import bvb.core.BigVolumeBrowser;
import bvb.gui.PanelTitle;
import bvb.gui.SelectedSources;
import bvb.utils.transform.TransformSetups;

public class TransformPanel extends JPanel 
{
	final BigVolumeBrowser bvb;
	
	final TransformSetups transformSetups;
	
	final TransformScalePanel transformScalePanel;
	
	final TransformCenterPanel transformCentersPanel;
	
	final TransformRotationPanel transformRotationPanel;
	
	public TransformPanel(final BigVolumeBrowser bvb_)
	{
		super();
		bvb = bvb_;
		
		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);
		this.setBorder(new PanelTitle(" Transform "));

		transformSetups = new TransformSetups(bvb);
		
		transformScalePanel = new TransformScalePanel(transformSetups);
		
		transformCentersPanel = new TransformCenterPanel(transformSetups);
		
		transformRotationPanel = new TransformRotationPanel(transformSetups);
		
		JTabbedPane tabTrPane = new JTabbedPane(SwingConstants.TOP);
		//URL icon_path = this.getClass().getResource("/icons/rotate.png");
	    //ImageIcon tabIcon = new ImageIcon(icon_path);
		tabTrPane.addTab( "Scale", transformScalePanel );
		tabTrPane.addTab( "Center", transformCentersPanel );
		tabTrPane.addTab( "Rotation", transformRotationPanel );
		
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		
	    this.add(tabTrPane,gbc);
	    
	    setSourceListeners();
	    
	    updateGUI();
	    
	    Color [] colors = new Color[3];
	    colors[0] =  new Color(198,34,0);
	    colors[1] =  new Color(67,154,0);
	    colors[2] =  new Color(0,34,213);

	    this.setSliderColors( colors );
	    

	}
	public synchronized void updateGUI()
	{
		final List< ConverterSetup > csList = transformSetups.selectedSources.getSelectedSources();
		if(csList== null || csList.isEmpty())
		{
			setPanelsEnabled(false);
			return;
		}
		setPanelsEnabled(true);
		transformScalePanel.updateGUI();
		transformCentersPanel.updateGUI();
	}
	
	private void setPanelsEnabled(boolean bEnabled)
	{
		transformScalePanel.setEnabled( bEnabled );
		transformCentersPanel.setEnabled( bEnabled );
		transformRotationPanel.setEnabled( bEnabled );
	}
	
	public void setSourceListeners()
	{
		
		transformSetups.selectedSources.addSourceSelectionListener(  new SelectedSources.Listener()
		{			
			@Override
			public void selectedSourcesChanged( )
			{
				updateGUI();
			}
		} );
		
	    //add listener in case number of sources, etc change
		transformSetups.converterSetups.listeners().add( s -> updateGUI() );

	}
	
	void setSliderColors(Color [] colors)
	{
		
		transformCentersPanel.setSliderColors( colors );
		transformRotationPanel.setSliderColors( colors );
		
	}
}
