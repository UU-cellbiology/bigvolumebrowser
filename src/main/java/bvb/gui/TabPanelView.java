package bvb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import bvb.core.BigVolumeBrowser;
import bvb.gui.clip.ClipPanel;

public class TabPanelView extends JPanel
{
	final public SourcesRenderPanel sourcesRenderPanel;

	final public ClipPanel clipPanel;
	
	public TabPanelView(final BigVolumeBrowser bvb, final SelectedSources selectedSources)
	{
		super(new GridBagLayout());	
			
	    GridBagConstraints gbc = new GridBagConstraints();
	    
	    sourcesRenderPanel = new SourcesRenderPanel(bvb.bvv.getBvvHandle().getConverterSetups(), selectedSources);
	    
		//Sources render method panel
	    JPanel panRender = new JPanel(new GridBagLayout()); 
	    panRender.setBorder(new PanelTitle(" Sources render "));
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 1.0;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    panRender.add(sourcesRenderPanel,gbc);
	    
	    gbc = new GridBagConstraints();
	    
	    clipPanel = new ClipPanel(bvb, selectedSources);		
	    //add panels to Navigation
	    gbc.insets = new Insets(4,3,4,3);
	    //View
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 1;
	    gbc.anchor = GridBagConstraints.NORTHWEST;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    
	    this.add(panRender,gbc);
	    
	    gbc.gridy++;
	    this.add(clipPanel,gbc);
	    
        // Blank/filler component
	    gbc.gridy++;
	    gbc.weightx = 0.01;
	    gbc.weighty = 0.01;
	    this.add(new JLabel(), gbc);
	   		
	}

}
