package bvb.gui.transform;

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
import bvb.transform.TransformSetups;

public class TransformPanel extends JPanel 
{
	final BigVolumeBrowser bvb;
	
	final TransformSetups transformSetups;
	
	final TransformTranslationPanel transformTranslationPanel;
	
	public TransformPanel(final BigVolumeBrowser bvb_)
	{
		super();
		bvb = bvb_;
		
		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);
		this.setBorder(new PanelTitle(" Transform "));

		transformSetups = new TransformSetups(bvb);
		
		transformTranslationPanel = new TransformTranslationPanel(transformSetups);
		JTabbedPane tabTrPane = new JTabbedPane(SwingConstants.TOP);
		//URL icon_path = this.getClass().getResource("/icons/rotate.png");
	    //ImageIcon tabIcon = new ImageIcon(icon_path);
		tabTrPane.addTab( "Translation", transformTranslationPanel );
		
		
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
	    

	}
	private synchronized void updateGUI()
	{
		final List< ConverterSetup > csList = transformSetups.selectedSources.getSelectedSources();
		if(csList== null || csList.isEmpty())
		{
			setPanelsEnabled(false);
			return;
		}
		setPanelsEnabled(true);
	}
	
	private void setPanelsEnabled(boolean bEnabled)
	{
		transformTranslationPanel.setEnabled(bEnabled);
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
}
